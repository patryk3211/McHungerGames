package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Difficulty;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.game.ILeaderboardProvider;
import org.patryk3211.hungergames.game.TrackedPlayerData;
import org.patryk3211.hungergames.http.ws.Subscriptions;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.LinkedList;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PlayingState extends GameStateHandler implements ILeaderboardProvider {
    private boolean shieldActive;
    private int shieldLeft;
    private int mapShrinkDelay;

    private int timeTicks;
    private boolean mapShrinking;

    @Override
    public void onEntry() {
        shieldActive = true;
        shieldLeft = Configuration.getPvpDelay() * 20;
        mapShrinkDelay = Configuration.getShrinkDelay() * 20;
        manager.movementAllowed = true;
        timeTicks = 0;

        // Wypełnij wszystkie skrzynki na początku gry
        manager.getCurrentMap().getChests().refillAll();

        // Wiadomość o starcie na actionbar
        for (TrackedPlayerData value : manager.players()) {
            if (value.playerInstance == null)
                continue;
            value.playerInstance.sendActionBar(Component.text("Start!", GOLD));
            Subscriptions.notifyTracked(value);
        }

        manager.world.setDifficulty(Difficulty.HARD);

        mapShrinking = false;

        manager.leaderboard.addProvider(100, this);
    }

    @Override
    public void onLeave() {
        manager.leaderboard.removeProvider(100);
    }

    @Override
    public void tick() {
        if(shieldActive) {
            if (shieldLeft == 0) {
                // Aktywuj PvP
                manager.server.sendMessage(Component.text("Koniec ochrony przed walką", GRAY));
                manager.pvpEnabled = true;
                shieldActive = false;
            } else {
                if(shieldLeft % (20 * 5) == 0) {
                    // Wiadomości co 5 sekund (100 ticków)
                    manager.server.sendMessage(Component.text("Pozostało " + (shieldLeft / 20) + " sekund ochrony", GRAY));
                }
            }
            --shieldLeft;
        } else if(!mapShrinking) {
            if(mapShrinkDelay == 0) {
                // Zacznij zmniejszać mapę
                MapConfig map = manager.getCurrentMap();
                manager.border.setSize(map.getShrunkSize(), map.getShrinkTime());
                mapShrinking = true;
                manager.server.sendMessage(Component.text("Mapa zaczyna się zmniejszać", GRAY));
            } else {
                if(mapShrinkDelay % (20 * 30) == 0) {
                    // Wiadomość co 30 sekund
                    manager.server.sendMessage(Component.text("Mapa zacznie się zmniejszać za " + (mapShrinkDelay / 20) + " sekund", GRAY));
                }
            }
            --mapShrinkDelay;
        }

        if(manager.getRemainingPlayerCount() <= 1) {
            // Został tylko jeden gracz więc gra została przez niego wygrana
            manager.nextState(GameState.End);
        }

        if(timeTicks % (15 * 20) == 0) {
            Subscriptions.notifyTime(gameTime());
        }
        ++timeTicks;
    }

    // Ile sekund trwa już gra
    public int gameTime() {
        return timeTicks / 20;
    }

    private String formattedTime() {
        int time = gameTime();
        int minutes = time / 60;
        int seconds = time % 60;
        return (minutes < 10 ? "0" + minutes : "" + minutes) + ":" + (seconds < 10 ? "0" + seconds : "" + seconds);
    }

    @Override
    public @Nullable Component[] lines() {
        List<Component> lines = new LinkedList<>();
        lines.add(Component.empty());
        lines.add(Component.join(JoinConfiguration.noSeparators(),
                Component.text(" Czas gry: ", GRAY),
                Component.text(formattedTime(), RED)
        ));
        lines.add(Component.join(JoinConfiguration.noSeparators(),
                Component.text(" Pozostało ", GRAY),
                Component.text(manager.getRemainingPlayerCount(), RED),
                Component.text(" graczy", GRAY)
        ));
        lines.add(Component.join(JoinConfiguration.noSeparators(),
                Component.text(" Rozmiar mapy: ", GRAY),
                Component.text(Math.floor(manager.border.getSize()*10)/10, mapShrinking ? RED : GREEN)
        ));
        if(shieldActive) {
            lines.add(Component.join(JoinConfiguration.noSeparators(),
                    Component.text(" Koniec ochrony za ", GRAY),
                    Component.text(shieldLeft / 20, RED)
            ));
        }
        return lines.toArray(new Component[0]);
    }
}
