package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.game.ILeaderboardProvider;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class StartCountdownState extends GameStateHandler implements ILeaderboardProvider {
    private int tickCount;
    private int timeLeft;

    @Override
    public @Nullable Component getHotBarTitle() {
        return Component.text("Start za " + timeLeft + " sekund", WHITE);
    }

    @Override
    public void onEntry() {
        tickCount = 0;
        timeLeft = 10;

        manager.leaderboard.addProvider(100, this);
    }

    @Override
    public void onLeave() {
        manager.leaderboard.removeProvider(100);
    }

    @Override
    public void tick() {
        if(tickCount++ == 20) {
            tickCount = 0;
            if(--timeLeft == 0) {
                manager.server.sendMessage(Component.text("Start", WHITE));
                manager.nextState(GameState.Playing);
            }
        }
    }

    @Override
    public @Nullable Component[] lines() {
        return new Component[] {
                Component.empty(),
                Component.join(JoinConfiguration.noSeparators(),
                        Component.text(" Start za ", GRAY),
                        Component.text(timeLeft, RED)
                )
        };
    }
}
