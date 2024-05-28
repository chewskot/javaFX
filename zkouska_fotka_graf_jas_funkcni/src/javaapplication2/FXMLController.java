package javaapplication2;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class FXMLController implements Initializable {

    @FXML
    private HBox hb;

    @FXML
    private Pane fotkaPane;
    
    @FXML
    private Button btnLoad;
    
    @FXML
    private Pane grafPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void onLoad(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );

        // Get the stage from the event source
        Stage stage = (Stage) btnLoad.getScene().getWindow();
        
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);

                // Set the ImageView to fit the size of fotkaPane
                imageView.setFitWidth(fotkaPane.getWidth());
                imageView.setFitHeight(fotkaPane.getHeight());
                imageView.setPreserveRatio(false);

                // Clear the pane before adding new image
                fotkaPane.getChildren().clear();
                fotkaPane.getChildren().add(imageView);

                // Bind the ImageView size to the fotkaPane size
                imageView.fitWidthProperty().bind(fotkaPane.widthProperty());
                imageView.fitHeightProperty().bind(fotkaPane.heightProperty());

                // Add event handler for mouse click
                imageView.setOnMouseClicked((MouseEvent e) -> {
                    showPixelInfo(e, image);
                });

                // Create histogram
                createHistogram(image);

            } catch (Exception e) {
                showErrorDialog("Error loading image", "There was an error loading the image file.");
            }
        }
    }

    private void createHistogram(Image image) {
        int[] histogram = new int[256];

        PixelReader pixelReader = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                int brightness = (int) Math.round(
                        (color.getRed() * 0.299 +
                         color.getGreen() * 0.587 +
                         color.getBlue() * 0.114) * 255
                );
                histogram[brightness]++;
            }
        }

        // Normalize histogram
        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > max) {
                max = histogram[i];
            }
        }

        double[] normalizedHistogram = new double[256];
        for (int i = 0; i < histogram.length; i++) {
            normalizedHistogram[i] = ((double) histogram[i] / max) * 100;
        }

        // Create BarChart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10); // Set tick unit to 10%
        yAxis.setMinorTickCount(1); // Number of minor ticks per major tick, for 2% increments

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setTitle(null);
        xAxis.setLabel(null);
        yAxis.setLabel(null);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(true);
        yAxis.setTickMarkVisible(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < normalizedHistogram.length; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), normalizedHistogram[i]));
        }

        barChart.getData().add(series);

        // Set the bar color to black
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: black;");
        }

        // Clear the pane before adding new chart
        grafPane.getChildren().clear();
        grafPane.getChildren().add(barChart);
    }

    private void showPixelInfo(MouseEvent e, Image image) {
        double mouseX = e.getX();
        double mouseY = e.getY();

        // Calculate image coordinates
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double viewWidth = ((ImageView) e.getSource()).getFitWidth();
        double viewHeight = ((ImageView) e.getSource()).getFitHeight();
        double xRatio = imageWidth / viewWidth;
        double yRatio = imageHeight / viewHeight;

        int imgX = (int) (mouseX * xRatio);
        int imgY = (int) (mouseY * yRatio);

        // Get pixel color
        PixelReader pixelReader = image.getPixelReader();
        Color color = pixelReader.getColor(imgX, imgY);

        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);

        String message = String.format("pixel[%d,%d] - RGB(%d,%d,%d)", imgX, imgY, red, green, blue);

        // Show pixel info dialog
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Pixel Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
