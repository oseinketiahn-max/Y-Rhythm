package ca.mindpulse.mindpulsejournal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class SyncService {
    public static void syncToCloud(Stage stage, String username) {
        File localFile = new File(username + "_journal.txt");

        if (!localFile.exists()) {
            new Alert(Alert.AlertType.WARNING, "No journal file found to sync.").show();
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Cloud Sync Folder (Dropbox/OneDrive/GDrive)");
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            try {
                File backupFile = new File(selectedDirectory, username + "_journal_backup.txt");
                Files.copy(localFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                new Alert(Alert.AlertType.INFORMATION, "Encrypted sync successful!").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Sync failed: " + e.getMessage()).show();
            }
        }
    }
}