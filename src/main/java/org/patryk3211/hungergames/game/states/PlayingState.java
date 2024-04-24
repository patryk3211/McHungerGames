package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Difficulty;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.game.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PlayingState extends GameStateHandler implements ILeaderboardProvider {
    private boolean shieldActive;
    private int shieldLeft;

    private int timeTicks;
    private boolean mapShrinking;
    private int mapSize;

    @Override
    public void onEntry() {
        shieldActive = true;
        shieldLeft = Configuration.getPvpDelay() * 20;
        manager.movementAllowed = true;
        timeTicks = 0;

        // Wypełnij wszystkie skrzynki na początku gry
        manager.getCurrentMap().getChests().refillAll();

        // Wiadomość o starcie na actionbar
        for (TrackedPlayerData value : manager.players()) {
            if (value.playerInstance == null)
                continue;
            value.playerInstance.sendActionBar(Component.text("Start!", WHITE));
        }

        manager.world.setDifficulty(Difficulty.HARD);

        mapSize = (int) manager.border.getSize();
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
                manager.server.sendMessage(Component.text("Koniec ochrony przed walką", WHITE));
                manager.pvpEnabled = true;
                shieldActive = false;
            } else {
                if(shieldLeft % (20 * 5) == 0) {
                    // Wiadomości co 5 sekund (100 ticków)
                    manager.server.sendMessage(Component.text("Pozostało " + (shieldLeft / 20) + " sekund ochrony", WHITE));
                }
            }
            --shieldLeft;
        }

//        if(manager.getRemainingPlayerCount() <= 1) {
//            // Został tylko jeden gracz więc gra została przez niego wygrana
//            TrackedPlayerData winnerData = manager.players().stream().filter(data -> data.getStatus() == PlayerStatus.Alive).toList().get(0);
//            manager.server.sendMessage(Component.text("Gre wygrał " + winnerData.name));
//            manager.nextState(GameState.PostGame);
//        }

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
                Component.text(mapSize, mapShrinking ? RED : GREEN)
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
