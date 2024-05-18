package org.patryk3211.hungergames.game.states;

import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.List;

public class MapPickState extends GameStateHandler {
    @Override
    public void onEntry() {
        if(manager.selectedMap == null) {
            HungerGamesPlugin.LOG.info("Game start triggered, picking map");
            final List<MapConfig> maps = Configuration.getMaps();
            manager.setMap(maps.get(manager.random.nextInt(maps.size())));
        } else {
            HungerGamesPlugin.LOG.info("Game start triggered, with map '" + manager.selectedMap.getName() + "'");
            manager.setMap(manager.selectedMap);
        }
        manager.nextState(GameState.Teleport);
    }

    @Override
    public void tick() {

    }
}
