package ca.yrhythm.app;

/**
 * Mood — two-tier emotion taxonomy (broad → specific).
 *
 * SAFETY CHANGE: SUICIDAL removed from the mood selector entirely.
 * Replaced with OVERWHELMED (intensity 1). Suicidal ideation is now handled
 * through journal keyword detection and the always-visible SOS button,
 * not as a self-reported mood label — per safe messaging guidelines.
 *
 * Crisis response on OVERWHELMED + keyword combination is handled in CrisisAnalyzer.
 */
public enum Mood {

    // ── Positive ─────────────────────────────────────────────────────────────
    JOYFUL(5,    "Joyful"),
    HAPPY(4,     "Happy"),
    GRATEFUL(4,  "Grateful"),
    CALM(4,      "Calm"),
    CONTENT(3,   "Content"),

    // ── Neutral ───────────────────────────────────────────────────────────────
    NEUTRAL(3,   "Neutral"),
    TIRED(3,     "Tired"),

    // ── Difficult ─────────────────────────────────────────────────────────────
    STRESSED(2,  "Stressed"),
    ANXIOUS(2,   "Anxious"),
    ANGRY(2,     "Angry"),
    SAD(2,       "Sad"),

    // ── Severe ────────────────────────────────────────────────────────────────
    VERY_SAD(1,     "Very Sad"),
    DEPRESSED(1,    "Depressed"),
    OVERWHELMED(1,  "Overwhelmed"),   // replaces SUICIDAL — triggers crisis check in CrisisAnalyzer
    HOPELESS(1,     "Hopeless");      // added for two-tier specificity

    private final int    intensity;
    private final String displayName;

    Mood(int intensity, String displayName) {
        this.intensity   = intensity;
        this.displayName = displayName;
    }

    public int    getIntensity()   { return intensity; }
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
