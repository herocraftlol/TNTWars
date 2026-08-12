package com.tntwars.plugin.listeners;

import com.tntwars.plugin.TntWarsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final TntWarsPlugin plugin;

    public PlayerConnectionListener(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getGameManager().handleDisconnect(event.getPlayer());
    }
}
