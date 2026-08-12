package com.tntwars.plugin.arena;

import com.tntwars.plugin.TntWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gère la liste des arènes : création/suppression, persistance dans arenas.yml et
 * les commandes de setup (pos1/pos2 pour la map, la zone de coffre, les zones
 * d'équipe et les spawns).
 */
public class ArenaManager {

    private final TntWarsPlugin plugin;
    private final File file;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    /** Sélections temporaires pos1/pos2 en cours de setup, par admin. clé libre ex "map1". */
    private final Map<UUID, Map<String, Location>> pending = new LinkedHashMap<>();

    public ArenaManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage.arenas-file", "arenas.yml"));
        load();
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Arena getArena(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    public boolean exists(String name) {
        return arenas.containsKey(name.toLowerCase());
    }

    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        arena.setTeamsCount(plugin.getConfig().getInt("game.default-teams", 2));
        arena.setTeamSize(plugin.getConfig().getInt("game.default-team-size", 4));
        arenas.put(name.toLowerCase(), arena);
        save();
        return arena;
    }

    public boolean deleteArena(String name) {
        Arena removed = arenas.remove(name.toLowerCase());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    // ── Sélection pos1/pos2 ─────────────────────────────────────────────

    public void setPending(UUID admin, String key, Location loc) {
        pending.computeIfAbsent(admin, k -> new LinkedHashMap<>()).put(key, loc);
    }

    public Location getPending(UUID admin, String key) {
        Map<String, Location> map = pending.get(admin);
        return map == null ? null : map.get(key);
    }

    public boolean setArenaMapPos(Arena arena, UUID admin, boolean isPos1, Location loc) {
        if (isPos1) {
            setPending(admin, "map1_" + arena.getName(), loc);
        } else {
            setPending(admin, "map2_" + arena.getName(), loc);
        }
        Location p1 = getPending(admin, "map1_" + arena.getName());
        Location p2 = getPending(admin, "map2_" + arena.getName());
        if (p1 != null && p2 != null) {
            arena.setMapRegion(new CuboidRegion(p1, p2));
            save();
            return true;
        }
        return false;
    }

    public boolean setChestPos(Arena arena, UUID admin, boolean isPos1, Location loc) {
        if (isPos1) {
            setPending(admin, "chest1_" + arena.getName(), loc);
        } else {
            setPending(admin, "chest2_" + arena.getName(), loc);
        }
        Location p1 = getPending(admin, "chest1_" + arena.getName());
        Location p2 = getPending(admin, "chest2_" + arena.getName());
        if (p1 != null && p2 != null) {
            arena.setChestRegion(new CuboidRegion(p1, p2));
            registerChests(arena);
            save();
            return true;
        }
        return false;
    }

    public boolean setZonePos(Arena arena, int teamIndex, UUID admin, boolean isPos1, Location loc) {
        String base = "zone" + teamIndex + "_" + arena.getName();
        if (isPos1) {
            setPending(admin, base + "_1", loc);
        } else {
            setPending(admin, base + "_2", loc);
        }
        Location p1 = getPending(admin, base + "_1");
        Location p2 = getPending(admin, base + "_2");
        if (p1 != null && p2 != null) {
            Team team = arena.getTeam(teamIndex);
            if (team == null) return false;
            team.setZone(new CuboidRegion(p1, p2));
            save();
            return true;
        }
        return false;
    }

    /** Scanne la zone de coffre et enregistre le contenu de chaque coffre trouvé comme modèle "infini". */
    public void registerChests(Arena arena) {
        arena.getChests().clear();
        CuboidRegion region = arena.getChestRegion();
        if (region == null) return;
        World world = region.getWorld();
        if (world == null) return;
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    BlockState bs = block.getState();
                    if (bs instanceof Chest chest) {
                        Inventory inv = chest.getBlockInventory();
                        List<ItemStack> template = new ArrayList<>();
                        for (ItemStack item : inv.getContents()) {
                            template.add(item == null ? null : item.clone());
                        }
                        arena.getChests().add(new ChestData(block.getLocation(), template));
                    }
                }
            }
        }
    }

    public void captureSnapshot(Arena arena) {
        if (arena.getSnapshot() != null) {
            arena.getSnapshot().capture();
        }
    }

    // ── Persistance ──────────────────────────────────────────────────────

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Arena arena : arenas.values()) {
            ConfigurationSection sec = config.createSection(arena.getName());
            sec.set("teamsCount", arena.getTeamsCount());
            sec.set("teamSize", arena.getTeamSize());
            if (arena.getMapRegion() != null) arena.getMapRegion().save(sec.createSection("map"));
            if (arena.getChestRegion() != null) arena.getChestRegion().save(sec.createSection("chest"));
            if (arena.getWaitingSpawn() != null) saveLocation(sec.createSection("waiting"), arena.getWaitingSpawn());
            ConfigurationSection teamsSec = sec.createSection("teams");
            for (Team t : arena.getTeams()) {
                ConfigurationSection ts = teamsSec.createSection(String.valueOf(t.getIndex()));
                if (t.getZone() != null) t.getZone().save(ts.createSection("zone"));
                if (t.getSpawn() != null) saveLocation(ts.createSection("spawn"), t.getSpawn());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder arenas.yml", e);
        }
    }

    private void saveLocation(ConfigurationSection sec, Location loc) {
        sec.set("world", loc.getWorld().getName());
        sec.set("x", loc.getX());
        sec.set("y", loc.getY());
        sec.set("z", loc.getZ());
        sec.set("yaw", loc.getYaw());
        sec.set("pitch", loc.getPitch());
    }

    private Location loadLocation(ConfigurationSection sec) {
        if (sec == null) return null;
        String worldName = sec.getString("world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"),
                (float) sec.getDouble("yaw"), (float) sec.getDouble("pitch"));
    }

    public void load() {
        arenas.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String name : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(name);
            if (sec == null) continue;
            Arena arena = new Arena(name);
            arena.setTeamsCount(sec.getInt("teamsCount", 2));
            arena.setTeamSize(sec.getInt("teamSize", 4));
            CuboidRegion mapRegion = CuboidRegion.load(sec.getConfigurationSection("map"));
            if (mapRegion != null) arena.setMapRegion(mapRegion);
            CuboidRegion chestRegion = CuboidRegion.load(sec.getConfigurationSection("chest"));
            if (chestRegion != null) arena.setChestRegion(chestRegion);
            Location waiting = loadLocation(sec.getConfigurationSection("waiting"));
            if (waiting != null) arena.setWaitingSpawn(waiting);
            ConfigurationSection teamsSec = sec.getConfigurationSection("teams");
            if (teamsSec != null) {
                for (String idxStr : teamsSec.getKeys(false)) {
                    int idx;
                    try {
                        idx = Integer.parseInt(idxStr);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    Team team = arena.getTeam(idx);
                    if (team == null) continue;
                    ConfigurationSection ts = teamsSec.getConfigurationSection(idxStr);
                    CuboidRegion zone = CuboidRegion.load(ts.getConfigurationSection("zone"));
                    if (zone != null) team.setZone(zone);
                    Location spawn = loadLocation(ts.getConfigurationSection("spawn"));
                    if (spawn != null) team.setSpawn(spawn);
                }
            }
            if (arena.getChestRegion() != null) registerChests(arena);
            if (arena.getMapRegion() != null) captureSnapshot(arena);
            arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
            arenas.put(name.toLowerCase(), arena);
        }
    }
}
