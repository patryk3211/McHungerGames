package org.patryk3211.hungergames.http.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.http.IntegratedWebServer;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.List;

public class Maps extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        ensureSessionValid(json);
        final List<MapConfig> maps = Configuration.getMaps();

        JsonObject response = new JsonObject();
        JsonArray mapArray = new JsonArray();
        for (int i = 0; i < maps.size(); ++i) {
            MapConfig map = maps.get(i);
            JsonObject mapObj = new JsonObject();
            mapObj.addProperty("id", i);
            mapObj.addProperty("name", map.getName());
            mapArray.add(mapObj);
        }
        response.add("maps", mapArray);
        String out = new Gson().toJson(response);
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", out);
    }
}
