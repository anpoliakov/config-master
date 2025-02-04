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

    /**
     * Пусть первый параметр при запуске приложения - вснгда будет properties файлом
     * его название не столь важно - если в нём будут все необходимые ключи
     **/
    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();
        validateParameters(args);
        if (SystemInfo.isLinux()) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
                primaryStage.setScene(new Scene(root));
                primaryStage.setTitle("Config Master");
                primaryStage.setMinWidth(350);
                primaryStage.setMinHeight(600);
                primaryStage.setResizable(false);
                primaryStage.centerOnScreen(); //центруем окно в середине монитора
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

        try {
            String configFilePath = args.get(0).trim();
            PropertiesManager.getInstance(configFilePath);
        } catch (IllegalArgumentException e) {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
