package ca.yrhythm.app;

import java.util.regex.Pattern;

/**
 * InputValidator — security utility to sanitise and validate user input.
 *
 * Changes this session:
 *   • Added isSafeUsername() — blocks path traversal and injection characters
 *     Required by DebugSandbox SEC 5 and SEC 6.
 *
 * Existing methods (sanitize, isValidSize, isBlank) unchanged.
 */

@SuppressWarnings("all")
public class InputValidator {

    /** Max characters for a single journal entry (~1 MB). */
    private static final int MAX_TEXT_LENGTH = 1_000_000;

    /** HTML / script tag pattern. */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    /**
     * Allowed username pattern: 3–40 alphanumeric chars, underscores, hyphens.
     * Blocks: path separators (/ \\ ..), shell metacharacters (: ; | \n \r),
     * empty strings, and names that start with a dot or hyphen.
     */
    private static final Pattern SAFE_USERNAME =
            Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_\\-]{2,39}$");

    // ── SEC 3: HTML / XSS sanitisation ───────────────────────────────────────

    /**
     * Removes HTML tags and neutralises angle brackets so injected script tags
     * cannot execute if content is ever rendered in a web context.
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        String sanitized = HTML_TAG_PATTERN.matcher(input).replaceAll("");
        return sanitized.replace("<", "&lt;").replace(">", "&gt;");
    }

    // ── SEC 4: DoS size guard ─────────────────────────────────────────────────

    /**
     * Returns true only if the input is within safe operational limits.
     * Rejects anything over 1 MB to prevent memory exhaustion.
     */
    public static boolean isValidSize(String input) {
        if (input == null) return true;
        return input.length() <= MAX_TEXT_LENGTH;
    }

    // ── SEC 5 & SEC 6: Username safety ───────────────────────────────────────

    /**
     * Returns true only if the username is safe for use as part of a filename
     * and cannot be used for path traversal or shell/CSV injection.
     *
     * Blocks:
     *   • Path traversal:  ../  ..\  /root  ../../etc/passwd
     *   • Shell chars:     ; | & $ ` ~ ! # % ^ * ( ) { } [ ] < > ? \
     *   • File format:     : (field separator in users.txt)
     *   • Whitespace:      spaces, newlines, tabs
     *   • Empty strings and single-char names
     *
     * Allows: letters, digits, underscore, hyphen (min 3 chars, max 40).
     */
    public static boolean isSafeUsername(String username) {
        if (username == null || username.isBlank()) return false;
        return SAFE_USERNAME.matcher(username).matches();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Returns true if the input is null or whitespace only. */
    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
