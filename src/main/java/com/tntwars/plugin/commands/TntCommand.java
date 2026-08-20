package com.tntwars.plugin.commands;

import com.tntwars.plugin.TntWarsPlugin;
import com.tntwars.plugin.arena.Arena;
import com.tntwars.plugin.arena.ArenaState;
import com.tntwars.plugin.tournament.Tournament;
import com.tntwars.plugin.tournament.TournamentTeam;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TntCommand implements CommandExecutor, TabCompleter {

    private final TntWarsPlugin plugin;

    public TntCommand(TntWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            dispatch(sender, args);
        } catch (Exception ex) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "Erreur en exécutant /tnt " + String.join(" ", args) + " (sender=" + sender.getName() + ")", ex);
            MessageUtil.send(sender, "§cUne erreur interne est survenue en exécutant cette commande.");
            MessageUtil.send(sender, "§cDétail (voir aussi la console) : §7" + ex.getClass().getSimpleName()
                    + (ex.getMessage() != null ? " - " + ex.getMessage() : ""));
        }
        return true;
    }

    private void dispatch(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getArenaListGUI().open(player);
            } else {
                sendHelp(sender);
            }
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "join" -> handleJoin(sender, args);
            case "leave" -> handleLeave(sender);
            case "list" -> handleList(sender);
            case "gui" -> {
                if (sender instanceof Player p) plugin.getArenaListGUI().open(p);
            }
            case "top", "leaderboard", "classement" -> {
                if (sender instanceof Player p) plugin.getLeaderboardGUI().open(p);
            }
            case "cosmetics", "cosmetiques" -> {
                if (sender instanceof Player p) plugin.getCosmeticGUI().open(p);
            }
            case "schema", "schemas", "plans" -> handleSchema(sender, args);
            case "level", "niveau", "progression" -> handleLevel(sender);
            case "stats" -> handleStats(sender, args);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "setpos1" -> handleSetMapPos(sender, args, true);
            case "setpos2" -> handleSetMapPos(sender, args, false);
            case "setchestpos1" -> handleSetChestPos(sender, args, true);
            case "setchestpos2" -> handleSetChestPos(sender, args, false);
            case "setzone1" -> handleSetZonePos(sender, args, true);
            case "setzone2" -> handleSetZonePos(sender, args, false);
            case "setspawn" -> handleSetSpawn(sender, args);
            case "setwaiting" -> handleSetWaiting(sender, args);
            case "setteams" -> handleSetTeams(sender, args);
            case "setteamsize" -> handleSetTeamSize(sender, args);
            case "console" -> handleConsole(sender, args);
            case "debug" -> handleDebug(sender, args);
            case "forcestart" -> handleForceStart(sender, args);
            case "stop" -> handleStop(sender, args);
            case "info" -> handleInfo(sender, args);
            case "tournament", "tournoi" -> handleTournament(sender, args);
            case "hologram" -> handleHologram(sender, args);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
    }

    // ── Joueur ────────────────────────────────────────────────────────────

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) {
            plugin.getArenaListGUI().open(player);
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        plugin.getGameManager().join(player, arena);
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        plugin.getGameManager().leave(player);
    }

    private void handleList(CommandSender sender) {
        MessageUtil.send(sender, "§7Arènes disponibles :");
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            MessageUtil.sendRaw(sender, " §8- §f" + arena.getName() + " §7(" + arena.getState() + ", "
                    + arena.totalPlayers() + "/" + (arena.getTeamsCount() * arena.getTeamSize()) + ")");
        }
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        player.performCommand("tnt top");
    }

    private void handleSchema(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length >= 2 && (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("cancel"))) {
            plugin.getSchematicBuilder().cancel(player);
            return;
        }
        plugin.getSchematicGUI().open(player);
    }

    private void handleLevel(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        var progress = plugin.getProgressManager();
        int level = progress.getLevel(player.getUniqueId());
        int points = progress.get(player.getUniqueId()).getPoints();
        int toNext = progress.getPointsToNextLevel(player.getUniqueId());
        MessageUtil.send(player, "§7Niveau §f" + level + "§7/" + progress.getMaxLevel() + " §7— §f" + points + " §7points");
        if (toNext >= 0) {
            MessageUtil.send(player, "§7Encore §f" + toNext + " §7points avant le niveau suivant.");
        } else {
            MessageUtil.send(player, "§6Niveau maximum atteint !");
        }
    }

    // ── Administration : arènes ─────────────────────────────────────────

    private boolean checkAdmin(CommandSender sender) {
        if (!sender.hasPermission("tntwars.admin")) {
            MessageUtil.send(sender, "§cVous n'avez pas la permission d'utiliser cette commande.");
            return false;
        }
        return true;
    }

    private Location targetLocation(Player player) {
        Block block = player.getTargetBlockExact(200);
        return block == null ? null : block.getLocation();
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        if (args.length < 2) {
            MessageUtil.send(sender, "§cUsage: /tnt create <nom>");
            return;
        }
        if (plugin.getArenaManager().exists(args[1])) {
            MessageUtil.send(sender, "§cCette arène existe déjà.");
            return;
        }
        plugin.getArenaManager().createArena(args[1]);
        MessageUtil.send(sender, "§aArène §f" + args[1] + " §acréée. Configurez-la avec /tnt setpos1/setpos2/setchestpos1/setchestpos2/setzone1/setzone2/setspawn/setwaiting.");
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        if (args.length < 2) {
            MessageUtil.send(sender, "§cUsage: /tnt delete <nom>");
            return;
        }
        if (plugin.getArenaManager().deleteArena(args[1])) {
            MessageUtil.send(sender, "§aArène supprimée.");
        } else {
            MessageUtil.send(sender, "§cArène introuvable.");
        }
    }

    private Arena requireArena(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            MessageUtil.send(sender, "§cIl manque le nom de l'arène.");
            return null;
        }
        Arena arena = plugin.getArenaManager().getArena(args[index]);
        if (arena == null) {
            MessageUtil.send(sender, "§cArène introuvable : " + args[index]);
        }
        return arena;
    }

    private void handleSetMapPos(CommandSender sender, String[] args, boolean isPos1) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        Location loc = targetLocation(player);
        if (loc == null) {
            MessageUtil.send(sender, "§cVisez un bloc.");
            return;
        }
        boolean complete = plugin.getArenaManager().setArenaMapPos(arena, player.getUniqueId(), isPos1, loc);
        MessageUtil.send(sender, "§aPosition " + (isPos1 ? "1" : "2") + " de la map définie pour " + arena.getName()
                + (complete ? " §7(zone complète, snapshot capturé)" : " §7(en attente de l'autre position)"));
        if (complete) {
            plugin.getArenaManager().captureSnapshot(arena);
            arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
            plugin.getArenaManager().save();
        }
    }

    private void handleSetChestPos(CommandSender sender, String[] args, boolean isPos1) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        Location loc = targetLocation(player);
        if (loc == null) {
            MessageUtil.send(sender, "§cVisez un bloc.");
            return;
        }
        boolean complete = plugin.getArenaManager().setChestPos(arena, player.getUniqueId(), isPos1, loc);
        MessageUtil.send(sender, "§aPosition " + (isPos1 ? "1" : "2") + " du coffre définie pour " + arena.getName()
                + (complete ? " §7(coffres enregistrés comme infinis)" : " §7(en attente de l'autre position)"));
        if (complete) {
            arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
            plugin.getArenaManager().save();
        }
    }

    private void handleSetZonePos(CommandSender sender, String[] args, boolean isPos1) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        if (args.length < 3) {
            MessageUtil.send(sender, "§cUsage: /tnt " + (isPos1 ? "setzone1" : "setzone2") + " <arène> <équipe (0.." + (arena.getTeamsCount() - 1) + ")>");
            return;
        }
        int teamIndex;
        try {
            teamIndex = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "§cIndice d'équipe invalide.");
            return;
        }
        Location loc = targetLocation(player);
        if (loc == null) {
            MessageUtil.send(sender, "§cVisez un bloc.");
            return;
        }
        boolean complete = plugin.getArenaManager().setZonePos(arena, teamIndex, player.getUniqueId(), isPos1, loc);
        MessageUtil.send(sender, "§aZone équipe " + teamIndex + " (" + (isPos1 ? "pos1" : "pos2") + ") définie pour " + arena.getName()
                + (complete ? " §7(zone complète)" : " §7(en attente de l'autre position)"));
        if (complete) {
            arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
            plugin.getArenaManager().save();
        }
    }

    private void handleSetSpawn(CommandSender sender, String[] args) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        if (args.length < 3) {
            MessageUtil.send(sender, "§cUsage: /tnt setspawn <arène> <équipe>");
            return;
        }
        int teamIndex;
        try {
            teamIndex = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "§cIndice d'équipe invalide.");
            return;
        }
        var team = arena.getTeam(teamIndex);
        if (team == null) {
            MessageUtil.send(sender, "§cCette équipe n'existe pas.");
            return;
        }
        team.setSpawn(player.getLocation());
        arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
        plugin.getArenaManager().save();
        MessageUtil.send(sender, "§aSpawn de l'équipe " + teamIndex + " défini pour " + arena.getName() + ".");
    }

    private void handleSetWaiting(CommandSender sender, String[] args) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        arena.setWaitingSpawn(player.getLocation());
        arena.setState(arena.isFullyConfigured() ? ArenaState.WAITING : ArenaState.DISABLED);
        plugin.getArenaManager().save();
        MessageUtil.send(sender, "§aSalle d'attente définie pour " + arena.getName() + ".");
    }

    private void handleSetTeams(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        if (args.length < 3) {
            MessageUtil.send(sender, "§cUsage: /tnt setteams <arène> <nombre d'équipes>");
            return;
        }
        try {
            arena.setTeamsCount(Integer.parseInt(args[2]));
            plugin.getArenaManager().save();
            MessageUtil.send(sender, "§aNombre d'équipes défini à " + args[2] + " pour " + arena.getName() + ".");
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "§cNombre invalide.");
        }
    }

    private void handleSetTeamSize(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        if (args.length < 3) {
            MessageUtil.send(sender, "§cUsage: /tnt setteamsize <arène> <joueurs par équipe>");
            return;
        }
        try {
            arena.setTeamSize(Integer.parseInt(args[2]));
            plugin.getArenaManager().save();
            MessageUtil.send(sender, "§aTaille d'équipe définie à " + args[2] + " pour " + arena.getName() + ".");
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "§cNombre invalide.");
        }
    }

    private void handleConsole(CommandSender sender, String[] args) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        plugin.getArenaConsoleGUI().open(player, arena);
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;

        if (args.length >= 3 && (args[2].equalsIgnoreCase("fix") || args[2].equalsIgnoreCase("unstuck"))) {
            plugin.getGameManager().forceUnstuck(arena);
            MessageUtil.send(sender, "§aArène §f" + arena.getName() + " §adébloquée : état forcé à §f"
                    + arena.getState() + "§a, joueurs/équipes réinitialisés.");
            return;
        }

        MessageUtil.send(sender, "§6§l== Debug arène " + arena.getName() + " ==");
        MessageUtil.sendRaw(sender, " §8- État: §f" + arena.getState());
        MessageUtil.sendRaw(sender, " §8- Configurée: §f" + arena.isFullyConfigured());
        MessageUtil.sendRaw(sender, " §8- Map définie: §f" + (arena.getMapRegion() != null));
        if (arena.getMapRegion() != null) {
            MessageUtil.sendRaw(sender, "   §7min(" + arena.getMapRegion().getMinX() + "," + arena.getMapRegion().getMinY() + "," + arena.getMapRegion().getMinZ()
                    + ") max(" + arena.getMapRegion().getMaxX() + "," + arena.getMapRegion().getMaxY() + "," + arena.getMapRegion().getMaxZ() + ")"
                    + " monde=" + arena.getMapRegion().getWorldName());
        }
        MessageUtil.sendRaw(sender, " §8- Snapshot capturé: §f" + (arena.getSnapshot() != null && arena.getSnapshot().isCaptured()));
        MessageUtil.sendRaw(sender, " §8- Zone de coffre définie: §f" + (arena.getChestRegion() != null) + " §7(" + arena.getChests().size() + " coffre(s) enregistré(s))");
        MessageUtil.sendRaw(sender, " §8- Salle d'attente: §f" + (arena.getWaitingSpawn() != null));
        MessageUtil.sendRaw(sender, " §8- Équipes: §f" + arena.getTeamsCount() + " x " + arena.getTeamSize());
        for (var team : arena.getTeams()) {
            MessageUtil.sendRaw(sender, "   §7Équipe " + team.getIndex() + ": zone=" + (team.getZone() != null) + ", spawn=" + (team.getSpawn() != null)
                    + ", membres=" + team.getMembers().size() + ", vivants=" + team.getAlive().size());
        }
        MessageUtil.sendRaw(sender, " §8- Joueurs trackés: §f" + arena.totalPlayers() + " §8- Spectateurs: §f" + arena.getSpectators().size());
        MessageUtil.sendRaw(sender, " §8- Countdown: §f" + arena.getCountdown() + " §8- Tournoi lié: §f" + arena.getTournamentName());
        if (arena.getState() == ArenaState.RESTARTING) {
            MessageUtil.sendRaw(sender, "§eCette arène est en régénération et n'est pas rejoignable tant que l'état ne repasse pas à WAITING.");
            MessageUtil.sendRaw(sender, "§eSi elle semble bloquée, utilisez §f/tnt debug " + arena.getName() + " fix §epour la débloquer immédiatement.");
        }
    }

    private void handleForceStart(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        plugin.getGameManager().forceStart(arena);
        MessageUtil.send(sender, "§aLancement forcé de " + arena.getName() + ".");
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        if (arena.getState() == ArenaState.INGAME) {
            plugin.getGameManager().endGame(arena, null);
            MessageUtil.send(sender, "§cPartie arrêtée sur " + arena.getName() + ".");
        } else {
            MessageUtil.send(sender, "§cAucune partie en cours sur cette arène.");
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        Arena arena = requireArena(sender, args, 1);
        if (arena == null) return;
        MessageUtil.send(sender, "§7Arène §f" + arena.getName());
        MessageUtil.sendRaw(sender, " §8- État: §f" + arena.getState());
        MessageUtil.sendRaw(sender, " §8- Équipes: §f" + arena.getTeamsCount() + " x " + arena.getTeamSize());
        MessageUtil.sendRaw(sender, " §8- Configurée: §f" + arena.isFullyConfigured());
        MessageUtil.sendRaw(sender, " §8- Coffres infinis: §f" + arena.getChests().size());
    }

    // ── Tournois ─────────────────────────────────────────────────────────

    private void handleTournament(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender, "§cUsage: /tnt tournament <create|register|start|list|info>");
            return;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "create" -> {
                if (!checkAdmin(sender)) return;
                if (args.length < 4) {
                    MessageUtil.send(sender, "§cUsage: /tnt tournament create <nom> <arène>");
                    return;
                }
                Arena arena = plugin.getArenaManager().getArena(args[3]);
                if (arena == null) {
                    MessageUtil.send(sender, "§cArène introuvable.");
                    return;
                }
                plugin.getTournamentManager().create(args[2], args[3]);
                MessageUtil.send(sender, "§aTournoi " + args[2] + " créé sur l'arène " + args[3] + ".");
            }
            case "register" -> {
                if (!(sender instanceof Player player)) return;
                if (args.length < 4) {
                    MessageUtil.send(sender, "§cUsage: /tnt tournament register <tournoi> <nom d'équipe>");
                    return;
                }
                Tournament t = plugin.getTournamentManager().get(args[2]);
                if (t == null) {
                    MessageUtil.send(sender, "§cTournoi introuvable.");
                    return;
                }
                TournamentTeam team = plugin.getTournamentManager().register(t, args[3], player);
                if (team != null) {
                    MessageUtil.send(sender, "§aÉquipe " + args[3] + " inscrite au tournoi " + t.getName() + ".");
                } else {
                    MessageUtil.send(sender, "§cInscriptions closes pour ce tournoi.");
                }
            }
            case "start" -> {
                if (!checkAdmin(sender)) return;
                if (args.length < 3) {
                    MessageUtil.send(sender, "§cUsage: /tnt tournament start <tournoi>");
                    return;
                }
                Tournament t = plugin.getTournamentManager().get(args[2]);
                if (t == null) {
                    MessageUtil.send(sender, "§cTournoi introuvable.");
                    return;
                }
                plugin.getTournamentManager().start(t);
                MessageUtil.send(sender, "§aTournoi " + t.getName() + " démarré avec " + t.getRegistered().size() + " équipe(s).");
            }
            case "list" -> {
                MessageUtil.send(sender, "§7Tournois :");
                for (Tournament t : plugin.getTournamentManager().all()) {
                    MessageUtil.sendRaw(sender, " §8- §f" + t.getName() + " §7(" + t.getState() + ", " + t.getRegistered().size() + " équipes)");
                }
            }
            default -> MessageUtil.send(sender, "§cAction inconnue.");
        }
    }

    // ── Hologrammes de classement ────────────────────────────────────────

    private void handleHologram(CommandSender sender, String[] args) {
        if (!checkAdmin(sender) || !(sender instanceof Player player)) return;
        if (args.length < 3) {
            MessageUtil.send(sender, "§cUsage: /tnt hologram <create|remove> <id>");
            return;
        }
        String action = args[1].toLowerCase();
        String id = args[2];
        if (action.equals("create")) {
            plugin.getHologramManager().create(id, player.getLocation());
            MessageUtil.send(sender, "§aHologramme de classement créé : " + id);
        } else if (action.equals("remove")) {
            if (plugin.getHologramManager().remove(id)) {
                MessageUtil.send(sender, "§aHologramme supprimé.");
            } else {
                MessageUtil.send(sender, "§cHologramme introuvable.");
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        MessageUtil.sendRaw(sender, "§6§l== TntWars ==");
        MessageUtil.sendRaw(sender, "§e/tnt §7- ouvre le menu des arènes");
        MessageUtil.sendRaw(sender, "§e/tnt join [arène] §7- rejoindre");
        MessageUtil.sendRaw(sender, "§e/tnt leave §7- quitter");
        MessageUtil.sendRaw(sender, "§e/tnt top §7- classement");
        MessageUtil.sendRaw(sender, "§e/tnt cosmetics §7- effets de kill");
        MessageUtil.sendRaw(sender, "§e/tnt schema §7- voir/débloquer les schémas de canons");
        MessageUtil.sendRaw(sender, "§e/tnt level §7- votre progression (points/niveau)");
        MessageUtil.sendRaw(sender, "§e/tnt tournament <create|register|start|list> §7- tournois");
        if (sender.hasPermission("tntwars.admin")) {
            MessageUtil.sendRaw(sender, "§c-- Admin --");
            MessageUtil.sendRaw(sender, "§c/tnt create|delete <arène>");
            MessageUtil.sendRaw(sender, "§c/tnt setpos1|setpos2 <arène>");
            MessageUtil.sendRaw(sender, "§c/tnt setchestpos1|setchestpos2 <arène>");
            MessageUtil.sendRaw(sender, "§c/tnt setzone1|setzone2 <arène> <équipe>");
            MessageUtil.sendRaw(sender, "§c/tnt setspawn <arène> <équipe>");
            MessageUtil.sendRaw(sender, "§c/tnt setwaiting <arène>");
            MessageUtil.sendRaw(sender, "§c/tnt setteams|setteamsize <arène> <valeur>");
            MessageUtil.sendRaw(sender, "§c/tnt console <arène>");
            MessageUtil.sendRaw(sender, "§c/tnt debug <arène> [fix] §7- diagnostic / déblocage d'une arène coincée");
            MessageUtil.sendRaw(sender, "§c/tnt forcestart|stop <arène>");
            MessageUtil.sendRaw(sender, "§c/tnt hologram create|remove <id>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> subs = new ArrayList<>(List.of("join", "leave", "list", "gui", "top", "cosmetics", "schema", "level", "tournament", "info", "help"));
        if (sender.hasPermission("tntwars.admin")) {
            subs.addAll(List.of("create", "delete", "setpos1", "setpos2", "setchestpos1", "setchestpos2",
                    "setzone1", "setzone2", "setspawn", "setwaiting", "setteams", "setteamsize",
                    "console", "debug", "forcestart", "stop", "hologram"));
        }
        if (args.length == 1) {
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && List.of("join", "delete", "setpos1", "setpos2", "setchestpos1", "setchestpos2",
                "setzone1", "setzone2", "setspawn", "setwaiting", "setteams", "setteamsize",
                "console", "debug", "forcestart", "stop", "info").contains(args[0].toLowerCase())) {
            return plugin.getArenaManager().getArenas().stream().map(Arena::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("debug")) {
            return List.of("fix").stream().filter(s -> s.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
