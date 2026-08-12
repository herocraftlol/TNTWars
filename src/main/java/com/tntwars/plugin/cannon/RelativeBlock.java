package com.tntwars.plugin.cannon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Un bloc d'un schéma de canon, positionné relativement à un point d'origine (0,0,0).
 * Le sens "avant" du canon est toujours +Z (le schéma est tourné pour faire face au
 * joueur au moment de l'aperçu).
 */
public class RelativeBlock {

    private final int dx, dy, dz;
    private final String blockDataString;

    public RelativeBlock(int dx, int dy, int dz, Material material) {
        this(dx, dy, dz, material.createBlockData().getAsString());
    }

    public RelativeBlock(int dx, int dy, int dz, String blockDataString) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.blockDataString = blockDataString;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDz() { return dz; }

    public BlockData toBlockData() {
        try {
            return Bukkit.createBlockData(blockDataString);
        } catch (IllegalArgumentException e) {
            return Bukkit.createBlockData(Material.STONE);
        }
    }
}
