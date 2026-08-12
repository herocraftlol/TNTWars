package com.tntwars.plugin.tournament;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class TournamentTeam {

    private final String name;
    private final UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();
    private boolean eliminated = false;

    public TournamentTeam(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
