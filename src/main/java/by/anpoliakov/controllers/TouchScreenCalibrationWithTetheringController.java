package by.anpoliakov.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class TouchScreenCalibrationWithTetheringController {
    @FXML
    private VBox monitorRadioButtonContainer; // Контейнер для радио-кнопок мониторов
    @FXML
    private VBox deviceRadioButtonContainer;  // Контейнер для радио-кнопок устройств
    @FXML
    private TextArea devicePropertiesArea;

    private ToggleGroup monitorToggleGroup = new ToggleGroup(); // Группа для мониторов
    private ToggleGroup deviceToggleGroup = new ToggleGroup();  // Группа для устройств

    @FXML
    public void initialize() {
        // Пример данных для мониторов и устройств
        String[] monitors = {"Монитор 1", "Монитор 2", "Монитор 3"};
        String[] devices = {"Устройство 1", "Устройство 2", "Устройство 3"};

        // Создание радио-кнопок для мониторов
        for (String monitor : monitors) {
            RadioButton radioButton = new RadioButton(monitor);
            radioButton.setToggleGroup(monitorToggleGroup); // Добавляем в группу
            radioButton.setStyle("-fx-font-size: 14px;");   // Применяем стиль
            monitorRadioButtonContainer.getChildren().add(radioButton); // Добавляем в контейнер
        }

        // Создание радио-кнопок для устройств
        for (String device : devices) {
            RadioButton radioButton = new RadioButton(device);
            radioButton.setToggleGroup(deviceToggleGroup); // Добавляем в группу
            radioButton.setStyle("-fx-font-size: 14px;");  // Применяем стиль
            deviceRadioButtonContainer.getChildren().add(radioButton); // Добавляем в контейнер
        }

        // Обработка выбора монитора
        monitorToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;
                devicePropertiesArea.setText("Выбран монитор: " + selectedRadioButton.getText());
            }
        });

        // Обработка выбора устройства
        deviceToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;
                devicePropertiesArea.setText("Выбрано устройство: " + selectedRadioButton.getText());
            }
        });
    }
}
