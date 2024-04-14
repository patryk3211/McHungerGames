package org.patryk3211.hungergames.http;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.temporal.TemporalAmount;
import java.util.Map;

public class IntegratedWebServer extends RouterNanoHTTPD {
    private static IntegratedWebServer instance = null;
    private final Logger LOG;

    private final SessionManager sessionManager;

    public IntegratedWebServer(int port, long sessionTimeout) {
        this(port, sessionTimeout, LoggerFactory.getLogger(IntegratedWebServer.class));
    }

    public IntegratedWebServer(int port, long sessionTimeout, Logger logger) {
        super(port);
        this.LOG = logger;
        addMappings();

        sessionManager = new SessionManager(sessionTimeout);
        instance = this;
    }

    @Override
    public void addMappings() {
        // Domyślne odpowiedzi
        setNotImplementedHandler(NotImplementedHandler.class);
        setNotFoundHandler(Error404UriHandler.class);

        // Metody API
        addRoute("/api/check", SessionCheck.class);
        addRoute("/api/auth", SessionAuth.class);

        // Inne odpowiedzi na zapytania będą odczytywane z plików
        addRoute("/.*", Frontend.class);
    }

    public static IntegratedWebServer get() {
        return instance;
    }

    public Logger getLogger() {
        return LOG;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public void start() throws IOException {
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        LOG.info("Started integrated http server on: http://0.0.0.0:" + super.getListeningPort());
    }

    // Ta klasa zwraca błąd dla każdej możliwej metody dostępu HTTP
    public static class Route implements UriResponder {
        @Override
        public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
        }

        @Override
        public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
        }

        @Override
        public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
        }

        @Override
        public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
        }

        @Override
        public NanoHTTPD.Response other(String s, RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
        }
    }

    public static class ApiRouteException extends Exception {
        public final Response.IStatus responseCode;
        public final String message;
        public final String contentType;

        public ApiRouteException(String message) {
            this.responseCode = Response.Status.BAD_REQUEST;
            this.message = message;
            this.contentType = "application/json";
        }

        public ApiRouteException(Response.IStatus status, String message, String contentType) {
            this.responseCode = status;
            this.message = message;
            this.contentType = contentType;
        }
    }

    // Ta klasa ułatwia korzystanie z danych przesyłanych w formacie JSON wczytując je do obiektu
    public static abstract class JsonRoute extends Route {
        public static String getJsonString(JsonObject object, String fieldName) throws ApiRouteException {
            try {
                JsonElement element = object.get(fieldName);
                if(element == null)
                    throw new ApiRouteException("{\"msg\":\"Field '" + fieldName + "' is missing\"}");
                return element.getAsString();
            } catch (UnsupportedOperationException | IllegalStateException e) {
                throw new ApiRouteException("{\"msg\":\"Field '" + fieldName + "' has an invalid type\"}");
            }
        }

        protected abstract Response handle(UriResource uriResource, JsonObject json, IHTTPSession ihttpSession) throws ApiRouteException;

        @Override
        public Response post(UriResource uriResource, Map<String, String> map, IHTTPSession ihttpSession) {
            // Sprawdzamy czy otrzymaliśmy odpowiedni format danych
            String contentTypeHeader = ihttpSession.getHeaders().get("content-type");
            if(contentTypeHeader == null) {
                return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "application/json",
                        "{\"msg\":\"Invalid content type specified\"}"
                );
            }
            ContentType contentType = new ContentType(contentTypeHeader);
            if(!contentType.getContentType().equals("application/json")) {
                return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "application/json",
                        "{\"msg\":\"Invalid content type specified\"}"
                );
            }
            // Odczytujemy otrzymane dane
            JsonReader reader = new JsonReader(new InputStreamReader(ihttpSession.getInputStream()));
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            if(root == null) {
                return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "application/json",
                        "{\"msg\":\"Malformed JSON received\"}"
                );
            }

            try {
                // Uruchamiamy metodę z odczytanymi danymi
                return handle(uriResource, root, ihttpSession);
            } catch (ApiRouteException e) {
                return newFixedLengthResponse(e.responseCode, e.contentType, e.message);
            }
        }
    }
}
