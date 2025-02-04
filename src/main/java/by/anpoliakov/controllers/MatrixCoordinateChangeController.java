package by.anpoliakov.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MatrixCoordinateChangeController {
    @FXML
    private TextField textField1;

    @FXML
    private TextField textField2;

    @FXML
    private TextField textField3;

    @FXML
    private TextField textField4;

    @FXML
    private TextField textField5;

    @FXML
    private TextField textField6;

    @FXML
    private TextField textField7;

    @FXML
    private TextField textField8;

    @FXML
    private TextField textField9;

    private String[] values;

    public void setValues(String[] values) {
        if (values != null && values.length == 9) {
            textField1.setText(values[0]);
            textField2.setText(values[1]);
            textField3.setText(values[2]);
            textField4.setText(values[3]);
            textField5.setText(values[4]);
            textField6.setText(values[5]);
            textField7.setText(values[6]);
            textField8.setText(values[7]);
            textField9.setText(values[8]);
            this.values = values;
        }
    }

    @FXML
    private void handleAcceptButton() {
        values[0] = textField1.getText().trim();
        values[1] = textField2.getText().trim();
        values[2] = textField3.getText().trim();
        values[3] = textField4.getText().trim();
        values[4] = textField5.getText().trim();
        values[5] = textField6.getText().trim();
        values[6] = textField7.getText().trim();
        values[7] = textField8.getText().trim();
        values[8] = textField9.getText().trim();

        closeStage();
    }

    @FXML
    private void handleCancelButton() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) textField1.getScene().getWindow();
        stage.close();
    }

    public String[] getUpdatedCoordinates() {
        return values;
    }
}
