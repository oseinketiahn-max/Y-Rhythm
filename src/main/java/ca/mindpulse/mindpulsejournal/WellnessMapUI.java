package ca.mindpulse.mindpulsejournal;

import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

public class WellnessMapUI {
    public static VBox getMapNode() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("York Region Wellness Spaces & Trails");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        WebView webView = new WebView();
        // Curated search for York Region nature trails and public libraries
        String mapUrl = "https://www.google.com/maps/search/nature+trails+and+parks+in+york+region+ontario";
        webView.getEngine().load(mapUrl);
        webView.setPrefHeight(600);

        container.getChildren().addAll(title, webView);
        return container;
    }
}