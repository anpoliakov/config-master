package by.anpoliakov.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

/**
 * Класс для создания скриптов автозапуска,
 * работает с директорией /home/user/.config/autostart/script.desktop
 **/
public class AutoStartupScriptManager {
    private static final Logger logger = LogManager.getLogger(AutoStartupScriptManager.class);

    public static void createAutoStartupScript(String scriptName, String script) {
        if (scriptName == null || scriptName.isEmpty() || script == null || script.isEmpty()) {
            logger.error("Validation failed: scriptName or script is null/empty.");
            throw new IllegalArgumentException("Parameters scriptName and/or script must not be null or empty!");
        }

        File autostartDir = new File(Constants.PATH_AUTOSTART_DIR);
        File autostartFile = new File(autostartDir, scriptName + Constants.SCRIPT_FILE_EXTENSION);

        if (!autostartDir.exists()) {
            if (!autostartDir.mkdirs()) {
                logger.error("Failed to create autostart directory: {}", Constants.PATH_AUTOSTART_DIR);
                throw new IllegalStateException("Could not create autostart directory. Read log file.");
            }
        }

        if (autostartFile.exists()) {
            if (!askUserToReplaceFile(scriptName)) {
                return;
            }
        }

        changeDirectoryOwner(Constants.NAME_USER, autostartDir);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(autostartFile))) {
            writeDesktopEntry(writer, scriptName, script);
            if (!autostartFile.setExecutable(true)) {
                logger.error("Failed to set the file as executable: {}", autostartFile.getAbsolutePath());
            }
            System.out.println("Скрипт автозапуска [" + scriptName + ".desktop] был успешно добавлен! Путь: " + autostartFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error creating the desktop entry at {}: {}", autostartFile.getAbsolutePath(), e.getMessage(), e);
            throw new RuntimeException("Failed to create the desktop entry file.", e);
        }
    }

    private static boolean askUserToReplaceFile(String scriptName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("File exists!");
        alert.setHeaderText(null);
        alert.setContentText("Скрипт \"" + scriptName + "\" уже существует. Хотите заменить его?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void writeDesktopEntry(BufferedWriter writer, String scriptName, String command) {
        StringBuilder content = new StringBuilder();
        content.append("[Desktop Entry]").append(System.lineSeparator())
                .append("Type=Application").append(System.lineSeparator())
                .append("Exec=").append(command).append(System.lineSeparator())
                .append("Hidden=false").append(System.lineSeparator())
                .append("NoDisplay=false").append(System.lineSeparator())
                .append("X-GNOME-Autostart-enabled=true").append(System.lineSeparator())
                .append("Name=").append(scriptName).append(System.lineSeparator())
                .append("Comment=created by ConfigMaster").append(System.lineSeparator());

        try {
            writer.write(content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void changeDirectoryOwner(String owner, File pathToDirectory) {
        if (pathToDirectory.exists()) {
            String command = String.format("chown -R %s:%s %s",
                    owner,
                    owner,
                    pathToDirectory.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            try {
                Process process = processBuilder.start();
                if (process.waitFor() == 0) {
                    System.out.println("Установлен владелец " + owner + ", директории " + pathToDirectory);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void deleteAutoStartupScript(String scriptName) {
        File autostartFile = new File(Constants.PATH_AUTOSTART_DIR + "/" + scriptName + Constants.SCRIPT_FILE_EXTENSION);

        if (autostartFile.exists()) {
            if (autostartFile.delete()) {
                SystemInfo.showInfoMessage("Скрипт " + scriptName + Constants.SCRIPT_FILE_EXTENSION + " был успешно удалён ");
            } else {
                System.out.println("Ошибка удаления скрипта " + scriptName + ", по пути: " + autostartFile.getParent());
                logger.error("The autorun script " + scriptName + " has not been deleted");
            }
        } else {
            SystemInfo.showErrorMessage(scriptName + Constants.SCRIPT_FILE_EXTENSION + " НЕ СУЩЕСТВУЕТ!");
        }
    }
}
