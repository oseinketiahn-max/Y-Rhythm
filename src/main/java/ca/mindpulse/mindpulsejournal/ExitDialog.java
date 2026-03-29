package ca.mindpulse.mindpulsejournal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class ExitDialog {
    public static void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ffb74d; -fx-border-width: 3; -fx-background-radius: 15; -fx-border-radius: 15;");

        Label msg = new Label("End your Y-Rhythm session?");
        msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e65100;");

        Button yes = new Button("Exit App");
        yes.setStyle("-fx-background-color: #ff7043; -fx-text-fill: white; -fx-font-weight: bold;");
        Button no = new Button("Stay");
        no.setStyle("-fx-background-color: #4db6ac; -fx-text-fill: white; -fx-font-weight: bold;");

        yes.setOnAction(e -> System.exit(0));
        no.setOnAction(e -> dialog.close());

        layout.getChildren().addAll(msg, new HBox(15, yes, no));
        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
    }
}