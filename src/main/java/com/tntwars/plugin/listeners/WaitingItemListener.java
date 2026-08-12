package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.game.GameManager;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Gère les 3 items de la salle d'attente :
 *  - Diamant (slot 0) : force le lancement de la partie, réservé aux admins.
 *  - Béton coloré : change l'équipe du joueur (rouge/vert/...).
 *  - Barrière : quitte l'arène et retourne à la position d'avant le /tnt join.
 */
public class WaitingItemListener implements Listener {

    private final TntWarsPlugin plugin;

    public WaitingItemListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        String action = meta.getPersistentDataContainer().get(GameManager.ACTION_KEY, PersistentDataType.STRING);
        if (action == null) return;

        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null) return;
        event.setCancelled(true);

        switch (action) {
            case "force_start" -> {
                if (!player.hasPermission("tntwars.admin")) {
                    MessageUtil.send(player, "§cVous n'avez pas la permission de lancer la partie.");
                    return;
                }
                plugin.getGameManager().forceStart(arena);
                plugin.getGameManager().broadcastArena(arena, "§bUn admin a forcé le lancement de la partie !");
            }
            case "color" -> plugin.getGameManager().cycleTeam(player);
            case "leave" -> plugin.getGameManager().leave(player);
            default -> {
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaOf(player);
        if (arena == null) return;
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(GameManager.ACTION_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
