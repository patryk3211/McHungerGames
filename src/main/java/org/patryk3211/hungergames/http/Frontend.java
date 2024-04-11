package org.patryk3211.hungergames.http;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status;

public class Frontend extends IntegratedWebServer.Route {
    // Klasa pomocnicza zwracająca strumień pliku i jego typ
    private static class FileResult {
        public final InputStream stream;
        public final String type;

        public FileResult(URLConnection connection) throws IOException {
            connection.setUseCaches(false);
            this.stream = connection.getInputStream();
            this.type = connection.getContentType();
        }
    }

    private FileResult getFile(String filename) {
        // Trochę kodu z internetu do otwierania plików z archiwum JAR
        URL url = this.getClass().getClassLoader().getResource(filename);
        if (url == null) {
            return null;
        } else {
            try {
                URLConnection connection = url.openConnection();
                return new FileResult(connection);
            } catch (IOException e) {
                return null;
            }
        }
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        // Ten fragment kodu usuwa z początku napisu realUri litery baseUri dopóki się one zgadzają
        String baseUri = uriResource.getUri();
        String realUri = RouterNanoHTTPD.normalizeUri(session.getUri());
        for(int index = 0; index < Math.min(baseUri.length(), realUri.length()); ++index) {
            if (baseUri.charAt(index) != realUri.charAt(index)) {
                realUri = RouterNanoHTTPD.normalizeUri(realUri.substring(index));
                break;
            }
        }

        // Sprawdzamy jaki plik otworzyć
        String filename;
        FileResult result;
        if(realUri.matches(".*\\..*")) {
            // Napis z kropką w środku, najpewniej rozszerzenie pliku
            filename = realUri;
        } else {
            // Prosta ścieżka, przekierowujemy do index.html w folderze
            filename = realUri + "/index.html";
        }
        result = getFile("http/" + filename);
        if(result == null) {
            // Plik nie został otwarty więc pewnie nie istnieje
            IntegratedWebServer.get().getLogger().warn("Requested file '" + filename + "' not found in http directory");
            return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "404 - Not Found");
        }

        // Wyślij otwarty plik
        return NanoHTTPD.newChunkedResponse(Status.OK, result.type, result.stream);
    }
}
