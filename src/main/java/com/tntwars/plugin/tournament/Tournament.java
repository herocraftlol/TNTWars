package com.tntwars.plugin.tournament;

import java.util.ArrayList;
import java.util.List;

public class Tournament {

    public enum State { REGISTRATION, IN_PROGRESS, FINISHED }

    private final String name;
    private final String arenaName;
    private State state = State.REGISTRATION;
    private final List<TournamentTeam> registered = new ArrayList<>();
    private final List<List<BracketMatch>> rounds = new ArrayList<>();
    private TournamentTeam champion;

    public Tournament(String name, String arenaName) {
        this.name = name;
        this.arenaName = arenaName;
    }

    public String getName() {
        return name;
    }

    public String getArenaName() {
        return arenaName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<TournamentTeam> getRegistered() {
        return registered;
    }

    public List<List<BracketMatch>> getRounds() {
        return rounds;
    }

    public TournamentTeam getChampion() {
        return champion;
    }

    public void setChampion(TournamentTeam champion) {
        this.champion = champion;
    }

    /** Génère le premier tour du tableau (avec "byes" si le nombre d'équipes n'est pas une puissance de 2). */
    public void generateBracket() {
        rounds.clear();
        List<TournamentTeam> teams = new ArrayList<>(registered);
        int size = 1;
        while (size < teams.size()) size *= 2;
        while (teams.size() < size) teams.add(null); // bye

        List<BracketMatch> firstRound = new ArrayList<>();
        int idx = 0;
        for (int i = 0; i < teams.size(); i += 2) {
            firstRound.add(new BracketMatch(0, idx++, teams.get(i), teams.get(i + 1)));
        }
        rounds.add(firstRound);
        // Génère les tours suivants vides, à remplir au fur et à mesure
        int roundSize = firstRound.size() / 2;
        int round = 1;
        while (roundSize >= 1) {
            List<BracketMatch> r = new ArrayList<>();
            for (int i = 0; i < roundSize; i++) {
                r.add(new BracketMatch(round, i, null, null));
            }
            rounds.add(r);
            roundSize /= 2;
            round++;
        }
    }

    /** Trouve le prochain match jouable (les deux équipes connues, pas encore joué). */
    public BracketMatch nextPlayableMatch() {
        for (List<BracketMatch> round : rounds) {
            for (BracketMatch match : round) {
                if (match.isReady()) return match;
            }
        }
        return null;
    }

    /** Propage le vainqueur d'un match vers le tour suivant. */
    public void advanceWinner(BracketMatch match) {
        int nextRoundIdx = match.getRound() + 1;
        if (nextRoundIdx >= rounds.size()) {
            champion = match.getWinner();
            state = State.FINISHED;
            return;
        }
        int nextMatchIdx = match.getIndexInRound() / 2;
        BracketMatch nextMatch = rounds.get(nextRoundIdx).get(nextMatchIdx);
        if (match.getIndexInRound() % 2 == 0) {
            nextMatch = new BracketMatch(nextMatch.getRound(), nextMatch.getIndexInRound(), match.getWinner(), nextMatch.getTeamB());
        } else {
            nextMatch = new BracketMatch(nextMatch.getRound(), nextMatch.getIndexInRound(), nextMatch.getTeamA(), match.getWinner());
        }
        rounds.get(nextRoundIdx).set(nextMatchIdx, nextMatch);
    }
}
