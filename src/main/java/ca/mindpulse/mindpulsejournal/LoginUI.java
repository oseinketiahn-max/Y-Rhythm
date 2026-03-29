package ca.mindpulse.mindpulsejournal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginUI {
    public LoginUI(Stage stage) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #e0f2f1);");

        Label title = new Label("Y-Rhythm");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #00796b;");
        Label subtitle = new Label("Connecting York Region Wellness");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #004d40;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle("-fx-background-radius: 15; -fx-border-color: #b2dfdb; -fx-border-radius: 15; -fx-padding: 8;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-background-radius: 15; -fx-border-color: #b2dfdb; -fx-border-radius: 15; -fx-padding: 8;");

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color: #00897b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-pref-width: 220;");

        Button regBtn = new Button("Create Account");
        regBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00796b; -fx-border-color: #00796b; -fx-border-radius: 25; -fx-pref-width: 220;");

        Button exitBtn = new Button("Exit App");
        exitBtn.setStyle("-fx-background-color: #ff7043; -fx-text-fill: white; -fx-background-radius: 25; -fx-pref-width: 100;");

        loginBtn.setOnAction(e -> {
            try {
                String username = userField.getText();
                char[] password = passField.getText().toCharArray();
                if (UserRepository.authenticate(username, password)) {
                    JournalService service = new JournalService(new FileEntryRepository(username, password));
                    new JournalUI(stage, service, username);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Access Denied").show();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        regBtn.setOnAction(e -> new RegisterUI(stage));
        exitBtn.setOnAction(e -> ExitDialog.show(stage));

        layout.getChildren().addAll(title, subtitle, userField, passField, loginBtn, regBtn, exitBtn);
        stage.setScene(new Scene(layout, 400, 520));
    }
}