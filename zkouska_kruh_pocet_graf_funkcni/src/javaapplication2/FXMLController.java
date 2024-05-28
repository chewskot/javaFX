package javaapplication2;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.event.ActionEvent;

public class FXMLController implements Initializable {

    @FXML
    private Button btnKonec;
    @FXML
    private Button btnVymaz;
    @FXML
    private Label lbPocet;
    @FXML
    private Pane grafPlocha;
    @FXML
    private Pane kruhyPlocha;
    Circle vetsiKruh;
    Circle mensiKruh;
    List<Circle> kruhy = new ArrayList<>();
    int pocet = 0;
    private PieChart pieChart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializace kruhů
        vetsiKruh = new Circle();
        mensiKruh = new Circle();

        // Nastavení barev a průhlednosti kruhů
        vetsiKruh.setStroke(Color.RED);
        mensiKruh.setStroke(Color.RED);
        vetsiKruh.setFill(Color.TRANSPARENT);
        mensiKruh.setFill(Color.TRANSPARENT);

        kruhyPlocha.getChildren().addAll(vetsiKruh, mensiKruh);

        // Nastavení středu a poloměrů kruhů
        updateCirclePositionsAndSizes();

        // Posluchač pro změnu velikosti kruhyPlocha
        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateCirclePositionsAndSizes();
        kruhyPlocha.widthProperty().addListener(sizeListener);
        kruhyPlocha.heightProperty().addListener(sizeListener);

        // Vytvoření VBox pro legendu s mezerou 5 pixelů
        VBox legenda = new VBox(5);
        grafPlocha.getChildren().add(legenda);

        // Inicializace PieChart s preferovanými rozměry
        pieChart = new PieChart();
        pieChart.setPrefSize(300, 300);  // Zvětšení velikosti pro lepší viditelnost

        PieChart.Data uvnitrMalehoData = new PieChart.Data("Uvnitř malého: 0%", 0);
        PieChart.Data uvnitrVelkehoData = new PieChart.Data("Uvnitř velkého: 0%", 0);
        PieChart.Data mimoKruhyData = new PieChart.Data("Mimo kruhy: 0%", 0);

        pieChart.getData().addAll(uvnitrMalehoData, uvnitrVelkehoData, mimoKruhyData);
        pieChart.setLabelsVisible(true);  // Nastavení viditelnosti popisků

        // Přidání grafu do grafPlocha
        grafPlocha.getChildren().add(pieChart);

        // Umístění grafu a legendy
        pieChart.relocate(10, 10);  // Umístění grafu v grafPlocha
        legenda.relocate(320, 10);  // Umístění legendy vedle grafu
    }

    private void updateCirclePositionsAndSizes() {
        double width = kruhyPlocha.getWidth();
        double height = kruhyPlocha.getHeight();

        double centerX = width / 2;
        double centerY = height / 2;

        double radius = Math.min(width, height) / 3;  // Změna velikosti kruhů pro lepší viditelnost

        // Nastavení středů a poloměrů kruhů
        vetsiKruh.setCenterX(centerX);
        vetsiKruh.setCenterY(centerY);
        vetsiKruh.setRadius(radius);

        mensiKruh.setCenterX(centerX);
        mensiKruh.setCenterY(centerY);
        mensiKruh.setRadius(radius / 2);
    }

    @FXML
    private void onKonec(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void onVymaz(ActionEvent event) {
        kruhyPlocha.getChildren().removeAll(kruhy);
        kruhy.clear();
        pocet = 0;
        lbPocet.setText("Počet: " + pocet);
        updateGraf();
    }

    @FXML
    private void onPress(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            Circle c = new Circle(5);
            c.setStroke(Color.BLUE);
            c.setFill(Color.YELLOW);
            c.setCenterX(event.getX());
            c.setCenterY(event.getY());
            kruhyPlocha.getChildren().add(c);
            kruhy.add(c);
            pocet++;
            lbPocet.setText("Počet: " + pocet);
            updateGraf();
        } else if (event.getButton() == MouseButton.SECONDARY) {
            removeNearestCircle(event.getX(), event.getY());
        }
    }

    private void removeNearestCircle(double x, double y) {
        Circle nearestCircle = null;
        double minDistance = Double.MAX_VALUE;

        for (Circle circle : kruhy) {
            double distance = Math.sqrt(Math.pow(circle.getCenterX() - x, 2) + Math.pow(circle.getCenterY() - y, 2));
            if (distance < minDistance) {
                minDistance = distance;
                nearestCircle = circle;
            }
        }

        if (nearestCircle != null) {
            kruhyPlocha.getChildren().remove(nearestCircle);
            kruhy.remove(nearestCircle);
            pocet--;
            lbPocet.setText("Počet: " + pocet);
            updateGraf();
        }
    }

    private void updateGraf() {
        int uvnitrMaleho = 0;
        int uvnitrVelkeho = 0;
        double total = kruhy.size();

        for (Circle kruh : kruhy) {
            double distance = Math.sqrt(Math.pow(kruh.getCenterX() - mensiKruh.getCenterX(), 2) + Math.pow(kruh.getCenterY() - mensiKruh.getCenterY(), 2));
            if (distance < mensiKruh.getRadius()) {
                uvnitrMaleho++;
            } else if (distance < vetsiKruh.getRadius()) {
                uvnitrVelkeho++;
            }
        }

        int mimoKruhy = kruhy.size() - uvnitrMaleho - uvnitrVelkeho;

        // Výpočet procentuálního zastoupení
        double finalUvnitrMaleho = (total > 0) ? (uvnitrMaleho / total) * 100 : 0;
        double finalUvnitrVelkeho = (total > 0) ? (uvnitrVelkeho / total) * 100 : 0;
        double finalMimoKruhy = (total > 0) ? (mimoKruhy / total) * 100 : 0;

        Platform.runLater(() -> {
            pieChart.getData().get(0).setPieValue(finalUvnitrMaleho);
            pieChart.getData().get(1).setPieValue(finalUvnitrVelkeho);
            pieChart.getData().get(2).setPieValue(finalMimoKruhy);

            pieChart.getData().get(0).setName(String.format("Uvnitř malého: %.2f%%", finalUvnitrMaleho));
            pieChart.getData().get(1).setName(String.format("Uvnitř velkého: %.2f%%", finalUvnitrVelkeho));
            pieChart.getData().get(2).setName(String.format("Mimo kruhy: %.2f%%", finalMimoKruhy));
        });
    }
}
