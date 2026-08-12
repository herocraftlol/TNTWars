package com.tntwars.plugin.arena;

import com.tntwars.plugin.TntWarsPlugin;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Gère les coffres "infinis" des arènes : dès qu'un joueur y prend un item, le contenu
 * est restauré au tick suivant pour que le coffre reste toujours plein. Ces coffres
 * ne peuvent pas être détruits tant que leur arène est configurée.
 */
public class ChestManager {

    private final TntWarsPlugin plugin;

    public ChestManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    /** Cherche à quelle arène / ChestData appartient une location de bloc coffre. */
    public ChestData findChestData(Location blockLocation) {
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            for (ChestData data : arena.getChests()) {
                if (sameBlock(data.getLocation(), blockLocation)) {
                    return data;
                }
            }
        }
        return null;
    }

    public Arena findArenaForChest(Location blockLocation) {
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            for (ChestData data : arena.getChests()) {
                if (sameBlock(data.getLocation(), blockLocation)) {
                    return arena;
                }
            }
        }
        return null;
    }

    public boolean isProtectedChestBlock(Location blockLocation) {
        return findChestData(blockLocation) != null;
    }

    private boolean sameBlock(Location a, Location b) {
        if (a == null || b == null) return false;
        if (a.getWorld() == null || b.getWorld() == null) return false;
        return a.getWorld().equals(b.getWorld()) && a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }

    /** Retourne les ChestData concernées par un inventaire ouvert (gère les doubles coffres). */
    public List<ChestData> resolveChestData(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        List<ChestData> result = new java.util.ArrayList<>();
        if (holder instanceof DoubleChest doubleChest) {
            Chest left = (Chest) doubleChest.getLeftSide();
            Chest right = (Chest) doubleChest.getRightSide();
            if (left != null) {
                ChestData d = findChestData(left.getLocation());
                if (d != null) result.add(d);
            }
            if (right != null) {
                ChestData d = findChestData(right.getLocation());
                if (d != null) result.add(d);
            }
        } else if (holder instanceof Chest chest) {
            ChestData d = findChestData(chest.getLocation());
            if (d != null) result.add(d);
        }
        return result;
    }

    /** Remet le contenu de chaque coffre infini de l'arène à son état plein d'origine. */
    public void refillAll(Arena arena) {
        for (ChestData data : arena.getChests()) {
            refill(data);
        }
    }

    public void refill(ChestData data) {
        if (data.getLocation().getWorld() == null) return;
        if (!data.getLocation().getChunk().isLoaded()) return;
        if (!(data.getLocation().getBlock().getState() instanceof Chest chest)) return;
        Inventory inv = chest.getBlockInventory();
        List<ItemStack> template = data.getTemplate();
        ItemStack[] contents = new ItemStack[inv.getSize()];
        for (int i = 0; i < contents.length && i < template.size(); i++) {
            ItemStack src = template.get(i);
            contents[i] = src == null ? null : src.clone();
        }
        inv.setContents(contents);
    }
}
