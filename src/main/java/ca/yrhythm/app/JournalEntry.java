package ca.yrhythm.app;

import java.time.LocalDate;

public class JournalEntry {
    private int id;
    private String username;
    private LocalDate date;
    private Mood mood;
    private String content;

    // PRIMARY: Used by JournalUI and Reminders
    public JournalEntry(int id, String username, LocalDate date, Mood mood, String content) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.mood = mood;
        this.content = content;
    }

    /**
     * OVERLOADED: Fixes DebugSandbox and RegisterUI compilation errors.
     * Defaults the username so the 4-argument call still works.
     */
    public JournalEntry(int id, LocalDate date, Mood mood, String content) {
        this(id, "System", date, mood, content);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public LocalDate getDate() { return date; }
    public Mood getMood() { return mood; }
    public String getContent() { return content; }

    public String getMoodDisplayName() {
        return (mood != null) ? mood.toString() : "";
    }

    public String toFileFormat() {
        return id + "|" + username + "|" + date + "|" + mood.name() + "|" + content;
    }

    public static JournalEntry fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        try {
            String moodStr = parts.length == 5 ? parts[3] : parts[2];

            // Legacy migration: SUICIDAL was renamed to HOPELESS
            if (moodStr.equals("SUICIDAL") || moodStr.equals("VERY_SAD")) {
                moodStr = "HOPELESS";
            }

            Mood mood = Mood.valueOf(moodStr);

            if (parts.length == 5) {
                return new JournalEntry(
                        Integer.parseInt(parts[0]), parts[1],
                        LocalDate.parse(parts[2]), mood, parts[4]);
            } else {
                return new JournalEntry(
                        Integer.parseInt(parts[0]), "LegacyUser",
                        LocalDate.parse(parts[1]), mood, parts[3]);
            }
        } catch (Exception e) {
            System.err.println("Error parsing entry: " + e.getMessage() + " in line: " + line);
            return null;
        }
    }
}
