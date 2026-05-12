package ca.yrhythm.app;

/**
 * PromptService — delegates to ContentService.
 *
 * All content has moved to ContentService.
 * This class is kept so existing call sites (JournalUI) compile unchanged.
 */

@SuppressWarnings("all")
public class PromptService {

    public static String getPromptByMood(Mood mood) {
        return ContentService.getPrompt(mood);
    }

    public static String getRandomPrompt() {
        return ContentService.getRandomPrompt();
    }
}
