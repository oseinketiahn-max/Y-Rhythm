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
    private static volatile boolean isListening = false;
    private static TargetDataLine line;

    /**
     * Updates the model path based on Settings input.
     * Prevents redundant loading of heavy Model objects.
     */
    public static void setModelPath(String path) {
        File modelFolder = new File(path);
        if (modelFolder.exists() && !modelFolder.getAbsolutePath().equals(currentLangPath)) {
            try {
                currentModel = new Model(modelFolder.getAbsolutePath());
                currentLangPath = modelFolder.getAbsolutePath();
            } catch (Exception e) {
                System.err.println("Speech Error: " + e.getMessage());
            }
        }
    }

    public static void setLanguage(String lang) throws IOException {
        String folderName = switch (lang) {
            case "French" -> "model-fr";
            case "Spanish" -> "model-es";
            default -> "model-en";
        };
        setModelPath(Paths.get(System.getProperty("user.dir"), "models", folderName).toString());
    }

    public static void startListening(Consumer<String> onResult, Consumer<String> onError) {
        if (isListening) return;
        new Thread(() -> {
            isListening = true;
            try {
                if (currentModel == null) setLanguage("English");
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                try (Recognizer recognizer = new Recognizer(currentModel, 16000)) {
                    byte[] buffer = new byte[4096];
                    int nbytes;
                    while (isListening && (nbytes = line.read(buffer, 0, buffer.length)) >= 0) {
                        if (recognizer.acceptWaveForm(buffer, nbytes)) {
                            String text = extractTextFromJson(recognizer.getResult());
                            if (!text.isEmpty()) {
                                javafx.application.Platform.runLater(() -> onResult.accept(text));
                                isListening = false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> onError.accept("Speech Error: " + e.getMessage()));
            } finally {
                cleanup();
            }
        }).start();
    }

    public static void stopListening() {
        isListening = false;
        cleanup();
    }

    private static void cleanup() {
        if (line != null) {
            line.stop();
            line.flush(); // Clears residual audio data
            line.close();
            line = null;
        }
        isListening = false;
    }

    private static String extractTextFromJson(String json) {
        if (json == null || !json.contains("\"text\"")) return "";
        int start = json.indexOf("\"text\"") + 9;
        int end = json.lastIndexOf("\"");
        return (start < end) ? json.substring(start, end).trim() : "";
    }
}