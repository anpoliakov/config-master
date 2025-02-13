package by.anpoliakov;

import by.anpoliakov.utils.PropertiesManager;
import by.anpoliakov.utils.SystemInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * @author Andrew Poliakov
 * @version 1.0
 */
public class Runner extends Application {
    private static final Logger logger = LogManager.getLogger(Runner.class);
    private static final byte NUM_REQUIRED_PARAMETERS = 1;

    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();

        validateParameters(args);
        String configFilePath = args.get(0);
        PropertiesManager.initializeInstance(configFilePath);
        if (SystemInfo.isLinux()) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
                primaryStage.setScene(new Scene(root));
                primaryStage.setTitle("Config Master");
                primaryStage.setMinWidth(350);
                primaryStage.setMinHeight(600);
                primaryStage.setResizable(false);
                primaryStage.centerOnScreen();
                primaryStage.show();
            } catch (IOException e) {
                throw new RuntimeException("Error during FXML loading!");
            }
        } else {
            System.out.println("ERROR: This program is designed to run on debian systems!");
        }
    }

    public void validateParameters(List<String> args) {
        if (args.isEmpty() || args.size() < NUM_REQUIRED_PARAMETERS) {
            logger.error("Not enough parameters to run the application!");
            throw new IllegalArgumentException("Not enough parameters to run the application!");
        }

        String configFilePath = args.get(0).trim();
        if(configFilePath == null || configFilePath.isEmpty()){
            throw new IllegalArgumentException("Not correct parameters!");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
