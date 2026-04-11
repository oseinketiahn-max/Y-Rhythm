package ca.yrhythm.app;

import java.util.Random;

public class QuoteService {
    private static final String[] QUOTES = {
            "The strongest people are those who win battles we know nothing about.",
            "Happiness can be found even in the darkest of times if one only remembers to turn on the light.",
            "You, yourself, as much as anybody in the universe, deserve your love and affection.",
            "Recovery is not one and done. It is a lifelong journey that takes place one day, one step at a time.",
            "Be brave enough to heal yourself even when it hurts.",
            "Stars can't shine without darkness.",
            "Healing begins where the wound was made.",
            "You are allowed to be both a masterpiece and a work in progress simultaneously.",
            "Small steps every day.",
            "Rock bottom became the solid foundation on which I rebuilt my life.",
            "Promise me you'll always remember: you're braver than you believe.",
            "The rain will pass, keep walking.",
            "Self-care is giving the world the best of you, not what's left of you.",
            "Your worth is not measured by your productivity.",
            "You don't have to control your thoughts; you just have to stop letting them control you.",
            "In the midst of winter, I found there was, within me, an invincible summer.",
            "You are still here; that is proof your story isn't over.",
            "Choose hope. It will be your most powerful weapon.",
            "Mental health... is not a destination but a process.",
            "Sometimes the most productive thing you can do is rest.",
            "Be gentle with yourself; you're doing the best you can.",
            "This feeling is a tunnel, not a cave. There is light at the end.",
            "You don't have to see the whole staircase, just take the first step.",
            "It’s okay to be a glowstick; sometimes we have to break before we shine.",
            "Your track record for surviving bad days is 100% so far.",
            "Growth is quiet. You're doing better than you think.",
            "One small positive thought in the morning can change your whole day.",
            "Rest is not quitting. It’s refueling.",
            "Be gentle with yourself. You are doing the best you can.",
            "Storms don't last forever, even when it feels like they do.",
            "You are more than your productivity or your mistakes.",
            "Healing is not linear. Ups and downs are part of the process.",
            "Focus on the step you are taking, not the miles ahead.",
            "Small progress is still progress.",
            "You have survived everything life has thrown at you. You are resilient.",
            "The world is better with you in it. Breathe through this moment."
    };

    public static String getRandomQuote() {
        return QUOTES[new Random().nextInt(QUOTES.length)];
    }
}