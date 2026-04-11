package ca.yrhythm.app;

import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class SpeechService {
    private static Model currentModel;
    private static String currentLangPath = "";
    private static boolean isListening = false; // Flag to prevent multiple threads

    public static void setLanguage(String lang) throws IOException {
        String folderName = switch (lang) {
            case "French" -> "model-fr";
            case "Spanish" -> "model-es";
            case "Farsi" -> "model-fa";
            case "Chinese" -> "model-cn";
            default -> "model-en";
        };

        // Use Paths.get for cross-platform slash handling (Fixes your log issue)
        File modelFolder = Paths.get(System.getProperty("user.dir"), "models", folderName).toFile();

        if (!modelFolder.exists()) {
            throw new IOException("Model not found at: " + modelFolder.getAbsolutePath());
        }

        if (!modelFolder.getAbsolutePath().equals(currentLangPath)) {
            // Model loading is heavy; ensure we don't reload if already at this path
            currentModel = new Model(modelFolder.getAbsolutePath());
            currentLangPath = modelFolder.getAbsolutePath();
        }
    }

    public static void startListening(Consumer<String> onResult, Consumer<String> onError) {
        if (isListening) return; // Prevent "Ghost Threads" from stacking up

        new Thread(() -> {
            isListening = true;
            TargetDataLine line = null;
            try {
                if (currentModel == null) setLanguage("English");

                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    throw new LineUnavailableException("Microphone not supported or denied access.");
                }

                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                try (Recognizer recognizer = new Recognizer(currentModel, 16000)) {
                    byte[] buffer = new byte[4096];
                    int nbytes;

                    // The loop should run until we get a solid result or are told to stop
                    while (isListening && (nbytes = line.read(buffer, 0, buffer.length)) >= 0) {
                        if (recognizer.acceptWaveForm(buffer, nbytes)) {
                            String resultJson = recognizer.getResult();
                            // Safely extract text from {"text" : "your result"}
                            String text = extractTextFromJson(resultJson);

                            if (!text.isEmpty()) {
                                javafx.application.Platform.runLater(() -> onResult.accept(text));
                                isListening = false; // Stop after one successful capture
                            }
                        }
                    }
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> onError.accept("Speech Error: " + e.getMessage()));
            } finally {
                if (line != null) {
                    line.stop();
                    line.close();
                }
                isListening = false;
            }
        }).start();
    }

    public static void stopListening() {
        isListening = false;
    }

    // A safer way to get the text without a full JSON library if you want to keep it light
    private static String extractTextFromJson(String json) {
        if (json == null || !json.contains("\"text\"")) return "";
        int start = json.indexOf("\"text\"") + 9;
        int end = json.lastIndexOf("\"");
        return (start < end) ? json.substring(start, end).trim() : "";
    }
}