package com.tntwars.plugin.cosmetics;

import org.bukkit.Particle;
import org.bukkit.Sound;

public enum KillEffect {

    NONE("Aucun", org.bukkit.Material.BARRIER, null, null),
    FIREWORK("Feu d'artifice", org.bukkit.Material.FIREWORK_ROCKET, Particle.FIREWORK, Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    EXPLOSION("Explosion", org.bukkit.Material.TNT, Particle.EXPLOSION, Sound.ENTITY_GENERIC_EXPLODE),
    HEARTS("Coeurs", org.bukkit.Material.RED_DYE, Particle.HEART, Sound.ENTITY_VILLAGER_YES),
    LIGHTNING_SOUND("Tonnerre", org.bukkit.Material.TRIDENT, Particle.CLOUD, Sound.ENTITY_LIGHTNING_BOLT_THUNDER),
    TOTEM("Totem", org.bukkit.Material.TOTEM_OF_UNDYING, Particle.TOTEM_OF_UNDYING, Sound.ITEM_TOTEM_USE);

    private final String displayName;
    private final org.bukkit.Material icon;
    private final Particle particle;
    private final Sound sound;

    KillEffect(String displayName, org.bukkit.Material icon, Particle particle, Sound sound) {
        this.displayName = displayName;
        this.icon = icon;
        this.particle = particle;
        this.sound = sound;
    }

    public String getDisplayName() {
        return displayName;
    }

    public org.bukkit.Material getIcon() {
        return icon;
    }

    public Particle getParticle() {
        return particle;
    }

    public Sound getSound() {
        return sound;
    }

    public static KillEffect fromId(String id) {
        for (KillEffect e : values()) {
            if (e.name().equalsIgnoreCase(id)) return e;
        }
        return NONE;
    }
}
