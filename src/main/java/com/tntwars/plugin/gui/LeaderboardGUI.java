package com.tntwars.plugin.gui;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class LeaderboardGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "🏆 Classement TNT Wars";

    private final TntWarsPlugin plugin;

    public LeaderboardGUI(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        List<PlayerStats> topKills = plugin.getStatsManager().topKills(9);
        List<PlayerStats> topWins = plugin.getStatsManager().topWins(9);

        for (int i = 0; i < 9 && i < topKills.size(); i++) {
            inv.setItem(i, statHead(topKills.get(i), "Kills : " + topKills.get(i).getKills()));
        }
        for (int i = 0; i < 9 && i < topWins.size(); i++) {
            inv.setItem(9 + i, statHead(topWins.get(i), "Victoires : " + topWins.get(i).getWins()));
        }

        PlayerStats mine = plugin.getStatsManager().get(player.getUniqueId());
        ItemStack myStats = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = myStats.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Mes statistiques");
        meta.setLore(List.of(
                ChatColor.GRAY + "Kills : " + ChatColor.WHITE + mine.getKills(),
                ChatColor.GRAY + "Morts : " + ChatColor.WHITE + mine.getDeaths(),
                ChatColor.GRAY + "K/D : " + ChatColor.WHITE + String.format("%.2f", mine.getKD()),
                ChatColor.GRAY + "Victoires : " + ChatColor.WHITE + mine.getWins(),
                ChatColor.GRAY + "Défaites : " + ChatColor.WHITE + mine.getLosses(),
                ChatColor.GRAY + "Parties jouées : " + ChatColor.WHITE + mine.getGamesPlayed()));
        myStats.setItemMeta(meta);
        inv.setItem(22, myStats);

        player.openInventory(inv);
    }

    private ItemStack statHead(PlayerStats stats, String line) {
        OfflinePlayer off = Bukkit.getOfflinePlayer(stats.getUuid());
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(off);
        meta.setDisplayName(ChatColor.YELLOW + stats.getName());
        meta.setLore(List.of(ChatColor.GRAY + line));
        skull.setItemMeta(meta);
        return skull;
    }
}
