package ca.yrhythm.app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JournalUI {
    private static final Logger LOGGER = Logger.getLogger(JournalUI.class.getName());

    private final JournalService service;
    private final TableView<JournalEntry> table = new TableView<>();
    private final LineChart<Number, Number> chart;
    private final Label riskLabel = new Label("Wellness Status: Calculating...");
    private final BorderPane root = new BorderPane();
    private final TextArea contentArea = new TextArea();
    private final String username;
    private final Stage stage;

    public JournalUI(Stage stage, JournalService service, String username) {
        this.stage = stage;
        this.service = service;
        this.username = username;

        // --- 1. HEADER ---
        DatePicker picker = new DatePicker(LocalDate.now());
        Button syncBtn = new Button("☁ Sync Cloud");
        syncBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-background-radius: 20;");

        HBox header = new HBox(15, new Label("History:"), picker, syncBtn);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-border-color: #a5d6a7; -fx-border-width: 0 0 2 0;");

        // --- 2. CENTER ---
        setupTable();
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 5, 1);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Mood Trends");
        chart.setMinHeight(300);

        VBox centerContent = new VBox(20, header, table, chart);
        centerContent.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(chart, Priority.ALWAYS);

        // --- 3. RIGHT PANEL ---
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(420);

        ComboBox<Mood> moodBox = new ComboBox<>(FXCollections.observableArrayList(Mood.values()));
        moodBox.setPromptText("How are you feeling?");
        moodBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> langPicker = new ComboBox<>(FXCollections.observableArrayList("English", "French", "Spanish", "Farsi", "Chinese"));
        langPicker.setValue("English");
        langPicker.setMaxWidth(Double.MAX_VALUE);

        Button recordBtn = new Button("🎙 Record Thought");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setStyle("-fx-background-color: #673ab7; -fx-text-fill: white; -fx-background-radius: 20;");
        recordBtn.setOnAction(e -> handleSpeech(recordBtn, langPicker));

        contentArea.setPromptText("Share your thoughts...");
        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-padding: 15px;");
        contentArea.setPrefHeight(300);

        Button saveBtn = new Button("Save Entry");
        saveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave(moodBox));

        VBox toolBox = createToolBox();

        rightPanel.getChildren().addAll(new Label("New Entry"), moodBox, new Label("Language"), langPicker, recordBtn, contentArea, saveBtn, toolBox);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Journal Dashboard", centerContent),
                new Tab("Crisis Dashboard", WellnessMapUI.getMapNode())
        );
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        root.setCenter(tabPane);
        root.setRight(rightPanel);
        root.setBottom(riskLabel);

        refresh();
    }

    private VBox createToolBox() {
        Button promptBtn = new Button("💡 Random Prompt");
        promptBtn.setMaxWidth(Double.MAX_VALUE);
        promptBtn.setStyle("-fx-background-radius: 20;");
        promptBtn.setOnAction(e -> contentArea.setText(PromptService.getRandomPrompt()));

        Button docReportBtn = new Button("📄 Doctor's Report (PDF)");
        docReportBtn.setMaxWidth(Double.MAX_VALUE);
        docReportBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-background-radius: 20;");
        docReportBtn.setOnAction(e -> handleExportPDF());

        Button exportBtn = new Button("📥 Export to CSV");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setStyle("-fx-background-radius: 20;");
        exportBtn.setOnAction(e -> handleExportCSV());

        Button deleteBtn = new Button("🗑 Delete Selected");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle("-fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 20; -fx-background-radius: 20;");
        deleteBtn.setOnAction(e -> handleDelete());

        Button helpBtn = new Button("🆘 Crisis Resources");
        helpBtn.setMaxWidth(Double.MAX_VALUE);
        helpBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        helpBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, service.getCrisisResources()).show());

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white; -fx-background-radius: 20;");
        logoutBtn.setOnAction(e -> {
            SpeechService.stopListening();
            new LoginUI(stage);
        });

        VBox toolBox = new VBox(10, new Label("Clinical & Data Tools"), promptBtn, docReportBtn, exportBtn, deleteBtn, helpBtn, new Separator(), logoutBtn);
        toolBox.setPadding(new Insets(10));
        toolBox.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 10;");
        return toolBox;
    }

    public Node getContentNode() {
        return this.root;
    }

    private void setupTable() {
        TableColumn<JournalEntry, LocalDate> dCol = new TableColumn<>("Date");
        dCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<JournalEntry, String> mCol = new TableColumn<>("Mood");
        mCol.setCellValueFactory(new PropertyValueFactory<>("moodDisplayName"));

        TableColumn<JournalEntry, String> cCol = new TableColumn<>("Notes Summary");
        cCol.setCellValueFactory(new PropertyValueFactory<>("content"));

        // This ensures the generic types are handled correctly for the column list
        table.getColumns().setAll(List.of(dCol, mCol, cCol));

        // Use this policy for compatibility with JavaFX 11, 17, and 21+
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void handleDelete() {
        JournalEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this entry permanently?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.deleteEntry(selected.getId());
                    refresh();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Deletion failed", e);
                }
            }
        });
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
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Save failed", ex);
        }
    }

    private void handleSpeech(Button btn, ComboBox<String> lang) {
        btn.setText("Initializing...");
        btn.setDisable(true);
        new Thread(() -> {
            try {
                SpeechService.setLanguage(lang.getValue());
                Platform.runLater(() -> {
                    btn.setText("Listening...");
                    SpeechService.startListening(
                            text -> Platform.runLater(() -> {
                                contentArea.appendText(text + " ");
                                btn.setText("🎙 Record Thought");
                                btn.setDisable(false);
                            }),
                            error -> Platform.runLater(() -> {
                                new Alert(Alert.AlertType.ERROR, "Speech Error: " + error).show();
                                btn.setText("🎙 Record Thought");
                                btn.setDisable(false);
                            })
                    );
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Speech setup failed", ex);
                Platform.runLater(() -> {
                    btn.setText("🎙 Record Thought");
                    btn.setDisable(false);
                });
            }
        }).start();
    }

    private void handleExportCSV() {
        try {
            CSVExporter.export(service.getAllEntries(), username + "_data.csv");
            new Alert(Alert.AlertType.INFORMATION, "Data exported successfully.").show();
        } catch (Exception e) { LOGGER.log(Level.WARNING, "CSV export failed", e); }
    }

    private void handleExportPDF() {
        try {
            String fileName = username + "_Clinical_Report.pdf";
            ReportService.generateDoctorReport(service.getAllEntries(), fileName);
            new Alert(Alert.AlertType.INFORMATION, "PDF Created: " + fileName).show();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "PDF report failed", e);
        }
    }

    private void refresh() {
        try {
            List<JournalEntry> entries = service.getAllEntries();
            table.setItems(FXCollections.observableArrayList(entries));
            int score = service.getCurrentRiskScore();
            riskLabel.setText("Health Rhythm: " + service.getRiskTier() + " (" + score + "/100)");
            if (score >= 100) Platform.runLater(this::triggerEmergencyHandshake);
            updateChart(entries);
            updateVisuals(entries);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Refresh failed", e);
        }
    }

    private void triggerEmergencyHandshake() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Y-Rhythm | Safety Protocol");
        alert.setHeaderText("Critical Mood Rhythm (100%) Detected.");
        alert.setContentText("Would you like to notify your trusted contact for a check-in?");
        ButtonType notify = new ButtonType("Notify Contact");
        ButtonType cope = new ButtonType("Call 310-COPE");
        ButtonType cancel = new ButtonType("I'm Safe", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(notify, cope, cancel);
        alert.showAndWait().ifPresent(type -> {
            if (type == notify) System.out.println("Handshake: Contact Notified.");
            if (type == cope) System.out.println("Handshake: Directing to York Region Crisis Services.");
        });
    }

    private void updateChart(List<JournalEntry> entries) {
        chart.getData().clear();
        if (entries.isEmpty()) return;
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Intensity");
        for (int i = 0; i < entries.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, entries.get(i).getMood().getIntensity()));
        }
        chart.getData().add(series);
    }

    private void updateVisuals(List<JournalEntry> entries) {
        if (entries.isEmpty()) return;
        double avg = new MoodAnalytics().calculateAverage(entries);
        String color = (avg >= 4.0) ? "#f1f8e9" : (avg >= 2.5) ? "#ffffff" : "#e3f2fd";
        root.setStyle("-fx-background-color: " + color + ";");
    }
}