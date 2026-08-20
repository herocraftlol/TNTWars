package com.tntwars.plugin.gui;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * "Console" d'administration propre à une arène : permet à un admin de démarrer,
 * arrêter, régénérer, vider ou supprimer une arène, et d'expulser les joueurs
 * présents, sans avoir à taper de commandes.
 */
public class ArenaConsoleGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_GRAY + "Console » ";

    public static final int SLOT_STATUS = 4;
    public static final int SLOT_FORCE_START = 10;
    public static final int SLOT_STOP = 12;
    public static final int SLOT_REGEN = 14;
    public static final int SLOT_KICK_ALL = 16;
    public static final int SLOT_DELETE = 22;
    public static final int SLOT_UNSTUCK = 24;
    public static final int PLAYERS_START = 27;
    public static final int PLAYERS_END = 44; // inclus

    private final TntWarsPlugin plugin;

    public ArenaConsoleGUI(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public String titleFor(Arena arena) {
        return TITLE_PREFIX + arena.getName();
    }

    public void open(Player admin, Arena arena) {
        Inventory inv = Bukkit.createInventory(null, 45, titleFor(arena));

        inv.setItem(SLOT_STATUS, named(Material.PAPER, ChatColor.AQUA + "Arène : " + ChatColor.WHITE + arena.getName(),
                List.of(ChatColor.GRAY + "État : " + arena.getState(),
                        ChatColor.GRAY + "Joueurs : " + arena.totalPlayers() + "/" + (arena.getTeamsCount() * arena.getTeamSize()),
                        ChatColor.GRAY + "Configurée : " + (arena.isFullyConfigured() ? ChatColor.GREEN + "Oui" : ChatColor.RED + "Non"))));

        inv.setItem(SLOT_FORCE_START, named(Material.DIAMOND, ChatColor.GREEN + "Forcer le lancement",
                List.of(ChatColor.GRAY + "Démarre la partie immédiatement.")));

        inv.setItem(SLOT_STOP, named(Material.REDSTONE_BLOCK, ChatColor.RED + "Arrêter la partie",
                List.of(ChatColor.GRAY + "Termine la partie en cours (sans vainqueur) et régénère la map.")));

        inv.setItem(SLOT_REGEN, named(Material.GRASS_BLOCK, ChatColor.YELLOW + "Régénérer la map",
                List.of(ChatColor.GRAY + "Restaure la map à son état d'origine.")));

        inv.setItem(SLOT_KICK_ALL, named(Material.BARRIER, ChatColor.RED + "Expulser tout le monde",
                List.of(ChatColor.GRAY + "Renvoie tous les joueurs à leur position d'avant /tnt join.")));

        inv.setItem(SLOT_DELETE, named(Material.TNT, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Supprimer l'arène",
                List.of(ChatColor.GRAY + "Supprime définitivement cette arène.", ChatColor.RED + "Cliquez à nouveau pour confirmer.")));

        inv.setItem(SLOT_UNSTUCK, named(Material.CLOCK, ChatColor.GOLD + "" + ChatColor.BOLD + "Débloquer l'arène",
                List.of(ChatColor.GRAY + "Force le retour à l'état 'en attente'",
                        ChatColor.GRAY + "si l'arène reste coincée en régénération.",
                        ChatColor.GRAY + "Équivalent à /tnt debug " + arena.getName() + " fix")));

        int slot = PLAYERS_START;
        for (UUID uuid : arena.getPlayerTeamMap().keySet()) {
            if (slot > PLAYERS_END) break;
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(p);
            meta.setDisplayName(ChatColor.YELLOW + p.getName());
            meta.setLore(List.of(ChatColor.GRAY + "Cliquez pour expulser"));
            skull.setItemMeta(meta);
            inv.setItem(slot++, skull);
        }

        admin.openInventory(inv);
    }

    private ItemStack named(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(new ArrayList<>(lore));
        item.setItemMeta(meta);
        return item;
    }
}
