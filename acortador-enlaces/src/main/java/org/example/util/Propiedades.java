package org.example.util;

import org.example.App;

import java.io.IOException;
import java.util.Properties;

public abstract class Propiedades {
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(App.class.getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
