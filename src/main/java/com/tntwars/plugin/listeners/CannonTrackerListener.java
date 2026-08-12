package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Attribue chaque TNT amorcée à l'équipe dans la zone de laquelle elle a été activée,
 * et récompense l'équipe en points de progression :
 *  - un peu de points à chaque TNT envoyée depuis sa propre zone,
 *  - plus de points par bloc adverse détruit par cette TNT,
 *  - un gros bonus la première fois qu'un canon touche effectivement la zone adverse
 *    dans une partie donnée ("canon fonctionnel").
 *
 * Attribution approximative par nature (une TNT qui provoque une réaction en chaîne
 * n'est pas retracée bloc par bloc), mais suffisante pour un système de progression.
 */
public class CannonTrackerListener implements Listener {

    private record Origin(String arenaName, int teamIndex) {
    }

    private final TntWarsPlugin plugin;
    private final Map<UUID, Origin> tracked = new HashMap<>();

    public CannonTrackerListener(TntWarsPlugin plugin) {
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
    public void onPrime(TNTPrimeEvent event) {
        Block block = event.getBlock();
        Arena arena = findArenaAt(block.getLocation());
        if (arena == null || arena.getState() != ArenaState.INGAME) return;

        Team owner = null;
        for (Team t : arena.getTeams()) {
            if (t.getZone() != null && t.getZone().contains(block.getLocation())) {
                owner = t;
                break;
            }
        }
        if (owner == null) return; // TNT amorcée en zone neutre : pas attribuée

        Team finalOwner = owner;
        Arena finalArena = arena;
        int points = plugin.getProgressManager().configuredPoints("tnt-launched");
        plugin.getProgressManager().awardTeamPoints(arena, owner, points, "TNT envoyée");

        // La TNT devient une entité TNTPrimed au tick suivant : on la retrouve pour la suivre jusqu'à l'explosion.
        Location loc = block.getLocation();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (loc.getWorld() == null) return;
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                if (e instanceof TNTPrimed && !tracked.containsKey(e.getUniqueId())) {
                    tracked.put(e.getUniqueId(), new Origin(finalArena.getName(), finalOwner.getIndex()));
                    break;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        Origin origin = tracked.remove(event.getEntity().getUniqueId());
        if (origin == null) return;

        Arena arena = plugin.getArenaManager().getArena(origin.arenaName());
        if (arena == null || arena.getState() != ArenaState.INGAME) return;
        Team originTeam = arena.getTeam(origin.teamIndex());
        if (originTeam == null) return;

        int destroyedEnemyBlocks = 0;
        for (Block b : event.blockList()) {
            for (Team other : arena.getTeams()) {
                if (other == originTeam) continue;
                if (other.getZone() != null && other.getZone().contains(b.getLocation())) {
                    destroyedEnemyBlocks++;
                    break;
                }
            }
        }
        if (destroyedEnemyBlocks == 0) return;

        int perBlock = plugin.getProgressManager().configuredPoints("enemy-block-destroyed");
        plugin.getProgressManager().awardTeamPoints(arena, originTeam, perBlock * destroyedEnemyBlocks, "dégâts sur la zone adverse");

        if (arena.getFunctionalCannonTeams().add(originTeam.getIndex())) {
            int bonus = plugin.getProgressManager().configuredPoints("cannon-functional-bonus");
            plugin.getProgressManager().awardTeamPoints(arena, originTeam, bonus, "canon fonctionnel !");
            plugin.getGameManager().broadcastArena(arena, "§d✦ L'équipe " + originTeam.getColoredName()
                    + " §da fait fonctionner un canon à TNT jusqu'à la zone adverse !");
        }
    }

    public void clear() {
        tracked.clear();
    }
}
