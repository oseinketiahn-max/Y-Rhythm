package ca.mindpulse.mindpulsejournal;

import java.util.List;

public class MoodAnalytics {

    public double calculateAverage(List<JournalEntry> entries) {

        if (entries.isEmpty()) return 0;

        int sum = 0;

        for (JournalEntry e : entries) {
            sum += e.getMood().getIntensity();
        }

        return (double) sum / entries.size();
    }
}
