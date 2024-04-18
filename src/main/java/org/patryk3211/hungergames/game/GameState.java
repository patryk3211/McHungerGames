package org.patryk3211.hungergames.game;

import org.patryk3211.hungergames.game.states.MapPickState;
import org.patryk3211.hungergames.game.states.StartCountdownState;
import org.patryk3211.hungergames.game.states.TeleportState;
import org.patryk3211.hungergames.game.states.WaitState;

public enum GameState {
    // W tym stanie gracze oczekują na start gry przez administratora
    // znajdują się na spawnie i dołączając są tam przenoszeni
    Waiting(new WaitState()),
    // Wybiera mapę do rozgrywki
    PickMap(new MapPickState()),
    // Teleportacja na mapę
    Teleport(new TeleportState()),
    // Odliczanie do startu gry
    StartCountdown(new StartCountdownState()),
    // Gra się toczy
    Playing(null);

    public final GameStateHandler stateManager;

    GameState(GameStateHandler stateManager) {
        this.stateManager = stateManager;
    }
}
