package org.patryk3211.hungergames.map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.patryk3211.hungergames.HungerGamesPlugin;

import java.util.LinkedList;
import java.util.List;

public class MapChests {
    private final List<BlockState> chests = new LinkedList<>();

    public MapChests(MapConfig map, World world) {
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
                    chests.add(entity);
                }
            }
        }

        HungerGamesPlugin.LOG.info("Found " + chests.size() + " chests for map '" + map.getName() + "'");
    }
}
