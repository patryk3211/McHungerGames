package org.patryk3211.hungergames;

import org.bukkit.plugin.java.JavaPlugin;
import org.patryk3211.hungergames.http.IntegratedWebServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

public final class HungerGamesPlugin extends JavaPlugin {
    public static Logger LOG;
    private IntegratedWebServer webServer;

    @Override
    public void onEnable() {
        LOG = getSLF4JLogger();

        // Konfiguracja jest ładowana jak najszybciej, aby reszta klas miała do niej dostęp
        Configuration.init(getConfig());

        // Stwórz serwer HTTP
        webServer = new IntegratedWebServer(Configuration.getHttpPort(), Configuration.getHttpSessionTimeout() * 60, LOG);

        // Uruchom serwer HTTP
        try {
            webServer.start();
        } catch (IOException e) {
            // Błąd podczas tworzenia serwera HTTP
            LOG.error("Failed to start the integrated HTTP server");
            throw new RuntimeException("Failed to start integrated HTTP server");
        }
    }

    @Override
    public void onDisable() {
        // Zatrzymaj serwer HTTP
        webServer.stop();
    }
}
