package org.patryk3211.hungergames.game;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.*;

public class GameManager implements Listener {
    private GameState currentState;
    private MapConfig currentMap;

    private final Queue<Runnable> actionQueue = new LinkedList<>();
    private final Set<Player> trackedPlayers = new HashSet<>();
    public final Random random;
    public final Server server;

    public boolean pvpEnabled;

    public GameManager(Server server) {
        currentMap = null;
        random = new Random();
        pvpEnabled = false;

        this.server = server;
        nextState(GameState.Waiting);
    }

    public GameState currentState() {
        return currentState;
    }

    public void nextState(GameState state) {
        if(currentState != state) {
            // Dodajemy akcję zmiany stanu do kolejki
            actionQueue.add(() -> {
                currentState = state;
                currentState.stateManager.setManager(this);
                currentState.stateManager.onEntry();
            });
        }
    }

    private void tick() {
        while(!actionQueue.isEmpty()) {
            // Po kolei uruchamia wszystkie akcje z kolejki
            Runnable action = actionQueue.remove();
            action.run();
        }

        // Uruchamia metode tick po wszystkich zmianach stanu
        currentState.stateManager.tick();
    }

    public void setMap(MapConfig mapConfig) {
        if(currentState != GameState.PickMap) {
            HungerGamesPlugin.LOG.warn("Tried to change map during the '" + currentState.toString() + "' state");
            return;
        }
        this.currentMap = mapConfig;
    }

    public MapConfig getCurrentMap() {
        return currentMap;
    }

    public void deferredAction(Runnable action) {
        actionQueue.add(action);
    }

    public Collection<Player> players() {
        return trackedPlayers;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            // Tylko zwykli gracze są przenoszeni do odpowiednich miejsc
            if(currentState == GameState.Waiting || currentMap == null) {
                // Tylko podczas oczekiwania gracze mogą dołączać do gry
                player.teleport(Configuration.getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();

                trackedPlayers.add(player);
            } else {
                // Przy dołączaniu podczas gry, gracz jest teleportowany do środka mapy jako obserwator
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(currentMap.getCenter());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(currentState == GameState.Waiting) {
            trackedPlayers.remove(player);
        } else {
            // Gracz wyszedł podczas gry
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player && !pvpEnabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event) {
        tick();
    }
}
