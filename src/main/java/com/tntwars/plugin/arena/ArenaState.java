package com.tntwars.plugin.arena;

public enum ArenaState {
    /** Arène incomplète (mal configurée), non rejoignable. */
    DISABLED,
    /** En attente de joueurs dans le lobby de l'arène. */
    WAITING,
    /** Décompte de lancement en cours. */
    STARTING,
    /** Partie en cours. */
    INGAME,
    /** Partie terminée, régénération de la map en cours. */
    RESTARTING
}
