package org.patryk3211.hungergames.game.states;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TeleportState extends GameStateHandler {
    @Override
    public void onEntry() {
        final List<Player> playerList = new LinkedList<>(manager.players());
        Collections.shuffle(playerList, manager.random);

        final MapConfig map = manager.getCurrentMap();
        final Iterator<Location> spawns = map.getSpawnLocations().iterator();
        while(!playerList.isEmpty()) {
            final Player player = playerList.remove(0);
            final Location spawn = spawns.next();
            player.teleport(spawn);
        }

        manager.nextState(GameState.StartCountdown);
    }

    @Override
    public void tick() {

    }
}
