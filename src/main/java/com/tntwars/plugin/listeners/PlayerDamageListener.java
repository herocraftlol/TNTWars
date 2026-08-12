package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final TntWarsPlugin plugin;

    public PlayerDamageListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null) return;

        // Aucun dégât en dehors d'une partie en cours (lobby / attente / spectateur).
        if (arena.getState() != ArenaState.INGAME) {
            event.setCancelled(true);
            return;
        }
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getConfig().getBoolean("game.friendly-fire", false) && event instanceof EntityDamageByEntityEvent e) {
            if (e.getDamager() instanceof Player attacker) {
                Team victimTeam = arena.getTeamOf(player);
                Team attackerTeam = arena.getTeamOf(attacker);
                if (victimTeam != null && victimTeam.equals(attackerTeam)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
