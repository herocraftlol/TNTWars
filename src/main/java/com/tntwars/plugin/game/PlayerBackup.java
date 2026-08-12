package com.tntwars.plugin.game;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Sauvegarde de l'état d'un joueur avant qu'il ne rejoigne une arène
 * (position, inventaire, mode de jeu, XP...) afin de tout lui restaurer
 * quand il quitte l'arène (/tnt leave) ou quand la partie se termine.
 */
public class PlayerBackup {

    private final Location location;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final GameMode gameMode;
    private final float exp;
    private final int level;
    private final double health;
    private final int foodLevel;

    public PlayerBackup(Location location, ItemStack[] inventory, ItemStack[] armor, GameMode gameMode,
                         float exp, int level, double health, int foodLevel) {
        this.location = location;
        this.inventory = inventory;
        this.armor = armor;
        this.gameMode = gameMode;
        this.exp = exp;
        this.level = level;
        this.health = health;
        this.foodLevel = foodLevel;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public float getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public double getHealth() {
        return health;
    }

    public int getFoodLevel() {
        return foodLevel;
    }
}
