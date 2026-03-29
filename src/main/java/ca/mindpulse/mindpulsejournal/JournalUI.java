package ca.mindpulse.mindpulsejournal;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class JournalUI {
    private final JournalService service;
    private final TableView<JournalEntry> table = new TableView<>();
    private final LineChart<Number, Number> chart;
    private final Label riskLabel = new Label("Wellness Status: Calculating...");

    public JournalUI(Stage stage, JournalService service, String username) {
        this.service = service;
        stage.setTitle("Y-Rhythm Dashboard | " + username);

        // Header
        DatePicker picker = new DatePicker(LocalDate.now());
        Button searchBtn = new Button("Search");
        HBox header = new HBox(10, new Label("History:"), picker, searchBtn);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #a5d6a7; -fx-border-width: 0 0 2 0;");

        // Table
        setupTable();

        // Chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 5, 1);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setPrefHeight(200);

        // Inputs
        ComboBox<Mood> moodBox = new ComboBox<>(FXCollections.observableArrayList(Mood.values()));
        TextArea entryArea = new TextArea();
        entryArea.setWrapText(true);
        entryArea.setStyle("-fx-border-color: #81c784;");

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-weight: bold;");
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ff7043; -fx-text-fill: white; -fx-background-radius: 15;");
        Button exportBtn = new Button("CSV Export");
        exportBtn.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-background-radius: 15;");
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #607d8b; -fx-text-fill: white; -fx-background-radius: 15;");

        saveBtn.setOnAction(e -> {
            try {
                service.saveEntry(new JournalEntry((int)System.currentTimeMillis(), LocalDate.now(), moodBox.getValue(), entryArea.getText()));
                entryArea.clear(); refresh();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        deleteBtn.setOnAction(e -> {
            JournalEntry s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                try { service.deleteEntry(s.getId()); refresh(); } catch (Exception ex) {}
            }
        });

        exportBtn.setOnAction(e -> {
            try {
                CSVExporter.export(service.getAllEntries(), username + "_YRhythm.csv");
                new Alert(Alert.AlertType.INFORMATION, "Exported to " + username + "_YRhythm.csv").show();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        logoutBtn.setOnAction(e -> ExitDialog.show(stage));

        VBox bottom = new VBox(10, riskLabel, moodBox, entryArea, new HBox(10, saveBtn, deleteBtn, exportBtn, logoutBtn));
        bottom.setPadding(new Insets(20));
        bottom.setStyle("-fx-background-color: #f1f8e9;");

        BorderPane root = new BorderPane();
        root.setTop(new VBox(header, chart));
        root.setCenter(table);
        root.setBottom(bottom);

        stage.setScene(new Scene(root, 1000, 850));
        refresh();
    }

    private void setupTable() {
        TableColumn<JournalEntry, LocalDate> dCol = new TableColumn<>("Date");
        dCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<JournalEntry, Mood> mCol = new TableColumn<>("Mood");
        mCol.setCellValueFactory(new PropertyValueFactory<>("mood"));
        TableColumn<JournalEntry, String> cCol = new TableColumn<>("Notes");
        cCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        table.getColumns().addAll(dCol, mCol, cCol);
    }

    private void refresh() {
        try {
            var entries = service.getAllEntries();
            table.setItems(FXCollections.observableArrayList(entries));
            RiskTier tier = service.getRiskTier();
            riskLabel.setText("Health Rhythm: " + tier + " (" + service.getCurrentRiskScore() + "/100)");
            riskLabel.setStyle(tier == RiskTier.HIGH ? "-fx-text-fill: #d32f2f; -fx-font-weight: bold;" : "-fx-text-fill: #2e7d32;");

            chart.getData().clear();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            for (int i = 0; i < entries.size(); i++) {
                series.getData().add(new XYChart.Data<>(i+1, entries.get(i).getMood().getIntensity()));
            }
            chart.getData().add(series);
        } catch (Exception e) {}
    }
}