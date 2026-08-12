package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Gère la mort d'un joueur en partie : pas de drop, élimination (passage spectateur)
 * et vérification de la victoire, respawn immédiat en tant que spectateur au dessus
 * de sa map.
 */
public class PlayerCombatListener implements Listener {

    private final TntWarsPlugin plugin;

    public PlayerCombatListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null || arena.getState() != ArenaState.INGAME) return;

        event.setCancelled(false);
        event.getDrops().clear();
        event.setDeathMessage(null);
        event.setKeepInventory(false);

        if (plugin.getStatsManager() != null) {
            Player killer = player.getKiller();
            if (killer != null && !killer.equals(player)) {
                plugin.getStatsManager().addKill(killer.getUniqueId());
                plugin.getCosmeticManager().playKillEffect(killer, player.getLocation());
            }
        }

        plugin.getGameManager().eliminatePlayer(player, arena, "");

        // Force le respawn immédiat (pas d'écran de mort à attendre) même sans le gamerule doImmediateRespawn.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && player.isDead()) {
                player.spigot().respawn();
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null) return;
        Team team = arena.getTeamOf(player);
        if (team != null && team.getSpawn() != null) {
            event.setRespawnLocation(team.getSpawn().clone().add(0, 15, 0));
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> player.setGameMode(GameMode.SPECTATOR));
    }
}
