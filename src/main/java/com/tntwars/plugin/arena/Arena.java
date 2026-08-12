package com.tntwars.plugin.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Une arène de TNT Wars : sa map (pos1/pos2), ses zones d'équipe, sa zone de coffre
 * infini, son salon d'attente, et son état de partie en cours.
 */
public class Arena {

    private final String name;
    private ArenaState state = ArenaState.DISABLED;

    private CuboidRegion mapRegion;      // pos1/pos2 de la map globale
    private CuboidRegion chestRegion;    // pos1/pos2 de la zone du/des coffre(s) infini(s)
    private Location waitingSpawn;

    private int teamsCount = 2;
    private int teamSize = 4;

    private final List<Team> teams = new ArrayList<>();
    private final List<ChestData> chests = new ArrayList<>();

    private ArenaSnapshot snapshot;

    // Etat runtime
    private final Map<UUID, Integer> playerTeam = new HashMap<>(); // uuid -> team index
    private final List<UUID> spectators = new ArrayList<>();
    private int countdown = -1;
    private long gameStartTime = -1;

    // Contexte tournoi (si cette partie fait partie d'un match de tournoi)
    private String tournamentName;
    private com.tntwars.plugin.tournament.BracketMatch tournamentMatch;

    public Arena(String name) {
        this.name = name;
        this.snapshot = null;
    }

    public String getName() {
        return name;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public CuboidRegion getMapRegion() {
        return mapRegion;
    }

    public void setMapRegion(CuboidRegion mapRegion) {
        this.mapRegion = mapRegion;
        this.snapshot = new ArenaSnapshot(mapRegion);
    }

    public CuboidRegion getChestRegion() {
        return chestRegion;
    }

    public void setChestRegion(CuboidRegion chestRegion) {
        this.chestRegion = chestRegion;
    }

    public Location getWaitingSpawn() {
        return waitingSpawn;
    }

    public void setWaitingSpawn(Location waitingSpawn) {
        this.waitingSpawn = waitingSpawn;
    }

    public int getTeamsCount() {
        return teamsCount;
    }

    public void setTeamsCount(int teamsCount) {
        this.teamsCount = teamsCount;
        while (teams.size() < teamsCount) {
            teams.add(new Team(teams.size()));
        }
        while (teams.size() > teamsCount) {
            teams.remove(teams.size() - 1);
        }
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getTeam(int index) {
        if (index < 0 || index >= teams.size()) return null;
        return teams.get(index);
    }

    public List<ChestData> getChests() {
        return chests;
    }

    public ArenaSnapshot getSnapshot() {
        return snapshot;
    }

    public boolean isFullyConfigured() {
        if (mapRegion == null || chestRegion == null || waitingSpawn == null) return false;
        if (teams.size() != teamsCount) return false;
        for (Team t : teams) {
            if (t.getZone() == null || t.getSpawn() == null) return false;
        }
        return true;
    }

    // ── Joueurs ──────────────────────────────────────────────────────────

    public int totalPlayers() {
        return playerTeam.size();
    }

    public Map<UUID, Integer> getPlayerTeamMap() {
        return playerTeam;
    }

    public List<UUID> getSpectators() {
        return spectators;
    }

    public Team getTeamOf(Player player) {
        Integer idx = playerTeam.get(player.getUniqueId());
        if (idx == null) return null;
        return getTeam(idx);
    }

    public boolean isFull() {
        return totalPlayers() >= teamsCount * teamSize;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public void setGameStartTime(long gameStartTime) {
        this.gameStartTime = gameStartTime;
    }

    /** Choisit l'équipe la moins remplie (équilibrage automatique à l'arrivée). */
    public Team pickBalancedTeam() {
        Team best = null;
        for (Team t : teams) {
            if (t.getMembers().size() >= teamSize) continue;
            if (best == null || t.getMembers().size() < best.getMembers().size()) {
                best = t;
            }
        }
        return best;
    }

    public void addPlayer(Player player, Team team) {
        playerTeam.put(player.getUniqueId(), team.getIndex());
        team.getMembers().add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        Integer idx = playerTeam.remove(player.getUniqueId());
        if (idx != null) {
            Team t = getTeam(idx);
            if (t != null) {
                t.getMembers().remove(player.getUniqueId());
                t.getAlive().remove(player.getUniqueId());
            }
        }
        spectators.remove(player.getUniqueId());
    }

    public void resetRuntime() {
        playerTeam.clear();
        spectators.clear();
        countdown = -1;
        gameStartTime = -1;
        for (Team t : teams) t.reset();
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public com.tntwars.plugin.tournament.BracketMatch getTournamentMatch() {
        return tournamentMatch;
    }

    public void setTournamentContext(String tournamentName, com.tntwars.plugin.tournament.BracketMatch match) {
        this.tournamentName = tournamentName;
        this.tournamentMatch = match;
    }

    public void clearTournamentContext() {
        this.tournamentName = null;
        this.tournamentMatch = null;
    }

    /** Nombre d'équipes qui ont encore au moins un joueur vivant. */
    public List<Team> teamsAlive() {
        List<Team> result = new ArrayList<>();
        for (Team t : teams) {
            if (!t.getAlive().isEmpty()) result.add(t);
        }
        return result;
    }
}
