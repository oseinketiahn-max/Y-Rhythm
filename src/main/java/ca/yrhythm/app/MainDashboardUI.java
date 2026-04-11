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

    public MainDashboardUI(Stage stage, JournalService service, String username) {
        this.journalService = service;
        this.username = username;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f7f6;");

        // --- 1. SIDEBAR NAVIGATION ---
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setStyle("-fx-background-color: #2c3e50; -fx-pref-width: 220;");

        Label appTitle = new Label("Y-RHYTHM");
        appTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        Button btnHome = createNavButton("🏠 Dashboard");
        Button btnMap = createNavButton("📍 Wellness Map");
        Button btnBreathe = createNavButton("🧘 Grounding");
        Button btnJournal = createNavButton("✍️ Journal");

        sidebar.getChildren().addAll(appTitle, new Separator(), btnHome, btnMap, btnBreathe, btnJournal);

        // --- 2. DYNAMIC CONTENT AREA ---
        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));

        // Navigation Actions
        btnHome.setOnAction(e -> contentArea.getChildren().setAll(createHomeView()));

        // Map and Breathe nodes (assuming static access works as before)
        btnMap.setOnAction(e -> contentArea.getChildren().setAll(WellnessMapUI.getMapNode()));
        btnBreathe.setOnAction(e -> contentArea.getChildren().setAll(BreatheUI.getBreatheNode()));

        // INTEGRATED JOURNAL LOGIC
        btnJournal.setOnAction(e -> {
            // Creates the UI and injects its root node into the dashboard center
            JournalUI journal = new JournalUI(stage, journalService, username);
            contentArea.getChildren().setAll(journal.getContentNode());
        });

        // Default view
        contentArea.getChildren().setAll(createHomeView());

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Y-Rhythm Dashboard | " + username);
        stage.centerOnScreen();
    }

    private VBox createHomeView() {
        VBox home = new VBox(25);
        home.setAlignment(Pos.CENTER);

        Label welcome = new Label("Welcome back, " + username);
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        // 3. INTEGRATING MOTIVATIONAL QUOTES
        Label quoteLabel = new Label("\"" + QuoteService.getRandomQuote() + "\"");
        quoteLabel.setWrapText(true);
        quoteLabel.setMaxWidth(600);
        quoteLabel.setAlignment(Pos.CENTER);
        quoteLabel.setStyle("-fx-font-size: 18px; -fx-font-style: italic; -fx-text-fill: #7f8c8d; " +
                "-fx-padding: 30; -fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        home.getChildren().addAll(welcome, quoteLabel);
        return home;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-cursor: hand;");

        // Hover effects
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px;"));

        return btn;
    }
}