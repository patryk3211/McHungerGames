package org.patryk3211.hungergames.game;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public abstract class GameStateHandler {
    protected GameManager manager;

    public void setManager(GameManager manager) {
        this.manager = manager;
    }

    public @Nullable Component getHotBarTitle() {
        return null;
    }

    public void onEntry() { }
    public void onLeave() { }
    public abstract void tick();
}
