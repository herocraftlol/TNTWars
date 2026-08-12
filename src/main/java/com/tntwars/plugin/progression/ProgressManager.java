package com.tntwars.plugin.progression;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.Team;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gère les points et niveaux des joueurs. Chaque action en jeu (kill, victoire, TNT
 * lancée, bloc adverse détruit, canon fonctionnel) rapporte des points configurables
 * dans config.yml (section "points"). Passer un palier de points débloque un ou
 * plusieurs schémas de canons à TNT (voir {@link com.tntwars.plugin.cannon.CannonSchematicRegistry}).
 */
public class ProgressManager {

    private final TntWarsPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerProgress> progress = new LinkedHashMap<>();

    /** Points cumulés nécessaires pour chaque niveau (index 0 = niveau 1). */
    private List<Integer> levelThresholds;

    public ProgressManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "progression.yml");
        loadThresholds();
        load();
    }

    private void loadThresholds() {
        List<Integer> defaults = List.of(0, 50, 150, 300, 600, 1000, 1500, 2200, 3000);
        levelThresholds = new ArrayList<>(plugin.getConfig().getIntegerList("levels.thresholds"));
        if (levelThresholds.isEmpty()) levelThresholds = new ArrayList<>(defaults);
    }

    public PlayerProgress get(UUID uuid) {
        return progress.computeIfAbsent(uuid, PlayerProgress::new);
    }

    /** Niveau actuel (1-based) d'un joueur en fonction de ses points. */
    public int getLevel(UUID uuid) {
        int points = get(uuid).getPoints();
        int level = 1;
        for (int i = 0; i < levelThresholds.size(); i++) {
            if (points >= levelThresholds.get(i)) level = i + 1;
        }
        return level;
    }

    public int getPointsForLevel(int level) {
        int idx = level - 1;
        if (idx < 0) return 0;
        if (idx >= levelThresholds.size()) return levelThresholds.get(levelThresholds.size() - 1);
        return levelThresholds.get(idx);
    }

    public int getPointsToNextLevel(UUID uuid) {
        int level = getLevel(uuid);
        if (level >= levelThresholds.size()) return -1; // niveau max atteint
        return getPointsForLevel(level + 1) - get(uuid).getPoints();
    }

    public int getMaxLevel() {
        return levelThresholds.size();
    }

    // ── Attribution de points ────────────────────────────────────────────

    public int configuredPoints(String key) {
        return plugin.getConfig().getInt("points." + key, 0);
    }

    public void awardPoints(Player player, int amount, String reason) {
        if (player == null || amount <= 0) return;
        PlayerProgress pp = get(player.getUniqueId());
        int levelBefore = getLevel(player.getUniqueId());
        pp.addPoints(amount);
        int levelAfter = getLevel(player.getUniqueId());
        MessageUtil.send(player, "§b+" + amount + " points §7(" + reason + ")");
        if (levelAfter > levelBefore) {
            handleLevelUp(player, levelBefore, levelAfter);
        }
        save();
    }

    /** Distribue des points à tous les membres en ligne d'une équipe (action collective : TNT/dégâts). */
    public void awardTeamPoints(Arena arena, Team team, int amount, String reason) {
        if (team == null || amount <= 0) return;
        for (UUID uuid : team.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) awardPoints(p, amount, reason);
        }
    }

    private void handleLevelUp(Player player, int oldLevel, int newLevel) {
        MessageUtil.send(player, "§6§l⬆ Niveau supérieur ! §eVous êtes maintenant niveau §f" + newLevel + "§e.");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        List<com.tntwars.plugin.cannon.CannonSchematic> unlocked = plugin.getSchematicRegistry().schematicsForLevel(newLevel);
        for (com.tntwars.plugin.cannon.CannonSchematic schema : unlocked) {
            MessageUtil.send(player, "§d✦ Nouveau schéma débloqué : §f" + schema.getDisplayName()
                    + " §7(/tnt schema pour le voir)");
            player.getInventory().addItem(plugin.getSchematicRegistry().createBookItem(schema));
        }
    }

    public boolean isUnlocked(UUID uuid, com.tntwars.plugin.cannon.CannonSchematic schema) {
        if (get(uuid).getManuallyUnlocked().contains(schema.getId())) return true;
        return getLevel(uuid) >= schema.getRequiredLevel();
    }

    // ── Persistance ──────────────────────────────────────────────────────

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (PlayerProgress pp : progress.values()) {
            ConfigurationSection sec = config.createSection(pp.getUuid().toString());
            sec.set("points", pp.getPoints());
            sec.set("unlocked", new ArrayList<>(pp.getManuallyUnlocked()));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder progression.yml", e);
        }
    }

    public void load() {
        progress.clear();
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
            PlayerProgress pp = new PlayerProgress(uuid);
            pp.setPoints(sec.getInt("points", 0));
            pp.getManuallyUnlocked().addAll(sec.getStringList("unlocked"));
            progress.put(uuid, pp);
        }
    }
}
