package org.patryk3211.hungergames.game;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface ILeaderboardProvider {
    @Nullable Component[] lines();
}
