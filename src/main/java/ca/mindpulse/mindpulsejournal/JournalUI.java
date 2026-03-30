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
import java.util.List;

public class JournalUI {
    private final JournalService service;
    private final TableView<JournalEntry> table = new TableView<>();
    private final LineChart<Number, Number> chart;
    private final Label riskLabel = new Label("Wellness Status: Calculating...");
    private final BorderPane root = new BorderPane();
    private final TextArea contentArea = new TextArea();
    private final String username;

    public JournalUI(Stage stage, JournalService service, String username) {
        this.service = service;
        this.username = username;
        stage.setTitle("Y-Rhythm | " + username);

        // --- 1. HEADER (Search & Sync) ---
        DatePicker picker = new DatePicker(LocalDate.now());
        Button searchBtn = new Button("Search");
        Button syncBtn = new Button("☁ Sync Cloud");
        syncBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-background-radius: 15;");
        syncBtn.setOnAction(e -> SyncService.syncToCloud(stage, username));

        HBox header = new HBox(10, new Label("History:"), picker, searchBtn, syncBtn);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-border-color: #a5d6a7; -fx-border-width: 0 0 2 0;");

        // --- 2. CENTER (Table & Chart) ---
        setupTable();
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 5, 1);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Mood Trends");

        VBox centerContent = new VBox(20, header, table, chart);
        centerContent.setPadding(new Insets(10));

        // --- 3. RIGHT PANEL (Entry & Tools) ---
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(350);

        ComboBox<Mood> moodBox = new ComboBox<>(FXCollections.observableArrayList(Mood.values()));
        moodBox.setPromptText("Select Mood");

        Button promptBtn = new Button("💡 Get Prompt");
        promptBtn.setOnAction(e -> contentArea.setPromptText(PromptService.getRandomPrompt()));

        Button saveBtn = new Button("Save Entry");
        saveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> handleSave(moodBox));

        // Restoration of Delete & Export
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete());

        Button exportBtn = new Button("Export CSV");
        exportBtn.setOnAction(e -> handleExport());

        // Restoration of Help Button
        Button helpBtn = new Button("Get Help");
        helpBtn.setStyle("-fx-border-color: #ffa000; -fx-text-fill: #ff6f00;");
        helpBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, service.getCrisisResources()).show());

        VBox toolBox = new VBox(10, new Label("Entry Tools:"), promptBtn, deleteBtn, exportBtn, helpBtn);
        toolBox.setPadding(new Insets(10));
        toolBox.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 10;");

        rightPanel.getChildren().addAll(new Label("New Entry:"), moodBox, contentArea, saveBtn, new Separator(), toolBox);

        // --- 4. TABS (Idea 1 Integration) ---
        TabPane tabPane = new TabPane();
        Tab dashTab = new Tab("Dashboard", centerContent);
        Tab mapTab = new Tab("York Wellness Map", WellnessMapUI.getMapNode());
        dashTab.setClosable(false);
        mapTab.setClosable(false);
        tabPane.getTabs().addAll(dashTab, mapTab);

        // --- 5. FINAL LAYOUT ---
        root.setCenter(tabPane);
        root.setRight(rightPanel);
        root.setBottom(riskLabel);
        BorderPane.setMargin(riskLabel, new Insets(10));

        stage.setScene(new Scene(root, 1200, 850));
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

    private void handleSave(ComboBox<Mood> moodBox) {
        try {
            if (moodBox.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Please select a mood.").show();
                return;
            }
            int id = (int) (System.currentTimeMillis() / 1000);
            JournalEntry entry = new JournalEntry(id, LocalDate.now(), moodBox.getValue(), contentArea.getText());
            service.saveEntry(entry);
            contentArea.clear();
            refresh();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleDelete() {
        JournalEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.deleteEntry(selected.getId());
                refresh();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void handleExport() {
        try {
            CSVExporter.export(service.getAllEntries(), username + "_export.csv");
            new Alert(Alert.AlertType.INFORMATION, "Exported to " + username + "_export.csv").show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refresh() {
        try {
            List<JournalEntry> entries = service.getAllEntries();
            table.setItems(FXCollections.observableArrayList(entries));

            RiskTier tier = service.getRiskTier();
            riskLabel.setText("Health Rhythm: " + tier + " (" + service.getCurrentRiskScore() + "/100)");

            updateChart(entries);
            updateVisuals(entries); // Idea 2: Dynamic Colors
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateChart(List<JournalEntry> entries) {
        chart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Mood Intensity");
        for (int i = 0; i < entries.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, entries.get(i).getMood().getIntensity()));
        }
        chart.getData().add(series);
    }

    private void updateVisuals(List<JournalEntry> entries) {
        if (entries.isEmpty()) return;
        double avg = new MoodAnalytics().calculateAverage(entries);
        String color1 = (avg >= 4.0) ? "#f1f8e9" : (avg >= 2.5) ? "#ffffff" : "#e3f2fd";
        String color2 = (avg >= 4.0) ? "#dcedc8" : (avg >= 2.5) ? "#f5f5f5" : "#bbdefb";
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + color1 + ", " + color2 + ");");
    }
}