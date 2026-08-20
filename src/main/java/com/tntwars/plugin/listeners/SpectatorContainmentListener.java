package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Pendant la phase de fin de partie (régénération de la map, joueurs passés spectateurs
 * le temps de voir le résultat), empêche les joueurs de sortir de la zone de la map en
 * volant hors des limites : le déplacement en dehors est annulé (la rotation de la
 * caméra reste libre).
 */
public class SpectatorContainmentListener implements Listener {

    private final TntWarsPlugin plugin;

    public SpectatorContainmentListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null || arena.getState() != ArenaState.RESTARTING) return;
        if (arena.getMapRegion() == null) return;

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null) return;
        if (arena.getMapRegion().contains(to)) return;

        // Bloque la position mais conserve la direction du regard (évite un effet saccadé
        // en spectateur quand on regarde juste autour de soi près d'un bord).
        Location corrected = new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch());
        event.setTo(corrected);
    }
}
