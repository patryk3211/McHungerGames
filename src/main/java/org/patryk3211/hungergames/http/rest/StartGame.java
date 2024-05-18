package org.patryk3211.hungergames.http.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.Configuration;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.http.IntegratedWebServer;
import org.patryk3211.hungergames.map.MapConfig;

import java.util.List;

public class StartGame extends IntegratedWebServer.JsonRoute {
    @Override
    protected NanoHTTPD.Response handle(RouterNanoHTTPD.UriResource uriResource, JsonObject json, NanoHTTPD.IHTTPSession ihttpSession) throws IntegratedWebServer.ApiRouteException {
        ensureSessionValid(json);

        GameState state = HungerGamesPlugin.manager.currentState();
        if(state != GameState.Waiting) {
            throw new IntegratedWebServer.ApiRouteException(NanoHTTPD.Response.Status.CONFLICT, "{\"msg\":\"Nie można rozpocząć gry ponieważ ona już trwa\"}");
        }

        HungerGamesPlugin.manager.selectedMap = null;
        JsonElement mapElement = json.get("map");
        if(mapElement != null) {
            try {
                int mapId = mapElement.getAsInt();
                List<MapConfig> maps = Configuration.getMaps();
                if(mapId < 0 || mapId >= maps.size())
                    throw new IntegratedWebServer.ApiRouteException("{\"msg\":\"Mapa nie istnieje\"}");
                HungerGamesPlugin.manager.selectedMap = maps.get(mapId);
            } catch (UnsupportedOperationException | NumberFormatException | IllegalStateException e) {
                throw new IntegratedWebServer.ApiRouteException("{\"msg\":\"Nieprawidłowy format id mapy\"}");
            }
        }

        HungerGamesPlugin.manager.nextState(GameState.StartGame);
        return status(true);
    }
}
