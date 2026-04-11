package ca.yrhythm.app;

import java.time.LocalDate;
import java.util.*;

public class DebugSandbox {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   Y-RHYTHM ULTRA-TIGHTENING STRESS TEST      ");
        System.out.println("==============================================");

        testStreamResilience();   // T1: Nulls
        testCrisisBoundary();     // T2: Peak Detection
        testParsingRobustness();  // T3: Data Corruption
        testPerformance();        // T4: Scaling
        testRiskProgression();    // T5: Logic (YOUR PREVIOUS FAILURE)
        testBoundaryIntensity();  // T6: Suicidal Ceiling
        testEmptyDataResilience(); // T7: Zero-state
        testTemporalAnomalies();  // T8: Sorting
        testPayloadStress();      // T9: String Limits
        testContextConflict();    // T10: Sarcasm/Override
        testTrendDetection();     // T11: Downward Trends
        testCharacterIntegrity(); // T12: Special Symbols

        System.out.println("\n==============================================");
        System.out.println("         SYSTEM AUDIT COMPLETE                ");
        System.out.println("==============================================");
    }

    private static void testStreamResilience() {
        System.out.print("[TEST 1] Stream Resilience: ");
        try {
            List<JournalEntry> entries = Arrays.asList(
                    new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Ok"),
                    null,
                    new JournalEntry(2, LocalDate.now(), Mood.JOYFUL, "Ok")
            );
            double avg = entries.stream().filter(Objects::nonNull).mapToInt(e -> e.getMood().getIntensity()).average().orElse(0);
            System.out.println(avg == 4.5 ? "PASSED" : "FAILED");
        } catch (Exception e) { System.out.println("FAILED (Crash)"); }
    }

    private static void testCrisisBoundary() {
        System.out.print("[TEST 2] Crisis Boundary: ");
        CrisisAnalyzer analyzer = new CrisisAnalyzer();
        List<JournalEntry> entries = new ArrayList<>();
        for(int i=0; i<3; i++) entries.add(new JournalEntry(i, LocalDate.now(), Mood.HAPPY, "Good"));
        entries.add(new JournalEntry(4, LocalDate.now(), Mood.DEPRESSED, "I want to end it"));
        int score = analyzer.calculateRiskScore(entries);
        System.out.println(score >= 75 ? "PASSED (" + score + "%)" : "FAILED");
    }

    private static void testParsingRobustness() {
        System.out.print("[TEST 3] Parsing Robustness: ");
        String[] corrupt = {"!!!", "1|2024|BAD|Notes", "|||"};
        int blocked = 0;
        for (String s : corrupt) {
            try { JournalEntry.fromFileFormat(s); } catch (Exception e) { blocked++; }
        }
        System.out.println(blocked == corrupt.length ? "PASSED" : "FAILED");
    }

    private static void testPerformance() {
        System.out.print("[TEST 4] Performance (10k Rows): ");
        List<JournalEntry> list = new ArrayList<>();
        for(int i=0; i<10000; i++) list.add(new JournalEntry(i, LocalDate.now(), Mood.HAPPY, "Text"));
        long start = System.currentTimeMillis();
        new CrisisAnalyzer().calculateRiskScore(list);
        long end = System.currentTimeMillis();
        System.out.println((end - start) < 100 ? "PASSED (" + (end-start) + "ms)" : "FAILED");
    }

    private static void testRiskProgression() {
        System.out.print("[TEST 5] Risk Progression (Happy vs Sad): ");
        CrisisAnalyzer analyzer = new CrisisAnalyzer();
        int happy = analyzer.calculateRiskScore(List.of(new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Ok")));
        int sad = analyzer.calculateRiskScore(List.of(new JournalEntry(1, LocalDate.now(), Mood.SAD, "Ok")));
        // FIX: Sad (Intensity 2) must be riskier than Happy (Intensity 4)
        if (sad > happy) System.out.println("PASSED (" + happy + " vs " + sad + ")");
        else System.err.println("FAILED (Sadness not weighted correctly)");
    }

    private static void testBoundaryIntensity() {
        System.out.print("[TEST 6] Suicidal Ceiling: ");
        int score = new CrisisAnalyzer().calculateRiskScore(List.of(new JournalEntry(1, LocalDate.now(), Mood.SUICIDAL, "Help")));
        System.out.println(score >= 90 ? "PASSED (" + score + "%)" : "FAILED");
    }

    private static void testEmptyDataResilience() {
        System.out.print("[TEST 7] Empty Data: ");
        try {
            new CrisisAnalyzer().calculateRiskScore(new ArrayList<>());
            System.out.println("PASSED");
        } catch (Exception e) { System.out.println("FAILED"); }
    }

    private static void testTemporalAnomalies() {
        System.out.print("[TEST 8] Temporal Sorting: ");
        List<JournalEntry> list = new ArrayList<>(List.of(
                new JournalEntry(1, LocalDate.now().plusDays(1), Mood.NEUTRAL, "Later"),
                new JournalEntry(2, LocalDate.now().minusDays(1), Mood.NEUTRAL, "Earlier")
        ));
        EntrySorter.sortByDate(list);
        System.out.println(list.get(0).getContent().equals("Earlier") ? "PASSED" : "FAILED");
    }

    private static void testPayloadStress() {
        System.out.print("[TEST 9] Payload Stress: ");
        String huge = "A".repeat(10000);
        JournalEntry e = new JournalEntry(1, LocalDate.now(), Mood.NEUTRAL, huge);
        System.out.println(e.toFileFormat().length() > 10000 ? "PASSED" : "FAILED");
    }

    private static void testContextConflict() {
        System.out.print("[TEST 10] Mood-Keyword Conflict: ");
        int score = new CrisisAnalyzer().calculateRiskScore(List.of(new JournalEntry(1, LocalDate.now(), Mood.JOYFUL, "end it")));
        System.out.println(score > 40 ? "PASSED (Override: " + score + "%)" : "FAILED");
    }

    private static void testTrendDetection() {
        System.out.print("[TEST 11] Downward Trend Logic: ");
        PatternDetector detector = new PatternDetector();
        List<JournalEntry> trend = List.of(
                new JournalEntry(1, LocalDate.now(), Mood.JOYFUL, "5"),
                new JournalEntry(2, LocalDate.now(), Mood.HAPPY, "4"),
                new JournalEntry(3, LocalDate.now(), Mood.NEUTRAL, "3"),
                new JournalEntry(4, LocalDate.now(), Mood.STRESSED, "2"),
                new JournalEntry(5, LocalDate.now(), Mood.SAD, "1")
        );
        System.out.println(detector.downwardTrend(trend) ? "PASSED" : "FAILED");
    }

    private static void testCharacterIntegrity() {
        System.out.print("[TEST 12] Symbol Integrity: ");
        String physicsNotes = "Δp = m(v_f - v_i). Conservation: Σp_i = Σp_f.";
        JournalEntry e = new JournalEntry(1, LocalDate.now(), Mood.NEUTRAL, physicsNotes);
        try {
            String encrypted = CryptoUtils.encrypt(e.toFileFormat(), "pass".toCharArray());
            String decrypted = CryptoUtils.decrypt(encrypted, "pass".toCharArray());
            System.out.println(decrypted.contains("Δp") ? "PASSED" : "FAILED (Symbol Corruption)");
        } catch (Exception ex) { System.out.println("FAILED (Crypto Error)"); }
    }
}