package javaapplication2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class FXMLController implements Initializable {

    @FXML
    private AnchorPane Aplikace;

    @FXML
    private Pane paneOvladani;
    @FXML
    private Button btnNahraj;
    @FXML
    private Pane paneFotka;
    @FXML
    private Pane paneGraf;
    @FXML
    private Label lbVelikost;
    private LineChart<Number, Number> graf; // Ensure this is LineChart<Number, Number> for both axes

    private ImageView currentImageView = null; // Track the current image view
    private Rectangle selectionRectangle = new Rectangle(0, 0, 0, 0);
    private double startX;
    private double startY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectionRectangle.setStroke(javafx.scene.paint.Color.RED);
        selectionRectangle.setStrokeWidth(1);
        selectionRectangle.setFill(javafx.scene.paint.Color.TRANSPARENT);

        lbVelikost.setVisible(false);

        paneFotka.setOnMousePressed(this::handleMousePressed);
        paneFotka.setOnMouseDragged(this::handleMouseDragged);
        paneFotka.setOnMouseReleased(this::handleMouseReleased);

        // Initialize the axes directly in the controller to ensure proper types
        NumberAxis xAxis = new NumberAxis(0, 255, 64); // Numeric X axis
        //xAxis.setLabel("Intensity");

        NumberAxis yAxis = new NumberAxis(0, 100, 10); // Numeric Y axis
        //yAxis.setLabel("Percentage (%)");

        graf = new LineChart<>(xAxis, yAxis);
        //graf.setTitle("Color Intensity Histogram");
        graf.setCreateSymbols(false); // Disable symbols
        graf.setLegendVisible(false); // Disable the legend

        // Add the LineChart to paneGraf
        paneGraf.getChildren().add(graf);

        // Bind the size of the graph to the size of paneGraf
        graf.prefWidthProperty().bind(paneGraf.widthProperty());
        graf.prefHeightProperty().bind(paneGraf.heightProperty());

        // Add listeners to paneFotka to handle resizing
        paneFotka.widthProperty().addListener((obs, oldVal, newVal) -> handleResize());
        paneFotka.heightProperty().addListener((obs, oldVal, newVal) -> handleResize());
    }

    private void handleResize() {
        paneFotka.getChildren().remove(selectionRectangle);
        graf.getData().clear();
        if (currentImageView != null) {
            double paneWidth = paneFotka.getWidth();
            double paneHeight = paneFotka.getHeight();
            double imageWidth = currentImageView.getImage().getWidth();
            double imageHeight = currentImageView.getImage().getHeight();

            if (imageWidth <= paneWidth && imageHeight <= paneHeight) {
                double x = (paneWidth - imageWidth) / 2;
                double y = (paneHeight - imageHeight) / 2;

                currentImageView.setLayoutX(x);
                currentImageView.setLayoutY(y);
                currentImageView.setFitWidth(paneWidth);
                currentImageView.setFitHeight(paneHeight);
                currentImageView.setPreserveRatio(true);
            } else {
                paneFotka.getChildren().remove(currentImageView);
                currentImageView = null;
            }
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (currentImageView != null &&
            event.getX() >= currentImageView.getLayoutX() &&
            event.getY() >= currentImageView.getLayoutY() &&
            event.getX() <= currentImageView.getLayoutX() + currentImageView.getFitWidth() &&
            event.getY() <= currentImageView.getLayoutY() + currentImageView.getFitHeight()) {

            lbVelikost.setVisible(true);
            paneFotka.getChildren().remove(selectionRectangle);

            startX = event.getX();
            startY = event.getY();

            selectionRectangle.setX(startX);
            selectionRectangle.setY(startY);
            selectionRectangle.setWidth(0);
            selectionRectangle.setHeight(0);

            paneFotka.getChildren().add(selectionRectangle);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (currentImageView != null) {
            double imageX = currentImageView.getLayoutX();
            double imageY = currentImageView.getLayoutY();
            double imageWidth = currentImageView.getBoundsInParent().getWidth();
            double imageHeight = currentImageView.getBoundsInParent().getHeight();

            double maxEndX = imageX + imageWidth;
            double maxEndY = imageY + imageHeight;

            double endX = Math.min(event.getX(), maxEndX);
            double endY = Math.min(event.getY(), maxEndY);

            endX = Math.max(endX, imageX);
            endY = Math.max(endY, imageY);

            double rectX = Math.min(startX, endX);
            double rectY = Math.min(startY, endY);
            double rectWidth = Math.abs(endX - startX);
            double rectHeight = Math.abs(endY - startY);

            if (rectX + rectWidth > maxEndX) {
                rectWidth = maxEndX - rectX;
            }
            if (rectY + rectHeight > maxEndY) {
                rectHeight = maxEndY - rectY;
            }

            selectionRectangle.setX(rectX);
            selectionRectangle.setY(rectY);
            selectionRectangle.setWidth(rectWidth);
            selectionRectangle.setHeight(rectHeight);

            lbVelikost.setText(String.format("Velikost %.1f x %.1f", selectionRectangle.getWidth(), selectionRectangle.getHeight()));
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        lbVelikost.setVisible(false);
        updateHistogram();
    }

    @FXML
    private void onNahraj(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image Files (*.bmp, *.jpg)", "*.bmp", "*.jpg");
        fileChooser.getExtensionFilters().add(filter);

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file));
                if (image.getWidth() <= paneFotka.getWidth() && image.getHeight() <= paneFotka.getHeight()) {
                    paneFotka.getChildren().clear();
                    currentImageView = new ImageView(image);

                    currentImageView.setFitWidth(paneFotka.getWidth());
                    currentImageView.setFitHeight(paneFotka.getHeight());
                    currentImageView.setPreserveRatio(true);

                    double x = (paneFotka.getWidth() - currentImageView.getBoundsInParent().getWidth()) / 2;
                    double y = (paneFotka.getHeight() - currentImageView.getBoundsInParent().getHeight()) / 2;

                    currentImageView.setLayoutX(x);
                    currentImageView.setLayoutY(y);

                    paneFotka.getChildren().add(currentImageView);
                } else {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Image does not fit in the display pane.");
                    alert.showAndWait();
                }
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Loading Image");
                alert.setHeaderText(null);
                alert.setContentText("Could not load image: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void updateHistogram() {
        if (currentImageView == null || selectionRectangle.getWidth() == 0 || selectionRectangle.getHeight() == 0) {
            graf.getData().clear();
            return;
        }

        PixelReader pixelReader = currentImageView.getImage().getPixelReader();
        if (pixelReader == null) return;

        int x0 = (int) Math.max(selectionRectangle.getX() - currentImageView.getLayoutX(), 0);
        int y0 = (int) Math.max(selectionRectangle.getY() - currentImageView.getLayoutY(), 0);
        int w = (int) selectionRectangle.getWidth();
        int h = (int) selectionRectangle.getHeight();

        // Make sure the selection is within the image bounds
        x0 = Math.min(x0, (int) currentImageView.getImage().getWidth() - 1);
        y0 = Math.min(y0, (int) currentImageView.getImage().getHeight() - 1);
        w = Math.min(w, (int) currentImageView.getImage().getWidth() - x0);
        h = Math.min(h, (int) currentImageView.getImage().getHeight() - y0);

        int[] redHistogram = new int[256];
        int[] greenHistogram = new int[256];
        int[] blueHistogram = new int[256];

        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                int argb = pixelReader.getArgb(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8)  & 0xFF;
                int b =  argb & 0xFF;
                redHistogram[r]++;
                greenHistogram[g]++;
                blueHistogram[b]++;
            }
        }

        XYChart.Series<Number, Number> redSeries = new XYChart.Series<>();
        redSeries.setName("Red");
        
        XYChart.Series<Number, Number> greenSeries = new XYChart.Series<>();
        //greenSeries.setName("Green");
        XYChart.Series<Number, Number> blueSeries = new XYChart.Series<>();
       // blueSeries.setName("Blue");

        int max = 0;
        for (int i = 0; i < 256; i++) {
            max = Math.max(max, redHistogram[i]);
            max = Math.max(max, greenHistogram[i]);
            max = Math.max(max, blueHistogram[i]);
        }

        if (max == 0) max = 1; // Prevent division by zero

        for (int i = 0; i < 256; i++) {
            redSeries.getData().add(new XYChart.Data<>(i, (redHistogram[i] * 100.0 / max)));
            greenSeries.getData().add(new XYChart.Data<>(i, (greenHistogram[i] * 100.0 / max)));
            blueSeries.getData().add(new XYChart.Data<>(i, (blueHistogram[i] * 100.0 / max)));
        }

        graf.getData().clear();
        graf.getData().addAll(redSeries, greenSeries, blueSeries);

        // Nastavení barev přímo v kódu
        setSeriesStyle(redSeries, "-fx-stroke: #ff0000;"); // červená barva
        setSeriesStyle(greenSeries, "-fx-stroke: #00ff00;"); // zelená barva
        setSeriesStyle(blueSeries, "-fx-stroke: #0000ff;"); // modrá barva
    }

    private void setSeriesStyle(XYChart.Series<Number, Number> series, String style) {
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle(style);
            }
        });
        for (XYChart.Data<Number, Number> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(style);
                }
            });
        }
    }
}
