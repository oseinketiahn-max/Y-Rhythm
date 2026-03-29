package ca.mindpulse.mindpulsejournal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebugSandbox {

    public static void main(String[] args) throws Exception {
        System.out.println("=== DEBUG MODE: FIXED & UPGRADED ===");

        testMoodAnalyticsFixed();
        testCrisisAnalyzerFixed();
    }

    private static void testMoodAnalyticsFixed(){
        List<JournalEntry> entries = new ArrayList<>();
        entries.add(new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Good day"));
        entries.add(new JournalEntry(2, LocalDate.now(), null, "Data with missing mood")); // Edge case

        // FIX: Use .filter(Objects::nonNull) and check if mood itself is null before calling getIntensity()
        double avg = entries.stream()
                .filter(e -> e != null && e.getMood() != null)
                .mapToInt(e -> e.getMood().getIntensity())
                .average()
                .orElse(0.0);

        System.out.println("Fixed Average mood (ignoring nulls) = " + avg);
    }

    private static void testCrisisAnalyzerFixed(){
        List<JournalEntry> entries = new ArrayList<>();

        // Test high-risk keyword detection from CrisisAnalyzer.java
        entries.add(new JournalEntry(1, LocalDate.now(), Mood.DEPRESSED, "I feel so alone and hopeless"));
        entries.add(new JournalEntry(2, LocalDate.now(), Mood.VERY_SAD, "Everything is worthless"));

        CrisisAnalyzer analyzer = new CrisisAnalyzer();

        // Ensure the list isn't empty before analysis to provide meaningful feedback
        if (entries.isEmpty()) {
            System.out.println("Risk score = N/A (No entries to analyze)");
        } else {
            int score = analyzer.calculateRiskScore(entries);
            System.out.println("Polished Risk score = " + score);

            // Integration with RiskTier
            if (score >= 70) System.out.println("ALERT: High Risk Detected!");
        }
    }
}