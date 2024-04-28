package org.patryk3211.hungergames.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.http.ws.Subscriptions;

public class TrackedPlayerData {
    public Player playerInstance;
    public final String name;
    private int kills;
    private int deaths;
    private int wins;
    public Scoreboard scoreboard;

    public TrackedPlayerData(Player instance, String name) {
        this.playerInstance = instance;
        this.name = name;
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
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

    public void addKill() {
        ++this.kills;
        Subscriptions.notifyTracked(this);
    }

    public void addDeath() {
        ++this.deaths;
        Subscriptions.notifyTracked(this);
    }

    public void addWin() {
        ++this.wins;
        Subscriptions.notifyTracked(this);
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getWins() {
        return wins;
    }
}
