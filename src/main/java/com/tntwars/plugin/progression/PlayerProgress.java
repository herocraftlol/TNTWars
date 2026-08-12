package com.tntwars.plugin.progression;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Progression d'un joueur : points cumulés (gagnés en kills, victoires, TNT envoyées,
 * blocs adverses détruits, canons fonctionnels) et niveau qui en découle.
 * Le niveau détermine quels schémas de canons à TNT sont débloqués.
 */
public class PlayerProgress {

    private final UUID uuid;
    private int points;
    private final Set<String> manuallyUnlocked = new LinkedHashSet<>();

    public PlayerProgress(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        if (amount <= 0) return;
        this.points += amount;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    /** Schémas débloqués manuellement (ex: commande admin), en plus de ceux débloqués par niveau. */
    public Set<String> getManuallyUnlocked() {
        return manuallyUnlocked;
    }
}
