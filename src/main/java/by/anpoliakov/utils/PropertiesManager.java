package by.anpoliakov.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    private static PropertiesManager instance;
    private Properties properties;
    private String configFilePath;

    private PropertiesManager(String configFilePath) {
        this.configFilePath = configFilePath;
        properties = new Properties();
        loadProperties();
    }

    public static PropertiesManager getInstance(String configFilePath) {
        if (instance == null) {
            instance = new PropertiesManager(configFilePath);
        }
        return instance;
    }

    public static PropertiesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PropertiesManager не был инициализирован. Вызовите getInstance(String configFilePath) сначала.");
        }
        return instance;
    }

    private void loadProperties() {
        try (FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

//    public void setProperty(String key, String value) {
//        properties.setProperty(key, value);
//        try (FileOutputStream output = new FileOutputStream(configFilePath)) {
//            properties.store(output, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void updateProperty(String key, String newValue) {
        if (properties.containsKey(key)) {
            properties.setProperty(key, newValue);
            saveProperties();
        } else {
            throw new IllegalArgumentException("Ключ " + key + " не найден в конфигурации.");
        }
    }

    private void saveProperties() {
        try (FileOutputStream output = new FileOutputStream(configFilePath)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
