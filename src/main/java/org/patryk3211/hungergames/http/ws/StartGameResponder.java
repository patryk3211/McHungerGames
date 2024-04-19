package org.patryk3211.hungergames.http.ws;

import com.google.gson.JsonObject;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.game.GameState;
import org.patryk3211.hungergames.http.WebSocketRoute;

import static org.patryk3211.hungergames.http.WebSocketRoute.errorResponse;
import static org.patryk3211.hungergames.http.WebSocketRoute.successResponse;

public class StartGameResponder implements WebSocketRoute.IWebSocketResponder {
    @Override
    public String respond(JsonObject request) {
        GameState state = HungerGamesPlugin.manager.currentState();
        if(state != GameState.Waiting) {
            return errorResponse("Cannot start game in a state other than waiting");
        }
        HungerGamesPlugin.manager.nextState(GameState.StartGame);
        return successResponse("Game has been started");
    }
}
