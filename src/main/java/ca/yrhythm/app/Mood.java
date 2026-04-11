package ca.yrhythm.app;

public enum Mood {
    JOYFUL(5, "Joyful"), HAPPY(4, "Happy"), CALM(4, "Calm"),
    GRATEFUL(4, "Grateful"), NEUTRAL(3, "Neutral"), STRESSED(2, "Stressed"),
    ANXIOUS(2, "Anxious"), ANGRY(2, "Angry"), SAD(2, "Sad"),
    VERY_SAD(1, "Very Sad"), DEPRESSED(1, "Depressed"), SUICIDAL(0, "Suicidal");

    private final int intensity;
    private final String displayName;

    Mood(int intensity, String displayName) {
        this.intensity = intensity;
        this.displayName = displayName;
    }

    public int getIntensity() { return intensity; }
    public String getDisplayName() { return displayName; }
    @Override public String toString() { return displayName; }
}