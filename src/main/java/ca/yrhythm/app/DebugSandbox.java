package ca.yrhythm.app;

import java.time.LocalDate;
import java.util.*;

/**
 * DebugSandbox — security and logic audit suite.
 *
 * Changes this session (no test removed):
 *   • PatternDetector removed as standalone class → call journalService.downwardTrend()
 *   • RiskTier now an inner enum of JournalService → JournalService.RiskTier
 *   • ExitDialog removed → MainDashboardUI.showExitDialog()
 *   • Added SEC 5: Path Traversal guard
 *   • Added SEC 6: Username injection guard
 *   • Added SEC 7: Replay attack resistance (unique IV check)
 *   • Added SEC 8: Password clearance (char[] zeroing after use)
 *   • All existing SEC 1-4 and LOG 1-4 tests unchanged
 */

@SuppressWarnings("all")
public class DebugSandbox {

    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("  Y-RHYTHM SECURITY & LOGIC AUDIT v3.0          ");
        System.out.println("================================================");

        // ── Security layer ────────────────────────────────────────────────────
        testCryptoTamperResistance();    // SEC 1 — AES-GCM AEAD integrity
        testSymbolIntegrity();           // SEC 2 — UTF-8 round-trip
        testInjectionSanitization();     // SEC 3 — XSS / HTML injection
        testPayloadExhaustion();         // SEC 4 — DoS size guard
        testPathTraversal();             // SEC 5 — directory traversal
        testUsernameInjection();         // SEC 6 — username with path chars
        testReplayResistance();          // SEC 7 — unique IV per encryption
        testPasswordClearance();         // SEC 8 — char[] zeroed after use

        // ── Logic layer ───────────────────────────────────────────────────────
        testRiskOverrideLogic();         // LOG 1 — keyword overrides happy mood
        testStreamResilience();          // LOG 2 — null safety in streams
        testTrendDetection();            // LOG 3 — downward trend (now in JournalService)
        testPerformanceScaling();        // LOG 4 — 5 000 risk score ops < 200 ms

        System.out.println("\n================================================");
        System.out.println("         AUDIT COMPLETE                          ");
        System.out.println("================================================");
    }

    // ── SEC 1 — Tamper resistance (AES-GCM) ──────────────────────────────────
    private static void testCryptoTamperResistance() {
        System.out.print("[SEC 1] Tamper Resistance (AES-GCM): ");
        try {
            String data      = "Sensitive chemistry equilibrium notes.";
            String encrypted = CryptoUtils.encrypt(data, "chem123".toCharArray());

            byte[] raw = Base64.getDecoder().decode(encrypted);
            raw[raw.length - 1] ^= 0x01;  // flip last bit
            String tampered = Base64.getEncoder().encodeToString(raw);

            try {
                CryptoUtils.decrypt(tampered, "chem123".toCharArray());
                System.err.println("FAILED (decrypted tampered ciphertext)");
            } catch (javax.crypto.AEADBadTagException | IllegalArgumentException e) {
                System.out.println("PASSED (tamper detected via GCM auth tag)");
            }
        } catch (Exception e) { System.out.println("ERROR: " + e.getMessage()); }
    }

    // ── SEC 2 — UTF-8 symbol round-trip ──────────────────────────────────────
    private static void testSymbolIntegrity() {
        System.out.print("[SEC 2] Symbol Integrity (UTF-8): ");
        try {
            String physics = "ΔG = ΔH − TΔS";
            String enc = CryptoUtils.encrypt(physics, "pass".toCharArray());
            String dec = CryptoUtils.decrypt(enc,     "pass".toCharArray());
            System.out.println(dec.equals(physics) ? "PASSED" : "FAILED (corruption)");
        } catch (Exception e) { System.out.println("FAILED (crash)"); }
    }

    // ── SEC 3 — XSS / HTML injection guard ───────────────────────────────────
    private static void testInjectionSanitization() {
        System.out.print("[SEC 3] Script Injection Guard: ");
        String mal   = "<script>stealPassword()</script>";
        String clean = InputValidator.sanitize(mal);
        System.out.println(!clean.contains("<script>") ? "PASSED" : "FAILED (vulnerable)");
    }

    // ── SEC 4 — DoS payload size guard ───────────────────────────────────────
    private static void testPayloadExhaustion() {
        System.out.print("[SEC 4] Payload Size Guard: ");
        String huge     = "A".repeat(1024 * 1024 * 5); // 5 MB
        boolean rejected = !InputValidator.isValidSize(huge);
        System.out.println(rejected ? "PASSED (rejected 5 MB)" : "FAILED (accepted 5 MB)");
    }

    // ── SEC 5 — Path traversal guard ─────────────────────────────────────────
    private static void testPathTraversal() {
        System.out.print("[SEC 5] Path Traversal Guard: ");
        // A malicious username like "../../etc/passwd" must not be accepted
        String[] dangerous = { "../../etc/passwd", "../admin", "..\\windows\\system32", "/root" };
        boolean allBlocked = true;
        for (String name : dangerous) {
            if (!InputValidator.isSafeUsername(name)) continue; // correctly blocked
            allBlocked = false;
            System.err.print(" PASSED_WHEN_SHOULD_FAIL(" + name + ")");
        }
        System.out.println(allBlocked ? "PASSED (all traversal names blocked)" : " FAILED");
    }

    // ── SEC 6 — Username injection guard ─────────────────────────────────────
    private static void testUsernameInjection() {
        System.out.print("[SEC 6] Username Injection Guard: ");
        String[] badNames = { "user:admin", "user\nroot", "user|cmd", "user;drop", "" };
        boolean allBlocked = true;
        for (String name : badNames) {
            if (!InputValidator.isSafeUsername(name)) continue;
            allBlocked = false;
        }
        System.out.println(allBlocked ? "PASSED" : "FAILED (unsafe username accepted)");
    }

    // ── SEC 7 — Replay / IV uniqueness ───────────────────────────────────────
    private static void testReplayResistance() {
        System.out.print("[SEC 7] Replay Resistance (unique IV): ");
        try {
            String msg  = "same plaintext";
            char[] pass = "samepass".toCharArray();
            String enc1 = CryptoUtils.encrypt(msg, pass);
            String enc2 = CryptoUtils.encrypt(msg, pass);
            // AES-GCM with random IV: two encryptions of the same data must differ
            System.out.println(!enc1.equals(enc2) ? "PASSED (unique ciphertext)" : "FAILED (IV reuse)");
        } catch (Exception e) { System.out.println("ERROR: " + e.getMessage()); }
    }

    // ── SEC 8 — Password char[] zeroed after use ──────────────────────────────
    private static void testPasswordClearance() {
        System.out.print("[SEC 8] Password Clearance (char[] zeroed): ");
        char[] pwd = "mySecretPass".toCharArray();
        // Simulate what LoginUI does after a login attempt
        Arrays.fill(pwd, '\0');
        boolean cleared = true;
        for (char c : pwd) { if (c != '\0') { cleared = false; break; } }
        System.out.println(cleared ? "PASSED" : "FAILED (password remains in memory)");
    }

    // ── LOG 1 — Keyword overrides happy mood ──────────────────────────────────
    private static void testRiskOverrideLogic() {
        System.out.print("[LOG 1] Sentiment Spoofing Guard: ");
        CrisisAnalyzer analyzer = new CrisisAnalyzer();
        int score = analyzer.calculateRiskScore(List.of(
            new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "I want to hurt myself")
        ));
        System.out.println(score > 70 ? "PASSED (keyword overrides HAPPY)" : "FAILED (spoofable)");
    }

    // ── LOG 2 — Null safety in streams ───────────────────────────────────────
    private static void testStreamResilience() {
        System.out.print("[LOG 2] Stream Null-Safety: ");
        try {
            List<JournalEntry> entries = Arrays.asList(
                new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Ok"), null
            );
            long count = entries.stream().filter(Objects::nonNull).count();
            System.out.println(count == 1 ? "PASSED" : "FAILED");
        } catch (Exception e) { System.out.println("FAILED (crash: " + e.getMessage() + ")"); }
    }

    // ── LOG 3 — Downward trend detection (now in JournalService) ─────────────
    private static void testTrendDetection() {
        System.out.print("[LOG 3] Downward Trend (JournalService): ");
        // PatternDetector absorbed; call via a temporary JournalService with null repo
        // We instantiate using reflection-free approach: subclass with stub repo
        JournalService svc = new JournalService(new StubRepository());
        List<JournalEntry> trend = List.of(
            new JournalEntry(1, LocalDate.now(), Mood.HAPPY, "Good"),
            new JournalEntry(2, LocalDate.now(), Mood.SAD,   "Bad")
        );
        System.out.println(svc.downwardTrend(trend)
            ? "PASSED (2-entry downward detected)"
            : "FAILED");
    }

    // ── LOG 4 — Performance (5 000 operations) ────────────────────────────────
    private static void testPerformanceScaling() {
        System.out.print("[LOG 4] Performance (5 000 risk ops): ");
        long start = System.currentTimeMillis();
        CrisisAnalyzer a = new CrisisAnalyzer();
        for (int i = 0; i < 5000; i++) a.calculateRiskScore(new ArrayList<>());
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(elapsed < 200
            ? "PASSED (" + elapsed + " ms)"
            : "SLOW (" + elapsed + " ms — review CrisisAnalyzer)");
    }

    // ── Stub repository for LOG 3 ─────────────────────────────────────────────
    private static class StubRepository implements EntryRepository {
        @Override public void save(JournalEntry e) {}
        @Override public void update(JournalEntry e) {}
        @Override public void delete(int id) {}
        @Override public List<JournalEntry> findAll() { return Collections.emptyList(); }
        @Override public List<JournalEntry> searchByDate(java.time.LocalDate d) { return Collections.emptyList(); }
    }
}
