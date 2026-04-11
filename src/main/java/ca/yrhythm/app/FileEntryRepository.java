package ca.yrhythm.app;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FileEntryRepository implements EntryRepository {
    private final File file;
    private final char[] password;

    public FileEntryRepository(String username, char[] password) {
        this.file = new File(username + "_journal.txt");
        this.password = password;
    }

    @Override
    public void save(JournalEntry entry) throws Exception {
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(CryptoUtils.encrypt(entry.toFileFormat(), password) + "\n");
        }
    }

    @Override
    public void update(JournalEntry entry) throws Exception {
        List<JournalEntry> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == entry.getId()) {
                all.set(i, entry);
                break;
            }
        }
        rewriteFile(all);
    }

    @Override
    public void delete(int id) throws Exception {
        List<JournalEntry> all = findAll();
        List<JournalEntry> filtered = all.stream()
                .filter(e -> e.getId() != id)
                .collect(Collectors.toList());
        rewriteFile(filtered);
    }

    private void rewriteFile(List<JournalEntry> entries) throws Exception {
        try (FileWriter writer = new FileWriter(file, false)) {
            for (JournalEntry e : entries) {
                writer.write(CryptoUtils.encrypt(e.toFileFormat(), password) + "\n");
            }
        }
    }

    @Override
    public List<JournalEntry> findAll() throws Exception {
        List<JournalEntry> entries = new ArrayList<>();
        if (!file.exists()) return entries;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // DECRYPTION FIX: Skip lines that cause Tag Mismatch (wrong password/corruption)
                    String decrypted = CryptoUtils.decrypt(line, password);
                    entries.add(JournalEntry.fromFileFormat(decrypted));
                } catch (javax.crypto.AEADBadTagException e) {
                    System.err.println("Integrity check failed for an entry (Tag Mismatch). Skipping row.");
                } catch (Exception e) {
                    System.err.println("Error parsing entry: " + e.getMessage());
                }
            }
        }
        return entries;
    }

    @Override
    public List<JournalEntry> searchByDate(LocalDate date) throws Exception {
        return findAll().stream()
                .filter(e -> e.getDate().equals(date))
                .collect(Collectors.toList());
    }
}