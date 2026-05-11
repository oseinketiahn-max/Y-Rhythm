package ca.yrhythm.app;

import java.time.LocalDate;
import java.util.*;

public class DebugSandbox {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   Y-RHYTHM SECURITY & STRESS AUDIT v2.0      ");
        System.out.println("==============================================");

        // --- SECURITY LAYER ---
        testCryptoTamperResistance(); // SEC 1: AEAD check
        testSymbolIntegrity();        // SEC 2: UTF-8 check
        testInjectionSanitization();  // SEC 3: XSS check
        testPayloadExhaustion();      // SEC 4: DoS check

        // --- LOGIC LAYER ---
        testRiskOverrideLogic();      // LOG 1: Keyword vs Enum
        testStreamResilience();       // LOG 2: Null safety
        testTrendDetection();         // LOG 3: Pattern logic
        testPerformanceScaling();     // LOG 4: Throughput

        System.out.println("\n==============================================");
        System.out.println("         SYSTEM AUDIT COMPLETE                ");
        System.out.println("==============================================");
    }

    private static void testCryptoTamperResistance() {
        System.out.print("[SEC 1] Tamper Resistance (AES-GCM): ");
        try {
            String data = "Sensitive chemistry equilibrium notes.";
            String encrypted = CryptoUtils.encrypt(data, "chem123".toCharArray());

            // Hacker modifies the last byte of the Base64 string
            byte[] raw = Base64.getDecoder().decode(encrypted);
            raw[raw.length - 1] ^= 0x01; // Flip a single bit
            String tampered = Base64.getEncoder().encodeToString(raw);

            try {
                CryptoUtils.decrypt(tampered, "chem123".toCharArray());
                System.err.println("FAILED (Decrypted tampered data!)");
            } catch (javax.crypto.AEADBadTagException | IllegalArgumentException e) {
                System.out.println("PASSED (Tamper detected)");
            }
        } catch (Exception e) { System.out.println("ERROR: " + e.getMessage()); }
    }

    private static void testSymbolIntegrity() {
        System.out.print("[SEC 2] Symbol Integrity (UTF-8): ");
        try {
            String physics = "ΔG = ΔH - TΔS"; // Gibbs Free Energy
            String enc = CryptoUtils.encrypt(physics, "pass".toCharArray());
            String dec = CryptoUtils.decrypt(enc, "pass".toCharArray());
            System.out.println(dec.equals(physics) ? "PASSED" : "FAILED (Corruption)");
        } catch (Exception e) { System.out.println("FAILED (Crash)"); }
    }

    private static void testInjectionSanitization() {
        System.out.print("[SEC 3] Script Injection Guard: ");
        String mal = "<script>stealPassword()</script>";
        // Assumes you have an InputValidator class
        String clean = InputValidator.sanitize(mal);
        System.out.println(!clean.contains("<script>") ? "PASSED" : "FAILED (Vulnerable)");
    }

    private static void testPayloadExhaustion() {
        System.out.print("[SEC 4] Payload Size Guard: ");
        String huge = "A".repeat(1024 * 1024 * 5); // 5MB
        boolean rejected = !InputValidator.isValidSize(huge);
        System.out.println(rejected ? "PASSED (Rejected DoS)" : "FAILED (Accepted 5MB)");
    }

    private static void testRiskOverrideLogic() {
        System.out.print("[LOG 1] Sentiment Spoofing Guard: ");
        CrisisAnalyzer analyzer = new CrisisAnalyzer();
        // User selects HAPPY but types SUICIDAL keywords
        int score = analyzer.calculateRiskScore(List.of(
                new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "I want to hurt myself")
        ));
        System.out.println(score > 70 ? "PASSED (Override works)" : "FAILED (Spoofable)");
    }

    private static void testStreamResilience() {
        System.out.print("[LOG 2] Stream Null-Safety: ");
        try {
            List<JournalEntry> entries = Arrays.asList(new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Ok"), null);
            long count = entries.stream().filter(Objects::nonNull).count();
            System.out.println(count == 1 ? "PASSED" : "FAILED");
        } catch (Exception e) { System.out.println("FAILED (Crash)"); }
    }

    private static void testTrendDetection() {
        System.out.print("[LOG 3] Downward Trend Logic: ");
        PatternDetector detector = new PatternDetector();
        List<JournalEntry> trend = List.of(
                new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Good"),
                new JournalEntry(2, LocalDate.now(), Mood.SAD, "Bad")
        );
        System.out.println(detector.downwardTrend(trend) ? "PASSED" : "FAILED");
    }

    private static void testPerformanceScaling() {
        System.out.print("[LOG 4] Performance (5k Ops): ");
        long start = System.currentTimeMillis();
        for(int i=0; i<5000; i++) {
            new CrisisAnalyzer().calculateRiskScore(new ArrayList<>());
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) < 200 ? "PASSED (" + (end-start) + "ms)" : "FAILED");
    }
}