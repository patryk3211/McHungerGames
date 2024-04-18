package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.game.GameStateHandler;

public class WaitState extends GameStateHandler {
    @Override
    public @Nullable Component getHotBarTitle() {
        return Component.text("Oczekiwanie na rozpoczęcie gry", Style.style(TextColor.color(255, 255,255)));
    }

    @Override
    public void onEntry() {
        manager.pvpEnabled = false;
    }

    @Override
    public void tick() {

    }
}
