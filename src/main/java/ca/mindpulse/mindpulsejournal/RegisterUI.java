package ca.mindpulse.mindpulsejournal;

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

        TextField user = new TextField();
        user.setPromptText("New Username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirm Password");

        String style = "-fx-background-radius: 12; -fx-border-color: #c5e1a5; -fx-border-radius: 12;";
        user.setStyle(style); pass.setStyle(style); confirm.setStyle(style);

        Button regBtn = new Button("Start Journey");
        regBtn.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-pref-width: 200;");

        Button backBtn = new Button("Cancel");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #558b2f;");

        regBtn.setOnAction(e -> {
            if (!pass.getText().equals(confirm.getText())) {
                new Alert(Alert.AlertType.ERROR, "Passwords do not match").show();
                return;
            }
            try {
                UserRepository.register(user.getText(), pass.getText().toCharArray());
                new LoginUI(stage);
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
        });

        backBtn.setOnAction(e -> new LoginUI(stage));

        layout.getChildren().addAll(title, user, pass, confirm, regBtn, backBtn);
        stage.setScene(new Scene(layout, 400, 500));
    }
}