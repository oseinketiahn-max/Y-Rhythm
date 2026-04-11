package ca.yrhythm.app;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class WellnessMapUI {
    public static Node getMapNode() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f8f9fa;");

        Label header = new Label("York Region Wellness Navigator");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // --- THE RESOURCE DISPLAY (Must be declared before logic so buttons can reference it) ---
        VBox resourceContainer = new VBox(12);
        resourceContainer.setPadding(new Insets(15));

        ScrollPane resourceScroll = new ScrollPane(resourceContainer);
        resourceScroll.setFitToWidth(true);
        resourceScroll.setPrefHeight(500);
        resourceScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // --- THE GEOGRAPHIC QUICK-GRID ---
        GridPane geoGrid = new GridPane();
        geoGrid.setHgap(15);
        geoGrid.setVgap(15);
        geoGrid.setAlignment(Pos.CENTER);

        // Implementation of Filter Logic: Buttons now call updateResourceList
        geoGrid.add(createRegionButton("North York Region", "Georgina, East Gwillimbury", "#27ae60", e -> updateResourceList(resourceContainer, "NORTH")), 0, 0);
        geoGrid.add(createRegionButton("Central York Region", "Newmarket, Aurora, Stouffville", "#2980b9", e -> updateResourceList(resourceContainer, "CENTRAL")), 1, 0);
        geoGrid.add(createRegionButton("South York Region", "Vaughan, Richmond Hill, Markham", "#8e44ad", e -> updateResourceList(resourceContainer, "SOUTH")), 0, 1);
        geoGrid.add(createRegionButton("Immediate Crisis", "24/7 Support & ERs", "#c0392b", e -> updateResourceList(resourceContainer, "CRISIS")), 1, 1);

        // Initial Load
        updateResourceList(resourceContainer, "ALL");

        container.getChildren().addAll(header, geoGrid, new Separator(), resourceScroll);
        return container;
    }

    private static VBox createRegionButton(String title, String towns, String colorHex, javafx.event.EventHandler<javafx.scene.input.MouseEvent> event) {
        VBox btn = new VBox(5);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(15));
        btn.setPrefSize(280, 100);
        btn.setStyle("-fx-background-color: white; -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 5; -fx-background-radius: 5; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f1f1f1; -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 5;"));
        btn.setOnMouseClicked(event);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");
        Label lblTowns = new Label(towns);
        lblTowns.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");

        btn.getChildren().addAll(lblTitle, lblTowns);
        return btn;
    }

    private static void updateResourceList(VBox container, String filter) {
        container.getChildren().clear();

        // --- EXPANDED DATASET ---
        if (filter.equals("ALL") || filter.equals("CRISIS")) {
            container.getChildren().addAll(
                    createResourceCard("Southlake Regional Health Centre", "Newmarket | 905-895-4521", "EMERGENCY", "#c0392b"),
                    createResourceCard("Mackenzie Health Hospital", "Richmond Hill | 905-883-1212", "EMERGENCY", "#c0392b"),
                    createResourceCard("Cortellucci Vaughan Hospital", "Vaughan | 905-417-2000", "EMERGENCY", "#c0392b"),
                    createResourceCard("310-COPE Crisis Line", "24/7 Support | 1-855-310-2673", "CRISIS", "#c0392b")
            );
        }

        if (filter.equals("ALL") || filter.equals("NORTH")) {
            container.getChildren().addAll(
                    createResourceCard("Georgina Public Health Clinic", "Keswick | 1-877-464-9675", "HEALTH", "#27ae60"),
                    createResourceCard("Blue Door Shelters", "East Gwillimbury | 905-898-1015", "SUPPORT", "#27ae60"),
                    createResourceCard("Sibbald Point Provincial Park", "Sutton West | Green Space", "PARK", "#2ecc71"),
                    createResourceCard("Rogers Reservoir Conservation", "East Gwillimbury | Trails", "PARK", "#2ecc71")
            );
        }

        if (filter.equals("ALL") || filter.equals("CENTRAL")) {
            container.getChildren().addAll(
                    createResourceCard("Aurora Community Centre", "Aurora | 905-727-1375", "COMMUNITY", "#2980b9"),
                    createResourceCard("Fairy Lake Park", "Newmarket | Walking Trails", "PARK", "#3498db"),
                    createResourceCard("Stouffville Medical Centre", "Stouffville | 905-640-3050", "HEALTH", "#2980b9"),
                    createResourceCard("Mabel Davis Conservation Area", "Newmarket | Nature Area", "PARK", "#3498db")
            );
        }

        if (filter.equals("ALL") || filter.equals("SOUTH")) {
            container.getChildren().addAll(
                    createResourceCard("Vaughan Metropolitan Centre", "Vaughan | Public Space", "HUB", "#8e44ad"),
                    createResourceCard("Richmond Green Sports Park", "Richmond Hill | Outdoor", "PARK", "#9b59b6"),
                    createResourceCard("Markham Stouffville Hospital", "Markham | 905-472-7000", "HEALTH", "#8e44ad"),
                    createResourceCard("Bob Hunter Memorial Park", "Markham | Hiking", "PARK", "#9b59b6")
            );
        }
    }

    private static HBox createResourceCard(String name, String detailsText, String tagText, String tagColor) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        VBox details = new VBox(2);
        Label n = new Label(name); n.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label l = new Label(detailsText); l.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        details.getChildren().addAll(n, l);

        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);

        Label tag = new Label(tagText);
        tag.setStyle("-fx-background-color: " + tagColor + "; -fx-text-fill: white; -fx-padding: 3 10; " +
                "-fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");

        card.getChildren().addAll(details, s, tag);
        return card;
    }
}