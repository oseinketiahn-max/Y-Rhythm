package ca.yrhythm.app;

/**
 * RiskTier — unified risk classification.
 * Replaces the now-removed CrisisLevel enum (which duplicated this concept).
 * Used by JournalService.getRiskTier() and JournalUI refresh().
 */
public enum RiskTier {
    NONE,
    NORMAL,
    LOW,
    MODERATE,
    HIGH
}
