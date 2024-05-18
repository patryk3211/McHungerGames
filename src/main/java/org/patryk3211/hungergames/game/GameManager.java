package org.patryk3211.hungergames.game;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.http.ws.Subscriptions;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.*;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class GameManager implements Listener {
    private GameState currentState;
    private MapConfig currentMap;

    public MapConfig selectedMap;

    private final Queue<Runnable> actionQueue = new LinkedList<>();
    private final Map<UUID, TrackedPlayerData> trackedPlayers = new HashMap<>();
    public final Random random;
    public final Server server;
    public final WorldBorder border;
    public final World world;

    public final Leaderboard leaderboard;

    public int onlineCount = 0;

    private int actionBarTime = 0;
    private int sidebarTime = 0;

    public boolean pvpEnabled;
    public boolean movementAllowed;

    public GameManager(Server server) {
        currentMap = null;
        random = new Random();
        pvpEnabled = false;
        movementAllowed = true;
        selectedMap = null;

        this.server = server;
        this.world = server.getWorlds().get(0);
        this.border = world.getWorldBorder();
        this.leaderboard = new Leaderboard(server);

        for (Player player : world.getPlayers()) {
            if(player.isOp())
                continue;
            TrackedPlayerData data = new TrackedPlayerData(player, player.getName());
            trackedPlayers.put(player.getUniqueId(), data);
            leaderboard.showTo(player, data);
            onlineCount++;
        }
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
                if(currentState != null)
                    currentState.stateManager.onLeave();
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

        if(++sidebarTime == 10) {
            for(TrackedPlayerData value : trackedPlayers.values()) {
                if(value.playerInstance == null)
                    continue;
                leaderboard.updateScoreboard(value);
            }
            sidebarTime = 0;
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

    public void setBorderMax() {
        MapConfig map = getCurrentMap();

        double cX = map.getCenter().x();
        double cZ = map.getCenter().z();
        border.setCenter(cX, cZ);

        double sX = map.getStartPos().x();
        double eX = map.getEndPos().x();
        double maxDistX = Math.max(Math.abs(cX - sX), Math.abs(cX - eX));

        double sZ = map.getStartPos().z();
        double eZ = map.getEndPos().z();
        double maxDistZ = Math.max(Math.abs(cZ - sZ), Math.abs(cZ - eZ));

        double maxDist = Math.max(maxDistX, maxDistZ);
        border.setSize(maxDist * 2);

        border.setWarningDistance(5);
        border.setDamageBuffer(1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            // Tylko zwykli gracze są przenoszeni do odpowiednich miejsc
            if(currentState == GameState.Waiting || currentMap == null) {
                // Tylko podczas oczekiwania gracze mogą dołączać do gry
                Location loc = Configuration.getSpawnLocation();
                loc.setWorld(world);
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
            leaderboard.showTo(player, data);
            ++onlineCount;
            Subscriptions.notifyTracked(data);
            Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
        }
    }

    public static void dropPlayerInventory(Player player) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if(itemStack == null)
                continue;
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            player.getInventory().removeItem(itemStack);
        }
        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if(itemStack == null)
                continue;
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            player.getInventory().removeItem(itemStack);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            TrackedPlayerData data = trackedPlayers.get(player.getUniqueId());
            if (currentState == GameState.Waiting) {
                if(data != null) {
                    data.playerInstance = null;
                    Subscriptions.notifyTracked(data);
                }
            } else {
                if(player.getGameMode() == GameMode.SURVIVAL) {
                    // Gracz wyszedł podczas gry
                    server.sendMessage(Component.text("Gracz " + player.getName() + " został wyeliminowany za wyjście z gry"));
                    dropPlayerInventory(player);
                    if (data != null) {
                        data.addDeath();
                        data.playerInstance = null;
                    }
                }
            }
            --onlineCount;
            Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
            if(data != null)
                Subscriptions.notifyTracked(data);
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
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            // Zakazujemy stawiania bloków dla zwykłych graczy
            player.sendMessage("Nie możesz stawiać bloków");
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
    public void onPlayerAnyDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player) {
            if(player.isOp())
                return;
            if(event.getFinalDamage() > player.getHealth()) {
                event.setCancelled(true);

                if(currentState == GameState.Waiting) {
                    // Gdyby gracz jakoś umarł na spawnie
                    player.setGameMode(GameMode.SURVIVAL);
                    Location loc = Configuration.getSpawnLocation();
                    loc.setWorld(world);
                    player.teleport(loc);
                } else {
                    dropPlayerInventory(player);
                    player.setGameMode(GameMode.SPECTATOR);
                    try {
                        TrackedPlayerData data = trackedPlayers.get(player.getUniqueId());
                        data.addDeath();
                        Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
                        if (event.getDamageSource().getCausingEntity() instanceof Player damager) {
                            server.sendMessage(Component.text("Gracz " + player.getName() + " został wyeliminowany przez " + damager.getName()));
                            trackedPlayers.get(damager.getUniqueId()).addKill();
                        } else {
                            server.sendMessage(Component.text("Gracz " + player.getName() + " został wyeliminowany"));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
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

    public void clearTrackedPlayers() {
        trackedPlayers.clear();
        Subscriptions.notifyTrackedReset();
        onlineCount = 0;

        // Tworzy nowe dane dla graczy online
        for (Player player : world.getPlayers()) {
            if(player.isOp())
                continue;
            TrackedPlayerData data = new TrackedPlayerData(player, player.getName());
            trackedPlayers.put(player.getUniqueId(), data);
            deferredAction(() -> leaderboard.showTo(player, data));
            onlineCount++;
            Subscriptions.notifyTracked(data);
        }

        Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
    }

    public boolean stopGame() {
        if(currentState == GameState.Waiting)
            return false;

        deferredAction(() -> {
            for (TrackedPlayerData playerData : trackedPlayers.values()) {
                if (playerData.playerInstance != null) {
                    Player player = playerData.playerInstance;
                    Location loc = Configuration.getSpawnLocation();
                    loc.setWorld(world);
                    player.teleport(loc);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();
                    player.clearActivePotionEffects();
                    player.setArrowsInBody(0, true);
                    Subscriptions.notifyTracked(playerData);
                }
            }
            server.sendMessage(Component.text("Gra została zatrzymana przez administratora", RED));
            nextState(GameState.Waiting);
            Subscriptions.notifyTimeStop();
            Subscriptions.notifyCount(onlineCount, getRemainingPlayerCount());
        });
        return true;
    }
}
