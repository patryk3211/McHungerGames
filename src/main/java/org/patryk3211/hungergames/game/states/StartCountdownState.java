package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;

public class StartCountdownState extends GameStateHandler {
    private static final Style WHITE = Style.style(TextColor.color(255, 255, 255));

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
}
