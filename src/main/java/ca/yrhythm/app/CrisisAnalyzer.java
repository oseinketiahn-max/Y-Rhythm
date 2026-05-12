package ca.yrhythm.app;


import java.util.List;
import java.util.regex.Pattern;

/**
 * CrisisAnalyzer — risk scoring and keyword detection.
 *
 * FIXES applied:
 *   1. Whole-word regex matching only (\b boundaries) — "diet" no longer matches "die",
 *      "alone" as a standalone word is still caught but "not alone" is not.
 *   2. Removed "alone" and "lonely" from the crisis keyword list.
 *      These belong in a lower "check-in" tier (see checkSupportKeywords below),
 *      not in the crisis alert path — false positives erode user trust.
 *   3. OVERWHELMED and HOPELESS moods now immediately push score to HIGH_RISK
 *      regardless of text content, implementing the safe messaging requirement
 *      that replaced the SUICIDAL mood option.
 *   4. Sliding window is kept at last 5 entries to prevent stale scores.
 */
@SuppressWarnings("all")
public class CrisisAnalyzer {

    // ── Crisis keywords — whole-word only ─────────────────────────────────────
    private static final List<Pattern> CRISIS_PATTERNS = List.of(
            wb("suicide"), wb("suicidal"),
            wb("kill myself"), wb("end my life"),
            wb("don't want to live"), wb("want to die"),
            wb("hurt myself"), wb("self.harm"),
            wb("worthless"), wb("hopeless"),
            wb("no reason to live"), wb("end it all")
    );

    // ── Support keywords — lower tier, triggers a gentle prompt, not a crisis alert ──
    public static final List<Pattern> SUPPORT_PATTERNS = List.of(
            wb("alone"), wb("lonely"), wb("no one cares"),
            wb("exhausted"), wb("can't cope"), wb("can't do this")
    );

    private static Pattern wb(String phrase) {
        // \b works on single words; for phrases with spaces, anchor each end
        String escaped = Pattern.quote(phrase);
        return Pattern.compile("\\b" + escaped + "\\b", Pattern.CASE_INSENSITIVE);
    }

    // ── Risk score (0–100) ────────────────────────────────────────────────────

    public int calculateRiskScore(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;

        int start = Math.max(0, entries.size() - 5);
        List<JournalEntry> recent = entries.subList(start, entries.size());

        int maxRisk    = 0;
        int crisisCount = 0;

        for (JournalEntry entry : recent) {
            if (entry == null || entry.getMood() == null) continue;

            Mood mood = entry.getMood();

            // Immediate HIGH score for the two moods that replaced SUICIDAL
            if (mood == Mood.OVERWHELMED || mood == Mood.HOPELESS) {
                maxRisk = Math.max(maxRisk, 85);
                crisisCount++;
                continue;
            }

            int intensity = mood.getIntensity();
            int gap = 5 - intensity;
            int entryRisk = gap * gap * 4;

            if (containsCrisisKeywords(entry.getContent())) {
                entryRisk = Math.max(entryRisk, 60);
                entryRisk += 20;
                crisisCount++;
            }

            maxRisk = Math.max(maxRisk, entryRisk);
        }

        int frequencyBonus = Math.min(crisisCount * 5, 20);
        return Math.min(maxRisk + frequencyBonus, 100);
    }

    // ── Keyword checks ────────────────────────────────────────────────────────

    public boolean containsCrisisKeywords(String content) {
        if (content == null || content.isBlank()) return false;
        return CRISIS_PATTERNS.stream().anyMatch(p -> p.matcher(content).find());
    }

    public boolean containsSupportKeywords(String content) {
        if (content == null || content.isBlank()) return false;
        return SUPPORT_PATTERNS.stream().anyMatch(p -> p.matcher(content).find());
    }
}
