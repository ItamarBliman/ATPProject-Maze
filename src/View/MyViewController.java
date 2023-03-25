package View;

import ViewModel.MyViewModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;


public class MyViewController implements IView, Observer {
    public TextField textField_mazeRows;
    public TextField textField_mazeColumns;
    public MazeDisplayer mazeDisplayer;
    public Label playerRow;
    public Label playerCol;

    public Button pauseResumeButton;
    public Button clearSolutionButton;
    public Button startOverButton;
    public Button solveMazeButton;

    public Pane mazePane;
    public BorderPane borderPane;

    private MyViewModel myViewModel;
    private Scene mainScene;
    private Stage mainStage;
    private Main myMain;

    public static MediaPlayer mediaPlayer = null;
    public static boolean sound = false;
    private boolean zoomOn = false;
    private MouseEvent mouseEventPressed = null;

    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();
    private double MousePressedTranslateX;
    private double MousePressedTranslateY;


    public String getUpdatePlayerRow() {
        return updatePlayerRow.get();
    }

    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(updatePlayerRow + "");
    }

    public String getUpdatePlayerCol() {
        return updatePlayerCol.get();
    }

    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(updatePlayerCol + "");
    }


    public void initialize(MyViewModel viewModel, Stage stage, Scene scene, Main m) {
        myViewModel = viewModel;
        mainScene = scene;
        mainStage = stage;
        myMain = m;

        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);

        ImageView pauseResumeImage = new ImageView(new Image("/images/play-pause.jpg"));
        pauseResumeImage.setFitHeight(60);
        pauseResumeImage.setFitWidth(60);
        pauseResumeImage.setPreserveRatio(true);
        pauseResumeButton.setGraphic(pauseResumeImage);
        pauseResumeButton.setPrefSize(60, 60);

        ImageView redoImage = new ImageView(new Image("/images/redo.png"));
        redoImage.setFitHeight(20);
        redoImage.setFitWidth(25);
        redoImage.setPreserveRatio(true);
        clearSolutionButton.setGraphic(redoImage);

        mazePane.prefHeightProperty().bind(borderPane.heightProperty());
        mazePane.prefWidthProperty().bind(borderPane.widthProperty());

        setResizeEvent();
        mazeDisplayer.resize(mainScene.getWidth(), mainScene.getHeight());
        mazeDisplayer.initializeImages();
    }

    @Override
    public void displayMaze(int[][] maze) {
        setUpdatePlayerRow(myViewModel.getPlayerRow());
        setUpdatePlayerCol(myViewModel.getPlayerCol());
        mazeDisplayer.refreshMaze(maze, myViewModel.getPlayerRow(), myViewModel.getPlayerCol());
    }

    public void generateMaze() {
        int rows, cols;

        try {
            rows = Integer.parseInt(textField_mazeRows.getText());
            cols = Integer.parseInt(textField_mazeColumns.getText());
            if (rows <= 0 || cols <= 0)
                throw new Exception();
        } catch (Exception e) {
            popAlert("Maze size needs to be a valid number", Alert.AlertType.ERROR);
            return;
        }
        myViewModel.generateBoard(rows, cols);
    }

    public void startOver(ActionEvent actionEvent) {
        myViewModel.startOver();
        solveMazeButton.setDisable(false);
        clearSolutionButton.setDisable(true);
    }

    public void solveMaze(ActionEvent actionEvent) {
        solveMazeButton.setDisable(true);
        clearSolutionButton.setDisable(false);
        myViewModel.solveBoard();
    }

    public void clearSolution(ActionEvent actionEvent) {
        myViewModel.clearSolution();
        displayMaze(myViewModel.getBoard());
        solveMazeButton.setDisable(false);
        clearSolutionButton.setDisable(true);
    }

    public void saveMaze() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save maze");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze files (*.maze)", "*.maze"));
        File directory = new File("./mazes");
        if(!directory.exists())
            directory.mkdirs();
        fc.setInitialDirectory(directory);
        fc.setInitialFileName("maze.maze");
        File chosen = fc.showSaveDialog(null);
        if(chosen == null)
            return;
        if(myViewModel.saveBoard(chosen.toString()))
            popAlert("Saved maze successfully", Alert.AlertType.INFORMATION);
        else
            popAlert("Could not save maze", Alert.AlertType.ERROR);
    }

    public void LoadMaze(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open maze");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze files (*.maze)", "*.maze"));
        File directory = new File("./mazes");
        if(!directory.exists())
            directory.mkdirs();
        fc.setInitialDirectory(directory);
        File chosen = fc.showOpenDialog(null);
        if(chosen == null)
            return;
        if(myViewModel.loadBoard(chosen.toString())) {
            showNewMaze();
            popAlert("Load maze successfully", Alert.AlertType.INFORMATION);
        }
        else
            popAlert("Could not load maze", Alert.AlertType.ERROR);
    }

    public void showProperties(ActionEvent actionEvent) {

        Stage propertiesStage = new Stage();
        propertiesStage.setTitle("Properties");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Properties.fxml").openStream());
            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(getClass().getResource("SecondStyle.css").toExternalForm());
            propertiesStage.setScene(scene);
            propertiesStage.initModality(Modality.APPLICATION_MODAL); //Lock the window
            propertiesStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showHelp() {
        Stage helpStage = new Stage();
        helpStage.setTitle("Help");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Help.fxml").openStream());
            Scene scene = new Scene(root, 600, 350);
            scene.getStylesheets().add(getClass().getResource("SecondStyle.css").toExternalForm());
            helpStage.setScene(scene);
            helpStage.initModality(Modality.APPLICATION_MODAL); //Lock the window
            helpStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAbout() {
        Stage helpAbout = new Stage();
        helpAbout.setTitle("About");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 600, 350);
            scene.getStylesheets().add(getClass().getResource("SecondStyle.css").toExternalForm());
            helpAbout.setScene(scene);
            helpAbout.initModality(Modality.APPLICATION_MODAL); //Lock the window
            helpAbout.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void safeExit()
    {
        myMain.safeExit(mainStage);
    }

    public void keyPressed(KeyEvent keyEvent) {
        myViewModel.movePlayer(keyEvent.getCode());
        keyEvent.consume();
    }

    public void setPlayerPosition(int row, int col) {
        mazeDisplayer.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
    }

    public void popAlert(String msg, Alert.AlertType type)
    {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.show();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o == myViewModel) {
            switch ((String) arg) {
                case "generateMaze" -> {
                    showNewMaze();
                }
                case "solveMaze" -> {
                    popAlert("Maze was solved!", Alert.AlertType.INFORMATION);
                    zoomResetMaze();
                    displayMaze(myViewModel.getBoard());
                }
                case "movePlayer" -> {
                    displayMaze(myViewModel.getBoard());
                }
                case "succeed" -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Thanks for helping Marco");
                    ImageView iv = new ImageView(new Image("/images/end.jpg"));
                    iv.setFitHeight(70);
                    iv.setFitWidth(80);
                    alert.setGraphic(iv);
                    alert.setHeaderText("Congratulation! You have solved the maze!");
                    alert.setTitle("Good Job");
                    playMusic("endSong");
                    alert.showAndWait();
                    mediaPlayer.stop();
                    mediaPlayer = null;
                    sound = false;
                }
            }
        }
    }

    public static void playMusic(String operation) {
        if(mediaPlayer != null)
            mediaPlayer.stop();
        switch (operation) {
            case "themeSong" -> {
                try {
                    mediaPlayer = new MediaPlayer(new Media(MyViewController.class.getResource("/music/song.mp3").toURI().toString()));
                } catch (URISyntaxException e) {
                }
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
                sound = true;
            }
            case "endSong" -> {
                try {
                    mediaPlayer = new MediaPlayer(new Media(MyViewController.class.getResource("/music/mama.mp3").toURI().toString()));
                } catch (URISyntaxException e) {
                }
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
                sound = true;
            }
        }
    }

    public void pauseResume()
    {
        if(mediaPlayer != null)
        {
            if(sound) {
                mediaPlayer.pause();
                sound = false;
            }
            else
            {
                mediaPlayer.play();
                sound = true;
            }
        }
        else
        {
            playMusic("themeSong");
        }
    }

    public void setResizeEvent() {
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            mazeDisplayer.resize(mainScene.getWidth(), mainScene.getHeight());
        };

        mainScene.heightProperty().addListener(stageSizeListener);
        mainScene.widthProperty().addListener(stageSizeListener);
    }

    public void zoomScroll(ScrollEvent scrollEvent){
        if(myViewModel.getBoard() == null)
            return;

        double zoomValue = 1.05, scroll;
        if(scrollEvent.isControlDown()) {
            scroll = scrollEvent.getDeltaY();
            if (scroll < 0) {
                zoomValue = 0.95;
            }
            mazeDisplayer.setScaleX(mazeDisplayer.getScaleX() * zoomValue);
            mazeDisplayer.setScaleY(mazeDisplayer.getScaleY() * zoomValue);
            zoomOn = true;
        }
    }

    private void zoomResetMaze(){
        mazeDisplayer.setTranslateX(mazeDisplayer.getParent().getTranslateX());
        mazeDisplayer.setTranslateY(mazeDisplayer.getParent().getTranslateY());
        mazeDisplayer.setScaleX(mazeDisplayer.getParent().getScaleX());
        mazeDisplayer.setScaleY(mazeDisplayer.getParent().getScaleY());
        zoomOn = false;
    }

    private void showNewMaze()
    {
        mazeDisplayer.setMaze(myViewModel.getBoard(), myViewModel.getStartRow(), myViewModel.getStartCol(), myViewModel.getGoalRow(), myViewModel.getGoalCol());
        setPlayerPosition(myViewModel.getPlayerRow(),myViewModel.getPlayerCol());
        startOverButton.setDisable(false);
        solveMazeButton.setDisable(false);
        clearSolutionButton.setDisable(true);
        zoomResetMaze();
        if(!sound)
            playMusic("themeSong");
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        if (myViewModel.getBoard() == null)
            return;

        int mouseX, mouseY, PlayerX, PlayerY;

        mouseX = (int) (mouseEvent.getX() / (mazeDisplayer.getWidth() / myViewModel.getBoard()[0].length));
        mouseY = (int) (mouseEvent.getY() / (mazeDisplayer.getHeight() / myViewModel.getBoard().length));

        PlayerY = myViewModel.getPlayerRow();
        PlayerX = myViewModel.getPlayerCol();

        if (mouseY < (PlayerY - 1) || mouseY > (PlayerY + 1) || mouseX < (PlayerX - 1) || mouseX > (PlayerX + 1))
        {
            moveZoom(mouseEvent);
            return;
        }

        if (mouseY < PlayerY && mouseX == PlayerX)
            myViewModel.movePlayer(KeyCode.UP);

        else if (mouseY > PlayerY && mouseX == PlayerX)
            myViewModel.movePlayer(KeyCode.DOWN);

        else if (mouseY == PlayerY && mouseX > PlayerX)
            myViewModel.movePlayer(KeyCode.RIGHT);

        else if (mouseY == PlayerY && mouseX < PlayerX)
            myViewModel.movePlayer(KeyCode.LEFT);

        else if (mouseY < PlayerY && mouseX > PlayerX)
            myViewModel.movePlayer(KeyCode.NUMPAD9);

        else if (mouseY > PlayerY && mouseX > PlayerX)
            myViewModel.movePlayer(KeyCode.NUMPAD3);

        else if (mouseY > PlayerY && mouseX < PlayerX)
            myViewModel.movePlayer(KeyCode.NUMPAD1);

        else if (mouseY < PlayerY && mouseX < PlayerX)
            myViewModel.movePlayer(KeyCode.NUMPAD7);
    }

    public void onMouseClicked(MouseEvent mouseEvent)
    {
        MousePressedTranslateX = mazeDisplayer.getTranslateX();
        MousePressedTranslateY = mazeDisplayer.getTranslateY();
        mouseEventPressed = mouseEvent;
    }

    private void moveZoom(MouseEvent mouseEvent) {

        if (!zoomOn || mouseEventPressed == null)
            return;

        mazeDisplayer.setTranslateX((int) (MousePressedTranslateX + (mouseEvent.getSceneX() - mouseEventPressed.getSceneX())));
        mazeDisplayer.setTranslateY((int) (MousePressedTranslateY + (mouseEvent.getSceneY() - mouseEventPressed.getSceneY())));

    }

    public void Focus(MouseEvent mouseEvent){
        mazeDisplayer.requestFocus();
    }
}



