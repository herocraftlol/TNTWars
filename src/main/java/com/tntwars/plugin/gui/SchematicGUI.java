package com.tntwars.plugin.gui;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.cannon.CannonSchematic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI listant tous les schémas de canons à TNT : débloqués (cliquables pour lancer la
 * construction réelle, bloc par bloc, dans sa propre zone) ou verrouillés (affiche le
 * niveau requis).
 */
public class SchematicGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Schémas de canons à TNT";
    public static final NamespacedKey SCHEMA_KEY = new NamespacedKey("tntwars", "schema_id");

    private final TntWarsPlugin plugin;

    public SchematicGUI(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        List<CannonSchematic> schematics = plugin.getSchematicRegistry().all();
        int size = Math.max(9, (int) Math.ceil(schematics.size() / 9.0) * 9);
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        int level = plugin.getProgressManager().getLevel(player.getUniqueId());
        for (CannonSchematic schema : schematics) {
            boolean unlocked = plugin.getProgressManager().isUnlocked(player.getUniqueId(), schema);
            inv.addItem(buildIcon(schema, unlocked));
        }

        player.openInventory(inv);
    }

    private ItemStack buildIcon(CannonSchematic schema, boolean unlocked) {
        ItemStack item = new ItemStack(unlocked ? schema.getIcon() : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((unlocked ? ChatColor.LIGHT_PURPLE : ChatColor.DARK_GRAY) + "" + ChatColor.BOLD + schema.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau requis : " + ChatColor.WHITE + schema.getRequiredLevel());
        if (unlocked) {
            lore.add("");
            lore.add(ChatColor.GREEN + "▶ Cliquez pour construire (en jeu, dans votre zone)");
            lore.add(ChatColor.GRAY + "Blocs réels posés petit à petit devant vous.");
        } else {
            lore.add("");
            lore.add(ChatColor.RED + "🔒 Verrouillé");
        }
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(SCHEMA_KEY, PersistentDataType.STRING, schema.getId());
        item.setItemMeta(meta);
        return item;
    }
}
