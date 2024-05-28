package javaapplication2;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLController implements Initializable {

    @FXML
    private AnchorPane Aplikace;

    @FXML
    private Pane paneOvladani;
    @FXML
    private CheckBox chHorni;
    @FXML
    private CheckBox chLevy;
    @FXML
    private CheckBox chDolni;
    @FXML
    private CheckBox chPravy;
    @FXML
    private Label lbPixel;
    @FXML
    private Pane paneMenu;
    @FXML
    private Button btnFile;
    @FXML
    private Pane plochaPane;
    @FXML
    private Button btnReset;
    @FXML
    private Rectangle barvaRect;

    private ImageView imageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setOnMouseClicked(this::onImageClicked);

        plochaPane.getChildren().add(imageView);

        // Add listeners to CheckBoxes to update image layout
        chHorni.setOnAction(this::updateImageLayout);
        chLevy.setOnAction(this::updateImageLayout);
        chDolni.setOnAction(this::updateImageLayout);
        chPravy.setOnAction(this::updateImageLayout);

        // Add a listener to handle resize of plochaPane
        plochaPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (imageView.getImage() != null) {
                updateImageLayout(null);
            }
        });
        plochaPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (imageView.getImage() != null) {
                updateImageLayout(null);
            }
        });
    }

    @FXML
    private void onFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filterImages = new FileChooser.ExtensionFilter("Image Files", "*.bmp", "*.jpg", "*.gif", "*.png");
        fileChooser.getExtensionFilters().add(filterImages);

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());

            if (image.getWidth() > plochaPane.getWidth() || image.getHeight() > plochaPane.getHeight()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Image Too Large", "The image cannot be loaded because it is larger than the display area.");
                return;
            }

            // If image is smaller than the plochaPane in both dimensions, reset the layout
            if (image.getWidth() < plochaPane.getWidth() && image.getHeight() < plochaPane.getHeight()) {
                resetLayout();
            }

            imageView.setImage(image);
            updateImageLayout(null);
        }
    }

    private void updateImageLayout(ActionEvent event) {
        if (imageView.getImage() != null) {
            plochaPane.getChildren().clear();
            plochaPane.getChildren().add(imageView);

            // Check the state of the CheckBoxes
            boolean left = chLevy.isSelected();
            boolean right = chPravy.isSelected();
            boolean top = chHorni.isSelected();
            boolean bottom = chDolni.isSelected();

            // Reset ImageView properties
            imageView.setFitWidth(imageView.getImage().getWidth());
            imageView.setFitHeight(imageView.getImage().getHeight());
            imageView.setPreserveRatio(true);

            // Calculate new width and height based on CheckBox selection
            double newWidth = imageView.getFitWidth();
            double newHeight = imageView.getFitHeight();

            if (left && right) {
                newWidth = plochaPane.getWidth();
                imageView.setPreserveRatio(false);
            }
            if (top && bottom) {
                newHeight = plochaPane.getHeight();
                imageView.setPreserveRatio(false);
            }

            imageView.setFitWidth(newWidth);
            imageView.setFitHeight(newHeight);

            // Calculate new X and Y positions
            double newX = (plochaPane.getWidth() - imageView.getFitWidth()) / 2;
            double newY = (plochaPane.getHeight() - imageView.getFitHeight()) / 2;

            if (left && !right) {
                newX = 0;
            } else if (right && !left) {
                newX = plochaPane.getWidth() - imageView.getFitWidth();
            }

            if (top && !bottom) {
                newY = 0;
            } else if (bottom && !top) {
                newY = plochaPane.getHeight() - imageView.getFitHeight();
            }

            // Apply new X and Y positions
            imageView.setX(newX);
            imageView.setY(newY);
        }
    }

    @FXML
    private void onReset(ActionEvent event) {
        resetLayout();
    }

    private void resetLayout() {
        if (imageView.getImage() != null) {
            imageView.setImage(null);
        }

        //plochaPane.getChildren().clear();
        plochaPane.getChildren().add(imageView);

        chHorni.setSelected(false);
        chLevy.setSelected(false);
        chDolni.setSelected(false);
        chPravy.setSelected(false);

        lbPixel.setText("");
        barvaRect.setFill(Color.TRANSPARENT);
    }

    @FXML
    private void onImageClicked(MouseEvent event) {
        if (imageView.getImage() != null) {
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            if (pixelReader != null) {
                try {
                    double x = event.getX() - imageView.getBoundsInParent().getMinX();
                    double y = event.getY() - imageView.getBoundsInParent().getMinY();

                    double scaleX = imageView.getImage().getWidth() / imageView.getFitWidth();
                    double scaleY = imageView.getImage().getHeight() / imageView.getFitHeight();

                    int imageX = (int) (x * scaleX);
                    int imageY = (int) (y * scaleY);

                    Color color = pixelReader.getColor(imageX, imageY);

                    barvaRect.setFill(color);
                    lbPixel.setText(String.format("RGB(%d, %d, %d)", 
                        (int) (color.getRed() * 255),
                        (int) (color.getGreen() * 255),
                        (int) (color.getBlue() * 255)));

                } catch (Exception e) {
                    showAlert(Alert.AlertType.WARNING, "Error", "Error reading pixel", e.getMessage());
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
