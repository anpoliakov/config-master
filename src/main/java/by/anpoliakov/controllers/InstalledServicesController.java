package by.anpoliakov.controllers;

import by.anpoliakov.utils.Constants;
import by.anpoliakov.utils.SystemInfo;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class InstalledServicesController {
    private static Logger logger = LogManager.getLogger(InstalledServicesController.class);

    @FXML
    private ListView<String> installedServicesListView;

    @FXML
    private void initialize() {
        showInstalledServicesList();
    }

    private void showInstalledServicesList() {
        List<String> services = getInstalledServicesPaths();
        this.installedServicesListView.getItems().clear();
        this.installedServicesListView.getItems().addAll(services.toArray(new String[0]));
    }

    /**
     * Use this method to get the path to installed services.
     *
     * @return the paths of installed services of the form /opt/digialq/server
     */
    private List<String> getInstalledServicesPaths() {
        Path pathToDirServices = Paths.get(Constants.PATH_DEF_FOLDER_SERVICES);

        if (!Files.exists(pathToDirServices) || !Files.isDirectory(pathToDirServices)) {
            throw new IllegalArgumentException("Path does not exist or is not a directory: " + pathToDirServices);
        }


        try {
            return Files.list(pathToDirServices)
                    .filter(Files::isDirectory)
                    .flatMap(innerDir -> {
                        try {
                            return Files.list(innerDir).filter(Files::isDirectory);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .map(Path::toString)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Failed to read directories: " + pathToDirServices, e);
        }
    }

    @FXML
    private void showLogSelectedService() {
        String selectedService = getSelectedService();
        if (selectedService == null) {
            return;
        }

        try {
            Path pathLogFile = searchPathLogFile(selectedService);
            String command = String.format("tail -f -n 500 %s", pathLogFile);
            String[] terminalCommand = {
                    "/bin/bash",
                    "-c",
                    String.format("xterm -hold -fa 'Monospace' -fs 12 -bg grey -fg black -e '/bin/bash -c \"%s\"'", command)
            };

            ProcessBuilder processBuilder = new ProcessBuilder(terminalCommand);
            processBuilder.start();

        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @FXML
    private void deleteSelectedService() {
        String selectedService = getSelectedService();
        if (selectedService == null) {
            return;
        }

        String packageName = findPackageName(selectedService);
        System.out.println("Удаление пакета " + packageName + "...");

        if (manageService(packageName, "stop")) {
            System.out.println("Сервис " + packageName + " остановлен.");
        }

        if (manageService(packageName, "disable")) {
            System.out.println("Сервис " + packageName + " исключён из автозапуска.");
        }

        String removeService = String.format("sudo apt remove -y %s", packageName);
        String removeFolder = String.format("sudo rm -rf %s", selectedService);
        ProcessBuilder processBuilder1 = new ProcessBuilder("/bin/bash", "-c", removeService);
        ProcessBuilder processBuilder2 = new ProcessBuilder("/bin/bash", "-c", removeFolder);


        try {
            Process process = processBuilder1.start();
            if (process.waitFor() == 0) {
                Process process1 = processBuilder2.start();
                if (process1.waitFor() == 0) {
                    System.out.println(packageName + " - успешно удалён!");
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        showInstalledServicesList();
    }

    private String findPackageName(String pathToService) {
        String result = null;
        String commandSearchService = String.format("dpkg-query -S %s", pathToService);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", commandSearchService);

        try {
            Process process = processBuilder.start();

            try (BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = buf.readLine();

                if (output != null && !output.isEmpty()) {
                    result = output.split(":")[0].trim();
                } else {
                    System.out.println("No package found for the selected service.");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Error: Command exited with code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred while searching for the package: " + e.getMessage());
        }

        return result;
    }

    private Path searchPathLogFile(String pathToService) throws IOException {
        if (pathToService == null || pathToService.isEmpty()) {
            throw new IllegalArgumentException("The base path should not be empty or null!");
        }

        Path serviceDir = Paths.get(pathToService);
        String nameService = serviceDir.getFileName().toString();
        Path logDir = serviceDir.resolve(Constants.NAME_DIR_LOGS);

        // Ищем директорию logs
        if (!Files.isDirectory(logDir)) {
            SystemInfo.showInfoMessageWithTitle("Not found!", "Directory 'logs' doesn't exist!");
            throw new IOException("The 'logs' folder was not found in the directory: " + serviceDir);
        }

        // Ищем файл server.log
        Path logFile = logDir.resolve(nameService + ".log");
        if (!Files.exists(logFile)) {
            SystemInfo.showInfoMessageWithTitle("Not found!", "File " + nameService + ".log doesn't exist!");
            throw new IOException("File " + nameService + ".log doesn't found in directory: " + logDir);
        }

        return logFile;
    }

    private String getSelectedService() {
        String selectedService = installedServicesListView.getSelectionModel().getSelectedItem();

        if (selectedService == null || selectedService.isEmpty()) {
            logger.warn("No service selected. Please select a service to view its logs.");
            SystemInfo.showWaringMessage("First you have to choose a service!");
            return null;
        }

        return selectedService;
    }

    private boolean manageService(String packageName, String action) {
        String command = String.format("systemctl %s %s", action, packageName);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);

        try {
            Process process = processBuilder.start();
            if (process.waitFor() == 0) {
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

//    private void executeCommand(String command) {
//        // Разбить команду на части
//        String[] commandParts = command.split(" ");
//
//        try {
//            // Создать процесс
//            ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
//            processBuilder.redirectErrorStream(true); // Перенаправить ошибки в стандартный поток вывода
//            Process process = processBuilder.start();
//
//            // Чтение вывода процесса
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String finalLine = line;
//                // Обновление интерфейса должно происходить в JavaFX Application Thread
//                javafx.application.Platform.runLater(() -> outputArea.appendText(finalLine + "\n"));
//            }
//
//            // Дождаться завершения процесса
//            int exitCode = process.waitFor();
//            javafx.application.Platform.runLater(() -> outputArea.appendText("Процесс завершён с кодом: " + exitCode + "\n"));
//
//        } catch (Exception e) {
//            javafx.application.Platform.runLater(() -> outputArea.appendText("Ошибка: " + e.getMessage() + "\n"));
//        }
//    }


}
