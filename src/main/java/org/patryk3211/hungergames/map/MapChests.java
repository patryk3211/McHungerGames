package org.patryk3211.hungergames.map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.loot.LootConfig;

import java.util.LinkedList;
import java.util.List;

public class MapChests {
    private final List<Location> chests = new LinkedList<>();
    private final World world;

    public MapChests(MapConfig map, World world) {
        this.world = world;

        Location start = map.getStartPos();
        Location end = map.getEndPos();

        int chunkStartX = start.getBlockX() / 16;
        int chunkEndX = end.getBlockX() / 16;
        if(start.getBlockX() < 0) --chunkStartX;
        if(end.getBlockX() < 0) --chunkEndX;
        int chunkStartZ = start.getBlockZ() / 16;
        int chunkEndZ = end.getBlockZ() / 16;
        if(start.getBlockZ() < 0) --chunkStartZ;
        if(end.getBlockZ() < 0) --chunkEndZ;

        for(int chunkX = chunkStartX; chunkX < chunkEndX; ++chunkX) {
            for(int chunkZ = chunkStartZ; chunkZ < chunkEndZ; ++chunkZ) {
                // Przejdź przez każdy chunk mapy i znajdź wszystkie skrzynki
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                final BlockState[] tileEntities = chunk.getTileEntities();
                for(BlockState entity : tileEntities) {
                    if(entity.getType() != Material.CHEST)
                        continue;
                    if(!map.isInMap(entity.getLocation()))
                        continue;
                    chests.add(entity.getLocation());
                }
            }
        }

        HungerGamesPlugin.LOG.info("Found " + chests.size() + " chests for map '" + map.getName() + "'");
    }

    public void refillAll() {
        LootConfig loot = Configuration.getLoot();
        for (Location chestLocation : chests) {
            BlockState state = world.getBlockState(chestLocation);
            if(state instanceof Chest chest) {
                int targetCost = HungerGamesPlugin.manager.random.nextInt(5, 16);
                chest.getBlockInventory().clear();
                loot.fillChest(chest, targetCost);
            } else {
                HungerGamesPlugin.LOG.warn("Chest found at " + chestLocation + " is no longer a chest");
            }
        }
    }
}
