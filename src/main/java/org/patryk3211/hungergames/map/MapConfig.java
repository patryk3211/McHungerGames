package org.patryk3211.hungergames.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.patryk3211.hungergames.HungerGamesPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapConfig {
    private static final String MAP_NAME = "name";
    private static final String MAP_BB_START = "bounding_box.start";
    private static final String MAP_BB_END = "bounding_box.end";
    private static final String MAP_SPAWN_LOCATIONS = "spawn_locations";
    private static final String MAP_CENTER = "center";
    private final FileConfiguration file;
    private String name;
    private Location startPos;
    private Location endPos;
    private Location center;
    private final List<Location> spawnLocations = new ArrayList<>();

    public MapConfig(File file) {
        this.file = YamlConfiguration.loadConfiguration(file);
        this.name = file.getName();
    }

    public boolean process() {
        List<Integer> startPosList = this.file.getIntegerList(MAP_BB_START);
        if (startPosList.size() != 3) {
            HungerGamesPlugin.LOG.error(MAP_BB_START + " has an invalid length");
            return false;
        }
        this.startPos = new Location(null, startPosList.get(0), startPosList.get(1), startPosList.get(2));
        List<Integer> endPosList = this.file.getIntegerList(MAP_BB_END);
        if (endPosList.size() != 3) {
            HungerGamesPlugin.LOG.error(MAP_BB_END + " has an invalid length");
            return false;
        }
        this.endPos = new Location(null, endPosList.get(0), endPosList.get(1), endPosList.get(2));
        List<Integer> centerList = this.file.getIntegerList(MAP_CENTER);
        if (centerList.size() != 3) {
            HungerGamesPlugin.LOG.error(MAP_CENTER + " has an invalid length");
            return false;
        }
        this.center = new Location(null, centerList.get(0), centerList.get(1), centerList.get(2));
        int centerX = this.center.getBlockX();
        int centerZ = this.center.getBlockZ();
        String confName = this.file.getString(MAP_NAME);
        if (confName == null) {
            HungerGamesPlugin.LOG.warn("Name not provided for map, using file name");
            Pattern pattern = Pattern.compile("/(\\w+)\\..+/");
            Matcher matcher = pattern.matcher(this.name);
            if (matcher.find()) {
                String matchedName = matcher.group(1);
                if (matchedName != null) {
                    this.name = matchedName;
                }
            }
        }

        List<?> spawnLocations = this.file.getList(MAP_SPAWN_LOCATIONS);
        if (spawnLocations == null) {
            HungerGamesPlugin.LOG.error("Map is missing the spawn location definitions");
            return false;
        }
        for(Object o : spawnLocations) {
            if (!(o instanceof List<?> genericList)) {
                HungerGamesPlugin.LOG.error(MAP_SPAWN_LOCATIONS + " entry has an invalid value");
                return false;
            }

            if (genericList.size() == 3 && genericList.get(0) instanceof Integer) {
                int x = (Integer) genericList.get(0);
                int y = (Integer) genericList.get(1);
                int z = (Integer) genericList.get(2);
                double yaw = Math.atan2(x - centerX, centerZ - z);
                yaw = Math.toDegrees((yaw + Math.PI * 2) % (Math.PI * 2));
                this.spawnLocations.add(new Location(null, x, y, z, (float) yaw, 0.0f));
                continue;
            }

            HungerGamesPlugin.LOG.error("Spawn locations must be defined by 3 integers");
            return false;
        }

        Location diff = this.endPos.subtract(this.startPos);
        HungerGamesPlugin.LOG.info("Loaded map '" + this.name + "', dimensions (" + diff.getBlockX() + ", " + diff.getBlockY() + ", " + diff.getBlockZ() + ") with spawn locations:");
        int index = 0;
        for(final Location loc : this.spawnLocations) {
            HungerGamesPlugin.LOG.info((++index) + ": x = " + loc.getBlockX() + ", y = " + loc.getBlockY() + ", z = " + loc.getBlockZ() + ", angle = " + loc.getYaw());
        }

        return true;
    }
}
