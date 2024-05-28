package javaapplication2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Side;
import javafx.stage.FileChooser;

public class FXMLController implements Initializable {

    @FXML
    private Button btnKonec;
    @FXML
    private Button btnPridej;
    @FXML
    private Button btnOdeber;
    @FXML
    private Button btnOdeberVse;
    @FXML
    private Button btnUloz;
    @FXML
    private Button btnNacti;
    @FXML
    private ListView<String> lvSlova;
    @FXML
    private Pane grafPane;
    ObservableList<String> slova = FXCollections.observableArrayList();
    private InputController input;
    String slovo;

    private PieChart letterFrequencyChart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lvSlova.setItems(slova);

        // Inicializace koláčového grafu
        letterFrequencyChart = new PieChart();
        //letterFrequencyChart.setTitle("Frekvence písmen");

        // Nastavení vlastností grafu
        letterFrequencyChart.setLegendVisible(true);
        letterFrequencyChart.setLegendSide(Side.RIGHT);

        // Přidání grafu do grafPane
        grafPane.getChildren().add(letterFrequencyChart);
        letterFrequencyChart.setPrefSize(grafPane.getPrefWidth(), grafPane.getPrefHeight());
    }

    @FXML
    private void onKonec(ActionEvent event) {
        Stage stage = (Stage) btnKonec.getScene().getWindow();
        stage.close(); // Zavře okno
    }

    @FXML
    private void onPridej(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Input.fxml"));
            Parent root = loader.load();
            input = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (input.isOkPressed()) {
                slovo = input.getSlovaTxt().getText();
                System.out.println(slovo);
                slova.add(slovo);
                updateChart(); // Aktualizujte graf po přidání slova
            }
        } catch (IOException e) {
            System.out.println("chyba");
            e.printStackTrace();
        }
    }

    private void updateChart() {
        Map<String, Integer> letterFrequency = new HashMap<>();

        // Spočítat frekvence písmen ve všech slovech
        for (String word : slova) {
            for (char ch : word.toCharArray()) {
                String letter = String.valueOf(ch).toLowerCase();
                letterFrequency.put(letter, letterFrequency.getOrDefault(letter, 0) + 1);
            }
        }

        // Aktualizovat data v koláčovém grafu
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        letterFrequency.forEach((letter, count) -> {
            String label = String.format("%s (%d)", letter.toUpperCase(), count);
            pieChartData.add(new PieChart.Data(label, count));
        });

        letterFrequencyChart.setData(pieChartData);
    }

    @FXML
    private void onOdeber(ActionEvent event) {
        String selectedItem = lvSlova.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            slova.remove(selectedItem);
            updateChart(); // Aktualizujte graf po odebrání slova
        }
    }

    @FXML
    private void onOdeberVse(ActionEvent event) {
        slova.clear();
        updateChart(); // Aktualizujte graf po odebrání všech slov
    }

   @FXML
    private void onUloz(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Uložit slova do souboru");
        
        // Nastavení filtrů pro typy souborů
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(txtFilter, allFilter);

        File file = fileChooser.showSaveDialog(btnUloz.getScene().getWindow());
        if (file != null) {
            saveWordsToFile(file);
        }
    }

     @FXML
    private void onNacti(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Načíst slova ze souboru");
        
        // Nastavení filtrů pro typy souborů
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(txtFilter, allFilter);

        File file = fileChooser.showOpenDialog(btnNacti.getScene().getWindow());
        if (file != null) {
            loadWordsFromFile(file);
        }
    }
    private void saveWordsToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String word : slova) {
                writer.write(word);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Nepodařilo se uložit slova do souboru.");
            e.printStackTrace();
        }
    }
    private void loadWordsFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            slova.clear();
            while ((line = reader.readLine()) != null) {
                slova.add(line);
            }
            updateChart(); // Aktualizujte graf po načtení slov
        } catch (IOException e) {
            System.out.println("Nepodařilo se načíst slova ze souboru.");
            e.printStackTrace();
        }
    }
}
