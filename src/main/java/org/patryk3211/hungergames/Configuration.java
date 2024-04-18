package org.patryk3211.hungergames;

import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {
    private static FileConfiguration configuration = null;

    /* Definicje nazw kluczy w konfiguracji (pozwala to uniknąć tzw. "magicznych wartości") */
    private static final String HTTP_PORT_PATH = "http_port";
    private static final String HTTP_USER_PATH = "user";
    private static final String HTTP_PASSWORD_PATH = "password";
    private static final String HTTP_SESSION_TIMEOUT = "session_timeout";

    public static void init(FileConfiguration config) {
        // Definicja domyślnych wartości konfiguracji
        config.addDefault(HTTP_PORT_PATH, 25580);
        config.addDefault(HTTP_USER_PATH, "admin");
        config.addDefault(HTTP_PASSWORD_PATH, "1");
        config.addDefault(HTTP_SESSION_TIMEOUT, 30);

        configuration = config;
    }

    /* -----===== Funkcje dające dostęp do konfiguracji =====----- */
    public static int getHttpPort() {
        return configuration.getInt(HTTP_PORT_PATH);
    }

    public static String getHttpUser() {
        return configuration.getString(HTTP_USER_PATH);
    }

    public static String getHttpPassword() {
        return configuration.getString(HTTP_PASSWORD_PATH);
    }

    public static int getHttpSessionTimeout() {
        return configuration.getInt(HTTP_SESSION_TIMEOUT);
    }
}
