package ca.yrhythm.app;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class BreatheUI {
    public static Node getBreatheNode() {
        VBox container = new VBox(40);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 50;");

        // UI Elements
        Label instructionLabel = new Label("Ready to center yourself?");
        instructionLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Label subLabel = new Label(QuoteService.getRandomQuote());
        subLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");
        subLabel.setWrapText(true);
        subLabel.setMaxWidth(400);

        // The Breathing Circle
        Circle circle = new Circle(60, Color.web("#3498db", 0.4));
        circle.setStroke(Color.web("#2980b9"));
        circle.setStrokeWidth(3);

        // --- ANIMATION LOGIC (4s Intervals) ---
        Duration fourSeconds = Duration.seconds(4);

        // 1. Inhale: Scale up and increase Opacity
        ScaleTransition inhaleScale = new ScaleTransition(fourSeconds, circle);
        inhaleScale.setToX(2.5);
        inhaleScale.setToY(2.5);
        FadeTransition inhaleFade = new FadeTransition(fourSeconds, circle);
        inhaleFade.setToValue(0.8);
        ParallelTransition inhale = new ParallelTransition(inhaleScale, inhaleFade);
        inhale.setOnFinished(e -> instructionLabel.setText("Hold..."));

        // 2. Hold Pause
        PauseTransition hold1 = new PauseTransition(fourSeconds);
        hold1.setOnFinished(e -> instructionLabel.setText("Exhale..."));

        // 3. Exhale: Scale down and decrease Opacity
        ScaleTransition exhaleScale = new ScaleTransition(fourSeconds, circle);
        exhaleScale.setToX(1.0);
        exhaleScale.setToY(1.0);
        FadeTransition exhaleFade = new FadeTransition(fourSeconds, circle);
        exhaleFade.setToValue(0.3);
        ParallelTransition exhale = new ParallelTransition(exhaleScale, exhaleFade);
        exhale.setOnFinished(e -> instructionLabel.setText("Hold..."));

        // 4. Second Hold Pause
        PauseTransition hold2 = new PauseTransition(fourSeconds);
        hold2.setOnFinished(e -> {
            instructionLabel.setText("Inhale...");
            subLabel.setText(QuoteService.getRandomQuote()); // Update quote each cycle
        });

        // Loop the sequence
        SequentialTransition breathingLoop = new SequentialTransition(inhale, hold1, exhale, hold2);
        breathingLoop.setCycleCount(Animation.INDEFINITE);

        // Control Button
        Button actionBtn = new Button("Start Box Breathing");
        actionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-background-radius: 25; -fx-padding: 10 30; -fx-cursor: hand;");

        actionBtn.setOnAction(e -> {
            if (breathingLoop.getStatus() == Animation.Status.RUNNING) {
                breathingLoop.stop();
                actionBtn.setText("Start Box Breathing");
                actionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 10 30;");
                instructionLabel.setText("Breath Paused.");
            } else {
                instructionLabel.setText("Inhale...");
                breathingLoop.play();
                actionBtn.setText("Stop");
                actionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 10 30;");
            }
        });

        container.getChildren().addAll(instructionLabel, circle, subLabel, actionBtn);
        return container;
    }
}