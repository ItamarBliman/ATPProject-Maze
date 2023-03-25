package ViewModel;

import Model.IModel;
import javafx.scene.input.KeyCode;
import java.util.Observable;
import java.util.Observer;



public class MyViewModel extends Observable implements Observer {

    private IModel model;

    public MyViewModel(IModel model) {
        this.model = model;
    }

    public void generateBoard(int row, int col) {
        model.generateBoard(row, col);
    }

    public int[][] getBoard() {
        return model.getBoard();
    }

    public int getStartRow() {
        return model.getStartRow();
    }

    public int getStartCol() {
        return model.getStartCol();
    }

    public int getGoalRow() {
        return model.getGoalRow();
    }

    public int getGoalCol() {
        return model.getGoalCol();
    }

    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    public int getPlayerCol() {
        return model.getPlayerCol();
    }

    public void solveBoard() {
        model.solveBoard();
    }

    public boolean saveBoard(String filePath) {
        return model.saveBoard(filePath);
    }

    public boolean loadBoard(String filePath) {
        return model.loadBoard(filePath);
    }

    public void movePlayer(KeyCode move) {
        model.movePlayer(move);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            switch ((String) arg) {
                case "generateMaze" -> {
                    setChanged();
                    notifyObservers("generateMaze");
                }
                case "solveMaze" -> {
                    setChanged();
                    notifyObservers("solveMaze");
                }
                case "movePlayer" -> {
                    setChanged();
                    notifyObservers("movePlayer");
                }
                case "succeed" -> {
                    setChanged();
                    notifyObservers("succeed");
                }
            }
        }
    }

    public void startOver() {
        model.startOver();
    }

    public void clearSolution() {
        model.clearSolution();
    }
}

