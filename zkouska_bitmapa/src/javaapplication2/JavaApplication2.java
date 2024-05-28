/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javaapplication2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author mojan
 */
public class JavaApplication2 extends Application {

    private ImageView imageView;
    private BarChart<String, Number> histogramChart;

    @Override
    public void start(Stage primaryStage) {
        Button loadImageButton = new Button("Načíst obrázek");
        loadImageButton.setOnAction(this::handleLoadImageButton);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(300);

        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().addAll(imageView);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        histogramChart = new BarChart<>(xAxis, yAxis);

        VBox histogramBox = new VBox(10);
        histogramBox.setAlignment(Pos.CENTER);
        histogramBox.getChildren().addAll(histogramChart);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(imageBox);
        root.setRight(histogramBox);
        root.setBottom(loadImageButton);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Image Histogram");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLoadImageButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file));
                imageView.setImage(image);
                generateHistogram(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateHistogram(Image image) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int[] histogram = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int gray = (red + green + blue) / 3;
                histogram[gray]++;
            }
        }

        List<String> categories = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            categories.add(String.valueOf(i));
        }

        histogramChart.getData().clear();
        ObservableList<BarChart.Series<String, Number>> barChartData = FXCollections.observableArrayList();
        BarChart.Series<String, Number> series = new BarChart.Series<>();
        for (int i = 0; i < categories.size(); i++) {
            series.getData().add(new BarChart.Data<>(categories.get(i), histogram[i]));
        }
        barChartData.add(series);
        histogramChart.setData(barChartData);
        histogramChart.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();

            int index = (int) (x / histogramChart.getXAxis().getWidth() * histogram.length);
            int value = histogram[index];

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Pixel Information");
            alert.setHeaderText("Pozice pixelu: (" + x + ", " + y + ")");
            alert.setContentText("Hodnoty jasu (RGB): " + value + " " + value + " " + value);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}
