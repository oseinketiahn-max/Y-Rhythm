package ca.yrhythm.app;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WellnessMapUI — Canada-wide crisis & wellness resource finder.
 *
 * Data source: canadian_crisis_resources.json (bundled in resources)
 * Coverage   : 205+ resources across all 13 provinces & territories,
 *              every major city, town, village, and rural community.
 *
 * Search logic:
 *   1. User types any city, town, or village name
 *   2. Results = NATIONAL resources + resources whose city list contains
 *      the search term (case-insensitive, partial match)
 *   3. Province filter narrows further when selected
 *   4. "Find Near Me on Google" opens Google Maps for live results
 *
 * Offline: all bundled data works without internet.
 * Online : Google Maps search for real-time nearby results.
 */
public class WellnessMapUI {

    private static HostServices hostServices;

    // ── Resource record ───────────────────────────────────────────────────────

    private record Resource(
            String name,
            String address,
            String phone,
            String type,
            List<String> cities,
            String province,
            List<String> tags
    ) {
        /** True if this resource matches a city/town search string. */
        boolean matchesCity(String query) {
            if (query == null || query.isBlank()) return true;
            String q = query.toLowerCase().trim();
            if (cities.contains("ALL")) return true;
            return cities.stream().anyMatch(c -> c.toLowerCase().contains(q));
        }

        /** True if this resource matches the selected province filter. */
        boolean matchesProvince(String prov) {
            if (prov == null || prov.equals("All Provinces & Territories")) return true;
            return province.equals("NATIONAL") || province.equals(prov);
        }

        /** Always show national resources regardless of filter. */
        boolean isNational() { return province.equals("NATIONAL"); }

        String typeColor() {
            return switch (type) {
                case "CRISIS"        -> "#c0392b";
                case "HOSPITAL"      -> "#8e44ad";
                case "MENTAL_HEALTH" -> "#2980b9";
                case "YOUTH"         -> "#27ae60";
                case "WOMEN"         -> "#e67e22";
                case "LGBTQ"         -> "#9b59b6";
                case "INDIGENOUS"    -> "#d35400";
                case "SENIORS"       -> "#16a085";
                case "SHELTER"       -> "#7f8c8d";
                case "SUBSTANCE"     -> "#c0392b";
                default              -> "#2c3e50";
            };
        }

        String typeLabel() {
            return switch (type) {
                case "CRISIS"        -> "CRISIS";
                case "HOSPITAL"      -> "HOSPITAL";
                case "MENTAL_HEALTH" -> "MENTAL HEALTH";
                case "YOUTH"         -> "YOUTH";
                case "WOMEN"         -> "WOMEN";
                case "LGBTQ"         -> "LGBTQ+";
                case "INDIGENOUS"    -> "INDIGENOUS";
                case "SENIORS"       -> "SENIORS";
                case "SHELTER"       -> "SHELTER";
                case "SUBSTANCE"     -> "SUBSTANCE";
                default              -> "GENERAL";
            };
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private static List<Resource> ALL_RESOURCES = null;

    private static List<Resource> loadResources() {
        if (ALL_RESOURCES != null) return ALL_RESOURCES;
        try {
            InputStream is = WellnessMapUI.class
                    .getResourceAsStream("/ca/yrhythm/app/canadian_crisis_resources.json");
            if (is == null) {
                System.err.println("WellnessMapUI: resource file not found.");
                return Collections.emptyList();
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONArray arr = new JSONArray(json);
            List<Resource> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                List<String> cities = new ArrayList<>();
                JSONArray ca = o.getJSONArray("cities");
                for (int j = 0; j < ca.length(); j++) cities.add(ca.getString(j));
                List<String> tags = new ArrayList<>();
                if (o.has("tags")) {
                    JSONArray ta = o.getJSONArray("tags");
                    for (int j = 0; j < ta.length(); j++) tags.add(ta.getString(j));
                }
                list.add(new Resource(
                        o.getString("name"),
                        o.getString("address"),
                        o.getString("phone"),
                        o.getString("type"),
                        cities,
                        o.getString("province"),
                        tags
                ));
            }
            ALL_RESOURCES = list;
        } catch (Exception e) {
            System.err.println("WellnessMapUI load error: " + e.getMessage());
            ALL_RESOURCES = Collections.emptyList();
        }
        return ALL_RESOURCES;
    }

    // ── Province list ─────────────────────────────────────────────────────────

    private static final List<String> PROVINCES = List.of(
            "All Provinces & Territories",
            "AB — Alberta",
            "BC — British Columbia",
            "MB — Manitoba",
            "NB — New Brunswick",
            "NL — Newfoundland & Labrador",
            "NS — Nova Scotia",
            "NT — Northwest Territories",
            "NU — Nunavut",
            "ON — Ontario",
            "PE — Prince Edward Island",
            "QC — Québec",
            "SK — Saskatchewan",
            "YT — Yukon"
    );

    // Maps display string → province code used in data
    private static String provinceCode(String display) {
        if (display == null || display.startsWith("All")) return null;
        return display.substring(0, 2);
    }

    // ── Public entry point ────────────────────────────────────────────────────

    public static void setHostServices(HostServices hs) { hostServices = hs; }

    public static Node getMapNode() {
        List<Resource> resources = loadResources();

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f8f9fa;");

        // ── Top bar ───────────────────────────────────────────────────────────
        VBox topBar = new VBox(10);
        topBar.setPadding(new Insets(22, 24, 16, 24));
        topBar.setStyle("-fx-background-color: #1a237e;");

        Label header = new Label("🇨🇦  Canadian Crisis & Wellness Resource Finder");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label(
                "Covers every province, territory, city, town, and village in Canada. " +
                "All crisis numbers work offline. Google Maps opens for live nearby results.");
        sub.setWrapText(true);
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #c5cae9;");

        // ── Always-visible national crisis strip ──────────────────────────────
        HBox nationalStrip = new HBox(20);
        nationalStrip.setPadding(new Insets(10, 14, 10, 14));
        nationalStrip.setAlignment(Pos.CENTER_LEFT);
        nationalStrip.setStyle("-fx-background-color: #c0392b; -fx-background-radius: 8;");

        Label sos = new Label("🆘  NATIONAL 24/7:");
        sos.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

        Label n988 = pill("988 — Suicide Crisis", "white", "#c0392b");
        Label nKids = pill("Kids Help: 1-800-668-6868", "white", "#c0392b");
        Label nTrans = pill("Trans Lifeline: 1-877-330-6366", "white", "#c0392b");
        Label nIndigenous = pill("Hope for Wellness: 1-855-242-4310", "white", "#c0392b");

        nationalStrip.getChildren().addAll(sos, n988, nKids, nTrans, nIndigenous);

        topBar.getChildren().addAll(header, sub, nationalStrip);

        // ── Search row ────────────────────────────────────────────────────────
        HBox searchRow = new HBox(10);
        searchRow.setPadding(new Insets(16, 24, 12, 24));
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                           "-fx-border-width: 0 0 1 0;");

        TextField cityField = new TextField();
        cityField.setPromptText("🔍  Search city, town, or village (e.g. Kenora, Lac-Mégantic, Rankin Inlet)…");
        cityField.setPrefWidth(380);
        cityField.setStyle("-fx-background-radius: 20; -fx-padding: 9 14; -fx-font-size: 13px;");
        HBox.setHgrow(cityField, Priority.ALWAYS);

        ComboBox<String> provBox = new ComboBox<>();
        provBox.getItems().addAll(PROVINCES);
        provBox.setValue("All Provinces & Territories");
        provBox.setPrefWidth(230);

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll(
                "All Types", "CRISIS", "HOSPITAL", "MENTAL HEALTH",
                "YOUTH", "WOMEN", "LGBTQ+", "INDIGENOUS", "SHELTER"
        );
        typeFilter.setValue("All Types");

        // ── Google Maps search button ──────────────────────────────────────────
        ComboBox<String> serviceType = new ComboBox<>();
        serviceType.getItems().addAll(
                "mental health clinic", "therapist", "crisis centre",
                "hospital", "shelter", "food bank", "counselling"
        );
        serviceType.setValue("mental health clinic");

        Button googleBtn = new Button("🗺  Find Near Me (Google Maps)");
        googleBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-background-radius: 20; " +
                           "-fx-cursor: hand; -fx-padding: 9 18;");
        googleBtn.setOnAction(e -> {
            String city  = cityField.getText().trim().isEmpty()
                         ? (provBox.getValue().startsWith("All") ? "Canada" : provBox.getValue().substring(5))
                         : cityField.getText().trim();
            String svc   = serviceType.getValue();
            // Remove spaces for URL encoding
            String query = (svc + " near " + city + " Canada").replace(" ", "+");
            String url   = "https://www.google.com/maps/search/" + query;
            if (hostServices != null) {
                hostServices.showDocument(url);
            } else {
                Alert a = new Alert(Alert.AlertType.INFORMATION,
                        "Open this URL in your browser:\n" + url);
                a.show();
            }
        });

        searchRow.getChildren().addAll(
                cityField, provBox, typeFilter, serviceType, googleBtn
        );

        // ── Results area ──────────────────────────────────────────────────────
        Label resultCount = new Label();
        resultCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 4 24 0 24;");

        VBox resultList = new VBox(8);
        resultList.setPadding(new Insets(12, 24, 24, 24));

        ScrollPane scroll = new ScrollPane(resultList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; " +
                        "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ── Wire up live search ───────────────────────────────────────────────
        Runnable refresh = () -> {
            String cityQuery = cityField.getText().trim();
            String provCode  = provinceCode(provBox.getValue());
            String typeQuery = typeFilter.getValue();

            List<Resource> filtered = resources.stream()
                    .filter(r -> r.matchesCity(cityQuery))
                    .filter(r -> r.matchesProvince(provCode))
                    .filter(r -> {
                        if (typeQuery == null || typeQuery.equals("All Types")) return true;
                        return switch (typeQuery) {
                            case "MENTAL HEALTH" -> r.type().equals("MENTAL_HEALTH");
                            case "LGBTQ+"        -> r.type().equals("LGBTQ");
                            default              -> r.type().equals(typeQuery);
                        };
                    })
                    // Always show NATIONAL at top
                    .sorted(Comparator.comparing(r -> r.isNational() ? 0 : 1))
                    .collect(Collectors.toList());

            resultList.getChildren().clear();

            if (filtered.isEmpty()) {
                Label none = new Label(
                        "No offline resources found for \"" + cityQuery + "\". " +
                        "Try the Google Maps button for live results, or search a nearby larger city.");
                none.setWrapText(true);
                none.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px; -fx-padding: 20;");
                resultList.getChildren().add(none);
                resultCount.setText("0 resources");
            } else {
                resultCount.setText(filtered.size() + " resources found");
                for (Resource res : filtered) {
                    resultList.getChildren().add(buildCard(res));
                }
            }
        };

        // Trigger search on any input change
        cityField.textProperty().addListener((obs, o, n) -> refresh.run());
        provBox.setOnAction(e -> refresh.run());
        typeFilter.setOnAction(e -> refresh.run());

        // Initial load — show all national + Ontario resources
        cityField.setText("");
        provBox.setValue("All Provinces & Territories");
        refresh.run();

        root.getChildren().addAll(topBar, searchRow, resultCount, scroll);
        return root;
    }

    // ── Card builder ──────────────────────────────────────────────────────────

    private static HBox buildCard(Resource res) {
        HBox card = new HBox(14);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 6, 0, 0, 2);");

        // Left accent bar via border
        String color = res.typeColor();
        card.setStyle(card.getStyle() +
                      "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 5; " +
                      "-fx-border-radius: 0 10 10 0;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Name row
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(res.name());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a2e;");

        if (res.tags().contains("24/7")) {
            Label badge = pill("24/7", "white", "#c0392b");
            nameRow.getChildren().addAll(name, badge);
        } else {
            nameRow.getChildren().add(name);
        }

        // Province badge for national/multi-province results
        if (res.isNational()) {
            Label natBadge = pill("National", "white", "#1a237e");
            nameRow.getChildren().add(natBadge);
        }

        Label addr = new Label("📍 " + res.address());
        addr.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        addr.setWrapText(true);

        Label phone = new Label("📞 " + res.phone());
        phone.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1565c0;");

        // Extra tags (women, indigenous, youth, etc.)
        HBox tagRow = new HBox(5);
        for (String tag : res.tags()) {
            if (!tag.equals("24/7")) { // 24/7 already shown as badge
                tagRow.getChildren().add(pill(tag, "#555", "#f0f0f0"));
            }
        }

        info.getChildren().addAll(nameRow, addr, phone);
        if (!tagRow.getChildren().isEmpty()) info.getChildren().add(tagRow);

        // Type label (right side)
        Label typeLbl = new Label(res.typeLabel());
        typeLbl.setStyle(
                "-fx-background-color: " + color + "22; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-padding: 5 10; -fx-background-radius: 12; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-border-color: " + color + "; -fx-border-radius: 12;");
        typeLbl.setMinWidth(90);
        typeLbl.setAlignment(Pos.CENTER);

        card.getChildren().addAll(info, typeLbl);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(
                "-fx-background-color: white", "-fx-background-color: #fafafa") +
                "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(
                "-fx-background-color: #fafafa", "-fx-background-color: white")));

        return card;
    }

    // ── Pill label helper ─────────────────────────────────────────────────────

    private static Label pill(String text, String fg, String bg) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + fg + "; " +
                "-fx-padding: 3 9; -fx-background-radius: 10; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }
}
