package javaapplication2;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;

public class FXMLController implements Initializable {

    @FXML
    private AnchorPane Aplikace;
    @FXML
    private Button btnKonec;
    @FXML
    private Button btnVymazat;
    @FXML
    private ColorPicker cBarva;
    @FXML
    private Label lbPocet;
    @FXML
    private Pane plochaPane;

    private List<Circle> points = new ArrayList<>();
    private List<CubicCurve> curves = new ArrayList<>();
    private Circle selectedPoint = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        plochaPane.setOnMouseClicked(this::handleMouseClick);
        plochaPane.setOnMousePressed(this::handleMousePressed);
        plochaPane.setOnMouseDragged(this::handleMouseDragged);
        cBarva.setOnAction(event -> changeCurveColors(cBarva.getValue()));

        plochaPane.widthProperty().addListener((observable, oldValue, newValue) -> clearPane());
        plochaPane.heightProperty().addListener((observable, oldValue, newValue) -> clearPane());
    }

    @FXML
    private void onKonec(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void onVymazat(ActionEvent event) {
        clearPane();
    }

    private void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) {
            return; // Do not add points on right-click or middle-click
        }

        double x = event.getX();
        double y = event.getY();
        Circle point = new Circle(x, y, 5);
        point.setStroke(Color.BLACK);
        point.setFill(Color.TRANSPARENT);

        if (points.isEmpty() || points.size() % 3 == 0) {
            point.setFill(Color.RED);
        }

        points.add(point);
        plochaPane.getChildren().add(point);

        lbPocet.setText("Pocet bodu"+String.valueOf(points.size()));

        if (points.size() >= 4 && (points.size() - 1) % 3 == 0) {
            drawCurve();
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            for (Circle point : points) {
                if (point.contains(event.getX(), event.getY())) {
                    selectedPoint = point;
                    break;
                }
            }
        } else if (event.getButton() == MouseButton.MIDDLE) {
            for (Circle point : points) {
                if (point.contains(event.getX(), event.getY())) {
                    removePoint(point);
                    break;
                }
            }
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (selectedPoint != null && event.getButton() == MouseButton.SECONDARY) {
            selectedPoint.setCenterX(event.getX());
            selectedPoint.setCenterY(event.getY());
            updateCurves();
        }
    }

    private void drawCurve() {
        int size = points.size();
        if (size < 4) {
            return; // Need at least 4 points to draw a cubic curve
        }

        Circle p0 = points.get(size - 4);
        Circle p1 = points.get(size - 3);
        Circle p2 = points.get(size - 2);
        Circle p3 = points.get(size - 1);

        CubicCurve curve = new CubicCurve();
        curve.setStartX(p0.getCenterX());
        curve.setStartY(p0.getCenterY());
        curve.setControlX1(p1.getCenterX());
        curve.setControlY1(p1.getCenterY());
        curve.setControlX2(p2.getCenterX());
        curve.setControlY2(p2.getCenterY());
        curve.setEndX(p3.getCenterX());
        curve.setEndY(p3.getCenterX());

        curve.setStroke(cBarva.getValue());
        curve.setFill(Color.TRANSPARENT);

        curves.add(curve);
        plochaPane.getChildren().add(0, curve); // Add curve to the background

        bringPointsToFront();
    }

    private void updateCurves() {
        plochaPane.getChildren().removeIf(node -> node instanceof CubicCurve);
        curves.clear();

        for (int i = 0; i < points.size() - 3; i += 3) {
            Circle p0 = points.get(i);
            Circle p1 = points.get(i + 1);
            Circle p2 = points.get(i + 2);
            Circle p3 = points.get(i + 3);

            CubicCurve curve = new CubicCurve();
            curve.setStartX(p0.getCenterX());
            curve.setStartY(p0.getCenterY());
            curve.setControlX1(p1.getCenterX());
            curve.setControlY1(p1.getCenterY());
            curve.setControlX2(p2.getCenterX());
            curve.setControlY2(p2.getCenterY());
            curve.setEndX(p3.getCenterX());
            curve.setEndY(p3.getCenterY());

            curve.setStroke(cBarva.getValue());
            curve.setFill(Color.TRANSPARENT);

            curves.add(curve);
            plochaPane.getChildren().add(0, curve); // Add curve to the background
        }

        bringPointsToFront();
        lbPocet.setText("Pocet bodu"+String.valueOf(points.size()));
    }

    private void changeCurveColors(Color color) {
        for (CubicCurve curve : curves) {
            curve.setStroke(color);
        }
    }

    private void clearPane() {
        plochaPane.getChildren().clear();
        points.clear();
        curves.clear();
        lbPocet.setText("Pocet bodu:0");
    }

    private void removePoint(Circle point) {
        points.remove(point);
        plochaPane.getChildren().remove(point);
        updateCurves();
    }

    private void bringPointsToFront() {
        for (Circle point : points) {
            plochaPane.getChildren().remove(point);
            plochaPane.getChildren().add(point);
        }
    }
}
