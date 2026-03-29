package ca.mindpulse.mindpulsejournal;
import java.util.List;

public class CrisisAnalyzer {
    public int calculateRiskScore(List<JournalEntry> entries) {
        int score = 0;
        int consecutiveLow = 0;

        for (JournalEntry e : entries) {
            if (e.getMood().getIntensity() <= 2) {
                score += 10;
                consecutiveLow++;
            } else {
                consecutiveLow = 0;
            }

            String text = e.getContent().toLowerCase();
            if (text.contains("hopeless") || text.contains("alone") || text.contains("end it")) {
                score += 25;
            }

            if (consecutiveLow >= 3) score += 20;
        }
        return Math.min(score, 100);
    }
}