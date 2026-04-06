package ca.mindpulse.mindpulsejournal;

import java.util.Random;

public class PromptService {
    private static final Random random = new Random();

    // Prompts sorted by emotional "Mood" categories
    private static final String[] POSITIVE_PROMPTS = {
            "What is one victory you experienced today?",
            "Who is someone in your community you are grateful for?",
            "What made you smile today, even for a brief moment?",
            "What are you looking forward to in the coming week?"
    };

    private static final String[] NEUTRAL_PROMPTS = {
            "Describe a local space in York Region where you feel most at peace.",
            "Write about a conversation that stayed with you today.",
            "How did you take care of your physical or mental health today?"
    };

    private static final String[] NEGATIVE_PROMPTS = {
            "What is a challenge you faced today, and how did you handle it?",
            "If you could give yourself one piece of advice right now, what would it be?",
            "What is one small thing you can control right now?",
            "What is a goal you're working toward, and what's the next step?"
    };

    /**
     * Logic to sort and provide a prompt based on the mood intensity.
     */
    public static String getPromptByMood(Mood mood) {
        if (mood == null) return "Write about your day...";

        int intensity = mood.getIntensity();

        // Sorting logic: High intensity (4-5) gets positive, 3 gets neutral, <3 gets negative
        if (intensity >= 4) {
            return POSITIVE_PROMPTS[random.nextInt(POSITIVE_PROMPTS.length)];
        } else if (intensity == 3) {
            return NEUTRAL_PROMPTS[random.nextInt(NEUTRAL_PROMPTS.length)];
        } else {
            return NEGATIVE_PROMPTS[random.nextInt(NEGATIVE_PROMPTS.length)];
        }
    }

    public static String getRandomPrompt() {
        String[][] all = {POSITIVE_PROMPTS, NEUTRAL_PROMPTS, NEGATIVE_PROMPTS};
        String[] selectedCategory = all[random.nextInt(3)];
        return selectedCategory[random.nextInt(selectedCategory.length)];
    }
}