package ca.yrhythm.app;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class QuoteService {
    private static String currentCategory = "Resilience";

    private static final String[] QUOTES = {
            "Resilience: The strongest people are those who win battles we know nothing about.",
            "Peace: Happiness can be found even in the darkest of times if one only remembers to turn on the light.",
            "Peace: You, yourself, as much as anybody in the universe, deserve your love and affection.",
            "Resilience: Recovery is not one and done. It is a lifelong journey that takes place one day, one step at a time.",
            "Resilience: Be brave enough to heal yourself even when it hurts.",
            "Motivation: Stars can't shine without darkness.",
            "Clinical Tips: Healing begins where the wound was made.",
            "Motivation: You are allowed to be both a masterpiece and a work in progress simultaneously.",
            "Motivation: Small steps every day.",
            "Resilience: Rock bottom became the solid foundation on which I rebuilt my life.",
            "Motivation: Promise me you'll always remember: you're braver than you believe.",
            "Motivation: The rain will pass, keep walking.",
            "Clinical Tips: Self-care is giving the world the best of you, not what's left of you.",
            "Clinical Tips: Your worth is not measured by your productivity.",
            "Clinical Tips: You don't have to control your thoughts; you just have to stop letting them control you.",
            "Resilience: In the midst of winter, I found there was, within me, an invincible summer.",
            "Resilience: You are still here; that is proof your story isn't over.",
            "Motivation: Choose hope. It will be your most powerful weapon.",
            "Clinical Tips: Mental health... is not a destination but a process.",
            "Peace: Sometimes the most productive thing you can do is rest.",
            "Peace: Be gentle with yourself; you're doing the best you can.",
            "Motivation: This feeling is a tunnel, not a cave. There is light at the end.",
            "Motivation: You don't have to see the whole staircase, just take the first step.",
            "Resilience: It’s okay to be a glowstick; sometimes we have to break before we shine.",
            "Resilience: Your track record for surviving bad days is 100% so far.",
            "Motivation: Growth is quiet. You're doing better than you think.",
            "Motivation: One small positive thought in the morning can change your whole day.",
            "Peace: Rest is not quitting. It’s refueling.",
            "Peace: Be gentle with yourself. You are doing the best you can.",
            "Resilience: Storms don't last forever, even when it feels like they do.",
            "Clinical Tips: You are more than your productivity or your mistakes.",
            "Clinical Tips: Healing is not linear. Ups and downs are part of the process.",
            "Clinical Tips: Focus on the step you are taking, not the miles ahead.",
            "Motivation: Small progress is still progress.",
            "Resilience: You have survived everything life has thrown at you. You are resilient.",
            "Peace: The world is better with you in it. Breathe through this moment."
    };

    /**
     * Updates the active category for filtering.
     * Called by the Settings UI.
     */
    public static void setCategory(String category) {
        currentCategory = category;
    }

    public static String getRandomQuote() {
        List<String> filtered = new ArrayList<>();
        for (String q : QUOTES) {
            if (q.startsWith(currentCategory)) {
                // Remove the category prefix for display
                filtered.add(q.substring(q.indexOf(":") + 1).trim());
            }
        }

        // Fallback if list is empty
        if (filtered.isEmpty()) {
            return "Take a deep breath. You're doing fine.";
        }

        return filtered.get(new Random().nextInt(filtered.size()));
    }
}