package org.patryk3211.hungergames.map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;

import java.util.LinkedList;
import java.util.List;

public class MapChests {
    private final List<BlockState> chests = new LinkedList<>();

    public MapChests(MapConfig map, World world) {
        Location start = map.getStartPos();
        Location end = map.getEndPos();

        for(int chunkX = start.getBlockX() / 16; chunkX < end.getBlockZ() / 16; ++chunkX) {
            for(int chunkZ = start.getBlockZ() / 16; chunkZ < end.getBlockZ() / 16; ++chunkZ) {
                // Przejdź przez każdy chunk mapy i znajdź wszystkie skrzynki
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            }
        }
    }
}
