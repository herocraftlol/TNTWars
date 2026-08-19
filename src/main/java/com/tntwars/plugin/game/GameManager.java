package com.tntwars.plugin.game;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.arena.Team;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gère le cycle de vie complet d'une partie de TNT Wars : rejoindre/quitter une arène,
 * la salle d'attente (items diamant / couleur / barrière), le décompte, le lancement,
 * l'élimination des joueurs (mort ou chute hors de la zone) et la fin de partie avec
 * régénération de la map.
 */
public class GameManager {

    public static final NamespacedKey ACTION_KEY = new NamespacedKey("tntwars", "action");
    public static final NamespacedKey LOCKED_ITEM_KEY = new NamespacedKey("tntwars", "locked_item");

    private final TntWarsPlugin plugin;

    private final Map<UUID, Arena> currentArena = new HashMap<>();
    private final Map<UUID, PlayerBackup> backups = new HashMap<>();

    public GameManager(TntWarsPlugin plugin) {
        this.plugin = plugin;
        startTickTask();
        startDropCleanupTask();
    }

    public Arena getArenaOf(Player player) {
        return currentArena.get(player.getUniqueId());
    }

    public boolean isInGame(Player player) {
        return currentArena.containsKey(player.getUniqueId());
    }

    // ── Rejoindre / Quitter ─────────────────────────────────────────────

    /** Rejoint une arène en forçant une équipe précise (utilisé par le système de tournoi). */
    public boolean joinTeam(Player player, Arena arena, Team team) {
        if (arena == null || team == null) return false;
        if (isInGame(player)) return false;
        if (!arena.isFullyConfigured() || arena.getState() == ArenaState.DISABLED) return false;
        if (team.isFull(arena.getTeamSize())) return false;

        backups.put(player.getUniqueId(), backupPlayer(player));
        arena.addPlayer(player, team);
        currentArena.put(player.getUniqueId(), arena);

        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.teleport(arena.getWaitingSpawn());
        giveWaitingItems(player, arena, team);
        return true;
    }

    public boolean join(Player player, Arena arena) {
        if (arena == null) {
            MessageUtil.send(player, "§cCette arène n'existe pas.");
            return false;
        }
        if (isInGame(player)) {
            MessageUtil.send(player, "§cVous êtes déjà dans une arène. Faites /tnt leave d'abord.");
            return false;
        }
        if (!arena.isFullyConfigured() || arena.getState() == ArenaState.DISABLED) {
            MessageUtil.send(player, "§cCette arène n'est pas encore configurée.");
            return false;
        }
        if (arena.getState() == ArenaState.INGAME || arena.getState() == ArenaState.RESTARTING) {
            MessageUtil.send(player, "§cUne partie est déjà en cours sur cette arène.");
            return false;
        }
        if (arena.isFull()) {
            MessageUtil.send(player, "§cCette arène est complète.");
            return false;
        }
        Team team = arena.pickBalancedTeam();
        if (team == null) {
            MessageUtil.send(player, "§cCette arène est complète.");
            return false;
        }

        backups.put(player.getUniqueId(), backupPlayer(player));
        arena.addPlayer(player, team);
        currentArena.put(player.getUniqueId(), arena);

        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.teleport(arena.getWaitingSpawn());
        giveWaitingItems(player, arena, team);

        broadcastArena(arena, "§e" + player.getName() + " §7a rejoint l'arène §f(" + arena.totalPlayers() + "/" + (arena.getTeamsCount() * arena.getTeamSize()) + ")");

        if (arena.getState() == ArenaState.WAITING && arena.totalPlayers() >= plugin.getConfig().getInt("game.min-players-to-start", 2)) {
            arena.setState(ArenaState.STARTING);
            arena.setCountdown(plugin.getConfig().getInt("game.countdown-seconds", 30));
        }
        return true;
    }

    public boolean leave(Player player) {
        Arena arena = currentArena.remove(player.getUniqueId());
        if (arena == null) {
            MessageUtil.send(player, "§cVous n'êtes pas dans une arène.");
            return false;
        }
        boolean wasIngame = arena.getState() == ArenaState.INGAME;
        arena.removePlayer(player);
        restorePlayer(player);

        if (wasIngame) {
            checkWinCondition(arena);
        } else if (arena.getState() == ArenaState.STARTING && arena.totalPlayers() < plugin.getConfig().getInt("game.min-players-to-start", 2)) {
            arena.setState(ArenaState.WAITING);
            arena.setCountdown(-1);
        }
        if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING) {
            broadcastArena(arena, "§e" + player.getName() + " §7a quitté l'arène.");
        }
        return true;
    }

    private PlayerBackup backupPlayer(Player player) {
        return new PlayerBackup(player.getLocation().clone(),
                player.getInventory().getContents().clone(),
                player.getInventory().getArmorContents().clone(),
                player.getGameMode(),
                player.getExp(), player.getLevel(),
                player.getHealth(), player.getFoodLevel());
    }

    private void restorePlayer(Player player) {
        PlayerBackup backup = backups.remove(player.getUniqueId());
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        if (player.isDead()) {
            // sera géré au respawn
        }
        if (backup != null) {
            player.getInventory().setContents(backup.getInventory());
            player.getInventory().setArmorContents(backup.getArmor());
            player.setGameMode(backup.getGameMode());
            player.setExp(backup.getExp());
            player.setLevel(backup.getLevel());
            try {
                player.setHealth(Math.min(backup.getHealth(), player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
            } catch (Exception ignored) {
            }
            player.setFoodLevel(backup.getFoodLevel());
            if (backup.getLocation().getWorld() != null) {
                player.teleport(backup.getLocation());
            }
        } else {
            player.setHealth(20);
        }
    }

    // ── Salle d'attente ──────────────────────────────────────────────────

    public void giveWaitingItems(Player player, Arena arena, Team team) {
        player.getInventory().clear();
        int diamondSlot = plugin.getConfig().getInt("waiting-room.diamond-slot", 0);
        int colorSlot = plugin.getConfig().getInt("waiting-room.color-item-slot", 4);
        int leaveSlot = plugin.getConfig().getInt("waiting-room.leave-item-slot", 8);

        if (player.hasPermission("tntwars.admin")) {
            player.getInventory().setItem(diamondSlot, makeItem(Material.DIAMOND, "§b§lLancer la partie", "force_start"));
        }
        player.getInventory().setItem(colorSlot, makeTeamItem(team));
        player.getInventory().setItem(leaveSlot, makeItem(Material.BARRIER, "§c§lQuitter l'arène", "leave"));
    }

    private ItemStack makeItem(Material material, String name, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.color(name));
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeTeamItem(Team team) {
        ItemStack item = new ItemStack(team.getConcrete());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.color("§fÉquipe : " + team.getColoredName() + " §7(clic pour changer)"));
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "color");
        item.setItemMeta(meta);
        return item;
    }

    /** Fait passer le joueur à l'équipe suivante disponible (item concrete cliqué). */
    public void cycleTeam(Player player) {
        Arena arena = getArenaOf(player);
        if (arena == null || arena.getState() == ArenaState.INGAME) return;
        Team current = arena.getTeamOf(player);
        if (current == null) return;
        int startIdx = current.getIndex();
        int n = arena.getTeams().size();
        for (int offset = 1; offset <= n; offset++) {
            Team candidate = arena.getTeam((startIdx + offset) % n);
            if (candidate == current) break;
            if (candidate.getMembers().size() < arena.getTeamSize()) {
                current.getMembers().remove(player.getUniqueId());
                candidate.getMembers().add(player.getUniqueId());
                arena.getPlayerTeamMap().put(player.getUniqueId(), candidate.getIndex());
                int colorSlot = plugin.getConfig().getInt("waiting-room.color-item-slot", 4);
                player.getInventory().setItem(colorSlot, makeTeamItem(candidate));
                MessageUtil.send(player, "§7Vous avez rejoint l'équipe " + candidate.getColoredName());
                return;
            }
        }
        MessageUtil.send(player, "§cAucune autre équipe disponible.");
    }

    public void forceStart(Arena arena) {
        if (arena.getState() != ArenaState.STARTING && arena.getState() != ArenaState.WAITING) return;
        arena.setCountdown(1);
        arena.setState(ArenaState.STARTING);
    }

    // ── Tick / Décompte ──────────────────────────────────────────────────

    private void startTickTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Arena arena : plugin.getArenaManager().getArenas()) {
                    tickArena(arena);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Nettoie automatiquement tous les items droppés (TNT non consommée, blocs cassés...) dans les arènes actives. */
    private void startDropCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Arena arena : plugin.getArenaManager().getArenas()) {
                    if (arena.getMapRegion() == null) continue;
                    if (arena.getState() != ArenaState.INGAME && arena.getState() != ArenaState.STARTING) continue;
                    org.bukkit.World world = arena.getMapRegion().getWorld();
                    if (world == null) continue;
                    for (org.bukkit.entity.Entity entity : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
                        if (arena.getMapRegion().contains(entity.getLocation())) {
                            entity.remove();
                        }
                    }
                    for (org.bukkit.entity.Entity entity : world.getEntitiesByClass(org.bukkit.entity.ExperienceOrb.class)) {
                        if (arena.getMapRegion().contains(entity.getLocation())) {
                            entity.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // toutes les 5 secondes
    }

    private void tickArena(Arena arena) {        if (arena.getState() == ArenaState.STARTING) {
            int c = arena.getCountdown();
            if (c <= 0) {
                startGame(arena);
                return;
            }
            if (c == 30 || c == 15 || c <= 10) {
                broadcastArena(arena, "§eLa partie commence dans §f" + c + " §eseconde" + (c > 1 ? "s" : "") + ".");
            }
            arena.setCountdown(c - 1);
        }
    }

    // ── Lancement de partie ─────────────────────────────────────────────

    public void startGame(Arena arena) {
        if (arena.totalPlayers() < 2) {
            arena.setState(ArenaState.WAITING);
            arena.setCountdown(-1);
            broadcastArena(arena, "§cPas assez de joueurs, la partie est annulée.");
            return;
        }
        plugin.getArenaManager().captureSnapshot(arena);
        arena.setState(ArenaState.INGAME);
        arena.setGameStartTime(System.currentTimeMillis());
        for (Team team : arena.getTeams()) {
            team.startRound();
            for (UUID uuid : team.getMembers()) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p == null) continue;
                p.getInventory().clear();
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20);
                p.setFoodLevel(20);
                p.teleport(team.getSpawn());
                dyeArmor(p, team);
                p.getInventory().setItem(0, createLockedPickaxe());
                MessageUtil.send(p, "§aLa partie commence ! Vous êtes dans l'équipe " + team.getColoredName() + ". Construisez votre canon à TNT avec le contenu du coffre !");
            }
        }
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().startArenaScoreboard(arena);
        }
    }

    /** Pioche en netherite incassable, verrouillée dans le slot 0 : ne peut ni se déplacer, ni se dropper, ni changer de slot. */
    public ItemStack createLockedPickaxe() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.color("§b§lPioche du bâtisseur"));
        meta.setLore(List.of(MessageUtil.color("§7Toujours dans votre slot 1, incassable."),
                MessageUtil.color("§7Sert à miner rapidement dans votre zone.")));
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE, org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(LOCKED_ITEM_KEY, PersistentDataType.STRING, "locked_pickaxe");
        item.setItemMeta(meta);
        return item;
    }

    private void dyeArmor(Player player, Team team) {
        org.bukkit.Color color = team.getIndex() % Team.CONCRETE_CYCLE.length == 0 ? org.bukkit.Color.RED
                : team.getIndex() % Team.CONCRETE_CYCLE.length == 1 ? org.bukkit.Color.LIME
                : team.getIndex() % Team.CONCRETE_CYCLE.length == 2 ? org.bukkit.Color.BLUE : org.bukkit.Color.YELLOW;
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        org.bukkit.inventory.meta.LeatherArmorMeta meta = (org.bukkit.inventory.meta.LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(color);
        chest.setItemMeta(meta);
        player.getInventory().setChestplate(chest);
    }

    // ── Elimination / Victoire ───────────────────────────────────────────

    public void eliminatePlayer(Player player, Arena arena, String reason) {
        Team team = arena.getTeamOf(player);
        if (team == null) return;
        if (!team.getAlive().remove(player.getUniqueId())) return; // déjà éliminé
        arena.getSpectators().add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        String suffix = (reason == null || reason.isBlank()) ? "" : " " + reason;
        broadcastArena(arena, "§c☠ §e" + player.getName() + " §7a été éliminé" + suffix + ".");
        if (plugin.getStatsManager() != null) {
            plugin.getStatsManager().addDeath(player.getUniqueId());
        }
        checkWinCondition(arena);
    }

    public void checkWinCondition(Arena arena) {
        if (arena.getState() != ArenaState.INGAME) return;
        List<Team> alive = arena.teamsAlive();
        if (alive.size() <= 1) {
            Team winner = alive.isEmpty() ? null : alive.get(0);
            endGame(arena, winner);
        }
    }

    public void endGame(Arena arena, Team winner) {
        if (arena.getState() == ArenaState.RESTARTING) return;
        arena.setState(ArenaState.RESTARTING);

        if (winner != null) {
            broadcastArena(arena, "§6§l" + winner.getColoredName() + " §6§la gagné la partie !");
            int winPoints = plugin.getProgressManager().configuredPoints("win");
            for (UUID uuid : winner.getMembers()) {
                if (plugin.getStatsManager() != null) plugin.getStatsManager().addWin(uuid);
                Player wp = plugin.getServer().getPlayer(uuid);
                if (wp != null) plugin.getProgressManager().awardPoints(wp, winPoints, "victoire");
            }
            for (Team t : arena.getTeams()) {
                if (t == winner) continue;
                for (UUID uuid : t.getMembers()) {
                    if (plugin.getStatsManager() != null) plugin.getStatsManager().addLoss(uuid);
                }
            }
        } else {
            broadcastArena(arena, "§7Match nul, aucun survivant.");
        }

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().stopArenaScoreboard(arena);
        }

        if (arena.getTournamentName() != null && plugin.getTournamentManager() != null) {
            plugin.getTournamentManager().onMatchFinished(arena, winner);
        }

        // Copie (dédupliquée) des membres avant reset, nécessaire pour restaurer chacun une seule fois
        java.util.Set<UUID> allMembers = new java.util.LinkedHashSet<>();
        for (Team t : arena.getTeams()) allMembers.addAll(t.getMembers());
        allMembers.addAll(arena.getSpectators());

        for (UUID uuid : allMembers) {
            Player p = plugin.getServer().getPlayer(uuid);
            currentArena.remove(uuid);
            if (p != null) {
                restorePlayer(p);
                MessageUtil.send(p, "§7La partie est terminée, régénération de la map en cours...");
            }
        }

        arena.resetRuntime();

        new BukkitRunnable() {
            @Override
            public void run() {
                arena.getSnapshot().restore(plugin, () -> {
                    plugin.getChestManager().refillAll(arena);
                    arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
                });
            }
        }.runTaskLater(plugin, plugin.getConfig().getInt("game.restarting-seconds", 8) * 20L);
    }

    public void broadcastArena(Arena arena, String message) {
        for (UUID uuid : arena.getPlayerTeamMap().keySet()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) MessageUtil.send(p, message);
        }
        for (UUID uuid : arena.getSpectators()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) MessageUtil.send(p, message);
        }
    }

    public void handleDisconnect(Player player) {
        Arena arena = currentArena.get(player.getUniqueId());
        if (arena == null) return;
        // On considère le joueur comme éliminé/quitté mais sans lui restaurer son inventaire
        // puisqu'il n'est plus connecté ; sa sauvegarde reste en mémoire pour une éventuelle reconnexion.
        boolean wasIngame = arena.getState() == ArenaState.INGAME;
        arena.removePlayer(player);
        currentArena.remove(player.getUniqueId());
        if (wasIngame) checkWinCondition(arena);
    }
}
