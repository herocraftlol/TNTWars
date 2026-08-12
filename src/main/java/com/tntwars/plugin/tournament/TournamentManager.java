package com.tntwars.plugin.tournament;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère des tournois simples à élimination directe : les joueurs s'inscrivent par
 * équipes (préconstituées), un tableau (bracket) est généré, et chaque match est
 * joué comme une partie normale sur une arène dédiée au tournoi.
 */
public class TournamentManager {

    private final TntWarsPlugin plugin;
    private final Map<String, Tournament> tournaments = new LinkedHashMap<>();

    public TournamentManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public Tournament create(String name, String arenaName) {
        Tournament t = new Tournament(name, arenaName);
        tournaments.put(name.toLowerCase(), t);
        return t;
    }

    public Tournament get(String name) {
        return name == null ? null : tournaments.get(name.toLowerCase());
    }

    public java.util.Collection<Tournament> all() {
        return tournaments.values();
    }

    public boolean delete(String name) {
        return tournaments.remove(name.toLowerCase()) != null;
    }

    public TournamentTeam register(Tournament tournament, String teamName, Player leader) {
        if (tournament.getState() != Tournament.State.REGISTRATION) return null;
        TournamentTeam team = new TournamentTeam(teamName, leader.getUniqueId());
        tournament.getRegistered().add(team);
        return team;
    }

    public void start(Tournament tournament) {
        if (tournament.getRegistered().size() < 2) return;
        tournament.generateBracket();
        tournament.setState(Tournament.State.IN_PROGRESS);
        launchNextMatch(tournament);
    }

    /** Lance le prochain match jouable du tournoi sur son arène dédiée. */
    public void launchNextMatch(Tournament tournament) {
        Arena arena = plugin.getArenaManager().getArena(tournament.getArenaName());
        if (arena == null) return;
        if (arena.getState() != ArenaState.WAITING) {
            return; // l'arène est occupée, on relancera à sa libération
        }
        BracketMatch match = tournament.nextPlayableMatch();
        if (match == null) {
            return; // tournoi terminé ou en attente d'autres résultats
        }

        arena.setTournamentContext(tournament.getName(), match);
        putTeamInArena(arena, arena.getTeam(0), match.getTeamA());
        putTeamInArena(arena, arena.getTeam(1), match.getTeamB());

        broadcast(tournament, "§6§lTournoi " + tournament.getName() + " §e: match " + match.getTeamA().getName() + " §7vs §e" + match.getTeamB().getName() + " §7va commencer sur l'arène " + arena.getName() + " !");
        plugin.getGameManager().forceStart(arena);
    }

    private void putTeamInArena(Arena arena, Team arenaTeam, TournamentTeam tTeam) {
        if (tTeam == null || arenaTeam == null) return;
        for (UUID uuid : tTeam.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            plugin.getGameManager().joinTeam(p, arena, arenaTeam);
        }
    }

    /**
     * Appelé par le GameManager quand une partie liée à un tournoi se termine.
     */
    public void onMatchFinished(Arena arena, Team winnerArenaTeam) {
        String tournamentName = arena.getTournamentName();
        BracketMatch match = arena.getTournamentMatch();
        if (tournamentName == null || match == null) return;
        Tournament tournament = get(tournamentName);
        arena.clearTournamentContext();
        if (tournament == null) return;

        TournamentTeam winner;
        if (winnerArenaTeam == null) {
            winner = match.getTeamA(); // en cas d'égalité, l'équipe A est qualifiée par défaut
        } else {
            winner = (winnerArenaTeam.getIndex() == 0) ? match.getTeamA() : match.getTeamB();
        }
        match.setWinner(winner);
        tournament.advanceWinner(match);

        if (tournament.getState() == Tournament.State.FINISHED) {
            broadcast(tournament, "§6§l🏆 " + tournament.getChampion().getName() + " §6§lremporte le tournoi " + tournament.getName() + " !");
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> launchNextMatch(tournament), 60L);
        }
    }

    private void broadcast(Tournament tournament, String message) {
        for (TournamentTeam team : tournament.getRegistered()) {
            for (UUID uuid : team.getMembers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) MessageUtil.send(p, message);
            }
        }
    }
}
