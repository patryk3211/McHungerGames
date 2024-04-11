package org.patryk3211.hungergames.http;

import com.google.gson.JsonDeserializer;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.Map;

public class SessionCheck extends IntegratedWebServer.Route {
    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        ihttpSession.getInputStream();
//        return super.post(uriResource, map, ihttpSession);
    }
}
