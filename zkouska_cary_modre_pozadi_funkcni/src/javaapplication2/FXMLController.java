package javaapplication2;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class FXMLController implements Initializable {

    @FXML
    private Pane plocha;
    @FXML
    private HBox hb;
    @FXML
    private Label pocet;
    @FXML
    private ColorPicker barva;
    @FXML
    private Slider tlouska;
    @FXML
    private ChoiceBox<String> styl;
    double startX;
    double startY;
    double endX;
    double endY;
    int cetnost = 0;
    private Line tempLine;
    @FXML
    private Label lbDelka;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Ellipse ellipse = new Ellipse();
        plocha.getChildren().add(ellipse);
        lbDelka.setVisible(false);
        barva.setValue(Color.RED);
        ellipse.setFill(Color.BLUE);
        Stop[] stops = new Stop[]{new Stop(0, Color.WHITE), new Stop(1, Color.BLUE)};
        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops);
        ellipse.setFill(gradient);
        styl.getItems().addAll("Plná", "Čárkovaná", "Čerchovaná");
        styl.setValue("Plná"); // Nastavit výchozí hodnotu

        // Nastavení vlastnosti Ellipse
        ellipse.centerXProperty().bind(plocha.widthProperty().divide(2)); // Střed X
        ellipse.centerYProperty().bind(plocha.heightProperty().divide(2)); // Střed Y

        // Nastavení šířky a výšky elipsy na polovinu minimálního rozměru páně (výška nebo šířka)
        double polomerX = plocha.getWidth() / 2;
        double polomerY = plocha.getHeight() / 2;
        ellipse.setRadiusX(polomerX);
        ellipse.setRadiusY(polomerY);

        // Reagovat na změny velikosti Pane
        plocha.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newPolomerX = newVal.doubleValue() / 2;
            ellipse.setRadiusX(newPolomerX);
            clearLines();
        });

        plocha.heightProperty().addListener((obs, oldVal, newVal) -> {
            double newPolomerY = newVal.doubleValue() / 2;
            ellipse.setRadiusY(newPolomerY);
            clearLines();
        });

    }

  @FXML
    private void onPress(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            startX = event.getX();
            startY = event.getY();
            // Připravíme temporální úsečku
            tempLine = new Line(startX, startY, startX, startY);
            applyLineStyle(tempLine);
            plocha.getChildren().add(tempLine);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            removeNearestLine(event.getX(), event.getY());
        }
    }
    @FXML
    private void onDrag(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (tempLine != null) {
                tempLine.setEndX(event.getX());
                tempLine.setEndY(event.getY());
                lbDelka.setVisible(true);
                updateLineLengthDisplay(tempLine);
            }
        }

    }
    private void updateLineLengthDisplay(Line line) {
        double length = Math.sqrt(Math.pow(line.getEndX() - line.getStartX(), 2) + Math.pow(line.getEndY() - line.getStartY(), 2));
        lbDelka.setText(String.format("%.1f", length));
    }
   @FXML
    private void onRelease(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            endX = event.getX();
            endY = event.getY();
            if (tempLine != null) {
                // Finalizujeme poslední pozice úsečky
                tempLine.setEndX(endX);
                tempLine.setEndY(endY);
                // Přidáme úsečku do počítadla a resetujeme temporální úsečku
                updateLineLengthDisplay(tempLine);
                cetnost++;
                pocet.setText(String.valueOf(cetnost));
                tempLine = null; // Reset tempLine po finalizaci
                lbDelka.setVisible(false);
            }
            
        }
        
    }
     private void applyLineStyle(Line line) {
        if ("Čárkovaná".equals(styl.getValue())) {
            line.getStrokeDashArray().setAll(5.0 * tlouska.getValue(), 5.0 * tlouska.getValue());
        } else if ("Čerchovaná".equals(styl.getValue())) {
            line.getStrokeDashArray().setAll(5.0 * tlouska.getValue(), 5.0 * tlouska.getValue(), tlouska.getValue(), 5.0 * tlouska.getValue());
        }
        line.setStroke(barva.getValue());
        line.setStrokeWidth(tlouska.getValue());
        line.setStrokeLineCap(StrokeLineCap.ROUND);
    }
    

    private void createLine() {
        Line line = new Line(startX, startY, endX, endY);
        

        if (styl.getValue() == "Čárkovaná") {
            // Nastavit čárkovaný styl
            line.getStrokeDashArray().setAll(5.0 * tlouska.getValue(), 5.0 * tlouska.getValue()); // Čára relativní k tloušťce

        } else if (styl.getValue() == "Čerchovaná") {
            // Nastavit čerchovaný styl
            line.getStrokeDashArray().setAll(5.0 * tlouska.getValue(), 5.0 * tlouska.getValue(), tlouska.getValue(), 5.0 * tlouska.getValue()); // Čára relativní k tloušťce

        }
        line.setStroke(barva.getValue());
        line.setStrokeWidth(tlouska.getValue());
        plocha.getChildren().add(line);
        cetnost++;
        pocet.setText(String.valueOf(cetnost));
    }
    
  private void removeNearestLine(double x, double y) {
    Line nearestLine = null;
    double nearestDistance = Double.MAX_VALUE;

    // Prohledání všech dětí plochy a hledání nejbližší úsečky
    for (var child : new ArrayList<>(plocha.getChildren())) { // Použijeme kopii seznamu pro bezpečné odstraňování
        if (child instanceof Line) {
            Line line = (Line) child;
            double midX = (line.getStartX() + line.getEndX()) / 2;
            double midY = (line.getStartY() + line.getEndY()) / 2;
            double distance = Math.sqrt(Math.pow(midX - x, 2) + Math.pow(midY - y, 2));

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestLine = line;
            }
        }
    }

    // Odstranění nejbližší úsečky, pokud byla nalezena
    if (nearestLine != null) {
        plocha.getChildren().remove(nearestLine);
        cetnost--;
        pocet.setText(String.valueOf(cetnost));
    }
}

    private void clearLines() {

        plocha.getChildren().removeIf(child -> child instanceof Line);
        cetnost = 0;
        pocet.setText(String.valueOf(cetnost));
    }

 

}
