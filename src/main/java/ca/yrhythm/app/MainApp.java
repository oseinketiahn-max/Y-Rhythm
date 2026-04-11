package ca.yrhythm.app;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Y-Rhythm | York Region Wellness");

        try {
            // Updated path handling to be more robust
            var iconStream = getClass().getResourceAsStream("/ca/yrhythm/app/images/appicon1.jpg");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            // Logged as a note so it doesn't clutter your main debug logs
            System.out.println("Note: Custom app icon not found, using system default.");
        }

        // Launch the Login UI
        // Transition to MainDashboardUI should happen inside LoginUI upon success
        new LoginUI(primaryStage);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}