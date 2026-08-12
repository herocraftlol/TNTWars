package com.tntwars.plugin.gui;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.cosmetics.KillEffect;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class GuiListener implements Listener {

    private final TntWarsPlugin plugin;
    /** Confirmation "cliquez à nouveau pour supprimer" par admin+arène. */
    private final Set<String> pendingDeleteConfirm = new HashSet<>();

    public GuiListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (title.equals(ArenaListGUI.TITLE)) {
            event.setCancelled(true);
            handleArenaListClick(player, event);
        } else if (title.startsWith(ArenaConsoleGUI.TITLE_PREFIX)) {
            event.setCancelled(true);
            handleConsoleClick(player, event, title.substring(ArenaConsoleGUI.TITLE_PREFIX.length()));
        } else if (title.equals(LeaderboardGUI.TITLE)) {
            event.setCancelled(true);
        } else if (title.equals(CosmeticGUI.TITLE)) {
            event.setCancelled(true);
            handleCosmeticClick(player, event);
        }
    }

    private void handleArenaListClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String display = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Arena arena = plugin.getArenaManager().getArena(display);
        if (arena == null) return;
        player.closeInventory();
        plugin.getGameManager().join(player, arena);
    }

    private void handleConsoleClick(Player admin, InventoryClickEvent event, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) return;
        int slot = event.getRawSlot();

        if (slot == ArenaConsoleGUI.SLOT_FORCE_START) {
            plugin.getGameManager().forceStart(arena);
            MessageUtil.send(admin, "§aLancement forcé de l'arène " + arena.getName() + ".");
            admin.closeInventory();
        } else if (slot == ArenaConsoleGUI.SLOT_STOP) {
            if (arena.getState() == ArenaState.INGAME) {
                plugin.getGameManager().endGame(arena, null);
                MessageUtil.send(admin, "§cPartie arrêtée sur " + arena.getName() + ".");
            }
            admin.closeInventory();
        } else if (slot == ArenaConsoleGUI.SLOT_REGEN) {
            plugin.getArenaManager().captureSnapshot(arena);
            MessageUtil.send(admin, "§aSnapshot repris pour " + arena.getName() + ".");
        } else if (slot == ArenaConsoleGUI.SLOT_KICK_ALL) {
            for (var uuid : new java.util.ArrayList<>(arena.getPlayerTeamMap().keySet())) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) plugin.getGameManager().leave(p);
            }
            plugin.getArenaConsoleGUI().open(admin, arena);
        } else if (slot == ArenaConsoleGUI.SLOT_DELETE) {
            String key = admin.getUniqueId() + ":" + arena.getName();
            if (pendingDeleteConfirm.remove(key)) {
                plugin.getArenaManager().deleteArena(arena.getName());
                MessageUtil.send(admin, "§cArène " + arena.getName() + " supprimée.");
                admin.closeInventory();
            } else {
                pendingDeleteConfirm.add(key);
                MessageUtil.send(admin, "§eCliquez à nouveau pour confirmer la suppression de " + arena.getName() + ".");
            }
        } else if (slot >= ArenaConsoleGUI.PLAYERS_START) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta() instanceof SkullMeta skullMeta && skullMeta.getOwningPlayer() != null) {
                Player target = Bukkit.getPlayer(skullMeta.getOwningPlayer().getUniqueId());
                if (target != null) {
                    plugin.getGameManager().leave(target);
                    MessageUtil.send(admin, "§e" + target.getName() + " §7a été expulsé.");
                    plugin.getArenaConsoleGUI().open(admin, arena);
                }
            }
        }
    }

    private void handleCosmeticClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(CosmeticGUI.EFFECT_KEY, PersistentDataType.STRING);
        if (id == null) return;
        KillEffect effect = KillEffect.fromId(id);
        plugin.getCosmeticManager().select(player, effect);
        MessageUtil.send(player, "§aEffet de kill équipé : " + effect.getDisplayName());
        plugin.getCosmeticGUI().open(player);
    }
}
