package ca.yrhythm.app;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.util.List;

public class ReportService {
    public static void generateDoctorReport(List<JournalEntry> entries, String path) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));
        document.open();

        // Styles
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font alertFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, java.awt.Color.RED);

        // Header
        document.add(new Paragraph("Y-RHYTHM: Clinical Health Summary", titleFont));
        document.add(new Paragraph("This report contains mood tracking data for clinical review.", subFont));
        document.add(new Paragraph("Export Date: " + java.time.LocalDate.now()));
        document.add(new Paragraph(" ")); // Spacer
        document.add(new Paragraph("-----------------------------------------------------------------------"));
        document.add(new Paragraph(" "));

        // Logic: Summarize Trends
        for (JournalEntry entry : entries) {
            Paragraph p = new Paragraph();
            p.add(new Chunk(entry.getDate() + ": ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            p.add(new Chunk(entry.getMood().getDisplayName()));

            // Highlight specific risks for the doctor
            if (entry.getMood() == Mood.SUICIDAL || entry.getMood() == Mood.DEPRESSED) {
                p.add(new Chunk(" [HIGH RISK FLAG]", alertFont));
            }

            document.add(p);
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Note: This data is user-reported and should be used as a supplement to clinical assessment."));
        document.close();
    }
}