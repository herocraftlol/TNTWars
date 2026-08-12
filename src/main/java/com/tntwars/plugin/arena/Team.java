package com.tntwars.plugin.arena;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Une équipe au sein d'une arène de TNT Wars.
 * Chaque équipe possède sa propre zone de construction, son spawn, sa couleur
 * (laine/béton) et la liste de ses membres (vivants ou éliminés).
 */
public class Team {

    public static final Material[] CONCRETE_CYCLE = {
            Material.RED_CONCRETE,
            Material.LIME_CONCRETE,
            Material.BLUE_CONCRETE,
            Material.YELLOW_CONCRETE
    };

    public static final String[] NAME_CYCLE = {"Rouge", "Vert", "Bleu", "Jaune"};

    private final int index;
    private CuboidRegion zone;
    private Location spawn;
    private final Set<UUID> members = new LinkedHashSet<>();
    private final Set<UUID> alive = new LinkedHashSet<>();

    public Team(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Material getConcrete() {
        return CONCRETE_CYCLE[index % CONCRETE_CYCLE.length];
    }

    public String getName() {
        return NAME_CYCLE[index % NAME_CYCLE.length];
    }

    public String getColoredName() {
        return switch (index % CONCRETE_CYCLE.length) {
            case 0 -> "§c" + getName();
            case 1 -> "§a" + getName();
            case 2 -> "§9" + getName();
            default -> "§e" + getName();
        };
    }

    public CuboidRegion getZone() {
        return zone;
    }

    public void setZone(CuboidRegion zone) {
        this.zone = zone;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getAlive() {
        return alive;
    }

    public boolean isFull(int teamSize) {
        return members.size() >= teamSize;
    }

    public void reset() {
        members.clear();
        alive.clear();
    }

    public void startRound() {
        alive.clear();
        alive.addAll(members);
    }
}
