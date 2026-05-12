package ca.yrhythm.app;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

@SuppressWarnings("all")
public class BreatheUI {
    private static String currentPattern = "Box (4-4-4-4)";
    private static SequentialTransition masterAnimation;

    public static void setPattern(String pattern) {
        currentPattern = pattern;
    }

    public static void stopAnimation() {
        if (masterAnimation != null) {
            masterAnimation.stop();
        }
    }

    public static Node getBreatheNode() {
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: transparent; -fx-padding: 50;");

        Label titleLabel = new Label("Breathing Style: " + currentPattern);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #2e7d32;");

        Label guideLabel = new Label("Ready?");
        guideLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");

        Circle circle = new Circle(60, Color.web("#a5d6a7", 0.4));
        circle.setStroke(Color.web("#2e7d32"));
        circle.setStrokeWidth(3);

        // --- DYNAMIC TIMING LOGIC ---
        double inhaleSec, holdInSec, exhaleSec, holdOutSec;

        switch (currentPattern) {
            case "4-7-8 Relax" -> {
                inhaleSec = 4.0; holdInSec = 7.0; exhaleSec = 8.0; holdOutSec = 0.5;
            }
            case "Equal (5-5)" -> {
                inhaleSec = 5.0; holdInSec = 0.5; exhaleSec = 5.0; holdOutSec = 0.5;
            }
            default -> { // Box (4-4-4-4)
                inhaleSec = 4.0; holdInSec = 4.0; exhaleSec = 4.0; holdOutSec = 4.0;
            }
        }

        // --- ANIMATION PHASES ---

        // 1. Inhale (Expand)
        ScaleTransition inhale = new ScaleTransition(Duration.seconds(inhaleSec), circle);
        inhale.setToX(2.5); inhale.setToY(2.5);
        inhale.setOnFinished(e -> {
            // Only say HOLD if there is a significant pause, otherwise jump to EXHALE
            guideLabel.setText(holdInSec > 1.0 ? "HOLD" : "EXHALE");
        });

        // 2. Hold In
        PauseTransition holdIn = new PauseTransition(Duration.seconds(holdInSec));
        holdIn.setOnFinished(e -> guideLabel.setText("EXHALE"));

        // 3. Exhale (Shrink)
        ScaleTransition exhale = new ScaleTransition(Duration.seconds(exhaleSec), circle);
        exhale.setToX(1.0); exhale.setToY(1.0);
        exhale.setOnFinished(e -> {
            guideLabel.setText(holdOutSec > 1.0 ? "HOLD" : "INHALE");
        });

        // 4. Hold Out
        PauseTransition holdOut = new PauseTransition(Duration.seconds(holdOutSec));
        holdOut.setOnFinished(e -> guideLabel.setText("INHALE"));

        masterAnimation = new SequentialTransition(inhale, holdIn, exhale, holdOut);
        masterAnimation.setCycleCount(Animation.INDEFINITE);

        // --- CONTROLS ---
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Start");
        styleBtn(startBtn, "#2e7d32");
        startBtn.setOnAction(e -> {
            guideLabel.setText("INHALE");
            masterAnimation.play();
        });

        Button stopBtn = new Button("Stop");
        styleBtn(stopBtn, "#c62828");
        stopBtn.setOnAction(e -> {
            stopAnimation();
            guideLabel.setText("Ready?");
            circle.setScaleX(1.0);
            circle.setScaleY(1.0);
        });

        controls.getChildren().addAll(startBtn, stopBtn);
        container.getChildren().addAll(titleLabel, guideLabel, circle, controls);
        return container;
    }

    private static void styleBtn(Button btn, String color) {
        btn.setStyle("-fx-background-radius: 20; -fx-padding: 10 30; " +
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
    }
}