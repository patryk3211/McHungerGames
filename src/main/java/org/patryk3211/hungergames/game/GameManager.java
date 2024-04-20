package org.patryk3211.hungergames.game;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.http.ws.Subscriptions;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.*;

public class GameManager implements Listener {
    private GameState currentState;
    private MapConfig currentMap;

    private final Queue<Runnable> actionQueue = new LinkedList<>();
    private final Map<UUID, TrackedPlayerData> trackedPlayers = new HashMap<>();
    public final Random random;
    public final Server server;

    public int onlineCount = 0;

    public int actionBarTime = 0;

    public boolean pvpEnabled;
    public boolean movementAllowed;

    public GameManager(Server server) {
        currentMap = null;
        random = new Random();
        pvpEnabled = false;
        movementAllowed = true;

        this.server = server;
        nextState(GameState.Waiting);
    }

    public GameState currentState() {
        return currentState;
    }

    public void nextState(GameState state) {
        while(state.isMetaState)
            state = state.targetState;
        final GameState actualState = state;
        if(currentState != actualState) {
            // Dodajemy akcję zmiany stanu do kolejki
            actionQueue.add(() -> {
                currentState = actualState;
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

        // Pokaż tekst nad paskiem ekwipunku
        if(++actionBarTime == 5) {
            Component title = currentState.stateManager.getHotBarTitle();
            if (title != null) {
                for (TrackedPlayerData value : trackedPlayers.values()) {
                    if (value.playerInstance == null)
                        continue;
                    value.playerInstance.sendActionBar(title);
                }
            }
            actionBarTime = 0;
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

    public Collection<TrackedPlayerData> players() {
        return trackedPlayers.values();
    }

    public int getRemainingPlayerCount() {
        if(currentState == GameState.Waiting)
            return onlineCount;
        int remaining = 0;
        for(TrackedPlayerData data : trackedPlayers.values()) {
            if(data.getStatus() == PlayerStatus.Alive)
                ++remaining;
        }
        return remaining;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            // Tylko zwykli gracze są przenoszeni do odpowiednich miejsc
            if(currentState == GameState.Waiting || currentMap == null) {
                // Tylko podczas oczekiwania gracze mogą dołączać do gry
                Location loc = Configuration.getSpawnLocation();
                loc.setWorld(server.getWorlds().get(0));
                player.teleport(loc);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
            } else {
                // Przy dołączaniu podczas gry, gracz jest teleportowany do środka mapy jako obserwator
                player.getInventory().clear();
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(currentMap.getCenter());
            }
            TrackedPlayerData data = trackedPlayers.compute(player.getUniqueId(), (key, value) -> {
                if(value == null) {
                    return new TrackedPlayerData(player, player.getName());
                } else {
                    value.playerInstance = player;
                    return value;
                }
            });
            ++onlineCount;
            Subscriptions.notifyTracked(data);
            Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            if (currentState == GameState.Waiting) {
                TrackedPlayerData data = trackedPlayers.get(player.getUniqueId());
                if(data != null) {
                    data.playerInstance = null;
                    Subscriptions.notifyTracked(data);
                }
            } else {
                // Gracz wyszedł podczas gry
            }
            --onlineCount;
            Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            // Zakazujemy niszczenia bloków dla zwykłych graczy
            player.sendMessage("Nie możesz niszczyć bloków");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player && !pvpEnabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMoved(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            if (!movementAllowed) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event) {
        tick();
    }
}
