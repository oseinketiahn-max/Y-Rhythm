package ca.yrhythm.app;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntrySorter {

    public static void sortByDate(List<JournalEntry> entries){

        Collections.sort(
            entries,
            Comparator.comparing(JournalEntry::getDate)
        );
    }
}
