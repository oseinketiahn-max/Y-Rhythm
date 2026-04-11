package ca.yrhythm.app;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 128;

    public static String encrypt(String text, char[] password) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; random.nextBytes(salt);
        byte[] iv = new byte[12]; random.nextBytes(iv);

        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKeySpec key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        // FIX: Explicitly use UTF_8 to preserve physics symbols
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[salt.length + iv.length + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, 16);
        System.arraycopy(iv, 0, combined, 16, 12);
        System.arraycopy(encrypted, 0, combined, 28, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String text, char[] password) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(text);
        byte[] salt = new byte[16]; System.arraycopy(decoded, 0, salt, 0, 16);
        byte[] iv = new byte[12]; System.arraycopy(decoded, 16, iv, 0, 12);
        byte[] cipherText = new byte[decoded.length - 28];
        System.arraycopy(decoded, 28, cipherText, 0, cipherText.length);

        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKeySpec key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        // FIX: Explicitly use UTF_8 when converting bytes back to String
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }
}