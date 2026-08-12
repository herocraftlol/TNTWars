package com.tntwars.plugin.hologram;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Hologrammes (piles d'ArmorStand invisibles) affichant en temps réel le top 5 des
 * meilleurs joueurs (kills). Un admin peut en placer un avec /tnt leaderboard create.
 */
public class LeaderboardHologramManager {

    private final TntWarsPlugin plugin;
    private final File file;
    private final Map<String, Location> holograms = new LinkedHashMap<>();
    private final Map<String, List<ArmorStand>> spawned = new LinkedHashMap<>();

    public LeaderboardHologramManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "leaderboards.yml");
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::refreshAll, 40L, 200L);
    }

    public void create(String id, Location loc) {
        remove(id);
        holograms.put(id, loc);
        save();
        spawn(id, loc);
    }

    public boolean remove(String id) {
        despawn(id);
        boolean existed = holograms.remove(id) != null;
        if (existed) save();
        return existed;
    }

    private void despawn(String id) {
        List<ArmorStand> stands = spawned.remove(id);
        if (stands != null) stands.forEach(ArmorStand::remove);
    }

    private void spawn(String id, Location loc) {
        List<PlayerStats> top = plugin.getStatsManager().topKills(5);
        List<ArmorStand> stands = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "🏆 Top Kills TNT Wars");
        for (int i = 0; i < top.size(); i++) {
            PlayerStats s = top.get(i);
            lines.add(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.WHITE + s.getName() + ChatColor.GRAY + " - " + s.getKills() + " kills");
        }
        double y = loc.getY() + (lines.size() - 1) * 0.25;
        for (String line : lines) {
            Location standLoc = loc.clone();
            standLoc.setY(y);
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(line);
            stands.add(stand);
            y -= 0.25;
        }
        spawned.put(id, stands);
    }

    public void refreshAll() {
        for (Map.Entry<String, Location> entry : holograms.entrySet()) {
            spawn(entry.getKey(), entry.getValue());
        }
    }

    public void loadIntoWorld() {
        for (Map.Entry<String, Location> entry : holograms.entrySet()) {
            spawn(entry.getKey(), entry.getValue());
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Location> entry : holograms.entrySet()) {
            ConfigurationSection sec = config.createSection(entry.getKey());
            Location loc = entry.getValue();
            sec.set("world", loc.getWorld().getName());
            sec.set("x", loc.getX());
            sec.set("y", loc.getY());
            sec.set("z", loc.getZ());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder leaderboards.yml", e);
        }
    }

    public void load() {
        holograms.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String id : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(id);
            if (sec == null) continue;
            String worldName = sec.getString("world");
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) continue;
            Location loc = new Location(world, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"));
            holograms.put(id, loc);
        }
    }
}
