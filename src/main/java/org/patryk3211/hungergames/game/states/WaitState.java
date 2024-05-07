package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.game.TrackedPlayerData;
import org.patryk3211.hungergames.http.ws.Subscriptions;

public class WaitState extends GameStateHandler {
    private int tickCount;

    @Override
    public @Nullable Component getHotBarTitle() {
        return Component.text("Oczekiwanie na rozpoczęcie gry", NamedTextColor.WHITE);
    }

    @Override
    public void onEntry() {
        manager.border.setCenter(Configuration.getSpawnLocation());
        manager.border.setSize(1000);

        manager.movementAllowed = true;
        manager.pvpEnabled = false;
        tickCount = 0;

        for (TrackedPlayerData player : manager.players()) {
            Subscriptions.notifyTracked(player);
        }
        Subscriptions.notifyCount(manager.onlineCount, manager.getRemainingPlayerCount());
    }

    @Override
    public void tick() {
        if(tickCount++ == 5) {
            for (TrackedPlayerData player : manager.players()) {
                if(player.playerInstance != null) {
                    player.playerInstance.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 6, 100, true, false, false));
                    player.playerInstance.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 6, 100, true, false, false));
                }
            }
            tickCount = 0;
        }
    }
}
