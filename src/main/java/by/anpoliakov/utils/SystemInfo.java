package by.anpoliakov.utils;

import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class SystemInfo {
    private static PropertiesManager propManager = PropertiesManager.getInstance();
    private static final String NAME_PARAM_CHECK_INTERNET = "check.internet";
    private static final String NAME_PARAM_CHECK_WG = "check.wg";

    private SystemInfo() {}

    public static boolean isInternetAvailable() {
        try (Socket socket = new Socket()) {
            String checkingIp = propManager.getProperty(NAME_PARAM_CHECK_INTERNET);
            socket.connect(new InetSocketAddress(checkingIp, 53), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isWireGuardAvailable() {
        try (Socket socket = new Socket()) {
            String checkingIp = propManager.getProperty(NAME_PARAM_CHECK_WG);
            socket.connect(new InetSocketAddress(checkingIp, 53), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static boolean isLinux() {
        return getOSName().contains("nix") || getOSName().contains("nux") || getOSName().contains("aix");
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static void showInfoMessageWithTitle(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFO");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfoMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFO");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showWaringMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("WARNING");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
