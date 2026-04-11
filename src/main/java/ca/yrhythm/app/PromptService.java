package ca.yrhythm.app;

import java.util.Random;

public class PromptService {
    private static final Random random = new Random();

    private static final String[] JOYFUL_PROMPTS = {
            "What is the highlight of your day?",
            "How can you share this positive energy with someone else today?",
            "What about this moment makes you feel most alive?",
            "What is a recent win that you are proud of?"
    };

    private static final String[] HAPPY_GRATEFUL_PROMPTS = {
            "What is one victory you experienced today?",
            "Who is someone in your community you are grateful for?",
            "What made you smile today, even for a brief moment?",
            "What are you looking forward to in the coming week?",
            "What is a simple pleasure that brought you comfort today?"
    };

    private static final String[] CALM_PROMPTS = {
            "Describe the stillness you feel right now.",
            "What does 'peace' look like to you in this moment?",
            "How can you maintain this sense of balance tomorrow?",
            "What is a local space in York Region where you feel most at peace?"
    };

    private static final String[] NEUTRAL_PROMPTS = {
            "Write about a conversation that stayed with you today.",
            "How did you take care of your physical or mental health today?",
            "What was the most interesting thing you observed today?",
            "List three things you accomplished, no matter how small."
    };

    private static final String[] STRESSED_ANXIOUS_PROMPTS = {
            "What is one small thing you can control right now?",
            "Take a deep breath. What are three things you can see and two things you can hear?",
            "What is a challenge you faced today, and how did you handle it?",
            "If you could offload one task today, what would it be?",
            "What does your body need right now (rest, water, movement)?"
    };

    private static final String[] ANGRY_PROMPTS = {
            "What boundary of yours was crossed today?",
            "Write a letter to what is making you angry (you don't have to send it).",
            "What is the core value behind this frustration?",
            "How can you release this heat in a healthy way?"
    };

    private static final String[] SAD_DEPRESSED_PROMPTS = {
            "If you could give yourself one piece of advice right now, what would it be?",
            "What is a small kindness someone showed you, or you showed yourself?",
            "It's okay to not be okay. What is weighing most on your heart?",
            "What is one thing that felt heavy today?",
            "Is there a memory that provides a small sense of comfort?"
    };

    private static final String[] CRISIS_PROMPTS = {
            "Please reach out to a friend or the York Region Crisis line (1-855-310-COPE). You are not alone.",
            "What is one reason to hold on for just the next hour?",
            "Who is one person you feel safe talking to right now?",
            "Describe one thing in the room that feels solid and real."
    };

    /**
     * Logic to provide a specific prompt based on the Mood enum.
     */
    public static String getPromptByMood(Mood mood) {
        if (mood == null) return "Write about your day...";

        return switch (mood) {
            case JOYFUL -> JOYFUL_PROMPTS[random.nextInt(JOYFUL_PROMPTS.length)];

            case HAPPY, GRATEFUL -> HAPPY_GRATEFUL_PROMPTS[random.nextInt(HAPPY_GRATEFUL_PROMPTS.length)];

            case CALM -> CALM_PROMPTS[random.nextInt(CALM_PROMPTS.length)];

            case NEUTRAL -> NEUTRAL_PROMPTS[random.nextInt(NEUTRAL_PROMPTS.length)];

            case STRESSED, ANXIOUS -> STRESSED_ANXIOUS_PROMPTS[random.nextInt(STRESSED_ANXIOUS_PROMPTS.length)];

            case ANGRY -> ANGRY_PROMPTS[random.nextInt(ANGRY_PROMPTS.length)];

            case SAD, VERY_SAD, DEPRESSED -> SAD_DEPRESSED_PROMPTS[random.nextInt(SAD_DEPRESSED_PROMPTS.length)];

            case SUICIDAL -> CRISIS_PROMPTS[random.nextInt(CRISIS_PROMPTS.length)];
        };
    }

    public static String getRandomPrompt() {
        // Fallback to a general variety if no mood is selected
        String[][] all = {JOYFUL_PROMPTS, HAPPY_GRATEFUL_PROMPTS, NEUTRAL_PROMPTS, STRESSED_ANXIOUS_PROMPTS, SAD_DEPRESSED_PROMPTS};
        String[] selectedCategory = all[random.nextInt(all.length)];
        return selectedCategory[random.nextInt(selectedCategory.length)];
    }
}