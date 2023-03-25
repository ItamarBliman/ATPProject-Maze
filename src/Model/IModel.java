package Model;

import javafx.scene.input.KeyCode;

public interface IModel {

    void generateBoard(int row, int col);
    int[][] getBoard();
    int getStartRow();
    int getStartCol();
    int getGoalRow();
    int getGoalCol();
    int getPlayerRow();
    int getPlayerCol();
    void solveBoard();
    boolean saveBoard(String filePath);
    boolean loadBoard(String filePath);
    void connectServers();
    void movePlayer(KeyCode move);
    boolean succeed();
    void close();
    void startOver();
    void clearSolution();
}
