package com.tntwars.plugin.gui;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI listant toutes les arènes disponibles, avec leur statut (en attente, en cours,
 * complet...) et le nombre de joueurs. Cliquer sur une arène la rejoint.
 */
public class ArenaListGUI {

    public static final String TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚔ Arènes TNT Wars";

    private final TntWarsPlugin plugin;

    public ArenaListGUI(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getArenas());
        int size = Math.max(9, (int) (Math.ceil(arenas.size() / 9.0) * 9));
        size = Math.min(size, 54);
        Inventory inv = plugin.getServer().createInventory(null, size, TITLE);
        for (Arena arena : arenas) {
            inv.addItem(buildIcon(arena));
        }
        player.openInventory(inv);
    }

    private ItemStack buildIcon(Arena arena) {
        Material material = switch (arena.getState()) {
            case WAITING -> Material.LIME_CONCRETE;
            case STARTING -> Material.YELLOW_CONCRETE;
            case INGAME -> Material.RED_CONCRETE;
            case RESTARTING -> Material.GRAY_CONCRETE;
            case DISABLED -> Material.BARRIER;
        };
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + arena.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Statut : " + stateLabel(arena.getState()));
        lore.add(ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + arena.totalPlayers() + "/" + (arena.getTeamsCount() * arena.getTeamSize()));
        lore.add(ChatColor.GRAY + "Équipes : " + ChatColor.WHITE + arena.getTeamsCount() + " x " + arena.getTeamSize());
        if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING) {
            lore.add("");
            lore.add(ChatColor.GREEN + "▶ Cliquez pour rejoindre");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String stateLabel(ArenaState state) {
        return switch (state) {
            case WAITING -> ChatColor.GREEN + "En attente";
            case STARTING -> ChatColor.YELLOW + "Démarrage imminent";
            case INGAME -> ChatColor.RED + "Partie en cours";
            case RESTARTING -> ChatColor.GRAY + "Régénération...";
            case DISABLED -> ChatColor.DARK_GRAY + "Non configurée";
        };
    }
}
