package org.patryk3211.hungergames;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.hungergames.loot.LootConfig;
import org.patryk3211.hungergames.map.MapConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

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
    private static final String SHRINK_DELAY = "shrink_delay";

    private static final List<MapConfig> maps = new ArrayList<>();

    private static LootConfig loot;

    public static void init(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        World overworld = plugin.getServer().getWorlds().get(0);

        // Zapisuje plik z JAR do folderu plugins
        plugin.saveDefaultConfig();

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

        File itemsFile = new File(Path.of(dataDirectory.getPath(), "items.yml").toUri());
        if(!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        loot = new LootConfig(itemsFile);

        File credentialsFile = new File(Path.of(dataDirectory.getPath(), "credentials.yml").toUri());
        if(!credentialsFile.exists()) {
            plugin.saveResource("credentials.yml", false);
        }
    }

    /* -----===== Funkcje dające dostęp do konfiguracji =====----- */
    public static int getHttpPort() {
        return configuration.getInt(HTTP_PORT_PATH);
    }

    @Deprecated
    public static String getHttpUser() {
        return configuration.getString(HTTP_USER_PATH);
    }

    @Deprecated
    public static String getHttpPassword() {
        return configuration.getString(HTTP_PASSWORD_PATH);
    }

    public static int getHttpSessionTimeout() {
        return configuration.getInt(HTTP_SESSION_TIMEOUT);
    }

    public static @NotNull Location getSpawnLocation() {
        List<Float> floats = configuration.getFloatList(PLAYER_SPAWN);
        if(floats.isEmpty())
            throw new IllegalStateException("Player spawn not defined in configuration file");
        return new Location(null, floats.get(0), floats.get(1), floats.get(2));
    }

    public static List<MapConfig> getMaps() {
        return maps;
    }

    public static LootConfig getLoot() {
        return loot;
    }

    public static int getPvpDelay() {
        return configuration.getInt(PVP_DELAY);
    }

    public static int getShrinkDelay() {
        return configuration.getInt(SHRINK_DELAY);
    }

    public record CredentialPair(String user, String password) { }

    public static Collection<CredentialPair> loadCredentials() {
        File credentialsFile = new File(Path.of(dataDirectory.getPath(), "credentials.yml").toUri());
        if(credentialsFile.exists()) {
            YamlConfiguration file = YamlConfiguration.loadConfiguration(credentialsFile);
            List<Map<?, ?>> credentialsMap = file.getMapList("credentials");
            List<CredentialPair> credentials = new LinkedList<>();
            credentialsMap.forEach(entry -> {
                Object user = entry.get("user");
                Object password = entry.get("password");
                if(!(user instanceof String userStr) || !(password instanceof String passwordStr)) {
                    HungerGamesPlugin.LOG.warn("Credentials file contains an invalid entry");
                    return;
                }
                credentials.add(new CredentialPair(userStr, passwordStr));
            });
            return credentials;
        }
        return null;
    }
}
