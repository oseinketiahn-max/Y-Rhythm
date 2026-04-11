package ca.yrhythm.app;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class RegisterUI {
    public RegisterUI(Stage stage) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f1f8e9);");

        Label title = new Label("Join Y-Rhythm");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        TextField userField = new TextField();
        userField.setPromptText("New Username");
        userField.setMaxWidth(250);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setMaxWidth(250);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setMaxWidth(250);

        String fieldStyle = "-fx-background-radius: 12; -fx-border-color: #c5e1a5; -fx-border-radius: 12;";
        userField.setStyle(fieldStyle);
        passField.setStyle(fieldStyle);
        confirmField.setStyle(fieldStyle);

        Button regBtn = new Button("Start Journey");
        regBtn.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-pref-width: 250; -fx-cursor: hand;");

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #558b2f; -fx-cursor: hand;");

        regBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText();

            if (username.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Username cannot be empty").show();
                return;
            }
            if (!password.equals(confirmField.getText())) {
                new Alert(Alert.AlertType.ERROR, "Passwords do not match").show();
                return;
            }

            try {
                // This creates the initial encrypted journal file for the user
                // Using a dummy entry to initialize the repository
                FileEntryRepository repo = new FileEntryRepository(username, password.toCharArray());
                repo.save(new JournalEntry(0, java.time.LocalDate.now(), Mood.NEUTRAL, "Welcome to Y-Rhythm!"));

                Alert success = new Alert(Alert.AlertType.INFORMATION, "Account successfully saved! You can now log in.");
                success.showAndWait();
                new LoginUI(stage);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Could not create account: " + ex.getMessage()).show();
            }
        });

        backBtn.setOnAction(e -> new LoginUI(stage));

        layout.getChildren().addAll(title, userField, passField, confirmField, regBtn, backBtn);

        Scene scene = new Scene(layout, 400, 550);
        stage.setScene(scene);
    }
}