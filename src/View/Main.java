package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private MyModel model;

    @Override
    public void start(Stage primaryStage) throws IOException {
        model = new MyModel();
        model.connectServers();

        MyViewModel myViewModel = new MyViewModel(model);
        model.addObserver(myViewModel);


        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("MyView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        primaryStage.setTitle("Maze!");
        primaryStage.setScene(scene);

        MyViewController view = fxmlLoader.getController();
        myViewModel.addObserver(view);
        view.initialize(myViewModel, primaryStage, scene, this);

        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            safeExit(primaryStage);
        });
    }

    public void safeExit(Stage mainStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("You're about to exit!");
        alert.setContentText("Are you sure you want to exit?");
        if(alert.showAndWait().get() == ButtonType.OK){
            model.close();
            mainStage.close();
            Platform.exit();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}