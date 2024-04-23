package org.patryk3211.hungergames.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BoundingBox;
import org.patryk3211.hungergames.HungerGamesPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    private BoundingBox boundingBox;
    private Location center;
    private final List<Location> spawnLocations = new ArrayList<>();

    private MapChests chests;

    public MapConfig(File file) {
        this.file = YamlConfiguration.loadConfiguration(file);
        this.name = file.getName();
    }

    public double genericNumeric(List<?> list, int index) {
        Object obj = list.get(index);
        if(obj instanceof Double)
            return (Double) obj;
        if(obj instanceof Float)
            return (Float) obj;
        if(obj instanceof Integer)
            return (Integer) obj;
        return Double.NaN;
    }

    public boolean process(World overworld) {
        List<Float> startPosList = this.file.getFloatList(MAP_BB_START);
        if (startPosList.size() != 3) {
            HungerGamesPlugin.LOG.error(MAP_BB_START + " has an invalid length");
            return false;
        }
        this.startPos = new Location(overworld, startPosList.get(0), startPosList.get(1), startPosList.get(2));

        List<Float> endPosList = this.file.getFloatList(MAP_BB_END);
        if (endPosList.size() != 3) {
            HungerGamesPlugin.LOG.error(MAP_BB_END + " has an invalid length");
            return false;
        }
        this.endPos = new Location(overworld, endPosList.get(0), endPosList.get(1), endPosList.get(2));
        this.boundingBox = new BoundingBox(startPos.x(), startPos.y(), startPos.z(), endPos.x(), endPos.y(), endPos.z());

        if(this.startPos.x() > this.endPos.x())
            HungerGamesPlugin.LOG.warn("Map start position x coordinate bigger than end coordinate");
        if(this.startPos.y() > this.endPos.y())
            HungerGamesPlugin.LOG.warn("Map start position y coordinate bigger than end coordinate");
        if(this.startPos.z() > this.endPos.z())
            HungerGamesPlugin.LOG.warn("Map start position z coordinate bigger than end coordinate");

        List<Float> centerList = this.file.getFloatList(MAP_CENTER);
        if (centerList.size() != 3) {
            HungerGamesPlugin.LOG.error(MAP_CENTER + " has an invalid length");
            return false;
        }
        this.center = new Location(overworld, centerList.get(0), centerList.get(1), centerList.get(2));

        double centerX = this.center.x();
        double centerZ = this.center.z();
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

            if (genericList.size() == 3) {
                double x = genericNumeric(genericList, 0);
                double y = genericNumeric(genericList, 1);
                double z = genericNumeric(genericList, 2);
                double yaw = Math.atan2(x - centerX, centerZ - z);
                yaw = Math.toDegrees((yaw + Math.PI * 2) % (Math.PI * 2));
                this.spawnLocations.add(new Location(overworld, x, y, z, (float) yaw, 0.0f));
                continue;
            }

            HungerGamesPlugin.LOG.error("Spawn locations must be defined by 3 integers");
            return false;
        }

        Location diff = this.endPos.clone().subtract(this.startPos);
        HungerGamesPlugin.LOG.info("Loaded map '" + this.name + "', dimensions (" + diff.getBlockX() + ", " + diff.getBlockY() + ", " + diff.getBlockZ() + ") with spawn locations:");
        int index = 0;
        for(final Location loc : this.spawnLocations) {
            HungerGamesPlugin.LOG.info((++index) + ": x = " + loc.getBlockX() + ", y = " + loc.getBlockY() + ", z = " + loc.getBlockZ() + ", angle = " + loc.getYaw());
        }

        return true;
    }

    public void findChests(World world) {
        chests = new MapChests(MapConfig.this, world);
    }

    public String getName() {
        return name;
    }

    public Location getStartPos() {
        return startPos;
    }

    public Location getEndPos() {
        return endPos;
    }

    public Location getCenter() {
        return center;
    }

    public boolean isInMap(Location location) {
        return boundingBox.contains(location.x(), location.y(), location.z());
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }

    public MapChests getChests() {
        return chests;
    }
}
