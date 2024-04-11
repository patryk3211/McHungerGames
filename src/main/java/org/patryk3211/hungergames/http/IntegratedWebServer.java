package org.patryk3211.hungergames.http;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.patryk3211.hungergames.HungerGamesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class IntegratedWebServer extends RouterNanoHTTPD {
    private static IntegratedWebServer instance = null;
    private final Logger LOG;

    public IntegratedWebServer(int port) {
        this(port, LoggerFactory.getLogger(IntegratedWebServer.class));
    }

    public IntegratedWebServer(int port, Logger logger) {
        super(port);
        this.LOG = logger;
        addMappings();

        instance = this;
    }

    @Override
    public void addMappings() {
        setNotImplementedHandler(NotImplementedHandler.class);
        setNotFoundHandler(Error404UriHandler.class);
//        addRoute("/", Frontend.class);
        addRoute("/.*", Frontend.class);
    }

    public static IntegratedWebServer get() {
        return instance;
    }

    public Logger getLogger() {
        return LOG;
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

    // Ta klasa ułatwia korzystanie z danych przesyłanych w formacie JSON wczytując je do obiektu
    public static abstract class JsonRoute extends Route {
        protected abstract Response handle(UriResource uriResource, JsonObject json, IHTTPSession ihttpSession);

        @Override
        public Response post(UriResource uriResource, Map<String, String> map, IHTTPSession ihttpSession) {
            JsonReader reader = new JsonReader(new InputStreamReader(ihttpSession.getInputStream()));
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            return handle(uriResource, root, ihttpSession);
        }
    }
}
