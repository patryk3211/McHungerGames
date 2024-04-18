package org.patryk3211.hungergames.game.states;

import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.List;

public class MapPickState extends GameStateHandler {
    @Override
    public void onEntry() {
        final List<MapConfig> maps = Configuration.getMaps();
        manager.setMap(maps.get(manager.random.nextInt(maps.size())));
        manager.nextState(GameState.Teleport);
    }

    @Override
    public void tick() {

    }
}
