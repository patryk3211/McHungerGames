package org.patryk3211.hungergames.http.rest;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.http.IntegratedWebServer;

import java.util.UUID;

public class SessionCheck extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        String uuidStr = getJsonString(json, "sid");
        try {
            UUID sid = UUID.fromString(uuidStr);
            return status(IntegratedWebServer.get().getSessionManager().isAuthorized(sid));
        } catch (IllegalArgumentException e) {
            throw new IntegratedWebServer.ApiRouteException("{\"msg\":\"Field 'sid' has a malformed UUID\"}");
        }
    }
}
