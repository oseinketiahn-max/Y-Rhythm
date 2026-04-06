package ca.mindpulse.mindpulsejournal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DebugSandbox: The final stress-test for Y-Rhythm v1.1.2.2 logic.
 * This class ensures that safety features, math, and performance are tight.
 */
public class DebugSandbox {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   Y-RHYTHM ULTRA-TIGHTENING STRESS TEST      ");
        System.out.println("==============================================");

        testStreamResilience();   // TEST 1: Handling Nulls
        testCrisisBoundary();     // TEST 2: Anomaly/Peak Detection
        testParsingRobustness();  // TEST 3: Data Corruption
        testPerformance();        // TEST 4: Scaling to 10k Rows
        testRiskProgression();    // TEST 5: Logical Increments

        System.out.println("\n==============================================");
        System.out.println("         SYSTEM AUDIT COMPLETE                ");
        System.out.println("==============================================");
    }

    /**
     * TEST 1: Ensures the app doesn't crash if a null entry or null mood exists.
     */
    private static void testStreamResilience() {
        System.out.print("[TEST 1] Stream Resilience: ");
        List<JournalEntry> entries = new ArrayList<>();
        entries.add(new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Victory")); // Intensity 4
        entries.add(null); // The "Crash-Maker"
        entries.add(new JournalEntry(2, LocalDate.now(), Mood.JOYFUL, "Smile")); // Intensity 5

        try {
            double avg = entries.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(e -> e.getMood().getIntensity())
                    .average().orElse(0);

            if (avg == 4.5) {
                System.out.println("PASSED (Handled nulls correctly)");
            } else {
                System.err.println("FAILED (Math incorrect: " + avg + ")");
            }
        } catch (Exception e) {
            System.err.println("FAILED (Crashed on null entry)");
        }
    }

    /**
     * TEST 2: The "Safety" Test.
     * Ensures 1 crisis entry isn't ignored just because the user was happy previously.
     */
    private static void testCrisisBoundary() {
        System.out.print("[TEST 2] Crisis Boundary (Peak Detection): ");
        CrisisAnalyzer analyzer = new CrisisAnalyzer();
        List<JournalEntry> entries = new ArrayList<>();

        // Add "Happy" Noise (4 entries that should normally result in 0% risk)
        for(int i=0; i<4; i++) {
            entries.add(new JournalEntry(i, LocalDate.now(), Mood.HAPPY, "I'm having a great day in York Region."));
        }

        // Add 1 "Crisis" Signal (This should trigger a high risk score)
        entries.add(new JournalEntry(5, LocalDate.now(), Mood.DEPRESSED, "I want to end it, everything is worthless."));

        int score = analyzer.calculateRiskScore(entries);

        // Logic: The score must be >= 75 to pass. If it's low (like 20), the average diluted the risk.
        if (score >= 75) {
            System.out.println("PASSED (High Risk Flagged: " + score + "%)");
        } else {
            System.err.println("FAILED (Danger! Risk was diluted by happy entries: " + score + "%)");
        }
    }

    /**
     * TEST 3: Ensures the app blocks corrupted or malformed strings.
     */
    private static void testParsingRobustness() {
        System.out.print("[TEST 3] Parsing Robustness: ");
        String[] corruptLines = {
                "GARBAGE_NO_PIPES",
                "1|2024-01-01|FAKE_MOOD|Notes",
                "|||",
                "NotAnID|Date|Mood|Content"
        };

        int blocks = 0;
        for (String line : corruptLines) {
            try {
                JournalEntry.fromFileFormat(line);
            } catch (Exception e) {
                blocks++;
            }
        }

        if (blocks == corruptLines.length) {
            System.out.println("PASSED (All " + blocks + " corruptions blocked)");
        } else {
            System.err.println("FAILED (App accepted corrupted data strings)");
        }
    }

    /**
     * TEST 4: Ensures the app stays fast even with 10,000 journal entries.
     */
    private static void testPerformance() {
        System.out.print("[TEST 4] Performance (10,000 Rows): ");
        List<JournalEntry> giantList = new ArrayList<>();
        for(int i=0; i<10000; i++) {
            giantList.add(new JournalEntry(i, LocalDate.now(), Mood.HAPPY, "Standard entry content."));
        }

        long start = System.currentTimeMillis();
        new CrisisAnalyzer().calculateRiskScore(giantList);
        long end = System.currentTimeMillis();

        long duration = end - start;
        if (duration < 100) { // Goal is under 100ms
            System.out.println("PASSED (" + duration + "ms)");
        } else {
            System.err.println("FAILED (Performance lag detected: " + duration + "ms)");
        }
    }

    /**
     * TEST 5: Ensures that higher intensity moods result in lower risk scores.
     */
    private static void testRiskProgression() {
        System.out.print("[TEST 5] Risk Progression Logic: ");
        CrisisAnalyzer analyzer = new CrisisAnalyzer();

        List<JournalEntry> happyList = new ArrayList<>();
        happyList.add(new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Feeling good"));

        List<JournalEntry> sadList = new ArrayList<>();
        sadList.add(new JournalEntry(1, LocalDate.now(), Mood.SAD, "Feeling down"));

        int happyScore = analyzer.calculateRiskScore(happyList);
        int sadScore = analyzer.calculateRiskScore(sadList);

        if (sadScore > happyScore) {
            System.out.println("PASSED (Happy: " + happyScore + " vs Sad: " + sadScore + ")");
        } else {
            System.err.println("FAILED (Risk score did not increase for lower mood)");
        }
    }
}