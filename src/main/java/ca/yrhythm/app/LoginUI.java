package ca.yrhythm.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginUI {

    public LoginUI(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        Label header = new Label("Y-Rhythm | Sign In");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);
        usernameField.setStyle("-fx-background-radius: 15; -fx-border-radius: 15;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);
        passwordField.setStyle("-fx-background-radius: 15; -fx-border-radius: 15;");

        Button loginBtn = new Button("Sign In");
        loginBtn.setPrefWidth(250);
        loginBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            char[] pass = passwordField.getText().toCharArray();
            if (!user.isEmpty() && pass.length > 0) {
                showDashboard(stage, user, pass);
            } else {
                new Alert(Alert.AlertType.WARNING, "Please enter your credentials.").show();
            }
        });

        Button createAccBtn = new Button("Create Account");
        createAccBtn.setPrefWidth(250);
        createAccBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2e7d32; -fx-border-color: #2e7d32; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        createAccBtn.setOnAction(e -> new RegisterUI(stage));

        Button exitBtn = new Button("Exit Application");
        exitBtn.setPrefWidth(250);
        exitBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(header, usernameField, passwordField, loginBtn, createAccBtn, exitBtn);

        Scene scene = new Scene(root, 400, 550);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void showDashboard(Stage stage, String username, char[] password) {
        try {
            // repository uses the password for AES decryption
            EntryRepository repository = new FileEntryRepository(username, password);
            JournalService service = new JournalService(repository);

            // FIXED: Now calls the Dashboard instead of just the Journal screen
            new MainDashboardUI(stage, service, username);

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Login failed. Check your password.").show();
            e.printStackTrace();
        }
    }
}