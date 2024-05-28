package javaapplication2;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class FXMLController implements Initializable {

    @FXML
    private AnchorPane Aplikace;

    @FXML
    private Pane paneOvladani;
    @FXML
    private Label lbVelikost;
    @FXML
    private Button btnVymaz;
    @FXML
    private Button btnKonec;
    @FXML
    private ColorPicker cPicker;
    @FXML
    private Pane plochaPane;

    private Circle drawingCircle;
    private Rectangle rectangle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vytvoření lineárního přechodu (světle modrá -> žlutá -> světle modrá)
        LinearGradient linearGradient = new LinearGradient(
                0, 0, 0, 1, true, // true means the gradient is proportional to the pane's size
                javafx.scene.paint.CycleMethod.REFLECT, // Reflect to create a mirrored effect
                new Stop(0, Color.LIGHTBLUE),
                new Stop(0.5, Color.YELLOW),
                new Stop(1, Color.LIGHTBLUE)
        );

        // Nastavení pozadí pro plochaPane
        plochaPane.setBackground(new Background(new BackgroundFill(linearGradient, CornerRadii.EMPTY, null)));

        // Vytvoření a přidání pravoúhelníku
        rectangle = new Rectangle();
        rectangle.setStroke(Color.RED);
        rectangle.setStrokeWidth(1);
        rectangle.setFill(Color.TRANSPARENT);
        plochaPane.getChildren().add(rectangle);

        // Binding pro změnu velikosti a centrování pravoúhelníku
        rectangle.widthProperty().bind(plochaPane.widthProperty().divide(2));
        rectangle.heightProperty().bind(plochaPane.heightProperty().divide(2));
        rectangle.xProperty().bind(plochaPane.widthProperty().subtract(rectangle.widthProperty()).divide(2));
        rectangle.yProperty().bind(plochaPane.heightProperty().subtract(rectangle.heightProperty()).divide(2));

        // Nastavení obslužných metod pro události myši
        plochaPane.setOnMousePressed(this::handleMousePressed);
        plochaPane.setOnMouseDragged(this::handleMouseDragged);
        plochaPane.setOnMouseReleased(this::handleMouseReleased);

        // Přidání posluchače změn pro ColorPicker
        cPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateCircleStrokes(newValue));

        // Přidání posluchače změn pro velikost plochaPane
        plochaPane.widthProperty().addListener((observable, oldValue, newValue) -> updateCircles());
        plochaPane.heightProperty().addListener((observable, oldValue, newValue) -> updateCircles());
    }

    @FXML
    private void onVymaz(ActionEvent event) {
        // Implementace pro vymazání obsahu
        plochaPane.getChildren().clear();
        plochaPane.getChildren().add(rectangle); // Znovu přidat rectangle, protože byl odstraněn
    }

    @FXML
    private void onKonec(ActionEvent event) {
        // Zobrazení potvrzovacího okna
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Potvrzení ukončení");
        alert.setHeaderText(null);
        alert.setContentText("Opravdu chcete ukončit aplikaci?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Zavření aplikace
            System.exit(0);
        }
    }

    private void handleMousePressed(MouseEvent event) {
        // Vytvoření nového kruhu při stisknutí levého tlačítka myši
        if (event.isPrimaryButtonDown()) {
            drawingCircle = new Circle();
            drawingCircle.setStroke(cPicker.getValue());
            drawingCircle.setStrokeWidth(3);
            drawingCircle.setFill(Color.TRANSPARENT);
            drawingCircle.setCenterX(event.getX());
            drawingCircle.setCenterY(event.getY());
            plochaPane.getChildren().add(drawingCircle);
        } else if (event.isSecondaryButtonDown()) {
            // Smazání nejbližšího kruhu při stisknutí pravého tlačítka myši
            Circle nearestCircle = findNearestCircle(event.getX(), event.getY());
            if (nearestCircle != null) {
                plochaPane.getChildren().remove(nearestCircle);
            }
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        // Aktualizace poloměru kruhu při tažení myši
        if (drawingCircle != null) {
            double radius = Math.hypot(event.getX() - drawingCircle.getCenterX(), event.getY() - drawingCircle.getCenterY());
            drawingCircle.setRadius(radius);
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        // Nastavení výplně kruhu po ukončení kreslení
        if (drawingCircle != null) {
            updateCircleFill(drawingCircle);
            drawingCircle = null;
        }
    }

    private void updateCircleFill(Circle circle) {
        double circleMinX = circle.getCenterX() - circle.getRadius();
        double circleMaxX = circle.getCenterX() + circle.getRadius();
        double circleMinY = circle.getCenterY() - circle.getRadius();
        double circleMaxY = circle.getCenterY() + circle.getRadius();

        double rectMinX = rectangle.getX();
        double rectMaxX = rectangle.getX() + rectangle.getWidth();
        double rectMinY = rectangle.getY();
        double rectMaxY = rectangle.getY() + rectangle.getHeight();

        if (circleMinX >= rectMinX && circleMaxX <= rectMaxX && circleMinY >= rectMinY && circleMaxY <= rectMaxY) {
            // Kruh je celý uvnitř obdélníku
            circle.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
        } else if (circleMaxX < rectMinX || circleMinX > rectMaxX || circleMaxY < rectMinY || circleMinY > rectMaxY) {
            // Kruh je celý mimo obdélník
            circle.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.5));
        } else {
            // Kruh je částečně uvnitř obdélníku
            circle.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.5));
        }
    }

    private void updateCircleStrokes(Color newColor) {
        plochaPane.getChildren().filtered(node -> node instanceof Circle).forEach(node -> {
            Circle circle = (Circle) node;
            circle.setStroke(newColor);
        });
    }

    private void updateCircles() {
        List<Circle> circlesToRemove = plochaPane.getChildren().filtered(node -> node instanceof Circle).stream()
            .map(node -> (Circle) node)
            .peek(this::updateCircleFill)
            .filter(circle -> !isCircleVisible(circle))
            .collect(Collectors.toList());

        plochaPane.getChildren().removeAll(circlesToRemove);
    }

    private boolean isCircleVisible(Circle circle) {
        double minX = circle.getCenterX() - circle.getRadius();
        double maxX = circle.getCenterX() + circle.getRadius();
        double minY = circle.getCenterY() - circle.getRadius();
        double maxY = circle.getCenterY() + circle.getRadius();

        return minX < plochaPane.getWidth() && maxX > 0 && minY < plochaPane.getHeight() && maxY > 0;
    }

    private Circle findNearestCircle(double x, double y) {
        List<Circle> circles = plochaPane.getChildren().filtered(node -> node instanceof Circle).stream()
                .map(node -> (Circle) node)
                .collect(Collectors.toList());

        return circles.stream()
                .min(Comparator.comparingDouble(circle -> Math.hypot(circle.getCenterX() - x, circle.getCenterY() - y)))
                .orElse(null);
    }
}
