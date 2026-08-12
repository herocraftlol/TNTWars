package com.tntwars.plugin.gui;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.cosmetics.KillEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CosmeticGUI {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ Cosmétiques (effets de kill)";
    public static final NamespacedKey EFFECT_KEY = new NamespacedKey("tntwars", "kill_effect");

    private final TntWarsPlugin plugin;

    public CosmeticGUI(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE);
        KillEffect selected = plugin.getCosmeticManager().getSelected(player);
        int slot = 0;
        for (KillEffect effect : KillEffect.values()) {
            ItemStack item = new ItemStack(effect.getIcon());
            ItemMeta meta = item.getItemMeta();
            boolean isSelected = effect == selected;
            meta.setDisplayName((isSelected ? ChatColor.GREEN + "✔ " : ChatColor.WHITE + "") + effect.getDisplayName());
            meta.setLore(List.of(isSelected ? ChatColor.GRAY + "Effet actuellement équipé" : ChatColor.GRAY + "Cliquez pour équiper"));
            meta.getPersistentDataContainer().set(EFFECT_KEY, PersistentDataType.STRING, effect.name());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }
}
