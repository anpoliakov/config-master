package by.anpoliakov.controllers;

import by.anpoliakov.utils.AutoStartupScriptManager;
import by.anpoliakov.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static by.anpoliakov.utils.AutoStartupScriptManager.createAutoStartupScript;

/**
 * Класс для работы с окном по переворачиванию touch screen
 **/
public class TouchScreenCalibrationWithMatrixController {
    private static final Logger logger = LogManager.getLogger(TouchScreenCalibrationWithMatrixController.class);
    private static final String DEVICE_SEARCH_REGEX = "\\s*↳\\s(.+?)\\s+id=";
    private static final String DISPLAY_SEARCH_REGEX = "(.+)\\s+connected\\s+.*";
    @FXML
    private ListView<String> deviceNames;
    @FXML
    private TextArea devicePropertiesArea;
    private String selectedDevice;

    @FXML
    private void initialize() {
        List<String> deviceNamesList = getDeviceNames();

        if (!deviceNamesList.isEmpty()) {
            deviceNames.getItems().addAll(deviceNamesList);
            deviceNames.getSelectionModel().select(0);

            this.selectedDevice = deviceNames.getSelectionModel().getSelectedItem();
            String propertiesDevice = getPropertiesDevice(this.selectedDevice);
            devicePropertiesArea.setText(propertiesDevice);

            deviceNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedDevice = newValue;
                    devicePropertiesArea.setText(getPropertiesDevice(this.selectedDevice));
                }
            });
        } else {
            logger.error("Your application does not have input devices!");

        }
    }

    private List<String> getDeviceNames() {
        ProcessBuilder processBuilder = new ProcessBuilder("xinput", "list");
        List<String> devices = new ArrayList();

        try {
            Process process = processBuilder.start();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            Pattern pattern = Pattern.compile(DEVICE_SEARCH_REGEX);
            String line;

            while ((line = buffReader.readLine()) != null) {
                if (line.contains("Virtual core pointer") || line.startsWith("⎜")) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        devices.add(matcher.group(1));
                    }
                }
            }
            process.waitFor();
        } catch (IOException e) {
            logger.error("IOException during execution of getDeviceNames() method");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return devices;
    }

    @FXML
    private void turnRightTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_RIGHT_ROTATION);
        devicePropertiesArea.setText(getPropertiesDevice(selectedDevice));
    }

    @FXML
    private void turnLeftTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_LEFT_ROTATION);
        devicePropertiesArea.setText(getPropertiesDevice(selectedDevice));
    }

    @FXML
    private void inversionTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_INVERSION);
        devicePropertiesArea.setText(getPropertiesDevice(selectedDevice));
    }

    @FXML
    private void setDefaultTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_DEFAULT);
        devicePropertiesArea.setText(getPropertiesDevice(selectedDevice));
    }

    @FXML
    private void editSettingsTouchScreen() {
        String[] currentMatrix = getCoordinateMatrix(this.selectedDevice);

        if (currentMatrix != null && currentMatrix.length == Constants.MATRIX_SIZE) {
            String[] newMatrix = openMatrixCoordinateEditor(this.selectedDevice, currentMatrix.clone());

            if (!Arrays.equals(currentMatrix, newMatrix) && newMatrix != null) {
                applyCoordinateMatrix(this.selectedDevice, newMatrix);
                devicePropertiesArea.setText(getPropertiesDevice(this.selectedDevice));
            } else {
                System.out.println("Изменения в координатах не производились.");
            }
        } else {
            devicePropertiesArea.setText("Couldn't get the current coordinate value of the selected device");
        }
    }

    @FXML
    private void deleteSettingsTouchScreen() {
        AutoStartupScriptManager.deleteAutoStartupScript(Constants.SCRIPT_NAME_FIX_TOUCH);
    }

    @FXML
    private void saveDeviceConfigToStartup() {
        String[] coordinateMatrix = getCoordinateMatrix(this.selectedDevice);

        String command = String.format(
                "sudo xinput set-prop \"%s\" --type=float \"%s\" %s",
                this.selectedDevice,
                Constants.NAME_PROPERTIES_TRANSFORMATION,
                String.join(" ", coordinateMatrix)
        );
        createAutoStartupScript(Constants.SCRIPT_NAME_FIX_TOUCH, command);
    }

    private void applyCoordinateMatrix(String deviceName, String[] values) {
        String command = String.format(
                "sudo xinput set-prop \"%s\" --type=float \"%s\" %s",
                deviceName,
                Constants.NAME_PROPERTIES_TRANSFORMATION,
                String.join(" ", values)
        );

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        try {
            Process process = processBuilder.start();
            System.out.println("Была выполнена команда: " + command);

            if (process.waitFor() != 0) {
                logger.error("Has errors in applyCoordinateMatrix()!");
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println("ERROR: " + errorLine);
                        logger.error(errorLine + "\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute command", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Process was interrupted", e);
        }
    }

    private String getPropertiesDevice(String deviceName) {
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", "xinput", "list-props", deviceName);
        StringBuilder builder = new StringBuilder();

        try {
            Process process = processBuilder.start();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = buffReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            devicePropertiesArea.setText("ERROR retrieving properties for device: " + deviceName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return builder.toString();
    }

    private String[] getCoordinateMatrix(String deviceName) {
        List<String> matrixList = new ArrayList<>();

        Pattern pattern = Pattern.compile(Constants.NAME_PROPERTIES_TRANSFORMATION + ".+\n");
        Matcher matcher = pattern.matcher(getPropertiesDevice(deviceName));

        if (matcher.find()) {
            String str = matcher.group();
            String[] parts = str.split(":")[1].trim().split(",\\s*");
            for (String part : parts) {
                double value = Double.parseDouble(part.trim());
                matrixList.add(value == (int) value ? String.valueOf((int) value) : String.valueOf(value));
            }
        } else {
            logger.error("Error while reading the coordinates of the matrix!");
        }

        return matrixList.toArray(new String[0]);
    }

    private String[] openMatrixCoordinateEditor(String selectedDevice, String[] matrix) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MatrixCoordinateChange.fxml"));

        try {
            Parent root = loader.load();
            MatrixCoordinateChangeController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Edit matrix - " + selectedDevice);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setMinHeight(300);
            stage.setMinWidth(800);
            stage.setScene(new Scene(root));
            controller.setValues(matrix);
            stage.showAndWait();

            return controller.getUpdatedCoordinates();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MatrixCoordinateChange.fxml", e);
        }
    }

    /**
     * Методы для работы с тачем по 2-му способу (через привязку тача к конкретному дисплею)
     **/
    @FXML
    private void setBind() {
//        String command = String.format("xinput --map-to-output \"%s\" %s",
//                nameInputDevice,
//                nameHDMI);

        
        getExistDisplayNames();
    }

    @FXML
    private void deleteBind() {

    }

    private List<String> getExistDisplayNames() {
        ProcessBuilder processBuilder = new ProcessBuilder("xrandr");
        Pattern pattern = Pattern.compile(DISPLAY_SEARCH_REGEX);
        List<String> displayNames = new ArrayList<>();

        try {
            Process process = processBuilder.start();

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        displayNames.add(matcher.group(1));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return displayNames;
    }
}
