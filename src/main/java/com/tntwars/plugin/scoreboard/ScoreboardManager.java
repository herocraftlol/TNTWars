package com.tntwars.plugin.scoreboard;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Affiche un tableau de bord (scoreboard) pendant une partie : équipes, joueurs
 * vivants par équipe, durée écoulée.
 */
public class ScoreboardManager {

    private final TntWarsPlugin plugin;
    private final Map<String, BukkitTask> tasks = new HashMap<>();

    public ScoreboardManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void startArenaScoreboard(Arena arena) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> update(arena), 0L, 20L);
        tasks.put(arena.getName(), task);
    }

    public void stopArenaScoreboard(Arena arena) {
        BukkitTask task = tasks.remove(arena.getName());
        if (task != null) task.cancel();
        for (UUID uuid : arena.getPlayerTeamMap().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private void update(Arena arena) {
        if (arena.getState() != ArenaState.INGAME) return;
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("tntwars", "dummy", ChatColor.RED + "" + ChatColor.BOLD + "TNT WARS");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        long elapsed = (System.currentTimeMillis() - arena.getGameStartTime()) / 1000;
        String time = String.format("%02d:%02d", elapsed / 60, elapsed % 60);

        int line = arena.getTeams().size() + 3;
        obj.getScore(ChatColor.GRAY + "Durée : " + ChatColor.WHITE + time).setScore(line--);
        obj.getScore(" ").setScore(line--);
        for (Team team : arena.getTeams()) {
            String entry = team.getColoredName() + ChatColor.GRAY + " : " + ChatColor.WHITE + team.getAlive().size() + "/" + team.getMembers().size();
            obj.getScore(entry).setScore(line--);
        }
        obj.getScore(ChatColor.YELLOW + "Arène : " + ChatColor.WHITE + arena.getName()).setScore(line);

        for (UUID uuid : arena.getPlayerTeamMap().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.setScoreboard(board);
        }
        for (UUID uuid : arena.getSpectators()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.setScoreboard(board);
        }
    }
}
