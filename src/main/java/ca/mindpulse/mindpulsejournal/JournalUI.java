package ca.mindpulse.mindpulsejournal;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

        // --- 1. HEADER ---
        DatePicker picker = new DatePicker(LocalDate.now());
        Button syncBtn = new Button("☁ Sync Cloud");
        syncBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-background-radius: 15;");
        syncBtn.setOnAction(e -> SyncService.syncToCloud(stage, username));

        HBox header = new HBox(15, new Label("History:"), picker, syncBtn);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-border-color: #a5d6a7; -fx-border-width: 0 0 2 0;");

        // --- 2. CENTER (Dashboard & Graph Fix) ---
        setupTable();
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 5, 1);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Mood Trends");
        chart.setMinHeight(300); // Prevents the graph from being squeezed

        VBox centerContent = new VBox(20, header, table, chart);
        centerContent.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(chart, Priority.ALWAYS);

        // --- 3. RIGHT PANEL (Writing & Tools) ---
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(420);

        ComboBox<Mood> moodBox = new ComboBox<>(FXCollections.observableArrayList(Mood.values()));
        moodBox.setPromptText("How are you feeling?");
        moodBox.setMaxWidth(Double.MAX_VALUE);

        contentArea.setPromptText("Share your thoughts...");
        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-line-spacing: 6px; -fx-padding: 15px;");
        contentArea.setPrefHeight(350);

        moodBox.getSelectionModel().selectedItemProperty().addListener((obs, oldMood, newMood) -> {
            if (newMood != null && contentArea.getText().trim().isEmpty()) {
                contentArea.setText(PromptService.getPromptByMood(newMood) + "\n\n");
            }
        });

        Button saveBtn = new Button("Save Entry");
        saveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave(moodBox));

        // TOOLS SECTION (Now with Delete restored)
        Button promptBtn = new Button("💡 Random Prompt");
        promptBtn.setMaxWidth(Double.MAX_VALUE);
        promptBtn.setOnAction(e -> contentArea.setText(PromptService.getRandomPrompt()));

        Button exportBtn = new Button("📥 Export to CSV");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setOnAction(e -> handleExport());

        Button deleteBtn = new Button("🗑 Delete Selected");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle("-fx-text-fill: #d32f2f; -fx-border-color: #d32f2f;");
        deleteBtn.setOnAction(e -> handleDelete());

        Button helpBtn = new Button("🆘 Get Help / Crisis Resources");
        helpBtn.setMaxWidth(Double.MAX_VALUE);
        helpBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        helpBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, service.getCrisisResources()).show());

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> new LoginUI(stage));

        VBox toolBox = new VBox(10, new Label("Journal Tools"), promptBtn, exportBtn, deleteBtn, helpBtn, new Separator(), logoutBtn);
        toolBox.setPadding(new Insets(10));
        toolBox.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 10;");

        rightPanel.getChildren().addAll(new Label("New Entry"), moodBox, contentArea, saveBtn, toolBox);

        // --- 4. LAYOUT ASSEMBLY ---
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(new Tab("Dashboard", centerContent), new Tab("Wellness Map", WellnessMapUI.getMapNode()));
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        root.setCenter(tabPane);
        root.setRight(rightPanel);
        root.setBottom(riskLabel);
        BorderPane.setMargin(riskLabel, new Insets(10));

        stage.setScene(new Scene(root, 1300, 900));
        refresh();
    }

    private void handleExport() {
        try {
            CSVExporter.export(service.getAllEntries(), username + "_journal_export.csv");
            new Alert(Alert.AlertType.INFORMATION, "Export Successful!").show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDelete() {
        JournalEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.deleteEntry(selected.getId());
                refresh();
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select an entry from the table to delete.").show();
        }
    }

    private void setupTable() {
        TableColumn<JournalEntry, LocalDate> dCol = new TableColumn<>("Date");
        dCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<JournalEntry, Mood> mCol = new TableColumn<>("Mood");
        mCol.setCellValueFactory(new PropertyValueFactory<>("mood"));
        TableColumn<JournalEntry, String> cCol = new TableColumn<>("Notes");
        cCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        table.getColumns().addAll(dCol, mCol, cCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void handleSave(ComboBox<Mood> moodBox) {
        try {
            Mood mood = moodBox.getValue();
            String content = contentArea.getText();
            if (mood == null || content.trim().isEmpty()) return;

            service.saveEntry(new JournalEntry((int)(System.currentTimeMillis()/1000), LocalDate.now(), mood, content));
            contentArea.clear();
            moodBox.getSelectionModel().clearSelection();
            refresh();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void refresh() {
        try {
            List<JournalEntry> entries = service.getAllEntries();
            table.setItems(FXCollections.observableArrayList(entries));
            riskLabel.setText("Health Rhythm: " + service.getRiskTier() + " (" + service.getCurrentRiskScore() + "/100)");
            updateChart(entries);
            updateVisuals(entries);
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