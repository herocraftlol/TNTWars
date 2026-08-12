package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.ChestData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;

/**
 * Dès qu'un joueur clique/drag dans un coffre "infini" d'une arène, on reprogramme
 * le remplissage complet du coffre au tick suivant afin qu'il ne se vide jamais.
 */
public class ChestRefillListener implements Listener {

    private final TntWarsPlugin plugin;

    public ChestRefillListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        List<ChestData> chests = plugin.getChestManager().resolveChestData(event.getView().getTopInventory());
        if (chests.isEmpty()) return;
        // On laisse l'événement se dérouler normalement (le joueur reçoit bien l'item),
        // puis on force le coffre à redevenir plein au tick suivant.
        plugin.getServer().getScheduler().runTask(plugin, () -> chests.forEach(plugin.getChestManager()::refill));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        List<ChestData> chests = plugin.getChestManager().resolveChestData(event.getInventory());
        if (chests.isEmpty()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> chests.forEach(plugin.getChestManager()::refill));
    }
}
