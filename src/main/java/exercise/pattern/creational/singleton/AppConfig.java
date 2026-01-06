package exercise.pattern.creational.singleton;

import java.util.HashMap;
import java.util.Map;

public class AppConfig {
    private final Map<String, String> config = new HashMap<>();


    private AppConfig() {
    }

    public static AppConfig getInstance() {
        final AppConfig INSTANCE = new AppConfig();
        return INSTANCE;
    }

    public String getString(String key) {
        return "";
    }

    public int getInt(String key) {
        return 0;
    }

    public void reload() { /* ??? */ }
}
