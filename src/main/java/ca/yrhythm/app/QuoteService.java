package ca.yrhythm.app;

/**
 * QuoteService — delegates to ContentService.
 *
 * All content has moved to ContentService so there is a single place to edit copy.
 * This class is kept so existing call sites (MainDashboardUI, Settings) compile unchanged.
 */

@SuppressWarnings("all")
public class QuoteService {

    private static String currentCategory = "Resilience";

    public static void setCategory(String category) {
        currentCategory = category;
    }

    public static String getRandomQuote() {
        return ContentService.getQuote(currentCategory);
    }
}
