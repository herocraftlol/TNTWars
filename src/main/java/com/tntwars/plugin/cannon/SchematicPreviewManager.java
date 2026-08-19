package com.tntwars.plugin.cannon;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Affiche un schéma de canon en "blocs fantômes" : des changements de blocs envoyés
 * uniquement au client du joueur (via {@link Player#sendBlockChange}), sans jamais
 * toucher au monde réel. Le schéma est orienté pour faire face à la direction regardée
 * par le joueur (arrondie au cap cardinal le plus proche) et posé devant lui.
 *
 * Les blocs se posent progressivement, un par un (~1 toutes les 1/3 de seconde), pour
 * bien montrer l'ordre de construction. L'aperçu (fini ou non) disparaît automatiquement
 * après un délai, quand le joueur en relance un autre, ou avec /tnt schema hide.
 */
public class SchematicPreviewManager {

    private final TntWarsPlugin plugin;

    /** Blocs déjà affichés (donc à restaurer) pour chaque joueur ayant un aperçu actif. */
    private final Map<UUID, List<Location>> shownBlocks = new HashMap<>();
    /** Tâche de construction progressive en cours, par joueur. */
    private final Map<UUID, BukkitTask> buildTasks = new HashMap<>();
    /** Tâche de disparition automatique programmée, par joueur. */
    private final Map<UUID, BukkitTask> expireTasks = new HashMap<>();

    private static final long TICKS_PER_BLOCK = 7L; // ~1/3 de seconde (20 ticks/s ÷ 3 ≈ 6.7, arrondi à 7)

    public SchematicPreviewManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, CannonSchematic schema) {
        hide(player); // annule proprement tout aperçu (fini ou en cours de construction) déjà affiché

        UUID uuid = player.getUniqueId();
        Location origin = computeOrigin(player);
        Vector forward = flattenedDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX());

        // Précalcule les positions cibles (alignées sur la grille de blocs) une seule fois.
        List<PlannedBlock> plan = new ArrayList<>();
        for (RelativeBlock rb : schema.getBlocks()) {
            Location loc = origin.clone()
                    .add(right.clone().multiply(rb.getDx()))
                    .add(new Vector(0, rb.getDy(), 0))
                    .add(forward.clone().multiply(rb.getDz()));
            loc = loc.getBlock().getLocation();
            plan.add(new PlannedBlock(loc, rb));
        }

        shownBlocks.put(uuid, new ArrayList<>());
        MessageUtil.send(player, "§d✦ Construction du schéma §f" + schema.getDisplayName() + " §den cours devant vous...");
        MessageUtil.send(player, "§7(visible par vous seul — /tnt schema hide pour l'annuler)");

        int[] index = {0};
        BukkitTask buildTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                hide(player);
                return;
            }
            if (index[0] >= plan.size()) {
                return; // sécurité, ne devrait pas arriver (la tâche est annulée juste après le dernier bloc)
            }
            PlannedBlock next = plan.get(index[0]);
            player.sendBlockChange(next.location(), next.relativeBlock().toBlockData());
            List<Location> shown = shownBlocks.get(uuid);
            if (shown != null) shown.add(next.location());
            index[0]++;

            if (index[0] >= plan.size()) {
                BukkitTask self = buildTasks.remove(uuid);
                if (self != null) self.cancel();
                MessageUtil.send(player, "§a✔ Construction terminée (" + plan.size() + " blocs). Disparaît dans " + previewSeconds() + "s.");
                BukkitTask expireTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> hide(player), previewSeconds() * 20L);
                expireTasks.put(uuid, expireTask);
            }
        }, 0L, TICKS_PER_BLOCK);

        buildTasks.put(uuid, buildTask);
    }

    public void hide(Player player) {
        UUID uuid = player.getUniqueId();

        BukkitTask buildTask = buildTasks.remove(uuid);
        if (buildTask != null) buildTask.cancel();
        BukkitTask expireTask = expireTasks.remove(uuid);
        if (expireTask != null) expireTask.cancel();

        List<Location> shown = shownBlocks.remove(uuid);
        if (shown == null || !player.isOnline()) return;
        for (Location loc : shown) {
            Block real = loc.getBlock();
            player.sendBlockChange(loc, real.getBlockData());
        }
    }

    public boolean hasActivePreview(Player player) {
        return shownBlocks.containsKey(player.getUniqueId());
    }

    public void hideAll() {
        for (UUID uuid : new ArrayList<>(shownBlocks.keySet())) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                hide(p);
            } else {
                BukkitTask bt = buildTasks.remove(uuid);
                if (bt != null) bt.cancel();
                BukkitTask et = expireTasks.remove(uuid);
                if (et != null) et.cancel();
                shownBlocks.remove(uuid);
            }
        }
    }

    private int previewSeconds() {
        return plugin.getConfig().getInt("schematics.preview-seconds", 25);
    }

    private Location computeOrigin(Player player) {
        Location base = player.getLocation().getBlock().getLocation();
        Vector forward = flattenedDirection(player);
        return base.add(forward.clone().multiply(2)); // le schéma commence 2 blocs devant le joueur
    }

    /** Direction horizontale (Y=0) normalisée sur laquelle le joueur regarde, arrondie sur les 4 axes cardinaux. */
    private Vector flattenedDirection(Player player) {
        double yaw = ((player.getLocation().getYaw() % 360) + 360) % 360;
        double[] dirs = {0, 90, 180, 270};
        double closest = dirs[0];
        double bestDiff = Double.MAX_VALUE;
        for (double d : dirs) {
            double diff = Math.min(Math.abs(yaw - d), 360 - Math.abs(yaw - d));
            if (diff < bestDiff) {
                bestDiff = diff;
                closest = d;
            }
        }
        // yaw 0 = sud (+Z), 90 = ouest (-X), 180 = nord (-Z), 270 = est (+X) en Bukkit
        if (closest == 0) return new Vector(0, 0, 1);
        if (closest == 90) return new Vector(-1, 0, 0);
        if (closest == 180) return new Vector(0, 0, -1);
        return new Vector(1, 0, 0);
    }

    private record PlannedBlock(Location location, RelativeBlock relativeBlock) {
    }
}
