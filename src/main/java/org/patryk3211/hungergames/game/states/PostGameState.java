package org.patryk3211.hungergames.game.states;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.game.TrackedPlayerData;

import java.util.Collection;

public class PostGameState extends GameStateHandler {
    @Override
    public void onEntry() {
        // Usuwa wszystkie itemy leżące na ziemi
        Collection<Entity> items = manager.world.getEntitiesByClasses(Item.class, Arrow.class, Firework.class);
        items.forEach(Entity::remove);

        // Przenosimy graczy na spawn
        Location loc = Configuration.getSpawnLocation();
        loc.setWorld(manager.world);
        for (TrackedPlayerData player : manager.players()) {
            if(player.playerInstance != null) {
                player.playerInstance.teleport(loc);
                player.playerInstance.setGameMode(GameMode.SURVIVAL);
                player.playerInstance.getInventory().clear();
                player.playerInstance.clearActivePotionEffects();
                player.playerInstance.setArrowsInBody(0, true);
            }
        }

        // Przechodzimy do stanu oczekiwania
        manager.nextState(GameState.Waiting);
    }

    @Override
    public void tick() {

    }
}
