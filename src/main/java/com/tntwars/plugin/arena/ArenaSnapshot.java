package com.tntwars.plugin.arena;

import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sauvegarde/restauration de tous les blocs d'une {@link CuboidRegion} (la map de base
 * de l'arène). Utilisé pour régénérer la map exactement comme elle était au début de
 * la partie, même si les joueurs ont fait exploser des blocs pendant celle-ci.
 *
 * La restauration se fait par lots répartis sur plusieurs ticks pour éviter de geler
 * le serveur sur les grosses arènes.
 */
public class ArenaSnapshot {

    private final CuboidRegion region;
    private BlockData[] data;
    private boolean captured = false;

    public ArenaSnapshot(CuboidRegion region) {
        this.region = region;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void capture() {
        World world = region.getWorld();
        if (world == null) return;
        int sizeX = region.getMaxX() - region.getMinX() + 1;
        int sizeY = region.getMaxY() - region.getMinY() + 1;
        int sizeZ = region.getMaxZ() - region.getMinZ() + 1;
        data = new BlockData[sizeX * sizeY * sizeZ];
        int i = 0;
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    data[i++] = world.getBlockAt(x, y, z).getBlockData().clone();
                }
            }
        }
        captured = true;
    }

    /**
     * Restaure la map de façon asynchrone/étalée sur plusieurs ticks.
     *
     * @param plugin   le plugin (pour planifier les tâches)
     * @param onFinish callback exécuté une fois la restauration terminée (thread principal)
     */
    public void restore(Plugin plugin, Runnable onFinish) {
        if (!captured) {
            if (onFinish != null) onFinish.run();
            return;
        }
        World world = region.getWorld();
        if (world == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        int sizeY = region.getMaxY() - region.getMinY() + 1;
        int sizeZ = region.getMaxZ() - region.getMinZ() + 1;

        final int totalBlocks = data.length;
        final int[] cursor = {0};
        final int blocksPerTick = Math.max(2000, totalBlocks / 200); // s'étale sur ~200 ticks max (10s)

        new BukkitRunnable() {
            @Override
            public void run() {
                int processed = 0;
                while (processed < blocksPerTick && cursor[0] < totalBlocks) {
                    int i = cursor[0];
                    int x = region.getMinX() + (i / (sizeY * sizeZ));
                    int rem = i % (sizeY * sizeZ);
                    int y = region.getMinY() + (rem / sizeZ);
                    int z = region.getMinZ() + (rem % sizeZ);
                    world.getBlockAt(x, y, z).setBlockData(data[i], false);
                    cursor[0]++;
                    processed++;
                }
                if (cursor[0] >= totalBlocks) {
                    cancel();
                    if (onFinish != null) onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public List<BlockData> getRawCopy() {
        return data == null ? new ArrayList<>() : List.of(data);
    }
}
