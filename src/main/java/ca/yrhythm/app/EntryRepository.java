package ca.yrhythm.app;

import java.time.LocalDate;
import java.util.List;

public interface EntryRepository {
    void save(JournalEntry entry) throws Exception;

    // FIX: Added to satisfy "Edit or delete previous entries" requirement
    void update(JournalEntry entry) throws Exception;
    void delete(int id) throws Exception;

    List<JournalEntry> findAll() throws Exception;
    List<JournalEntry> searchByDate(LocalDate date) throws Exception;
}