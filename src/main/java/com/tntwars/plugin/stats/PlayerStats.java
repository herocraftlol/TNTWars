package com.tntwars.plugin.stats;

import java.util.UUID;

public class PlayerStats {

    private final UUID uuid;
    private String name;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int gamesPlayed;
    private String selectedCosmetic = "none";

    public PlayerStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
        gamesPlayedIncrementIfNeeded();
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        wins++;
        gamesPlayed++;
    }

    public int getLosses() {
        return losses;
    }

    public void addLoss() {
        losses++;
        gamesPlayed++;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public double getKD() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public String getSelectedCosmetic() {
        return selectedCosmetic;
    }

    public void setSelectedCosmetic(String selectedCosmetic) {
        this.selectedCosmetic = selectedCosmetic;
    }

    private void gamesPlayedIncrementIfNeeded() {
        // gamesPlayed est incrémenté via addWin/addLoss (une fois par partie terminée)
    }
}
