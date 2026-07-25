package config;

import io.github.cdimascio.dotenv.Dotenv;

public final class TestConfig {
    public static final String BASE_URL = "https://cloud-api.yandex.net/v1";
    public static final String TOKEN;
    public static final int OPERATION_TIMEOUT_SECONDS = 60;
    public static final int OPERATION_POLL_INTERVAL_SECONDS = 1;

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        String fromDotenv = dotenv.get("YANDEX_DISK_TOKEN");
        String fromEnv = System.getenv("YANDEX_DISK_TOKEN");
        TOKEN = (fromDotenv != null && !fromDotenv.isEmpty()) ? fromDotenv : fromEnv;

        if (TOKEN == null || TOKEN.isEmpty()) {
            throw new IllegalStateException(
                    "YANDEX_DISK_TOKEN не найден");
        }
    }

    private TestConfig() {}
}