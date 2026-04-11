package ca.yrhythm.app;

import java.time.LocalDate;

public class JournalEntry {
    private int id;
    private LocalDate date;
    private Mood mood;
    private String content;

    public JournalEntry(int id, LocalDate date, Mood mood, String content) {
        this.id = id;
        this.date = date;
        this.mood = mood;
        this.content = content;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public Mood getMood() { return mood; }
    public String getContent() { return content; }

    // This is the specific method the TableView needs for the Mood column
    public String getMoodDisplayName() {
        return (mood != null) ? mood.toString() : "";
    }

    public String toFileFormat() {
        return id + "|" + date + "|" + mood.name() + "|" + content;
    }

    public static JournalEntry fromFileFormat(String line) {
        String[] parts = line.split("\\|", 4);
        return new JournalEntry(
                Integer.parseInt(parts[0]),
                LocalDate.parse(parts[1]),
                Mood.valueOf(parts[2]),
                parts[3]
        );
    }
}