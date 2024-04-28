package org.patryk3211.hungergames.http.ws;

import com.google.gson.*;
import fi.iki.elonen.NanoWSD;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.GameManager;
import org.patryk3211.hungergames.game.TrackedPlayerData;
import org.patryk3211.hungergames.http.WebSocketRoute;

import java.io.IOException;
import java.util.Collection;

public class PlayerList implements WebSocketRoute.IWebSocketResponder {
    @Override
    public void respond(JsonObject request, NanoWSD.WebSocket socket) throws IOException {
        GameManager manager = HungerGamesPlugin.manager;
        Collection<TrackedPlayerData> players = manager.players();

        JsonObject response = new JsonObject();
        JsonArray playersJson = new JsonArray();
        for(TrackedPlayerData player : players) {
            JsonObject playerEntry = new JsonObject();
            playerEntry.addProperty("name", player.name);
            playerEntry.addProperty("state", player.getStatus().localizedName);
            playerEntry.addProperty("deaths", player.getDeaths());
            playerEntry.addProperty("kills", player.getKills());
            playerEntry.addProperty("wins", player.getWins());
            playersJson.add(playerEntry);
        }
        response.add("players", playersJson);
        response.addProperty("type", "players");
        String out = new Gson().toJson(response);
        socket.send(out);
    }
}
