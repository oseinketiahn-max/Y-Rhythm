package ca.mindpulse.mindpulsejournal;

import java.util.List;
import java.util.Arrays;

public class CrisisAnalyzer {
    private static final List<String> CRISIS_KEYWORDS = Arrays.asList(
            "suicide", "hurt", "end it", "worthless", "hopeless", "kill", "die"
    );

    public int calculateRiskScore(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;

        int maxEntryRisk = 0;
        int crisisCount = 0;

        for (JournalEntry entry : entries) {
            // Calculate risk for THIS specific entry
            int currentEntryRisk = (5 - entry.getMood().getIntensity()) * 10;

            if (containsCrisisKeywords(entry.getContent())) {
                currentEntryRisk += 50; // Significant jump for keywords
                crisisCount++;
            }

            // TRACK THE PEAK: If this entry is scarier than previous ones, it becomes the new baseline
            if (currentEntryRisk > maxEntryRisk) {
                maxEntryRisk = currentEntryRisk;
            }
        }

        // LOGIC TIGHTENING:
        // Instead of averaging everything, we take the Highest Risk found
        // and slightly adjust it based on the frequency of crisis mentions.
        int finalScore = maxEntryRisk + (crisisCount * 5);

        return Math.min(finalScore, 100);
    }

    private boolean containsCrisisKeywords(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        return CRISIS_KEYWORDS.stream().anyMatch(lower::contains);
    }
}