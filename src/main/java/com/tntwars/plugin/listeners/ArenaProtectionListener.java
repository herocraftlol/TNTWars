package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.Iterator;

/**
 * Protège les coffres infinis (indestructibles) et restreint la construction :
 * pendant une partie, chaque joueur ne peut construire/casser que dans la zone de
 * SA propre équipe. En dehors d'une partie, seuls les admins peuvent modifier la map.
 */
public class ArenaProtectionListener implements Listener {

    private final TntWarsPlugin plugin;

    public ArenaProtectionListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    private Arena findArenaAt(Location loc) {
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getMapRegion() != null && arena.getMapRegion().contains(loc)) {
                return arena;
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event.getBlock().getLocation(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event.getBlock().getLocation(), event);
    }

    private void handle(Player player, Location loc, org.bukkit.event.Cancellable event) {
        if (plugin.getChestManager().isProtectedChestBlock(loc)) {
            event.setCancelled(true);
            MessageUtil.send(player, "§cCe coffre est protégé, il ne peut pas être modifié.");
            return;
        }

        Arena arena = findArenaAt(loc);
        if (arena == null) return;

        if (player.hasPermission("tntwars.admin") && !arena.isFullyConfigured()) {
            return; // configuration initiale de l'arène par un admin
        }

        if (arena.getState() != ArenaState.INGAME) {
            if (!player.hasPermission("tntwars.admin")) {
                event.setCancelled(true);
                MessageUtil.send(player, "§cVous ne pouvez pas construire ici en dehors d'une partie.");
            }
            return;
        }

        Team own = arena.getTeamOf(player);
        if (own == null) {
            event.setCancelled(true);
            return;
        }
        if (own.getZone() == null || !own.getZone().contains(loc)) {
            event.setCancelled(true);
            MessageUtil.send(player, "§cVous ne pouvez construire que dans la zone de votre équipe !");
        }
    }

    /** Empêche les explosions de TNT de casser les coffres infinis ou de sortir de la map définie. */
    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        Arena arena = findArenaAt(event.getLocation());
        List<Block> blocks = event.blockList();
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block b = it.next();
            if (plugin.getChestManager().isProtectedChestBlock(b.getLocation())) {
                it.remove();
                continue;
            }
            if (arena != null && arena.getMapRegion() != null && !arena.getMapRegion().contains(b.getLocation())) {
                it.remove();
            }
        }
    }
}
