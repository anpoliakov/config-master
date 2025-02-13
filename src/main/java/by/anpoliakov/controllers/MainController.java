package by.anpoliakov.controllers;

import by.anpoliakov.utils.PropertiesManager;
import by.anpoliakov.utils.SystemInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);
    private static final PropertiesManager propManager = PropertiesManager.getInstance();
    private static final String NAME_PARAM_FREQUENCY = "check.frequency";

    @FXML
    private CheckBox internetCheckBox;

    @FXML
    private CheckBox wgCheckBox;

    @FXML
    private void initialize() {
        checkInternetAndWgStatus();
    }

    private void checkInternetAndWgStatus() {
        Thread thread = new Thread(() -> {
            while (true) {
                boolean isInternetAvailable = SystemInfo.isInternetAvailable();
                boolean isWireGuardAvailable = SystemInfo.isWireGuardAvailable();
                Platform.runLater(() -> {
                    internetCheckBox.setSelected(isInternetAvailable);
                    wgCheckBox.setSelected(isWireGuardAvailable);
                });

                try {
                    int frequencyChecking = Integer.parseInt(propManager.getProperty(NAME_PARAM_FREQUENCY));
                    Thread.sleep(frequencyChecking);
                } catch (NumberFormatException e){
                    logger.error("Error in the parameter of key [" + NAME_PARAM_FREQUENCY + "].");
                } catch (InterruptedException e) {
                    logger.warn("Stopping the thread on analyzing internet and WireGuard availability");
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void showWindowCalibrationWithMatrix() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TouchScreenCalibrationWithMatrix.fxml"));
        try {
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("List Devices");
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void showWindowCalibrationWithTethering() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TouchScreenCalibrationWithTethering.fxml"));
        try {
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("List Devices");
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void showInstalledServicesWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InstalledServices.fxml"));
        try {
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Installed services");
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(550);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
