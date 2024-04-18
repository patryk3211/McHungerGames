package org.patryk3211.hungergames.http;

import java.io.IOException;

public class RunHttpFrontend {
    public static void main(String[] args) throws IOException {
        IntegratedWebServer server = new IntegratedWebServer(8080, 60 * 30);
        server.getSessionManager().addCredentials("admin", "1");

        server.start();

        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
