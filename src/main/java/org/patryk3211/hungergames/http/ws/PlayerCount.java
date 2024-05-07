package org.patryk3211.hungergames.http.ws;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoWSD;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.GameManager;
import org.patryk3211.hungergames.http.WebSocketRoute;

import java.io.IOException;

public class PlayerCount implements WebSocketRoute.IWebSocketResponder {
    @Override
    public void respond(JsonObject request, NanoWSD.WebSocket socket) throws IOException {
        GameManager manager = HungerGamesPlugin.manager;

        JsonObject response = new JsonObject();
        response.addProperty("online", manager.onlineCount);
        response.addProperty("remaining", manager.getRemainingPlayerCount());
        response.addProperty("type", "count");
        String out = new Gson().toJson(response);
        socket.send(out);
    }
}
