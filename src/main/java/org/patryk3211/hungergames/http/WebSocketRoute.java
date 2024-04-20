package org.patryk3211.hungergames.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoWSD;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.patryk3211.hungergames.http.ws.PlayerList;
import org.patryk3211.hungergames.http.ws.Subscriptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WebSocketRoute extends NanoWSD {
    private final Map<String, IWebSocketResponder> responderMap = new HashMap<>();

    public WebSocketRoute(int port) {
        super(port);
        addResponders();
    }

    private void addResponders() {
        addResponder("subscribe", new Subscriptions());
        addResponder("players", new PlayerList());
    }

    private void addResponder(String type, IWebSocketResponder responder) {
        responderMap.put(type, responder);
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession ihttpSession) {
        return new WebSocketImpl(ihttpSession, this);
    }

    public interface IWebSocketResponder {
        void respond(JsonObject request, WebSocket socket) throws IOException;
    }

    public static @Nullable String getJsonString(JsonObject object, String field) {
        try {
            JsonElement el = object.get(field);
            return el.getAsString();
        } catch (UnsupportedOperationException | IllegalStateException e) {
            return null;
        }
    }

    public static String errorResponse(String msg) {
        return "{\"type\":\"error\",\"msg\":\"" + msg + "\"}";
    }

    public static String successResponse(String msg) {
        return "{\"type\":\"success\",\"msg\":\"" + msg + "\"}";
    }

    public static class WebSocketImpl extends NanoWSD.WebSocket {
        private final WebSocketRoute route;
        private boolean authenticated = false;

        public WebSocketImpl(IHTTPSession handshakeRequest, WebSocketRoute route) {
            super(handshakeRequest);
            this.route = route;
        }

        private String auth(JsonObject json) {
            try {
                // Sprawdzamy czy podana sesja jest prawidłowa
                JsonElement sid = json.get("sid");
                if(sid == null)
                    return errorResponse("Missing SID");
                String sidStr = sid.getAsString();
                UUID uuidSid = UUID.fromString(sidStr);
                boolean status = IntegratedWebServer.get().getSessionManager().isAuthorized(uuidSid);
                if(!status)
                    return errorResponse("Session invalid");
                authenticated = true;
                return successResponse("Authenticated");
            } catch(UnsupportedOperationException | IllegalStateException | IllegalArgumentException e) {
                return errorResponse("Invalid SID");
            }
        }

        @Override
        protected void onOpen() {

        }

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode closeCode, String s, boolean b) {

        }

        @Override
        protected void onMessage(NanoWSD.WebSocketFrame webSocketFrame) {
            try {
                // Odczytujemy JSON z zapytania
                JsonObject root = new Gson().fromJson(webSocketFrame.getTextPayload(), JsonObject.class);
                if(root == null) {
                    send(errorResponse("Invalid JSON received"));
                    return;
                }

                // Sprawdzamy czy typ został podany i czy jest prawidłowy
                JsonElement type = root.get("type");
                if(type == null) {
                    send(errorResponse("Missing type parameter"));
                    return;
                }
                try {
                    // Autoryzuj lub przekaż zapytanie dalej
                    String typeStr = type.getAsString();
                    if(typeStr.equals("auth")) {
                        send(auth(root));
                    } else if(typeStr.equals("keep-alive")) {
                        send("{\"type\":\"keep-alive\"}");
                    } else {
                        if(!authenticated) {
                            send(errorResponse("Not authenticated"));
                            return;
                        }
                        IWebSocketResponder responder = route.responderMap.get(typeStr);
                        if (responder == null)
                            throw new UnsupportedOperationException();
                        responder.respond(root, this);
                    }
                } catch(UnsupportedOperationException | IllegalStateException e) {
                    send(errorResponse("Invalid type parameter"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPong(NanoWSD.WebSocketFrame webSocketFrame) {
        }

        @Override
        protected void onException(IOException e) {
            e.printStackTrace();
        }
    }
}
