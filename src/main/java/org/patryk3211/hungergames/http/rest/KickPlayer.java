package org.patryk3211.hungergames.http.rest;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.bukkit.entity.Player;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.http.IntegratedWebServer;

public class KickPlayer extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        ensureSessionValid(json);
        String user = getJsonString(json, "player");

        Player player = HungerGamesPlugin.get().getServer().getPlayer(user);
        if(player == null) {
            throw new IntegratedWebServer.ApiRouteException(NanoHTTPD.Response.Status.NOT_FOUND, "{\"msg\":\"Gracz nieistnieje\"}");
        }

        HungerGamesPlugin.manager.deferredAction(player::kick);
        return status(true);
    }
}
