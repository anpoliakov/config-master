package by.anpoliakov.controllers;

import by.anpoliakov.utils.AutoStartupScriptManager;
import by.anpoliakov.utils.Constants;
import by.anpoliakov.utils.SystemCommandExecutor;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static by.anpoliakov.utils.AutoStartupScriptManager.createAutoStartupScript;

public class TouchScreenCalibrationWithTetheringController {
    private static final String MONITORS_SEARCH_REGEX = "(.+)\\s+connected\\s+(\\d{3,4}x\\d{3,4}).*";
    private static final Logger logger = LogManager.getLogger(TouchScreenCalibrationWithMatrixController.class);

    @FXML
    private VBox monitorRadioButtonContainer;
    @FXML
    private VBox deviceRadioButtonContainer;
    @FXML
    private TextArea devicePropertiesArea;

    private ToggleGroup monitorToggleGroup = new ToggleGroup();
    private ToggleGroup deviceToggleGroup = new ToggleGroup();
    private String selectedMonitor;
    private String selectedInputDevice;

    @FXML
    public void initialize() {
        // Пример данных для мониторов и устройств
        List<String> monitors = getConnectedMonitors();
        List<String> devices = SystemCommandExecutor.getInputDeviceNames();

        // Создание радио-кнопок для мониторов
        for (String monitor : monitors) {
            RadioButton radioButton = new RadioButton(monitor);
            radioButton.setToggleGroup(monitorToggleGroup);
            radioButton.setStyle("-fx-font-size: 14px;");
            monitorRadioButtonContainer.getChildren().add(radioButton);
        }

        // Создание радио-кнопок для устройств
        for (String device : devices) {
            RadioButton radioButton = new RadioButton(device);
            radioButton.setToggleGroup(deviceToggleGroup); // Добавляем в группу
            radioButton.setStyle("-fx-font-size: 14px;");
            deviceRadioButtonContainer.getChildren().add(radioButton); // Добавляем в контейнер
        }

        // Обработка выбора монитора
        monitorToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;
                selectedMonitor = selectedRadioButton.getText();
            }
        });

        // Обработка выбора устройства
        deviceToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;
                selectedInputDevice = selectedRadioButton.getText();
                devicePropertiesArea.setText(SystemCommandExecutor.getPropertiesDevice(selectedRadioButton.getText()));
            }
        });

        if (!monitors.isEmpty()) {
            RadioButton firstMonitorRadioButton = (RadioButton) monitorRadioButtonContainer.getChildren().get(0);
            firstMonitorRadioButton.setSelected(true);
            this.selectedMonitor = firstMonitorRadioButton.getText();
        }

        if(!devices.isEmpty()){
            RadioButton firstDeviceRadioButton = (RadioButton) deviceRadioButtonContainer.getChildren().get(0);
            firstDeviceRadioButton.setSelected(true);
            this.selectedInputDevice = firstDeviceRadioButton.getText();
        }
    }

    private List<String> getConnectedMonitors() {
        List<String> monitors = new ArrayList<>();
        Pattern pattern = Pattern.compile(MONITORS_SEARCH_REGEX);
        ProcessBuilder processBuilder = new ProcessBuilder("xrandr");

        try {
            Process process = processBuilder.start();

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        monitors.add(matcher.group(1));
                        System.out.println("Разрешение - " + matcher.group(2));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error while getting the list of monitors");
            throw new RuntimeException(e);
        }

        return monitors;
    }

    @FXML
    private void setTest(){
        String command = String.format("sudo xinput --map-to-output \"%s\" %s",
                selectedInputDevice,
                selectedMonitor);
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);

        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void saveScript(){
        String command = String.format("sudo xinput --map-to-output \"%s\" %s",
                selectedInputDevice,
                selectedMonitor);
        createAutoStartupScript(Constants.SCRIPT_NAME_FIX_TOUCH, command);
    }

    @FXML
    private void deleteScript(){
        AutoStartupScriptManager.deleteAutoStartupScript(Constants.SCRIPT_NAME_FIX_TOUCH);
    }


}
