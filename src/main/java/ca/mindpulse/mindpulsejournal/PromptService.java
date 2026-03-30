package ca.mindpulse.mindpulsejournal;

import java.util.Random;
public class PromptService {
    private static final String[] PROMPTS = {
            "What is one victory you experienced today?",
        "   Who is someone in your community you are greatful for?",
            "Describe a local space in York Region where you feel most at peace.",
            "What is a challenge you faced today, and how did you handle it?",
            "If you could give yourself one piece of advice right now, what would it be?",
            "What made you smile today, even for a brief moment?",
            "How did you take care of your physical or mental health today?",
            "What is a goal you're working toward, and what's the next step?",
            "Write about a conversation that stayed with you today.",
            "What are you looking forward to in the coming week?"

    };
    private static final Random random = new Random();
    public static String getRandomPrompt() {
        return PROMPTS[random.nextInt(PROMPTS.length)];
    }
}
