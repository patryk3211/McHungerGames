package org.patryk3211.hungergames;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
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

    private static final String PLAYER_SPAWN = "spawn_location";
    private static final String PVP_DELAY = "pvp_delay";

    private static final List<MapConfig> maps = new ArrayList<>();

    public static void init(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        World overworld = plugin.getServer().getWorlds().get(0);

        // Definicja domyślnych wartości konfiguracji
        config.addDefault(HTTP_PORT_PATH, 25580);
        config.addDefault(HTTP_USER_PATH, "admin");
        config.addDefault(HTTP_PASSWORD_PATH, "1");
        config.addDefault(HTTP_SESSION_TIMEOUT, 30);
        config.addDefault(PLAYER_SPAWN, new Location(overworld, 0, 0, 0));
        config.addDefault(PVP_DELAY, 15);
        plugin.saveConfig();

        configuration = config;
        dataDirectory = plugin.getDataFolder();

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
                    if (!mapConf.process(overworld)) {
                        HungerGamesPlugin.LOG.error("Error while processing map file '" + map.getName() + "'");
                    } else {
                        mapConf.findChests(overworld);
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

    public static Location getSpawnLocation() {
        return configuration.getLocation(PLAYER_SPAWN);
    }

    public static List<MapConfig> getMaps() {
        return maps;
    }

    public static int getPvpDelay() {
        return configuration.getInt(PVP_DELAY);
    }
}
