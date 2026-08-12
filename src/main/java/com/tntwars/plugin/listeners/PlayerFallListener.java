package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Élimine un joueur qui tombe en dessous de la limite basse de la map de l'arène
 * (typiquement en tombant dans le vide après que le sol de sa zone a été détruit).
 */
public class PlayerFallListener implements Listener {

    private final TntWarsPlugin plugin;

    public PlayerFallListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null || arena.getState() != ArenaState.INGAME) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (arena.getTeamOf(player) == null || !arena.getTeamOf(player).getAlive().contains(player.getUniqueId())) return;

        int minY = arena.getMapRegion() != null ? arena.getMapRegion().getMinY() : Integer.MIN_VALUE;
        int offset = plugin.getConfig().getInt("game.spectator-fall-y-offset", 0);
        if (event.getTo().getY() < minY - offset) {
            plugin.getGameManager().eliminatePlayer(player, arena, "en tombant hors de la zone");
        }
    }
}
