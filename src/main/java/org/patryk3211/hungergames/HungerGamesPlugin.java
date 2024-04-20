package org.patryk3211.hungergames;

import org.bukkit.plugin.java.JavaPlugin;
import org.patryk3211.hungergames.game.GameManager;
import org.patryk3211.hungergames.http.Frontend;
import org.patryk3211.hungergames.http.IntegratedWebServer;
import org.slf4j.Logger;

import java.io.IOException;

public final class HungerGamesPlugin extends JavaPlugin {
    public static Logger LOG;
    public static GameManager manager;
    private static HungerGamesPlugin instance;

    private IntegratedWebServer webServer;

    @Override
    public void onEnable() {
        LOG = getSLF4JLogger();
        Frontend.classLoader = getClassLoader();
        instance = this;

        // Konfiguracja jest ładowana jak najszybciej, aby reszta klas miała do niej dostęp
        Configuration.init(this);

        // Stwórz serwer HTTP
        webServer = new IntegratedWebServer(Configuration.getHttpPort(), Configuration.getHttpSessionTimeout() * 60L, LOG);
        // Dodaj dane logowania z pliku konfiguracyjnego
        webServer.getSessionManager().addCredentials(Configuration.getHttpUser(), Configuration.getHttpPassword());

        // Tworzymy menedżer stanu gry
        manager = new GameManager(getServer());

        // Rejestrujemy event listener
        getServer().getPluginManager().registerEvents(manager, this);

        // Uruchom serwer HTTP
        try {
            webServer.start();
        } catch (IOException e) {
            // Błąd podczas tworzenia serwera HTTP
            LOG.error("Failed to start the integrated HTTP server");
            throw new RuntimeException("Failed to start integrated HTTP server");
        }
    }

    public static HungerGamesPlugin get() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Zatrzymaj serwer HTTP
        if(webServer != null) {
            LOG.info("Stopping the integrated http server");
            webServer.stop();
        }
    }
}
