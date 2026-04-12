package ca.yrhythm.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainDashboardUI {

    private final JournalService journalService;
    private final String username;
    private final Stage stage;
    private final BorderPane root = new BorderPane();
    private final StackPane contentArea = new StackPane();
    private final VBox sidebar = new VBox(15);

    // --- APPLICATION STATE (Reflected across all pages) ---
    private String currentLanguage = "English";
    private double sentimentSensitivity = 0.5;
    private boolean darkMode = false;
    private String emergencyContact = "Not Set";

    // --- UI REFERENCES ---
    private final Label appTitle = new Label("Y-RHYTHM");
    private final Button btnHome = new Button();      // Quote of the Day
    private final Button btnBreathe = new Button();   // Grounding
    private final Button btnJournal = new Button();   // Journal
    private final Button btnCrisis = new Button();    // Crisis Dashboard
    private final Button btnSettings = new Button();
    private final Button btnLogout = new Button();

    public MainDashboardUI(Stage stage, JournalService service, String username) {
        this.stage = stage;
        this.journalService = service;
        this.username = username;

        applyTheme();

        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setPrefWidth(250);
        appTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-padding: 0 0 20 0;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(appTitle, btnHome, btnBreathe, btnJournal, btnCrisis, btnSettings, spacer, btnLogout);

        setupNavigation();
        refreshFullAppTranslation();

        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().setAll(createHomeView()); // Initial View

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1280, 850);
        stage.setScene(scene);
    }

    private void setupNavigation() {
        btnHome.setOnAction(e -> contentArea.getChildren().setAll(createHomeView()));
        btnBreathe.setOnAction(e -> contentArea.getChildren().setAll(BreatheUI.getBreatheNode()));
        btnJournal.setOnAction(e -> {
            JournalUI journal = new JournalUI(stage, journalService, username, darkMode);
            contentArea.getChildren().setAll(journal.getContentNode());
        });
        btnCrisis.setOnAction(e -> contentArea.getChildren().setAll(WellnessMapUI.getMapNode()));
        btnSettings.setOnAction(e -> contentArea.getChildren().setAll(createSettingsView()));

        btnLogout.setOnAction(e -> {
            SpeechService.stopListening();
            new LoginUI(stage);
        });

        for (javafx.scene.Node n : sidebar.getChildren()) {
            if (n instanceof Button b) styleNavButton(b, b == btnLogout);
        }
    }

    private void refreshFullAppTranslation() {
        btnHome.setText(getLocaleString("nav.home"));
        btnBreathe.setText(getLocaleString("nav.breathe"));
        btnJournal.setText(getLocaleString("nav.journal"));
        btnCrisis.setText(getLocaleString("nav.crisis"));
        btnSettings.setText(getLocaleString("nav.settings"));
        btnLogout.setText(getLocaleString("nav.logout"));
    }

    private String getLocaleString(String key) {
        return switch (currentLanguage) {
            case "French" -> switch (key) {
                case "nav.home" -> "Citation du Jour";
                case "nav.breathe" -> "Ancrage";
                case "nav.journal" -> "Journal";
                case "nav.crisis" -> "Tableau de Crise";
                case "nav.settings" -> "Paramètres";
                case "nav.logout" -> "Déconnexion";
                default -> key;
            };
            case "Spanish" -> switch (key) {
                case "nav.home" -> "Cita del Día";
                case "nav.breathe" -> "Conexión";
                case "nav.journal" -> "Diario";
                case "nav.crisis" -> "Panel de Crisis";
                case "nav.settings" -> "Ajustes";
                case "nav.logout" -> "Cerrar Sesión";
                default -> key;
            };
            default -> switch (key) {
                case "nav.home" -> "Quote of the Day";
                case "nav.breathe" -> "Grounding";
                case "nav.journal" -> "Journal";
                case "nav.crisis" -> "Crisis Dashboard";
                case "nav.settings" -> "Settings";
                case "nav.logout" -> "Log Out";
                default -> key;
            };
        };
    }

    private void applyTheme() {
        if (darkMode) {
            root.setStyle("-fx-background-color: #121212;");
            sidebar.setStyle("-fx-background-color: #000000; -fx-border-color: #333; -fx-border-width: 0 1 0 0;");
        } else {
            root.setStyle("-fx-background-color: #f4f7f6;");
            sidebar.setStyle("-fx-background-color: #2c3e50;");
        }
    }

    private VBox createHomeView() {
        VBox home = new VBox(25);
        home.setAlignment(Pos.CENTER);
        Label welcome = new Label(getLocaleString("nav.home"));
        welcome.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "white" : "#2c3e50") + ";");
        Label quoteLabel = new Label("\"" + QuoteService.getRandomQuote() + "\"");
        quoteLabel.setWrapText(true);
        quoteLabel.setMaxWidth(600);
        quoteLabel.setStyle("-fx-font-size: 18px; -fx-font-style: italic; -fx-text-fill: #7f8c8d; -fx-padding: 40; " +
                "-fx-background-color: " + (darkMode ? "#1e1e1e" : "white") + "; -fx-background-radius: 20;");
        home.getChildren().addAll(welcome, quoteLabel);
        return home;
    }

    private VBox createSettingsView() {
        VBox settings = new VBox(25);
        settings.setPadding(new Insets(40));
        settings.setStyle("-fx-background-color: " + (darkMode ? "#1e1e1e" : "white") + "; -fx-background-radius: 15;");
        settings.setMaxWidth(800);

        Label title = new Label(getLocaleString("nav.settings"));
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? "#ecf0f1" : "#2c3e50") + ";");

        // Language Choice
        ComboBox<String> langPicker = new ComboBox<>();
        langPicker.getItems().addAll("English", "French", "Spanish");
        langPicker.setValue(currentLanguage);
        langPicker.setOnAction(e -> {
            currentLanguage = langPicker.getValue();
            refreshFullAppTranslation();
            contentArea.getChildren().setAll(createSettingsView());
        });

        // Dark Mode Toggle
        CheckBox darkToggle = new CheckBox("Enable Dark Mode");
        darkToggle.setSelected(darkMode);
        darkToggle.setStyle("-fx-text-fill: " + (darkMode ? "white" : "black") + ";");
        darkToggle.setOnAction(e -> {
            darkMode = darkToggle.isSelected();
            applyTheme();
            contentArea.getChildren().setAll(createSettingsView());
        });

        settings.getChildren().addAll(title, new Label("Language"), langPicker, darkToggle);
        return settings;
    }

    private void styleNavButton(Button btn, boolean isLogout) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 20, 12, 20));
        if (isLogout) {
            btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 15px; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 15px;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 15px;"));
        }
    }
}