package org.patryk3211.hungergames.http.rest;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.http.IntegratedWebServer;

import java.util.UUID;

public class SessionAuth extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        String userStr = getJsonString(json, "user");
        String passwordStr = getJsonString(json, "password");
        UUID id = IntegratedWebServer.get().getSessionManager().newSession(userStr, passwordStr);
        if(id == null) {
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.UNAUTHORIZED,
                    "application/json",
                    "{\"msg\":\"Unauthorized\"}"
            );
        }
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                "{\"sid\":\"" + id + "\"}"
        );
    }
}
