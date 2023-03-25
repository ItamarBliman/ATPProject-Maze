package View;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HelpController {

    @FXML
    private ImageView imageMarco;

    @FXML
    private ImageView imageMom;

    @FXML
    private ImageView imageNumbers;

    @FXML
    private ImageView imageWater;

    public void initialize() {
        imageMarco.setImage(new Image("/images/Marco1.png"));
        imageMom.setImage(new Image("/images/MarcoMom.png"));
        imageNumbers.setImage(new Image("/images/numpad.jpg"));
        imageWater.setImage(new Image("/images/Water.png"));
    }
}

