package ca.mindpulse.mindpulsejournal;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Set the Global Window Title
        primaryStage.setTitle("Y-Rhythm | York Region Wellness");

        try {
            // 2. Load the vibrant transparent logo
            // Path matches your resources folder: src/main/resources/ca/mindpulse/mindpulsejournal/images/img2.png
            var iconStream = getClass().getResourceAsStream("/ca/mindpulse/mindpulsejournal/images/img2.png");

            if (iconStream != null) {
                Image appIcon = new Image(iconStream, 64, 64, true, true);
                primaryStage.getIcons().add(appIcon);
            } else {
                // Log the specific missing file for easier debugging
                System.out.println("Warning: /images/img2.png not found. Using system default icon.");
            }

        } catch (Exception e) {
            System.err.println("Critical error loading application assets:");
            e.printStackTrace();
        }

        // 3. Launch the vibrant Login Screen
        // This will transition to JournalUI upon successful authentication
        new LoginUI(primaryStage);

        // 4. Ensure the window is visible to the user
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Standard JavaFX launch sequence
        launch(args);
    }
}