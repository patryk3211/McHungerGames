package org.patryk3211.hungergames.http.rest;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.http.IntegratedWebServer;

public class StopGame extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        ensureSessionValid(json);

        if(HungerGamesPlugin.manager.currentState() == GameState.Waiting) {
            throw new IntegratedWebServer.ApiRouteException(NanoHTTPD.Response.Status.CONFLICT, "{\"msg\":\"Gra nie została jeszcze rozpoczęta\"}");
        }

        return status(HungerGamesPlugin.manager.stopGame());
    }
}
