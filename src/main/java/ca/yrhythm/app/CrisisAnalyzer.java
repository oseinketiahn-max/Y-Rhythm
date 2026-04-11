package ca.yrhythm.app;

import java.util.List;
import java.util.Arrays;

public class CrisisAnalyzer {
    private static final List<String> CRISIS_KEYWORDS = Arrays.asList(
            "suicide", "alone", "lonely", "hurt", "end it", "worthless", "hopeless", "kill", "die"
    );

    public int calculateRiskScore(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;

        // To prevent the score from being "stuck" on an old crisis,
        // we should focus on the most RECENT entries (e.g., the last 3-5).
        int start = Math.max(0, entries.size() - 5);
        List<JournalEntry> recentEntries = entries.subList(start, entries.size());

        int maxRecentRisk = 0;
        int crisisCount = 0;

        for (JournalEntry entry : recentEntries) {
            if (entry == null || entry.getMood() == null) continue;

            int intensity = entry.getMood().getIntensity();
            int gap = 5 - intensity;
            int currentEntryRisk = gap * gap * 4;

            if (containsCrisisKeywords(entry.getContent())) {
                currentEntryRisk = Math.max(currentEntryRisk, 60);
                currentEntryRisk += 20;
                crisisCount++;
            }

            if (currentEntryRisk > maxRecentRisk) {
                maxRecentRisk = currentEntryRisk;
            }
        }

        int frequencyBonus = Math.min(crisisCount * 5, 20);
        return Math.min(maxRecentRisk + frequencyBonus, 100);
    }

    private boolean containsCrisisKeywords(String content) {
        if (content == null || content.isBlank()) return false;
        String lower = content.toLowerCase();
        return CRISIS_KEYWORDS.stream().anyMatch(lower::contains);
    }
}