package com.tntwars.plugin.arena;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Représente le "modèle" de contenu d'un coffre infini situé dans la zone de coffre
 * d'une arène. Dès qu'un joueur retire un item de ce coffre, son contenu est restauré
 * au tick suivant afin qu'il reste toujours plein.
 */
public class ChestData {

    private final Location location;
    private List<ItemStack> template;

    public ChestData(Location location, List<ItemStack> template) {
        this.location = location;
        this.template = template;
    }

    public Location getLocation() {
        return location;
    }

    public List<ItemStack> getTemplate() {
        return template;
    }

    public void setTemplate(List<ItemStack> template) {
        this.template = template;
    }
}
