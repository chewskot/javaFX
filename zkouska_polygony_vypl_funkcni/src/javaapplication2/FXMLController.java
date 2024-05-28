package javaapplication2;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class FXMLController implements Initializable {

    @FXML
    private Button btnKonec;
    @FXML
    private Button btnVymaz;
    @FXML
    private CheckBox chVypln;
    @FXML
    private ColorPicker barva;
    @FXML
    private Pane Panel;
    @FXML
    private Pane menu;
    @FXML
    private Rectangle modryRect;

    private List<Double> points = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();
    private List<Polygon> polygons = new ArrayList<>();
    private Line currentLine = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Panel.setStyle("-fx-background-color: rgb(255,255,192)");
        barva.setValue(Color.RED);

        // Bind the width of the Rectangle to the width of the Pane
        modryRect.widthProperty().bind(menu.widthProperty());

        Panel.setOnMouseMoved(this::onMouseMoved);
        Panel.setOnMousePressed(this::onPress);
        Panel.setOnMouseReleased(this::onRelease);
    }

    @FXML
    private void onKonec(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Potvrzení ukončení");
        alert.setHeaderText("Opravdu chcete ukončit aplikaci?");
        alert.setContentText("Klikněte na OK pro ukončení nebo Cancel pro návrat.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    @FXML
    private void onVymaz(ActionEvent event) {
        Panel.getChildren().removeAll(lines); // Odstraní všechny čáry z Panelu
        lines.clear(); // Vyprázdní seznam lines
        points.clear();

        // Odstraníme všechny polygony z Panelu a vyprázdníme seznam polygonů
        Panel.getChildren().removeAll(polygons);
        polygons.clear();
    }

    @FXML
    private void onVypln(ActionEvent event) {
    }

    @FXML
    private void onPress(MouseEvent event) {
        // Ignore right-clicks for creating lines or polygons
        if (event.getButton() == MouseButton.SECONDARY) {
            return;
        }

        double x = event.getX();
        double y = event.getY();

        if (event.getButton() == MouseButton.PRIMARY && event.isControlDown()) {
            points.add(x);
            points.add(y);
            if (points.size() > 2) {
                points.add(points.get(0)); // Add the first point's x-coordinate
                points.add(points.get(1)); // Add the first point's y-coordinate
                createPolygon();
            }
            if (currentLine != null) {
                Panel.getChildren().remove(currentLine);
                currentLine = null;
            }
        } else {
            if (points.isEmpty()) {
                points.add(x);
                points.add(y);
            } else {
                double lastX = points.get(points.size() - 2);
                double lastY = points.get(points.size() - 1);
                createLine(lastX, lastY, x, y);
                points.add(x);
                points.add(y);
            }
        }
    }

    @FXML
    private void onRelease(MouseEvent event) {
        if (currentLine != null) {
            Panel.getChildren().remove(currentLine);
            currentLine = null;
        }
    }

    private void onMouseMoved(MouseEvent event) {
        if (!points.isEmpty() && currentLine == null) {
            double lastX = points.get(points.size() - 2);
            double lastY = points.get(points.size() - 1);
            currentLine = new Line(lastX, lastY, event.getX(), event.getY());
            currentLine.setStroke(Color.BLACK); // Nastavíme barvu čáry na černou
            currentLine.setStrokeWidth(2.0);

            // Nastavíme čáru jako pruhovanou
            double dashWidth = 10.0; // Šířka pruhů
            double gapWidth = 10.0; // Šířka mezer mezi pruhy
            currentLine.getStrokeDashArray().addAll(dashWidth, gapWidth);

            Panel.getChildren().add(currentLine);
        } else if (currentLine != null) {
            currentLine.setEndX(event.getX());
            currentLine.setEndY(event.getY());
        }
    }

    private void createLine(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLACK); // Nastavíme barvu čáry na černou
        line.setStrokeWidth(2.0);

        // Nastavíme čáru jako pruhovanou
        double dashWidth = 10.0; // Šířka pruhů
        double gapWidth = 10.0; // Šířka mezer mezi pruhy
        line.getStrokeDashArray().addAll(dashWidth, gapWidth);

        lines.add(line);
        Panel.getChildren().add(line);
    }

    private void createPolygon() {
        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(points);

        if (chVypln.isSelected()) {
            polygon.setFill(barva.getValue());
        } else {
            polygon.setFill(Color.TRANSPARENT); // Nastavíme průhlednou výplň
        }

        polygon.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                removePolygon(polygon);
                event.consume(); // Consume the event so it doesn't propagate
            }
        });

        List<Line> polygonLines = createLinesFromPolygon(polygon);
        lines.addAll(polygonLines);

        // Přidáme polygon do seznamu polygonů
        polygons.add(polygon);

        Panel.getChildren().addAll(polygon);
        Panel.getChildren().addAll(polygonLines);
        points.clear();
    }

    private List<Line> createLinesFromPolygon(Polygon polygon) {
        List<Line> polygonLines = new ArrayList<>();
        List<Double> points = polygon.getPoints();
        int numPoints = points.size();

        for (int i = 0; i < numPoints; i += 2) {
            double startX = points.get(i);
            double startY = points.get(i + 1);
            double endX = points.get((i + 2) % numPoints);
            double endY = points.get((i + 3) % numPoints);
            Line line = new Line(startX, startY, endX, endY);
            line.setStroke(Color.BLACK); // Nastavíme barvu čáry na černou
            line.setStrokeWidth(2.0);
            polygonLines.add(line);
        }

        return polygonLines;
    }

    private void removePolygon(Polygon polygon) {
        Panel.getChildren().remove(polygon);
        polygons.remove(polygon);

        // Remove associated lines
        List<Line> linesToRemove = new ArrayList<>();
        for (Line line : lines) {
            if (polygon.getPoints().contains(line.getStartX()) && polygon.getPoints().contains(line.getStartY()) &&
                polygon.getPoints().contains(line.getEndX()) && polygon.getPoints().contains(line.getEndY())) {
                linesToRemove.add(line);
            }
        }
        lines.removeAll(linesToRemove);
        Panel.getChildren().removeAll(linesToRemove);
    }
}
