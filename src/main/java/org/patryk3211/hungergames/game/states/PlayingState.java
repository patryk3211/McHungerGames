package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.game.GameStateHandler;

public class PlayingState extends GameStateHandler {
    private static final Style WHITE = Style.style(TextColor.color(255, 255, 255));

    private boolean shieldActive;
    private int shieldLeft;

    private int timeTicks;

    @Override
    public void onEntry() {
        shieldActive = true;
        shieldLeft = Configuration.getPvpDelay() * 20;
        manager.movementAllowed = true;
        timeTicks = 0;
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

        ++timeTicks;
    }

    // Ile sekund trwa już gra
    public int gameTime() {
        return timeTicks / 20;
    }
}
