package ca.yrhythm.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * MainDashboardUI — 6-tab sidebar layout.
 *
 * Nav items (down from 11):
 *   Home · Wellness Tools · Journal · Get Help · Profile · Settings
 *   + Logout (bottom)
 *
 * Merges applied:
 *   - Breathe + Calming Station  →  Wellness Tools  (tabbed inside)
 *   - Crisis Dashboard + 311     →  Get Help        (tabbed inside)
 *   - Sleep Tracker              →  Journal         (tab inside Journal)
 *   - Donate                     →  Settings        (section at bottom)
 *
 * Settings: all changes are live — no save button required.
 */
public class MainDashboardUI {

    // ── State ─────────────────────────────────────────────────────────────────
    private final JournalService journalService;
    private final String         username;
    private final Stage          stage;
    private final BorderPane     root        = new BorderPane();
    private final StackPane      contentArea = new StackPane();
    private final VBox           sidebar     = new VBox(8);

    // Settings state — all fields watched live by createSettingsView()
    private boolean darkMode             = false;
    private boolean offlineReminders     = true;
    private int     reminderHour         = 20;
    private String  breathingPattern     = "Box (4-4-4-4)";
    private int     globalFontSize       = 15;
    private String  preferredQuoteCategory = "Resilience";
    private int     autoLockMinutes      = 10;
    private String  modelPath            = "models/model-en";

    // Nav buttons — 6 items + logout
    private final Label  appTitle   = new Label("Y-RHYTHM");
    private final Button btnHome     = new Button("🏠  Home");
    private final Button btnWellness = new Button("🌿  Wellness Tools");
    private final Button btnJournal  = new Button("📓  Journal");
    private final Button btnHelp     = new Button("🆘  Get Help");
    private final Button btnProfile  = new Button("👤  My Profile");
    private final Button btnSettings = new Button("⚙  Settings");
    private final Button btnLogout   = new Button("🚪  Log Out");

    private Thread reminderThread;

    // ── Constructor ───────────────────────────────────────────────────────────

    public MainDashboardUI(Stage stage, JournalService service, String username) {
        this.stage          = stage;
        this.journalService = service;
        this.username       = username;

        applyTheme();
        syncSettingsToServices();
        startReminderService();
        buildSidebar();
        setupNavigation();

        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().setAll(createHomeView());

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        stage.setScene(new Scene(root, 1280, 850));
        stage.setTitle("Y-Rhythm | Mental Wellness");
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private void buildSidebar() {
        sidebar.setPadding(new Insets(30, 18, 30, 18));
        sidebar.setPrefWidth(230);

        appTitle.setStyle("-fx-text-fill: #a5d6a7; -fx-font-weight: bold; " +
                "-fx-font-size: 22px; -fx-padding: 0 0 18 0;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                appTitle,
                btnHome,
                btnWellness,
                btnJournal,
                btnHelp,
                btnProfile,
                btnSettings,
                spacer,
                btnLogout
        );

        for (Node n : sidebar.getChildren()) {
            if (n instanceof Button b) styleNavButton(b, b == btnLogout);
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void setupNavigation() {
        btnHome.setOnAction(e -> {
            BreatheUI.stopAnimation();
            contentArea.getChildren().setAll(createHomeView());
        });

        // Wellness Tools = Breathe + Calming Station, shown as inner tabs
        btnWellness.setOnAction(e -> {
            BreatheUI.stopAnimation();
            contentArea.getChildren().setAll(createWellnessView());
        });

        // Journal = Journal entries + Sleep Tracker, shown as inner tabs
        btnJournal.setOnAction(e -> {
            BreatheUI.stopAnimation();
            contentArea.getChildren().setAll(createJournalView());
        });

        // Get Help = Crisis Dashboard + 311, shown as inner tabs
        btnHelp.setOnAction(e -> {
            BreatheUI.stopAnimation();
            contentArea.getChildren().setAll(createGetHelpView());
        });

        btnProfile.setOnAction(e -> {
            BreatheUI.stopAnimation();
            contentArea.getChildren().setAll(ProfileUI.getProfileNode(username, darkMode));
        });

        // Settings = all preferences + Donate section at bottom
        btnSettings.setOnAction(e -> {
            BreatheUI.stopAnimation();
            contentArea.getChildren().setAll(createSettingsView());
        });

        btnLogout.setOnAction(e -> {
            if (reminderThread != null) reminderThread.interrupt();
            BreatheUI.stopAnimation();
            SpeechService.stopListening();
            new LoginUI(stage);
        });
    }

    // ── Merged views ──────────────────────────────────────────────────────────

    /** Wellness Tools: Breathe tab + Calming Station tab. */
    private Node createWellnessView() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: transparent;");

        Tab breatheTab = new Tab("🌬  Breathe", BreatheUI.getBreatheNode());
        Tab calmTab    = new Tab("🧘  Calming Station", CalmingStationUI.getCalmingNode(darkMode));

        tabs.getTabs().addAll(breatheTab, calmTab);
        return tabs;
    }

    /** Journal: Journal entries tab + Sleep Tracker tab. */
    private Node createJournalView() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        JournalUI journal = new JournalUI(stage, journalService, username, darkMode, globalFontSize);
        Tab journalTab = new Tab("📓  Journal", journal.getContentNode());
        Tab sleepTab   = new Tab("🌙  Sleep Tracker", SleepTrackerUI.getSleepNode(username, darkMode));

        tabs.getTabs().addAll(journalTab, sleepTab);
        return tabs;
    }

    /** Get Help: Crisis Dashboard tab + Connect to 311 tab. */
    private Node createGetHelpView() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab crisisTab = new Tab("🆘  Crisis Dashboard", WellnessMapUI.getMapNode());
        Tab tab311    = new Tab("📞  Connect to 311",   Connect311UI.get311Node(darkMode));

        tabs.getTabs().addAll(crisisTab, tab311);
        return tabs;
    }

    // ── Home view ─────────────────────────────────────────────────────────────

    private VBox createHomeView() {
        VBox home = new VBox(25);
        home.setAlignment(Pos.CENTER);

        String titleColor = darkMode ? "#a5d6a7" : "#2c3e50";
        String quoteBg    = darkMode ? "#0a240a" : "white";
        String quoteText  = darkMode ? "#e8f5e9" : "#7f8c8d";

        Label welcome = new Label("Quote of the Day");
        welcome.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");

        Label quoteLabel = new Label("\"" + QuoteService.getRandomQuote() + "\"");
        quoteLabel.setWrapText(true);
        quoteLabel.setMaxWidth(600);
        quoteLabel.setStyle(
                "-fx-font-size: 18px; -fx-font-style: italic; -fx-text-fill: " + quoteText + "; " +
                        "-fx-padding: 40; -fx-background-color: " + quoteBg + "; -fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        home.getChildren().addAll(welcome, quoteLabel);
        return home;
    }

    // ── Settings view (live — all controls update state immediately) ──────────

    private Node createSettingsView() {
        String bg   = darkMode ? "#0a1a0a" : "white";
        String text = darkMode ? "white"   : "#2c3e50";

        VBox settings = new VBox(20);
        settings.setPadding(new Insets(30));
        settings.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 15;");
        settings.setMaxWidth(860);

        Label title = new Label("⚙  Settings");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // ── Appearance ────────────────────────────────────────────────────────
        settings.getChildren().addAll(title, new Separator(), sectionLabel("Appearance", text));

        CheckBox darkToggle = new CheckBox("Dark Mode");
        darkToggle.setSelected(darkMode);
        darkToggle.setStyle("-fx-text-fill: " + text + ";");
        darkToggle.setOnAction(e -> {
            darkMode = darkToggle.isSelected();
            applyTheme();
            // Rebuild settings live so colours update immediately
            contentArea.getChildren().setAll(createSettingsView());
        });
        settings.getChildren().add(darkToggle);

        // Font size
        HBox fontRow = labeledRow("Journal Font Size:", text);
        Spinner<Integer> fontSpinner = new Spinner<>(11, 24, globalFontSize);
        fontSpinner.setEditable(true);
        fontSpinner.setPrefWidth(90);
        fontSpinner.valueProperty().addListener((obs, o, n) -> globalFontSize = n);
        fontRow.getChildren().add(fontSpinner);
        settings.getChildren().add(fontRow);

        // ── Reminders ─────────────────────────────────────────────────────────
        settings.getChildren().addAll(new Separator(), sectionLabel("Reminders", text));

        CheckBox reminderToggle = new CheckBox("Daily Journal Reminder");
        reminderToggle.setSelected(offlineReminders);
        reminderToggle.setStyle("-fx-text-fill: " + text + ";");
        reminderToggle.setOnAction(e -> offlineReminders = reminderToggle.isSelected());
        settings.getChildren().add(reminderToggle);

        HBox hourRow = labeledRow("Reminder Hour (24h):", text);
        ComboBox<Integer> hourPicker = new ComboBox<>();
        for (int i = 0; i < 24; i++) hourPicker.getItems().add(i);
        hourPicker.setValue(reminderHour);
        hourPicker.setOnAction(e -> reminderHour = hourPicker.getValue());
        hourRow.getChildren().add(hourPicker);
        settings.getChildren().add(hourRow);

        // ── Wellness ──────────────────────────────────────────────────────────
        settings.getChildren().addAll(new Separator(), sectionLabel("Wellness Tools", text));

        HBox breathRow = labeledRow("Breathing Style:", text);
        ComboBox<String> breathBox = new ComboBox<>();
        breathBox.getItems().addAll("Box (4-4-4-4)", "4-7-8 Relax", "Equal (5-5)");
        breathBox.setValue(breathingPattern);
        breathBox.setOnAction(e -> {
            breathingPattern = breathBox.getValue();
            syncSettingsToServices();
        });
        breathRow.getChildren().add(breathBox);
        settings.getChildren().add(breathRow);

        HBox quoteRow = labeledRow("Quote Theme:", text);
        ComboBox<String> quoteBox = new ComboBox<>();
        quoteBox.getItems().addAll("Resilience", "Peace", "Motivation", "Clinical Tips");
        quoteBox.setValue(preferredQuoteCategory);
        quoteBox.setOnAction(e -> {
            preferredQuoteCategory = quoteBox.getValue();
            syncSettingsToServices();
        });
        quoteRow.getChildren().add(quoteBox);
        settings.getChildren().add(quoteRow);

        // ── Security ──────────────────────────────────────────────────────────
        settings.getChildren().addAll(new Separator(), sectionLabel("Security", text));

        HBox lockRow = labeledRow("Auto-Lock (minutes):", text);
        Spinner<Integer> lockSpinner = new Spinner<>(1, 60, autoLockMinutes);
        lockSpinner.setEditable(true);
        lockSpinner.setPrefWidth(90);
        lockSpinner.valueProperty().addListener((obs, o, n) -> autoLockMinutes = n);
        lockRow.getChildren().add(lockSpinner);
        settings.getChildren().add(lockRow);

        // ── Speech model ──────────────────────────────────────────────────────
        settings.getChildren().addAll(new Separator(), sectionLabel("Speech-to-Text Model Path", text));

        TextField pathField = new TextField(modelPath);
        pathField.setStyle("-fx-background-radius: 10;" +
                (darkMode ? "-fx-background-color:#1a2e1a; -fx-text-fill:white;" : ""));
        pathField.setMaxWidth(500);
        // Live update on every keystroke
        pathField.textProperty().addListener((obs, o, n) -> {
            modelPath = n;
            syncSettingsToServices();
        });
        settings.getChildren().add(pathField);

        // ── Cache ─────────────────────────────────────────────────────────────
        settings.getChildren().addAll(new Separator(), sectionLabel("Data", text));

        Button clearCacheBtn = new Button("Clear Local Cache");
        clearCacheBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; " +
                "-fx-background-radius: 15; -fx-padding: 8 20; -fx-cursor: hand;");
        clearCacheBtn.setOnAction(e -> {
            File cache = new File("cache.tmp");
            if (cache.exists()) cache.delete();
            new Alert(Alert.AlertType.INFORMATION, "Cache cleared.").show();
        });
        settings.getChildren().add(clearCacheBtn);

        // ── Donate (merged from DonationUI nav tab) ───────────────────────────
        settings.getChildren().addAll(new Separator(), sectionLabel("Support Y-Rhythm 💚", text));
        settings.getChildren().add(DonationUI.getDonationNode(darkMode));

        ScrollPane scroll = new ScrollPane(settings);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    // ── Reminder service ──────────────────────────────────────────────────────

    private void startReminderService() {
        reminderThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (offlineReminders) {
                        LocalTime now = LocalTime.now();
                        // Use a 1-minute window to avoid missing the trigger
                        if (now.getHour() == reminderHour && now.getMinute() == 0) {
                            boolean already = journalService.hasEntryForDate(username, LocalDate.now());
                            if (!already) Platform.runLater(this::showGentleNotification);
                        }
                    }
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Reminder error: " + e.getMessage());
                }
            }
        });
        reminderThread.setDaemon(true);
        reminderThread.start();
    }

    private void showGentleNotification() {
        Alert nudge = new Alert(Alert.AlertType.INFORMATION);
        nudge.setTitle("Daily Reflection");
        nudge.setHeaderText("Time for a quick check-in?");
        nudge.setContentText("A quick reflection can help clear your mind. Care to write a few words?");
        if (darkMode) {
            nudge.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #2e7d32;");
            nudge.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
        }
        nudge.show();
    }

    // ── Theme & services ──────────────────────────────────────────────────────

    private void applyTheme() {
        if (darkMode) {
            root.setStyle("-fx-background-color: #051405;");
            sidebar.setStyle("-fx-background-color: #0a240a; " +
                    "-fx-border-color: #1b4d1b; -fx-border-width: 0 1 0 0;");
        } else {
            root.setStyle("-fx-background-color: #f1f8e9;");
            sidebar.setStyle("-fx-background-color: #2e7d32;");
        }
    }

    private void syncSettingsToServices() {
        SpeechService.setModelPath(modelPath);
        QuoteService.setCategory(preferredQuoteCategory);
        BreatheUI.setPattern(breathingPattern);
    }

    // ── Styling helpers ───────────────────────────────────────────────────────

    private void styleNavButton(Button btn, boolean isLogout) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 20, 12, 16));
        if (isLogout) {
            btn.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8;");
        } else {
            String textCol  = darkMode ? "#a5d6a7" : "#ecf0f1";
            String hoverCol = darkMode ? "#1b4d1b" : "#388e3c";
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textCol +
                    "; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 8;");
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: " + hoverCol + "; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-background-radius: 8;"));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + textCol +
                            "; -fx-font-size: 14px; -fx-background-radius: 8;"));
        }
    }

    /** Small bold section header inside the settings view. */
    private Label sectionLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return l;
    }

    /** A labelled HBox row for settings controls. */
    private HBox labeledRow(String labelText, String color) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(labelText);
        l.setMinWidth(200);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
        row.getChildren().add(l);
        return row;
    }
}