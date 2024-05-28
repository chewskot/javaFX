/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javaapplication2;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

/**
 * FXML Controller class
 *
 * @author mojan
 */
public class FXMLController implements Initializable {

    @FXML
    private Button btnKonec;
    @FXML
    private Button btnVymaz;
    @FXML
    private Pane plocha;
    double zacX = 0;
    double zacY = 0;
    double konX = 0;
    double konY = 0;
    List<Circle> kruhy = new ArrayList<>();
    int pocet = 0;
    @FXML
    private Label lbPocet;
    private Circle tempCircle; // Temporální kružnice pro dynamické roztahování

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializace počtu kruhů na labelu
        lbPocet.setText("Pocet kruhu: " + pocet);
        // TODO
        Stop[] stops = new Stop[]{
            new Stop(0, Color.WHITE),
            new Stop(1, Color.RED)
        };

        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        plocha.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, null, null)));
        plocha.widthProperty().addListener((obs, oldVal, newVal) -> {
           
            clearCircles();
        });

        plocha.heightProperty().addListener((obs, oldVal, newVal) -> {
            
            clearCircles();        });
    }

    @FXML
    private void onKonec(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void onVymaz(ActionEvent event) {
        kruhy.clear();
        plocha.getChildren().clear();
        pocet = 0;
        lbPocet.setText("Pocet kruhu: " + pocet);
    }

    @FXML
    private void onRelease(MouseEvent event) {
        if (tempCircle != null) {
            // Přidáme kruh do seznamu a do plochy po uvolnění tlačítka myši
            kruhy.add(tempCircle);
            pocet++;
            lbPocet.setText("Pocet kruhu: " + pocet);
            checkIntersections(tempCircle);
            tempCircle = null; // Resetujeme tempCircle po přidání
        }
    }

    @FXML
    private void onDrag(MouseEvent event) {
        if (tempCircle != null) {
            // Výpočet poloměru kružnice jako vzdálenosti od zacX, zacY k aktuální pozici myši
            double radius = Math.sqrt(Math.pow(event.getX() - zacX, 2) + Math.pow(event.getY() - zacY, 2));
            // Nastavení poloměru temporální kružnice
            tempCircle.setRadius(radius);
            // Zkontrolovat, zda se nově vytvářející kruh protíná s ostatními kruhy
            checkIntersections(tempCircle);
        }
    }

    @FXML
    private void onPress(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            zacX = event.getX();
            zacY = event.getY();
            tempCircle = new Circle(zacX, zacY, 0, Color.BLUE);
            tempCircle.setStroke(Color.BLACK);
            tempCircle.setStrokeWidth(2);

            // Nastavení vzoru ohraničení: [10, 10] znamená 10 pixelů čára, 10 pixelů mezera
            tempCircle.getStrokeDashArray().addAll(10d, 10d);

            // Nastavení typu ohraničení a stylu konce čáry
            tempCircle.setStrokeType(StrokeType.OUTSIDE);
            tempCircle.setStrokeLineCap(StrokeLineCap.BUTT);

            plocha.getChildren().add(tempCircle); // Přidáme temporální kruh do plochy pro zobrazení
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Pravé tlačítko myši - pokusíme se najít a odstranit kruh
            Circle toRemove = null;
            for (Circle circle : kruhy) {
                if (circle.contains(event.getX(), event.getY())) {
                    toRemove = circle;
                    break;
                }
            }
            if (toRemove != null) {
                kruhy.remove(toRemove);
                plocha.getChildren().remove(toRemove);
                pocet--;
                lbPocet.setText("Pocet kruhu: " + pocet);
            }
        }
    }

    private void checkIntersections(Circle newCircle) {
        for (Circle existingCircle : kruhy) {
            if (newCircle != existingCircle && newCircle.getBoundsInParent().intersects(existingCircle.getBoundsInParent())) {
                newCircle.setFill(Color.LIME);
                existingCircle.setFill(Color.LIME);
                newCircle.setOpacity(0.5);
                existingCircle.setOpacity(0.5);
            }
        }
    }

    private void clearCircles() {
         plocha.getChildren().removeIf(child -> child instanceof Circle);
        pocet = 0;
        lbPocet.setText("Pocet kruhu: " + pocet);
    }
}
