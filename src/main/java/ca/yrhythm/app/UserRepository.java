package ca.yrhythm.app;

import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Manages user data including registration, authentication, and security checks.
 * Aligns with the "User registration and login" requirement of York Region MindPulse.
 */
public class UserRepository {

    private static final File file = new File("users.txt");

    /**
     * Checks if a specific username is already registered in the system.
     */
    public static boolean userExists(String username) throws Exception {
        if (!file.exists()) return false;

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith(username + ":")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Authenticates a user by comparing the hashed password with the stored record.
     */
    public static boolean authenticate(String username, char[] password) throws Exception {
        if (!file.exists()) return false;

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    String salt = parts[1];
                    String storedHash = parts[2];

                    // Hash the provided password with the stored salt to verify
                    String newHash = hash(password, salt);
                    return storedHash.equals(newHash);
                }
            }
        }
        return false;
    }

    /**
     * Registers a new user with a unique salt and a hashed password.
     * Enforces security standards for York Region MindPulse.
     */
    public static void register(String username, char[] password) throws Exception {
        // Basic validation: ensure username is unique and password meets length requirements
        if (userExists(username)) {
            throw new Exception("Username already taken.");
        }
        if (password.length < 8) {
            throw new Exception("Password must be at least 8 characters long.");
        }

        if (!file.exists()) file.createNewFile();

        String salt = generateSalt();
        String hash = hash(password, salt);

        // Store credentials in format: username:salt:hash
        try (FileWriter w = new FileWriter(file, true)) {
            w.write(username + ":" + salt + ":" + hash + "\n");
        }
    }

    /**
     * Generates a random 16-byte salt to protect against rainbow table attacks.
     */
    private static String generateSalt() {
        byte[] s = new byte[16];
        new SecureRandom().nextBytes(s);
        return Base64.getEncoder().encodeToString(s);
    }

    /**
     * Hashes a password using SHA-256 with a unique salt.
     */
    private static String hash(char[] password, String salt) throws Exception {
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        // Combine password and salt for hashing
        byte[] hash = d.digest((new String(password) + salt).getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}