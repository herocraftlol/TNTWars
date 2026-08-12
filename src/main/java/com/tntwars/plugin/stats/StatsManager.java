package com.tntwars.plugin.stats;

import com.tntwars.plugin.TntWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StatsManager {

    private final TntWarsPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerStats> stats = new LinkedHashMap<>();

    public StatsManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage.stats-file", "stats.yml"));
        load();
    }

    public PlayerStats get(UUID uuid) {
        return stats.computeIfAbsent(uuid, id -> {
            String name = Bukkit.getOfflinePlayer(id).getName();
            return new PlayerStats(id, name == null ? id.toString() : name);
        });
    }

    public void addKill(UUID uuid) {
        get(uuid).addKill();
        save();
    }

    public void addDeath(UUID uuid) {
        get(uuid).addDeath();
        save();
    }

    public void addWin(UUID uuid) {
        get(uuid).addWin();
        save();
    }

    public void addLoss(UUID uuid) {
        get(uuid).addLoss();
        save();
    }

    public List<PlayerStats> topBy(Comparator<PlayerStats> comparator, int limit) {
        List<PlayerStats> list = new ArrayList<>(stats.values());
        list.sort(comparator);
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<PlayerStats> topKills(int limit) {
        return topBy(Comparator.comparingInt(PlayerStats::getKills).reversed(), limit);
    }

    public List<PlayerStats> topWins(int limit) {
        return topBy(Comparator.comparingInt(PlayerStats::getWins).reversed(), limit);
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (PlayerStats s : stats.values()) {
            ConfigurationSection sec = config.createSection(s.getUuid().toString());
            sec.set("name", s.getName());
            sec.set("kills", s.getKills());
            sec.set("deaths", s.getDeaths());
            sec.set("wins", s.getWins());
            sec.set("losses", s.getLosses());
            sec.set("gamesPlayed", s.getGamesPlayed());
            sec.set("cosmetic", s.getSelectedCosmetic());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder stats.yml", e);
        }
    }

    public void load() {
        stats.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) continue;
            PlayerStats s = new PlayerStats(uuid, sec.getString("name", key));
            for (int i = 0; i < sec.getInt("kills", 0); i++) s.addKill();
            for (int i = 0; i < sec.getInt("wins", 0); i++) s.addWin();
            for (int i = 0; i < sec.getInt("losses", 0); i++) s.addLoss();
            // deaths sans double compter gamesPlayed : on force la valeur directement via un correctif
            int deaths = sec.getInt("deaths", 0);
            for (int i = 0; i < deaths; i++) s.addDeath();
            s.setSelectedCosmetic(sec.getString("cosmetic", "none"));
            stats.put(uuid, s);
        }
    }
}
