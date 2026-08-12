package com.tntwars.plugin.cannon;

import org.bukkit.Material;

import java.util.List;

public class CannonSchematic {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int requiredLevel;
    private final List<RelativeBlock> blocks;
    private final List<String> instructions;

    public CannonSchematic(String id, String displayName, Material icon, int requiredLevel,
                            List<RelativeBlock> blocks, List<String> instructions) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.requiredLevel = requiredLevel;
        this.blocks = blocks;
        this.instructions = instructions;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public List<RelativeBlock> getBlocks() {
        return blocks;
    }

    public List<String> getInstructions() {
        return instructions;
    }
}
