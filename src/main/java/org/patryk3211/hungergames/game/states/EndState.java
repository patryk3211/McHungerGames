package org.patryk3211.hungergames.game.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.game.GameStateHandler;
import org.patryk3211.hungergames.game.PlayerStatus;
import org.patryk3211.hungergames.game.TrackedPlayerData;
import org.patryk3211.hungergames.http.ws.Subscriptions;

import java.time.Duration;

import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class EndState extends GameStateHandler {
    private int tickCount;
    private TrackedPlayerData winnerData;

    @Override
    public void onEntry() {
        winnerData = manager.players().stream().filter(data -> data.getStatus() == PlayerStatus.Alive).toList().get(0);
        manager.server.sendMessage(Component.text("Gre wygrał " + winnerData.name));
        if(winnerData.playerInstance != null) {
            winnerData.playerInstance.showTitle(Title.title(
                    Component.text("Wygrana", GOLD),
                    Component.empty(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))));
        }

        winnerData.addWin();
        tickCount = 0;
        Subscriptions.notifyWin(winnerData);
    }

    @Override
    public void tick() {
        if(tickCount % 20 == 0) {
            // Fajerwerki
            for(int i = 0; i < 5; ++i) {
                Location spawnLoc = winnerData.playerInstance.getLocation();
                double angle = manager.random.nextDouble(0, Math.PI * 2);
                double x = Math.cos(angle) * 6;
                double z = Math.sin(angle) * 6;
                spawnLoc.add(x, 1, z);
                manager.world.spawnEntity(spawnLoc, EntityType.FIREWORK, true);
            }
        }

        if(tickCount++ >= (20 * 5)) {
            manager.nextState(GameState.PostGame);
        }
    }
}
