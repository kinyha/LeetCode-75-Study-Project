package exercise.pattern.creational.singleton;

public class Main {
    static void main() {
        System.out.println(123);


        AppConfig config = AppConfig.getInstance();
        String dbUrl = config.getString("database.url");
        int poolSize = config.getInt("database.pool.size");

        System.out.println(config);
    }
}
