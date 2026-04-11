package ca.yrhythm.app;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class JournalService {
    private final EntryRepository repository;
    private final CrisisAnalyzer analyzer = new CrisisAnalyzer();

    public JournalService(EntryRepository repository) { this.repository = repository; }

    public void saveEntry(JournalEntry entry) throws Exception { repository.save(entry); }
    public void deleteEntry(int id) throws Exception { repository.delete(id); }

    public List<JournalEntry> getAllEntries() throws Exception { return repository.findAll(); }

    public List<JournalEntry> searchByDate(LocalDate date) throws Exception {
        return repository.findAll().stream()
                .filter(e -> e.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public int getCurrentRiskScore() throws Exception {
        return analyzer.calculateRiskScore(repository.findAll());
    }

    /**
     * FIX: Added missing method to determine the RiskTier based on the current score.
     */
    public RiskTier getRiskTier() throws Exception {
        int score = getCurrentRiskScore();
        if (score >= 70) return RiskTier.HIGH;
        if (score >= 40) return RiskTier.MODERATE;
        if (score >= 20) return RiskTier.LOW;
        return RiskTier.NONE;
    }

    public String getCrisisResources() {
        return "York Region/South Simcoe Crisis Line: 1-855-310-COPE\n" +
                "Kids Help Phone: 1-800-668-6868\n" +
                "360 Kids: 905-475-6694\n" +
                "Suicide Crisis Helpline: 9-8-8\n" +
                "Connex Ontario: 1-866-531-2600";
    }
}