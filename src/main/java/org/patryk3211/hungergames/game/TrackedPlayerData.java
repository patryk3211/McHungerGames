package org.patryk3211.hungergames.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.patryk3211.hungergames.HungerGamesPlugin;

public class TrackedPlayerData {
    public Player playerInstance;
    public final String name;
    public int kills;
    public int deaths;
    public Scoreboard scoreboard;

    public TrackedPlayerData(Player instance, String name) {
        this.playerInstance = instance;
        this.name = name;
        this.kills = 0;
        this.deaths = 0;
    }

    public PlayerStatus getStatus() {
        if(playerInstance == null)
            return PlayerStatus.Offline;
        if(playerInstance.getGameMode() == GameMode.SPECTATOR)
            return PlayerStatus.Spectating;
        if(HungerGamesPlugin.manager.currentState() == GameState.Waiting)
            return PlayerStatus.Waiting;
        if(playerInstance.getGameMode() == GameMode.SURVIVAL)
            return PlayerStatus.Alive;
        return null;
    }
}
