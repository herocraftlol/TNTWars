package com.tntwars.plugin;

import com.tntwars.plugin.arena.ArenaManager;
import com.tntwars.plugin.arena.ChestManager;
import com.tntwars.plugin.cannon.CannonSchematicRegistry;
import com.tntwars.plugin.cannon.SchematicPreviewManager;
import com.tntwars.plugin.commands.TntCommand;
import com.tntwars.plugin.cosmetics.CosmeticManager;
import com.tntwars.plugin.game.GameManager;
import com.tntwars.plugin.gui.ArenaConsoleGUI;
import com.tntwars.plugin.gui.ArenaListGUI;
import com.tntwars.plugin.gui.CosmeticGUI;
import com.tntwars.plugin.gui.GuiListener;
import com.tntwars.plugin.gui.LeaderboardGUI;
import com.tntwars.plugin.gui.SchematicGUI;
import com.tntwars.plugin.hologram.LeaderboardHologramManager;
import com.tntwars.plugin.listeners.ArenaProtectionListener;
import com.tntwars.plugin.listeners.CannonTrackerListener;
import com.tntwars.plugin.listeners.ChestRefillListener;
import com.tntwars.plugin.listeners.LockedItemListener;
import com.tntwars.plugin.listeners.PlayerCombatListener;
import com.tntwars.plugin.listeners.PlayerConnectionListener;
import com.tntwars.plugin.listeners.PlayerDamageListener;
import com.tntwars.plugin.listeners.PlayerFallListener;
import com.tntwars.plugin.listeners.WaitingItemListener;
import com.tntwars.plugin.progression.ProgressManager;
import com.tntwars.plugin.scoreboard.ScoreboardManager;
import com.tntwars.plugin.stats.StatsManager;
import com.tntwars.plugin.tournament.TournamentManager;
import com.tntwars.plugin.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class TntWarsPlugin extends JavaPlugin {

    private ArenaManager arenaManager;
    private ChestManager chestManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private CosmeticManager cosmeticManager;
    private TournamentManager tournamentManager;
    private ScoreboardManager scoreboardManager;
    private LeaderboardHologramManager hologramManager;
    private ProgressManager progressManager;
    private CannonSchematicRegistry schematicRegistry;
    private SchematicPreviewManager schematicPreviewManager;
    private CannonTrackerListener cannonTrackerListener;

    private ArenaListGUI arenaListGUI;
    private ArenaConsoleGUI arenaConsoleGUI;
    private LeaderboardGUI leaderboardGUI;
    private CosmeticGUI cosmeticGUI;
    private SchematicGUI schematicGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        MessageUtil.setPrefix(getConfig().getString("messages.prefix", "&8[&cTNT&fWars&8] &r"));

        this.chestManager = new ChestManager(this);
        this.arenaManager = new ArenaManager(this);
        this.statsManager = new StatsManager(this);
        this.cosmeticManager = new CosmeticManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.progressManager = new ProgressManager(this);
        this.schematicRegistry = new CannonSchematicRegistry();
        this.schematicPreviewManager = new SchematicPreviewManager(this);
        this.gameManager = new GameManager(this);
        this.tournamentManager = new TournamentManager(this);
        this.hologramManager = new LeaderboardHologramManager(this);

        this.arenaListGUI = new ArenaListGUI(this);
        this.arenaConsoleGUI = new ArenaConsoleGUI(this);
        this.leaderboardGUI = new LeaderboardGUI(this);
        this.cosmeticGUI = new CosmeticGUI(this);
        this.schematicGUI = new SchematicGUI(this);

        this.cannonTrackerListener = new CannonTrackerListener(this);

        getServer().getPluginManager().registerEvents(new ArenaProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestRefillListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerFallListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new WaitingItemListener(this), this);
        getServer().getPluginManager().registerEvents(new LockedItemListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(cannonTrackerListener, this);

        TntCommand executor = new TntCommand(this);
        getCommand("tnt").setExecutor(executor);
        getCommand("tnt").setTabCompleter(executor);

        getLogger().info("TntWars activé (" + arenaManager.getArenas().size() + " arène(s) chargée(s)).");
    }

    @Override
    public void onDisable() {
        if (schematicPreviewManager != null) schematicPreviewManager.hideAll();
        if (arenaManager != null) arenaManager.save();
        if (statsManager != null) statsManager.save();
        if (hologramManager != null) hologramManager.save();
        if (progressManager != null) progressManager.save();
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public CosmeticManager getCosmeticManager() {
        return cosmeticManager;
    }

    public TournamentManager getTournamentManager() {
        return tournamentManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public LeaderboardHologramManager getHologramManager() {
        return hologramManager;
    }

    public ArenaListGUI getArenaListGUI() {
        return arenaListGUI;
    }

    public ArenaConsoleGUI getArenaConsoleGUI() {
        return arenaConsoleGUI;
    }

    public LeaderboardGUI getLeaderboardGUI() {
        return leaderboardGUI;
    }

    public CosmeticGUI getCosmeticGUI() {
        return cosmeticGUI;
    }

    public ProgressManager getProgressManager() {
        return progressManager;
    }

    public CannonSchematicRegistry getSchematicRegistry() {
        return schematicRegistry;
    }

    public SchematicPreviewManager getSchematicPreviewManager() {
        return schematicPreviewManager;
    }

    public SchematicGUI getSchematicGUI() {
        return schematicGUI;
    }
}
