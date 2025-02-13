package by.anpoliakov.controllers;

import by.anpoliakov.utils.AutoStartupScriptManager;
import by.anpoliakov.utils.Constants;
import by.anpoliakov.utils.SystemCommandExecutor;
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
    @FXML
    private ListView<String> deviceNames;
    @FXML
    private TextArea devicePropertiesArea;
    private String selectedDevice;

    @FXML
    private void initialize() {
        List<String> deviceNamesList = SystemCommandExecutor.getInputDeviceNames();

        if (!deviceNamesList.isEmpty()) {
            deviceNames.getItems().addAll(deviceNamesList);
            deviceNames.getSelectionModel().select(0);

            this.selectedDevice = deviceNames.getSelectionModel().getSelectedItem();
            String propertiesDevice = SystemCommandExecutor.getPropertiesDevice(this.selectedDevice);
            devicePropertiesArea.setText(propertiesDevice);

            deviceNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedDevice = newValue;
                    devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(this.selectedDevice));
                }
            });
        } else {
            logger.error("Your application does not have input devices!");

        }
    }

    @FXML
    private void turnRightTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_RIGHT_ROTATION);
        devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(this.selectedDevice));
    }

    @FXML
    private void turnLeftTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_LEFT_ROTATION);
        devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(this.selectedDevice));
    }

    @FXML
    private void inversionTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_INVERSION);
        devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(this.selectedDevice));
    }

    @FXML
    private void setDefaultTouch() {
        applyCoordinateMatrix(this.selectedDevice, Constants.COORDINATE_DEFAULT);
        devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(this.selectedDevice));
    }

    @FXML
    private void editSettingsTouchScreen() {
        String[] currentMatrix = getCoordinateMatrix(this.selectedDevice);

        if (currentMatrix != null && currentMatrix.length == Constants.MATRIX_SIZE) {
            String[] newMatrix = openMatrixCoordinateEditor(this.selectedDevice, currentMatrix.clone());

            if (!Arrays.equals(currentMatrix, newMatrix) && newMatrix != null) {
                applyCoordinateMatrix(this.selectedDevice, newMatrix);
                devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(this.selectedDevice));
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

    private String[] getCoordinateMatrix(String deviceName) {
        List<String> matrixList = new ArrayList<>();

        Pattern pattern = Pattern.compile(Constants.NAME_PROPERTIES_TRANSFORMATION + ".+\n");
        Matcher matcher = pattern.matcher(SystemCommandExecutor.getPropertiesDevice(deviceName));

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
}
