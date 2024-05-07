package org.patryk3211.hungergames.http.rest;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.http.IntegratedWebServer;

public class ResetTracking extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        ensureSessionValid(json);
        HungerGamesPlugin.manager.clearTrackedPlayers();
        return status(true);
    }
}
