package ca.yrhythm.app;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

/**
 * ReportService — all data export logic in one place.
 *
 * Absorbs:
 *   • CSVExporter (export to CSV — moved here; both classes are export utilities
 *     and there is no reason to split them across files)
 *
 * Usage:
 *   ReportService.generateDoctorReport(entries, "user_report.pdf");
 *   ReportService.exportCSV(entries, "user_data.csv");
 */
public class ReportService {

    // ── PDF Doctor's Report ───────────────────────────────────────────────────

    /**
     * Generates a clinical summary PDF for sharing with a therapist or doctor.
     * Warns in JournalUI before saving that the output file is unencrypted.
     */
    public static void generateDoctorReport(List<JournalEntry> entries, String path) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font alertFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12, Font.BOLD, java.awt.Color.RED);

        document.add(new Paragraph("Y-RHYTHM: Clinical Health Summary", titleFont));
        document.add(new Paragraph(
                "This report contains mood tracking data for clinical review.\n" +
                "This file is NOT encrypted — store securely.", subFont));
        document.add(new Paragraph("Export Date: " + java.time.LocalDate.now()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("-------------------------------------------------------------------"));
        document.add(new Paragraph(" "));

        for (JournalEntry entry : entries) {
            Paragraph p = new Paragraph();
            p.add(new Chunk(entry.getDate() + ": ",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            p.add(new Chunk(entry.getMood().getDisplayName()));

            if (entry.getMood() == Mood.OVERWHELMED || entry.getMood() == Mood.DEPRESSED) {
                p.add(new Chunk(" [HIGH RISK FLAG]", alertFont));
            }
            document.add(p);
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Note: This data is user-reported and should supplement clinical assessment."));
        document.close();
    }

    // ── CSV Export (absorbed from CSVExporter) ────────────────────────────────

    /**
     * Exports all journal entries to a CSV file.
     * Previously in CSVExporter.java — moved here since both methods are export utilities.
     */
    public static void exportCSV(List<JournalEntry> entries, String filename) throws Exception {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Date,Mood,Journal Entry\n");
            for (JournalEntry entry : entries) {
                String cleanContent = entry.getContent()
                        .replace("\"", "\"\"")   // escape quotes
                        .replace("\n", " ")       // flatten newlines
                        .replace(",", " ");       // remove commas

                writer.write(String.format("%s,%s,\"%s\"%n",
                        entry.getDate(),
                        entry.getMoodDisplayName(),
                        cleanContent));
            }
        }
    }
}
