package Model;

import Client.*;
import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.*;
import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Observable;

public class MyModel extends Observable implements IModel{

    private Maze myMaze;
    private Server generateServer;
    private Server solveServer;
    private Solution solution;

    private int rowPlayer;
    private int colPlayer;

    public MyModel() {
        myMaze = null;
        solution = null;
    }

    @Override
    public int[][] getBoard() {
        if(myMaze == null)
            return null;
        return myMaze.getMaze();
    }

    @Override
    public int getStartRow() {
        return myMaze.getStartPosition().getRowIndex();
    }

    @Override
    public int getStartCol() {
        return myMaze.getStartPosition().getColumnIndex();
    }

    @Override
    public int getGoalRow() {
        return myMaze.getGoalPosition().getRowIndex();
    }

    @Override
    public int getGoalCol() {
        return myMaze.getGoalPosition().getColumnIndex();
    }

    @Override
    public int getPlayerRow() {
        return rowPlayer;
    }

    @Override
    public int getPlayerCol() {
        return colPlayer;
    }

    @Override
    public void generateBoard(int row, int col) {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{row, col};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[])fromServer.readObject();
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[mazeDimensions[0] * mazeDimensions[1] + 24];
                        is.read(decompressedMaze);
                        myMaze = new Maze(decompressedMaze);
                        rowPlayer = myMaze.getStartPosition().getRowIndex();
                        colPlayer = myMaze.getStartPosition().getColumnIndex();
                    } catch (Exception var10) {
                        return;
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException var1) {
            var1.printStackTrace();
        }

        solution = null;
        setChanged();
        notifyObservers("generateMaze");
    }

    @Override
    public void solveBoard() {
        if (solution != null)
            showSolution();
        else {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            toServer.writeObject(myMaze);
                            toServer.flush();
                            solution = (Solution) fromServer.readObject();
                            showSolution();
                        } catch (Exception var10) {
                            var10.printStackTrace();
                        }
                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException var1) {
                var1.printStackTrace();
            }
        }

        setChanged();
        notifyObservers("solveMaze");
    }

    @Override
    public boolean saveBoard(String filePath) {
        if(myMaze == null)
            return false;

        clearSolution();

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try
        {
            byte[] byteArrayPlayer = new byte[8];
            Maze.mergeArrays(byteArrayPlayer, ByteBuffer.allocate(4).putInt(rowPlayer).array(), 0);
            Maze.mergeArrays(byteArrayPlayer, ByteBuffer.allocate(4).putInt(colPlayer).array(), 4);
            MyCompressorOutputStream os = new MyCompressorOutputStream(byteArray);
            os.out.write(byteArrayPlayer);
            os.write(myMaze.toByteArray());
        }
        catch (Exception e)
        {
            return false;
        }

        try {
            File mazePath = new File(filePath);
            OutputStream out = new FileOutputStream(mazePath);
            out.write(byteArray.toByteArray());
        }
        catch (Exception e)
        {
            return false;
        }

        if(solution != null)
            showSolution();

        return true;
    }


    public boolean loadBoard(String filePath) {
        byte[] savedMazeBytes = new byte[8], playerBytes = new byte[8];
        int rows, columns;
        try {
            MyDecompressorInputStream in = new MyDecompressorInputStream(new FileInputStream(filePath));


            in.in.read(playerBytes, 0, 8);
            in.in.read(savedMazeBytes, 0, 8);
            rows = savedMazeBytes[0] << 24 | (savedMazeBytes[1] & 255) << 16 | (savedMazeBytes[2] & 255) << 8 | savedMazeBytes[3] & 255;
            columns = savedMazeBytes[4] << 24 | (savedMazeBytes[5] & 255) << 16 | (savedMazeBytes[6] & 255) << 8 | savedMazeBytes[7] & 255;

            savedMazeBytes = new byte[rows * columns + 24];

            in = new MyDecompressorInputStream(new FileInputStream(filePath));
            in.in.read(playerBytes, 0, 8);
            in.read(savedMazeBytes);
            in.close();
        } catch (IOException var7) {
            return false;
        }

        rowPlayer = playerBytes[0] << 24 | (playerBytes[1] & 255) << 16 | (playerBytes[2] & 255) << 8 | playerBytes[3] & 255;
        colPlayer = playerBytes[4] << 24 | (playerBytes[5] & 255) << 16 | (playerBytes[6] & 255) << 8 | playerBytes[7] & 255;
        myMaze = new Maze(savedMazeBytes);
        solution = null;

        return true;
    }

        @Override
    public void connectServers() {
        generateServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        solveServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        generateServer.start();
        solveServer.start();
    }

    @Override
    public void movePlayer(KeyCode move) {
        if(myMaze == null)
            return;

        switch (move) {
            case UP:
            case NUMPAD8:
                if(moveLegit(rowPlayer - 1, colPlayer))
                    rowPlayer -= 1;
                break;

            case RIGHT:
            case NUMPAD6:
                if(moveLegit(rowPlayer, colPlayer + 1))
                    colPlayer += 1;
                break;

            case DOWN:
            case NUMPAD2:
                if(moveLegit(rowPlayer + 1, colPlayer))
                    rowPlayer += 1;
                break;

            case LEFT:
            case NUMPAD4:
                if(moveLegit(rowPlayer, colPlayer - 1))
                    colPlayer -= 1;
                break;

            case NUMPAD9:
                if(moveLegit(rowPlayer - 1, colPlayer + 1))
                {
                    rowPlayer -= 1;
                    colPlayer += 1;
                }
                break;

            case NUMPAD7:
                if(moveLegit(rowPlayer - 1, colPlayer - 1))
                {
                    rowPlayer -= 1;
                    colPlayer -= 1;
                }
                break;

            case NUMPAD3:
                if(moveLegit(rowPlayer + 1, colPlayer + 1))
                {
                    rowPlayer += 1;
                    colPlayer += 1;
                }
                break;

            case NUMPAD1:
                if(moveLegit(rowPlayer + 1, colPlayer - 1))
                {
                    rowPlayer += 1;
                    colPlayer -= 1;
                }
                break;
        }

        setChanged();
        notifyObservers("movePlayer");

        if (succeed())
        {
            setChanged();
            notifyObservers("succeed");
        }
    }

    @Override
    public void close() {
        generateServer.stop();
        solveServer.stop();
    }

    public boolean succeed(){
        return (rowPlayer == getGoalRow() && colPlayer == getGoalCol());
    }

    public void startOver(){
        if(myMaze == null)
            return;

        clearSolution();
        rowPlayer = myMaze.getStartPosition().getRowIndex();
        colPlayer = myMaze.getStartPosition().getColumnIndex();
        setChanged();
        notifyObservers("movePlayer");
    }

    private boolean moveLegit(int row, int col){
        if(row >= 0 && row < myMaze.getRowSize() && col >= 0 && col < myMaze.getColumnSize())
            return (myMaze.getCellValue(row, col) == 0 || myMaze.getCellValue(row, col) == 2);
        return false;
    }

    public void clearSolution(){
        // remove solution from the maze
        if((solution != null) && (myMaze != null))
            for(int i = 0; i < myMaze.getRowSize(); ++i)
                for(int j = 0; j < myMaze.getColumnSize(); ++j)
                    if(myMaze.getCellValue(i, j) == 2)
                        myMaze.setCell(i, j, 0);
    }

    private void showSolution(){
        // show the solution of the maze
        ArrayList<AState> mazeSolutionSteps = solution.getSolutionPath();
        for(AState state : mazeSolutionSteps)
            myMaze.setCell(((MazeState)state).getValue().getRowIndex(), ((MazeState)state).getValue().getColumnIndex(), 2);
    }
}
