package org.patryk3211.hungergames.game;

public enum PlayerStatus {
    Offline("Offline"),
    Waiting("Czeka"),
    Spectating("Obserwuje"),
    Alive("Żyje")
    ;

    public final String localizedName;
    PlayerStatus(String localizedName) {
        this.localizedName = localizedName;
    }
}
