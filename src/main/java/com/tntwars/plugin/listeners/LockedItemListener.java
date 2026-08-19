package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Empêche tout déplacement d'un item "verrouillé" (identifié par la clé persistante
 * {@link GameManager#LOCKED_ITEM_KEY}, ex. la pioche du bâtisseur en slot 0) :
 * il ne peut être ni bougé dans l'inventaire, ni jeté au sol, ni échangé avec la
 * main secondaire (touche F).
 */
public class LockedItemListener implements Listener {

    private final TntWarsPlugin plugin;

    public LockedItemListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isLocked(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(GameManager.LOCKED_ITEM_KEY, PersistentDataType.STRING);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (isLocked(event.getCurrentItem()) || isLocked(event.getCursor())) {
            event.setCancelled(true);
            return;
        }
        if (event.getClick() == ClickType.NUMBER_KEY && event.getWhoClicked() instanceof Player player) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (isLocked(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (isLocked(item)) {
                event.setCancelled(true);
                return;
            }
        }
        if (isLocked(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (isLocked(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (isLocked(event.getMainHandItem()) || isLocked(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }
}
