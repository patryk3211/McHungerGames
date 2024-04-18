package org.patryk3211.hungergames;

import org.bukkit.configuration.file.FileConfiguration;
import org.patryk3211.hungergames.map.MapConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private static FileConfiguration configuration = null;
    private static File dataDirectory = null;

    /* Definicje nazw kluczy w konfiguracji (pozwala to uniknąć tzw. "magicznych wartości") */
    private static final String HTTP_PORT_PATH = "http_port";
    private static final String HTTP_USER_PATH = "user";
    private static final String HTTP_PASSWORD_PATH = "password";
    private static final String HTTP_SESSION_TIMEOUT = "session_timeout";

    private static final List<MapConfig> maps = new ArrayList<>();

    public static void init(FileConfiguration config, File data) {
        // Definicja domyślnych wartości konfiguracji
        config.addDefault(HTTP_PORT_PATH, 25580);
        config.addDefault(HTTP_USER_PATH, "admin");
        config.addDefault(HTTP_PASSWORD_PATH, "1");
        config.addDefault(HTTP_SESSION_TIMEOUT, 30);

        configuration = config;
        dataDirectory = data;

        maps.clear();
        File mapsFolder = new File(Path.of(dataDirectory.getPath(), "maps").toUri());
        if (mapsFolder.exists()) {
            File[] files = mapsFolder.listFiles();
            if (files == null) {
                HungerGamesPlugin.LOG.error("Failed to read files in 'maps' folder");
                return;
            }

            for(File map : files) {
                if (map.isFile()) {
                    MapConfig mapConf = new MapConfig(map);
                    if (!mapConf.process()) {
                        HungerGamesPlugin.LOG.error("Error while processing map file '" + map.getName() + "'");
                    } else {
                        maps.add(mapConf);
                    }
                }
            }
        }

        if (maps.isEmpty()) {
            HungerGamesPlugin.LOG.warn("No maps were loaded");
        }
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
