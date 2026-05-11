package ca.yrhythm.app;

import java.util.regex.Pattern;

/**
 * Security utility to sanitize and validate user input.
 * Prevents Cross-Site Scripting (XSS) and Denial of Service (DoS) attacks.
 */
public class InputValidator {

    // Max allowed characters for a single journal entry (e.g., 1MB roughly)
    private static final int MAX_TEXT_LENGTH = 1_000_000;

    // Basic regex to identify common HTML/Script tags
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    /**
     * SEC 3: Removes HTML tags and potential script injections.
     * Replaces brackets to neutralize tags without deleting the content inside.
     */
    public static String sanitize(String input) {
        if (input == null) return "";

        // Remove literal script tags and other dangerous HTML
        String sanitized = HTML_TAG_PATTERN.matcher(input).replaceAll("");

        // Neutralize remaining brackets just in case
        return sanitized.replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * SEC 4: Checks if the input size is within safe operational limits.
     * Prevents memory exhaustion attacks (DoS).
     */
    public static boolean isValidSize(String input) {
        if (input == null) return true;
        return input.length() <= MAX_TEXT_LENGTH;
    }

    /**
     * Utility to check for empty or whitespace-only strings.
     */
    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}