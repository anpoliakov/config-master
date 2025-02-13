package by.anpoliakov.utils;

import by.anpoliakov.controllers.TouchScreenCalibrationWithMatrixController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemCommandExecutor {
    private static final String DEVICE_SEARCH_REGEX = "\\s*↳\\s(.+?)\\s+id=";
    private static final Logger logger = LogManager.getLogger(TouchScreenCalibrationWithMatrixController.class);

    /**
     * @return Список имён подключенных устройств ввода (мышка, тач, клавиатура и тд)
     * **/
    public static List<String> getInputDeviceNames() {
        ProcessBuilder processBuilder = new ProcessBuilder("xinput", "list");
        List<String> nameDevices = new ArrayList();

        try {
            Process process = processBuilder.start();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            Pattern pattern = Pattern.compile(DEVICE_SEARCH_REGEX);
            String line;

            while ((line = buffReader.readLine()) != null) {
                if (line.contains("Virtual core pointer") || line.startsWith("⎜")) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        nameDevices.add(matcher.group(1));
                    }
                }
            }
            process.waitFor();
        } catch (IOException e) {
            logger.error("IOException during execution of getInputDeviceNames() method");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return nameDevices;
    }

    public static String getPropertiesDevice(String inputDeviceName) {
        ProcessBuilder processBuilder = new ProcessBuilder("xinput", "list-props", inputDeviceName);
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
            logger.error("Error when getting the properties of the input device - [" + inputDeviceName + "]");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return builder.toString();
    }
}
