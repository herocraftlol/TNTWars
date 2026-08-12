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
 * par le joueur et posé devant lui. L'aperçu disparaît automatiquement après un délai
 * configurable, quand le joueur en ouvre un autre, ou avec /tnt schema hide.
 */
public class SchematicPreviewManager {

    private final TntWarsPlugin plugin;
    private final Map<UUID, List<Location>> activePreviews = new HashMap<>();
    private final Map<UUID, BukkitTask> expireTasks = new HashMap<>();

    public SchematicPreviewManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, CannonSchematic schema) {
        hide(player); // annule un éventuel aperçu précédent

        Location origin = computeOrigin(player);
        Vector forward = flattenedDirection(player);
        // Base orthogonale horizontale (perpendiculaire à "forward") pour l'axe X du schéma
        Vector right = new Vector(-forward.getZ(), 0, forward.getX());

        List<Location> touched = new ArrayList<>();
        for (RelativeBlock rb : schema.getBlocks()) {
            Location loc = origin.clone()
                    .add(right.clone().multiply(rb.getDx()))
                    .add(new Vector(0, rb.getDy(), 0))
                    .add(forward.clone().multiply(rb.getDz()));
            loc = loc.getBlock().getLocation(); // aligne sur la grille de blocs
            player.sendBlockChange(loc, rb.toBlockData());
            touched.add(loc);
        }
        activePreviews.put(player.getUniqueId(), touched);

        MessageUtil.send(player, "§d✦ Aperçu du schéma §f" + schema.getDisplayName() + " §d affiché devant vous (visible par vous seul).");
        MessageUtil.send(player, "§7L'aperçu disparaît dans " + previewSeconds() + "s, ou avec /tnt schema hide.");

        int ticks = previewSeconds() * 20;
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> hide(player), ticks);
        expireTasks.put(player.getUniqueId(), task);
    }

    public void hide(Player player) {
        List<Location> touched = activePreviews.remove(player.getUniqueId());
        BukkitTask task = expireTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
        if (touched == null) return;
        for (Location loc : touched) {
            if (!player.isOnline()) continue;
            Block real = loc.getBlock();
            player.sendBlockChange(loc, real.getBlockData());
        }
    }

    public boolean hasActivePreview(Player player) {
        return activePreviews.containsKey(player.getUniqueId());
    }

    public void hideAll() {
        for (UUID uuid : new ArrayList<>(activePreviews.keySet())) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) hide(p);
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
        Location loc = player.getLocation();
        float yaw = loc.getYaw();
        // Arrondit au cap cardinal le plus proche pour garder un schéma net aligné sur la grille.
        double[] dirs = {0, 90, 180, 270, 360};
        double normalizedYaw = ((yaw % 360) + 360) % 360;
        double closest = dirs[0];
        double bestDiff = Double.MAX_VALUE;
        for (double d : dirs) {
            double diff = Math.abs(normalizedYaw - d);
            if (diff < bestDiff) {
                bestDiff = diff;
                closest = d;
            }
        }
        closest = closest % 360;
        // yaw 0 = sud (+Z), 90 = ouest (-X), 180 = nord (-Z), 270 = est (+X) en Bukkit
        if (closest == 0) return new Vector(0, 0, 1);
        if (closest == 90) return new Vector(-1, 0, 0);
        if (closest == 180) return new Vector(0, 0, -1);
        return new Vector(1, 0, 0);
    }
}
