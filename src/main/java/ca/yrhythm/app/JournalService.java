package ca.yrhythm.app;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JournalService — core business logic for journaling.
 *
 * Absorbs (no new files, no removed features):
 *   • EntrySorter      → sortByDate()
 *   • MoodAnalytics    → calculateAverageMoodIntensity()
 *   • PatternDetector  → downwardTrend()   ← merged this session
 *   • RiskTier enum    → inner enum        ← merged this session
 *     (RiskTier was a 5-line enum used only by JournalService/JournalUI;
 *      keeping it as a top-level file added zero value)
 */
@SuppressWarnings("all")
public class JournalService {

    // ── RiskTier (absorbed from RiskTier.java) ────────────────────────────────
    /**
     * Unified risk classification used by getRiskTier() and JournalUI.refresh().
     * Formerly RiskTier.java — moved here since it is a pure output type of
     * this service and has no other consumers.
     */
    public enum RiskTier { NONE, NORMAL, LOW, MODERATE, HIGH }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final EntryRepository repository;
    private final CrisisAnalyzer  analyzer = new CrisisAnalyzer();

    public JournalService(EntryRepository repository) {
        this.repository = repository;
    }

    // ── Journal CRUD ──────────────────────────────────────────────────────────

    public void saveEntry(JournalEntry entry) throws Exception {
        repository.save(entry);
    }

    public void deleteEntry(int id) throws Exception {
        repository.delete(id);
    }

    public List<JournalEntry> getAllEntries() throws Exception {
        return repository.findAll();
    }

    public List<JournalEntry> searchByDate(LocalDate date) throws Exception {
        return repository.findAll().stream()
                .filter(e -> e.getDate().equals(date))
                .collect(Collectors.toList());
    }

    // ── Smart Reminder ────────────────────────────────────────────────────────

    /**
     * Returns true if the given user already has an entry for today,
     * so the reminder thread doesn't nudge them unnecessarily.
     */
    public boolean hasEntryForDate(String username, LocalDate date) throws Exception {
        if (username == null || date == null) return false;
        return repository.findAll().stream()
                .anyMatch(e -> e.getUsername().equalsIgnoreCase(username)
                        && e.getDate().equals(date));
    }

    // ── Risk & Crisis ─────────────────────────────────────────────────────────

    public int getCurrentRiskScore() throws Exception {
        return analyzer.calculateRiskScore(repository.findAll());
    }

    public RiskTier getRiskTier() throws Exception {
        int score = getCurrentRiskScore();
        if (score >= 70) return RiskTier.HIGH;
        if (score >= 40) return RiskTier.MODERATE;
        if (score >= 20) return RiskTier.LOW;
        return RiskTier.NONE;
    }

    /** Bundled offline — never requires a network call. */
    public String getCrisisResources() {
        return "988 Suicide & Crisis Lifeline: 9-8-8\n"
             + "Kids Help Phone: 1-800-668-6868 | Text: CONNECT to 686868\n"
             + "York Region / South Simcoe Crisis: 1-855-310-COPE\n"
             + "Connex Ontario: 1-866-531-2600\n"
             + "Crisis Services Canada: 1-833-456-4566";
    }

    // ── Sorting (absorbed from EntrySorter) ──────────────────────────────────

    /** Sorts entries ascending by date in-place. */
    public static void sortByDate(List<JournalEntry> entries) {
        Collections.sort(entries, Comparator.comparing(JournalEntry::getDate));
    }

    // ── Analytics (absorbed from MoodAnalytics) ───────────────────────────────

    /**
     * Returns the average mood intensity across all entries (0–5 scale).
     */
    public double calculateAverageMoodIntensity(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0.0;
        List<JournalEntry> valid = entries.stream()
                .filter(e -> e != null && e.getMood() != null)
                .collect(Collectors.toList());
        if (valid.isEmpty()) return 0.0;
        int sum = 0;
        for (JournalEntry e : valid) sum += e.getMood().getIntensity();
        return (double) sum / valid.size();
    }

    // ── Pattern detection (absorbed from PatternDetector.java) ────────────────

    /**
     * Detects a downward trend in mood intensity.
     *
     * Algorithm: if more than 50% of consecutive transitions are declining,
     * the trend is downward. Works for as few as 2 entries (1/1 = 100%).
     *
     * Previously in PatternDetector.java — moved here because it has no state,
     * no external callers, and operates entirely on JournalEntry data.
     * DebugSandbox test retained; call journalService.downwardTrend(entries).
     */
    public boolean downwardTrend(List<JournalEntry> entries) {
        if (entries == null || entries.size() < 2) return false;
        int declineCount = 0;
        for (int i = 1; i < entries.size(); i++) {
            int prev = entries.get(i - 1).getMood().getIntensity();
            int curr = entries.get(i).getMood().getIntensity();
            if (curr < prev) declineCount++;
        }
        double ratio = (double) declineCount / (entries.size() - 1);
        return ratio >= 0.5;
    }
}
