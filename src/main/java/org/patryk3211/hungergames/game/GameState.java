package org.patryk3211.hungergames.game;

import org.patryk3211.hungergames.game.states.*;

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
    Playing(new PlayingState()),
    // Stan końca gry, pozostała jedna osoba, wygrany
    End(new EndState()),
    // Stan uruchamiany po zakończeniu gry
    PostGame(new PostGameState()),

    // Pseudo stan do sygnalizowania początku gry
    StartGame(PickMap);

    public final boolean isMetaState;
    public final GameState targetState;
    public final GameStateHandler stateManager;

    GameState(GameStateHandler stateManager) {
        this.stateManager = stateManager;
        this.isMetaState = false;
        this.targetState = null;
    }

    GameState(GameState target) {
        this.stateManager = null;
        this.isMetaState = true;
        this.targetState = target;
    }
}
