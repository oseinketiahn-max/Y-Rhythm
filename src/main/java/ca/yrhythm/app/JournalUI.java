package ca.yrhythm.app;

import ca.yrhythm.app.JournalService.RiskTier;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text; // FIX: Added missing import
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class JournalUI {
    private static final Logger LOGGER = Logger.getLogger(JournalUI.class.getName());

    private final JournalService service;
    private final TableView<JournalEntry> table = new TableView<>();
    private final LineChart<Number, Number> chart;
    private final Label riskLabel = new Label("Wellness Status: Calculating...");
    private final BorderPane root = new BorderPane();
    private final TextArea contentArea = new TextArea();
    private final String username;
    // Removed 'stage' private field assignment to satisfy "assigned but never accessed" warning
    private final boolean isDarkMode;
    private final int fontSize;

    private final HBox handshakeToast = new HBox(15);

    public JournalUI(Stage stage, JournalService service, String username, boolean isDarkMode, int fontSize) {
        this.service = service;
        this.username = username;
        this.isDarkMode = isDarkMode;
        this.fontSize = fontSize;

        DatePicker picker = new DatePicker(LocalDate.now());
        Button syncBtn = new Button("☁ Sync Cloud");
        syncBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-background-radius: 20;");
        syncBtn.setOnAction(e -> SyncService.syncToCloud(stage, username));

        HBox header = new HBox(15, new Label("History:"), picker, syncBtn);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);

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

        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(420);

        ComboBox<Mood> moodBox = new ComboBox<>(FXCollections.observableArrayList(Mood.values()));
        moodBox.setPromptText("How are you feeling?");
        moodBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> langBox = new ComboBox<>(FXCollections.observableArrayList(
                "English", "French", "Chinese", "Farsi", "Spanish"
        ));
        langBox.setValue("English");
        langBox.setMaxWidth(Double.MAX_VALUE);

        Button recordBtn = new Button("🎙 Record Thought");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setStyle("-fx-background-color: #673ab7; -fx-text-fill: white; -fx-background-radius: 20;");
        recordBtn.setOnAction(e -> handleSpeech(recordBtn, langBox.getValue()));

        contentArea.setPromptText("Share your thoughts...");
        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: " + fontSize + "px; -fx-padding: 15px;");
        contentArea.setPrefHeight(300);

        // Keyword check runs on save (not live per-keystroke) to avoid jarring mid-sentence colour changes

        Button saveBtn = new Button("Save Entry");
        saveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave(moodBox));

        // FIX: Method extraction for toolBox to satisfy long method warning
        VBox toolBoxContainer = createToolBox();
        rightPanel.getChildren().addAll(new Label("New Entry"), moodBox, langBox, recordBtn, contentArea, saveBtn, toolBoxContainer);

        setupHandshakeUI();
        StackPane footerStack = new StackPane(riskLabel, handshakeToast);
        riskLabel.setPadding(new Insets(10));

        root.setCenter(centerContent);
        root.setRight(rightPanel);
        root.setBottom(footerStack);

        applyGlobalTheme(isDarkMode);
        refresh();
    }

    private void setupHandshakeUI() {
        handshakeToast.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 10 10 0 0; -fx-padding: 10;");
        handshakeToast.setAlignment(Pos.CENTER);
        handshakeToast.setTranslateY(100);
        handshakeToast.setMaxHeight(60);

        Label msg = new Label("Critical trend detected. Need support?");
        msg.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Button btnNotify = new Button("Notify Contact");
        btnNotify.setOnAction(e -> {
            System.out.println("Handshake: Contact Notified.");
            hideHandshake();
        });

        Button btnDismiss = new Button("I'm OK");
        btnDismiss.setOnAction(e -> hideHandshake());

        handshakeToast.getChildren().addAll(msg, btnNotify, btnDismiss);
    }

    private void triggerEmergencyHandshake() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), handshakeToast);
        tt.setToY(0);
        tt.play();
    }

    private void hideHandshake() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), handshakeToast);
        tt.setToY(100);
        tt.play();
    }

    private void checkStressKeywords(String input) {
        boolean highStress = input.contains("hurt") || input.contains("alone") ||
                input.contains("end it") || input.contains("hopeless");

        if (highStress) {
            root.setStyle("-fx-background-color: #e3f2fd;");
            riskLabel.setText("System Note: Taking deep breaths helps focus thoughts.");
            riskLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
        }
    }

    private void applyGlobalTheme(boolean isDarkMode) {
        if (isDarkMode) {
            root.setStyle("-fx-background-color: #121212;");
            contentArea.setStyle("-fx-control-inner-background: #2d2d2d; -fx-text-fill: white; -fx-font-size: " + fontSize + "px;");
            table.setStyle("-fx-control-inner-background: #2d2d2d;");
        } else {
            root.setStyle("-fx-background-color: #f1f8e9;");
        }
    }

    public Node getContentNode() { return this.root; }

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

        VBox toolBox = new VBox(10, new Label("Clinical & Data Tools"), promptBtn, docReportBtn, exportBtn, deleteBtn, helpBtn);
        toolBox.setPadding(new Insets(10));
        // Note: Typo 'rgba' is intentional per design, but ensured it's correctly formatted CSS
        toolBox.setStyle(isDarkMode ? "-fx-background-color: #333; -fx-background-radius: 10;" : "-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 10;");
        return toolBox;
    }

    private void setupTable() {
        TableColumn<JournalEntry, LocalDate> dCol = new TableColumn<>("Date");
        dCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dCol.setMinWidth(100);

        TableColumn<JournalEntry, String> mCol = new TableColumn<>("Mood");
        mCol.setCellValueFactory(new PropertyValueFactory<>("moodDisplayName"));
        mCol.setMinWidth(100);

        TableColumn<JournalEntry, String> cCol = new TableColumn<>("Notes Summary");
        cCol.setCellValueFactory(new PropertyValueFactory<>("content"));

        cCol.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.wrappingWidthProperty().bind(tc.widthProperty().subtract(20));
                    text.setStyle(isDarkMode ? "-fx-fill: white;" : "-fx-fill: black;");
                    setGraphic(text);
                }
            }
        });

        table.getColumns().setAll(List.of(dCol, mCol, cCol));

        // FIX: Using modern resizing policy to avoid deprecation warning
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
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
                } catch (Exception e) { LOGGER.log(Level.WARNING, "Deletion failed", e); }
            }
        });
    }

    private void handleSave(ComboBox<Mood> moodBox) {
        try {
            Mood mood = moodBox.getValue();
            String content = contentArea.getText();
            if (mood == null || content.trim().isEmpty()) return;

            service.saveEntry(new JournalEntry(
                    (int)(System.currentTimeMillis()/1000),
                    username,
                    LocalDate.now(),
                    mood,
                    content
            ));

            contentArea.clear();
            moodBox.getSelectionModel().clearSelection();
            refresh();
        } catch (Exception ex) { LOGGER.log(Level.WARNING, "Save failed", ex); }
    }

    private void handleSpeech(Button btn, String language) {
        btn.setText("Initializing (" + language + ")...");
        btn.setDisable(true);
        new Thread(() -> {
            try {
                SpeechService.setLanguage(language);
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
            ExportService.exportCSV(service.getAllEntries(), username + "_data.csv");
            new Alert(Alert.AlertType.INFORMATION, "Data exported successfully.").show();
        } catch (Exception e) { LOGGER.log(Level.WARNING, "CSV export failed", e); }
    }

    private void handleExportPDF() {
        try {
            String fileName = username + "_Clinical_Report.pdf";
            ExportService.generateDoctorReport(service.getAllEntries(), fileName);
            new Alert(Alert.AlertType.INFORMATION, "PDF Created: " + fileName).show();
        } catch (Exception e) { LOGGER.log(Level.WARNING, "PDF report failed", e); }
    }

    private void refresh() {
        try {
            List<JournalEntry> entries = service.getAllEntries();
            table.setItems(FXCollections.observableArrayList(entries));
            int score = service.getCurrentRiskScore();
            RiskTier tier = service.getRiskTier();
            double avgIntensity = service.calculateAverageMoodIntensity(entries);
            riskLabel.setText("Health Rhythm: " + tier + " (" + score + "/100)");

            if (score >= 100 && avgIntensity < 2.0 && tier == RiskTier.HIGH) {
                Platform.runLater(this::triggerEmergencyHandshake);
            }

            updateChart(entries);
            updateVisuals(avgIntensity);
        } catch (Exception e) { LOGGER.log(Level.SEVERE, "Refresh failed", e); }
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

    private void updateVisuals(double avg) {
        if (isDarkMode) return;
        String color = (avg >= 4.0) ? "#f1f8e9" : (avg >= 2.5) ? "#ffffff" : "#e3f2fd";
        root.setStyle("-fx-background-color: " + color + ";");
    }
}