package javaapplication2;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author mojan
 */
public class FXMLController implements Initializable {

    @FXML
    private Rectangle menu;
    @FXML
    private AnchorPane Aplikace;
    @FXML
    private Button Button;
    Image image;
    ImageView imageView;
    @FXML
    private Pane menuPlocha;
    @FXML
    private BorderPane fotoPlocha;
    @FXML
    private Rectangle ovladaciCtverec;
    @FXML
    private Rectangle BarvickaCtverec;
    @FXML
    private Label hodnotyRGB;
    @FXML
    private Rectangle barva;
    @FXML
    private CheckBox chNahore;
    @FXML
    private CheckBox chVpravo;
    @FXML
    private CheckBox chDole;
    @FXML
    private CheckBox chVlevo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Aplikace.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newPolomerX = newVal.doubleValue();
            menu.setWidth(newPolomerX);
        });

    }

    @FXML
    private void onFile(ActionEvent event) {
        prepare();
    }

    private void updateColorAndRGB(MouseEvent event) {
        if (image != null && imageView != null) {
            double x = event.getX(); // X souřadnice kliknutí
            double y = event.getY(); // Y souřadnice kliknutí

            int pixelX = (int) x;
            int pixelY = (int) y;

            // Získání barvy pixelu na daných souřadnicích
            Color color = image.getPixelReader().getColor(pixelX, pixelY);

            // Aktualizace BarvickaCtverec na získanou barvu
            barva.setFill(color);

            // Aktualizace hodnotyRGB na RGB hodnotu barvy
            String rgb = String.format("RGB(%d, %d, %d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
            hodnotyRGB.setText(rgb);
        }
    }

    private void prepare() {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PNG and JPG", "*.png", "*.jpg");
            fileChooser.getExtensionFilters().add(extensionFilter);
            File file = fileChooser.showOpenDialog(new Stage());

            if (file != null) {
                image = new Image(file.toURI().toString());

                if (image.isError()) {
                    prepare();
                    return;
                }

                imageView = new ImageView(image);
                imageView.setPreserveRatio(true); // Udržuje poměr stran obrázku
                imageView.setFitWidth(fotoPlocha.getWidth());
                imageView.setFitHeight(fotoPlocha.getHeight());
                imageView.setOnMouseClicked(this::updateColorAndRGB);

                // Vyčistit obsah fotoPlocha a přidat nový obrázek
                fotoPlocha.getChildren().clear();
                fotoPlocha.setCenter(imageView);
            }
        } catch (Exception e) {
            // Obsluha výjimky
            e.printStackTrace();
        }
    }

    @FXML
    private void onNahore(ActionEvent event) {
        // Ověřte, že imageView není null
        if (imageView != null) {
            // Odstranění imageView z horní části, pokud je tam již umístěn
            fotoPlocha.getChildren().remove(imageView);
            chDole.setSelected(false);
            chVlevo.setSelected(false);
            chVpravo.setSelected(false);
            // Přesunutí imageView do dolní části
            fotoPlocha.setTop(imageView);
        }
    }

    @FXML
    private void onVpravo(ActionEvent event) {
        if (imageView != null) {
            // Odstranění imageView z horní části, pokud je tam již umístěn
            fotoPlocha.getChildren().remove(imageView);
            chDole.setSelected(false);
            chVlevo.setSelected(false);
            chNahore.setSelected(false);
            // Přesunutí imageView do dolní části
            fotoPlocha.setRight(imageView);
        }
    }

    @FXML
    private void onDole(ActionEvent event) {
        if (imageView != null) {
            // Odstranění imageView z horní části, pokud je tam již umístěn
            fotoPlocha.getChildren().remove(imageView);
            chNahore.setSelected(false);
            chVlevo.setSelected(false);
            chVpravo.setSelected(false);
            // Přesunutí imageView do dolní části
            fotoPlocha.setBottom(imageView);
        }
    }

    @FXML
    private void onVlevo(ActionEvent event) {
        if (imageView != null) {
            // Odstranění imageView z horní části, pokud je tam již umístěn
            fotoPlocha.getChildren().remove(imageView);
            chNahore.setSelected(false);
            chDole.setSelected(false);
            chVpravo.setSelected(false);
            // Přesunutí imageView do dolní části
            fotoPlocha.setLeft(imageView);
        }
    }

}
