package ca.yrhythm.app;

import java.io.FileWriter;
import java.util.List;

public class CSVExporter {
    public static void export(List<JournalEntry> entries, String filename) throws Exception {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Date,Mood,Journal Entry\n");
            for (JournalEntry entry : entries) {
                // Clean content to avoid CSV errors
                String cleanContent = entry.getContent()
                        .replace("\"", "\"\"") // Escape quotes
                        .replace("\n", " ")    // Remove newlines
                        .replace(",", " ");    // Remove commas

                writer.write(String.format("%s,%s,\"%s\"\n",
                        entry.getDate(),
                        entry.getMoodDisplayName(),
                        cleanContent));
            }
        }
    }
}