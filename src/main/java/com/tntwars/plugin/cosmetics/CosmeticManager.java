package com.tntwars.plugin.cosmetics;

import com.tntwars.plugin.TntWarsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CosmeticManager {

    private final TntWarsPlugin plugin;

    public CosmeticManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public KillEffect getSelected(Player player) {
        return KillEffect.fromId(plugin.getStatsManager().get(player.getUniqueId()).getSelectedCosmetic());
    }

    public void select(Player player, KillEffect effect) {
        plugin.getStatsManager().get(player.getUniqueId()).setSelectedCosmetic(effect.name());
        plugin.getStatsManager().save();
    }

    public void playKillEffect(Player killer, Location deathLocation) {
        KillEffect effect = getSelected(killer);
        if (effect == KillEffect.NONE || deathLocation.getWorld() == null) return;
        if (effect.getParticle() != null) {
            deathLocation.getWorld().spawnParticle(effect.getParticle(), deathLocation.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
        }
        if (effect.getSound() != null) {
            deathLocation.getWorld().playSound(deathLocation, effect.getSound(), 1f, 1f);
        }
    }
}
