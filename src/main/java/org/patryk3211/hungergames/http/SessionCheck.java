package org.patryk3211.hungergames.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.UUID;

public class SessionCheck extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        String uuidStr = getJsonString(json, "sid");
        try {
            UUID sid = UUID.fromString(uuidStr);
            boolean status = IntegratedWebServer.get().getSessionManager().isAuthorized(sid);
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    "{\"status\":" + (status ? "true" : "false") + "}");
        } catch (IllegalArgumentException e) {
            throw new IntegratedWebServer.ApiRouteException("{\"msg\":\"Field 'sid' has a malformed UUID\"}");
        }
    }
}
