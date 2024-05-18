package org.patryk3211.hungergames.http.ws;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoWSD;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.TrackedPlayerData;
import org.patryk3211.hungergames.http.WebSocketRoute;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.patryk3211.hungergames.http.WebSocketRoute.*;

public class Subscriptions implements WebSocketRoute.IWebSocketResponder {
    private static Subscriptions instance;
    private final Map<String, Set<NanoWSD.WebSocket>> subscriptions = new HashMap<>();

    public Subscriptions() {
        instance = this;
    }

    public static Subscriptions get() {
        return instance;
    }

    public void notify(String channel, String message) {
        final Set<NanoWSD.WebSocket> sockets = subscriptions.get(channel);
        if(sockets == null)
            return;
        sockets.forEach(socket -> {
            try {
                socket.send(message);
            } catch (IOException e) {
                HungerGamesPlugin.LOG.error(e.getMessage());
            }
        });
    }

    @Override
    public void respond(JsonObject request, NanoWSD.WebSocket socket) throws IOException {
        final String channel = getJsonString(request, "channel");
        if(channel == null) {
            socket.send(errorResponse("Channel missing"));
            return;
        }

        final Set<NanoWSD.WebSocket> sockets = subscriptions.computeIfAbsent(channel, s -> new HashSet<>());
        if(!sockets.add(socket)) {
            socket.send(errorResponse("Channel already subscribed"));
        } else {
            socket.send(successResponse("Subscribed to channel"));
        }
    }

    public static void notifyTracked(TrackedPlayerData data) {
        instance.notify("tracked",
                "{\"type\":\"tracked\"," +
                        "\"name\":\"" + data.name + "\"," +
                        "\"state\":\"" + data.getStatus().localizedName + "\"," +
                        "\"kills\":" + data.getKills() + "," +
                        "\"deaths\":" + data.getDeaths() + "," +
                        "\"wins\":" + data.getWins() + "}");
    }

    public static void notifyTrackedReset() {
        instance.notify("tracked",
                "{\"type\":\"tracked\", \"reset\": true}");
    }

    public static void notifyCount(int online, int remaining) {
        instance.notify("count",
                "{\"type\":\"count\"," +
                        "\"online\":" + online + "," +
                        "\"remaining\":" + remaining + "}");
    }

    public static void notifyTime(int seconds) {
        instance.notify("time",
                "{\"type\":\"time\"," +
                        "\"time\":" + seconds + "}");
    }

    public static void notifyTimeStop() {
        instance.notify("time",
                "{\"type\":\"time\"," +
                        "\"time\":0," +
                        "\"stop\":true}");

    }

    public static void notifyWin(TrackedPlayerData winner) {
        instance.notify("win",
                "{\"type\":\"win\"," +
                        "\"winner\":\"" + winner.name + "\"}");
    }
}
