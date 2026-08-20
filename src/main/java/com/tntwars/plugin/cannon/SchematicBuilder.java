package com.tntwars.plugin.cannon;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Construit un schéma de canon en posant pour de vrai les blocs dans le monde, un par
 * un (~1 toutes les 1/3 de seconde), pour que les joueurs voient la construction se
 * dérouler en temps réel. Contrairement à un aperçu, les blocs posés sont réels et
 * persistent (ils font partie de la vraie map, comme s'ils avaient été posés à la main).
 *
 * Pour éviter tout abus, la construction n'est autorisée que pendant une partie en
 * cours, et uniquement si l'intégralité du schéma tient dans la zone de construction de
 * l'équipe du joueur (les mêmes règles de zone que la construction manuelle s'appliquent
 * donc de fait).
 */
public class SchematicBuilder {

    private final TntWarsPlugin plugin;

    /** Tâche de construction en cours par joueur (pour pouvoir l'annuler avec /tnt schema cancel). */
    private final Map<UUID, BukkitTask> buildTasks = new HashMap<>();

    private static final long TICKS_PER_BLOCK = 7L; // ~1/3 de seconde (20 ticks/s ÷ 3 ≈ 6.7, arrondi à 7)

    public SchematicBuilder(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void build(Player player, CannonSchematic schema) {
        cancel(player); // annule une éventuelle construction déjà en cours pour ce joueur

        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null || arena.getState() != ArenaState.INGAME) {
            MessageUtil.send(player, "§cVous devez être en pleine partie pour construire ce schéma.");
            return;
        }
        Team team = arena.getTeamOf(player);
        if (team == null || team.getZone() == null) {
            MessageUtil.send(player, "§cLa zone de votre équipe n'est pas définie sur cette arène.");
            return;
        }

        Location origin = computeOrigin(player);
        Vector forward = flattenedDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX());

        List<PlannedBlock> plan = new ArrayList<>();
        for (RelativeBlock rb : schema.getBlocks()) {
            Location loc = origin.clone()
                    .add(right.clone().multiply(rb.getDx()))
                    .add(new Vector(0, rb.getDy(), 0))
                    .add(forward.clone().multiply(rb.getDz()));
            loc = loc.getBlock().getLocation();
            plan.add(new PlannedBlock(loc, rb));
        }

        // Le canon doit tenir entièrement dans la zone de l'équipe, sinon on refuse de construire
        // (mêmes règles que la construction manuelle : impossible d'empiéter sur la zone adverse),
        // et ne doit jamais recouvrir un coffre infini protégé.
        for (PlannedBlock pb : plan) {
            if (!team.getZone().contains(pb.location())) {
                MessageUtil.send(player, "§cLe canon ne tient pas entièrement dans votre zone à cet endroit.");
                MessageUtil.send(player, "§7Replacez-vous plus au centre de votre zone, en regardant vers l'intérieur, puis réessayez.");
                return;
            }
            if (plugin.getChestManager().isProtectedChestBlock(pb.location())) {
                MessageUtil.send(player, "§cLe canon chevaucherait le coffre infini de l'arène. Choisissez un autre emplacement.");
                return;
            }
        }

        int seconds = (int) Math.ceil(plan.size() * TICKS_PER_BLOCK / 20.0);
        MessageUtil.send(player, "§a🔨 Construction du schéma §f" + schema.getDisplayName() + " §aen cours (" + plan.size() + " blocs, ~" + seconds + "s)...");
        plugin.getGameManager().broadcastArena(arena, "§7" + player.getName() + " §7construit un §f" + schema.getDisplayName() + "§7.");

        UUID uuid = player.getUniqueId();
        int[] index = {0};
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || index[0] >= plan.size()) {
                BukkitTask self = buildTasks.remove(uuid);
                if (self != null) self.cancel();
                return;
            }
            PlannedBlock next = plan.get(index[0]);
            next.location().getBlock().setBlockData(next.relativeBlock().toBlockData(), false);
            index[0]++;

            if (index[0] >= plan.size()) {
                BukkitTask self = buildTasks.remove(uuid);
                if (self != null) self.cancel();
                fillDispensersWithTnt(plan);
                MessageUtil.send(player, "§a✔ Construction terminée (" + plan.size() + " blocs posés). Pensez à activer le(s) levier(s)/bouton(s) !");
            }
        }, 0L, TICKS_PER_BLOCK);

        buildTasks.put(uuid, task);
    }

    /** Annule une construction de schéma en cours pour ce joueur (les blocs déjà posés restent). */
    public void cancel(Player player) {
        BukkitTask task = buildTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            MessageUtil.send(player, "§7Construction du schéma annulée (les blocs déjà posés restent en place).");
        }
    }

    public boolean isBuilding(Player player) {
        return buildTasks.containsKey(player.getUniqueId());
    }

    public void cancelAll() {
        for (BukkitTask task : buildTasks.values()) {
            task.cancel();
        }
        buildTasks.clear();
    }

    /** Remplit automatiquement de TNT tous les distributeurs qui viennent d'être posés, pour un canon prêt à tirer. */
    private void fillDispensersWithTnt(List<PlannedBlock> plan) {
        for (PlannedBlock pb : plan) {
            if (pb.location().getBlock().getType() != Material.DISPENSER) continue;
            if (!(pb.location().getBlock().getState() instanceof Dispenser dispenser)) continue;
            ItemStack[] contents = new ItemStack[dispenser.getInventory().getSize()];
            for (int i = 0; i < contents.length; i++) {
                contents[i] = new ItemStack(Material.TNT, 64);
            }
            dispenser.getInventory().setContents(contents);
            dispenser.update(true, false);
        }
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
