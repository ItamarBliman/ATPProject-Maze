package View;

import Server.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

public class Properties {
    public Label threadPoolSize;
    public Label mazeGeneratingAlgorithm;
    public Label mazeSearchingAlgorithm;

    StringProperty updatethreadPoolSize = new SimpleStringProperty();
    StringProperty updatemazeGeneratingAlgorithm = new SimpleStringProperty();
    StringProperty updatemazeSearchingAlgorithm = new SimpleStringProperty();


    public void initialize() {
        try {
            updatethreadPoolSize.set(String.valueOf(Configurations.getInstance().getThreadPoolSize()));
            threadPoolSize.textProperty().bind(updatethreadPoolSize);

            updatemazeGeneratingAlgorithm.set(Configurations.getInstance().getMazeGeneratingAlgorithm());
            mazeGeneratingAlgorithm.textProperty().bind(updatemazeGeneratingAlgorithm);

            updatemazeSearchingAlgorithm.set(Configurations.getInstance().getMazeSearchingAlgorithm());
            mazeSearchingAlgorithm.textProperty().bind(updatemazeSearchingAlgorithm);

        } catch (Exception e) {
        }
    }
}
