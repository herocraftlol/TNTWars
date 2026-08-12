package com.tntwars.plugin.tournament;

public class BracketMatch {

    public enum Status { PENDING, WAITING_OPPONENT, ONGOING, DONE }

    private final int round;
    private final int indexInRound;
    private TournamentTeam teamA;
    private TournamentTeam teamB;
    private TournamentTeam winner;
    private Status status = Status.PENDING;

    public BracketMatch(int round, int indexInRound, TournamentTeam teamA, TournamentTeam teamB) {
        this.round = round;
        this.indexInRound = indexInRound;
        this.teamA = teamA;
        this.teamB = teamB;
        if (teamA != null && teamB == null) {
            // Bye : qualification automatique
            this.winner = teamA;
            this.status = Status.DONE;
        }
    }

    public int getRound() {
        return round;
    }

    public int getIndexInRound() {
        return indexInRound;
    }

    public TournamentTeam getTeamA() {
        return teamA;
    }

    public TournamentTeam getTeamB() {
        return teamB;
    }

    public void setTeamB(TournamentTeam teamB) {
        this.teamB = teamB;
    }

    public TournamentTeam getWinner() {
        return winner;
    }

    public void setWinner(TournamentTeam winner) {
        this.winner = winner;
        this.status = Status.DONE;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isReady() {
        return status == Status.PENDING && teamA != null && teamB != null;
    }
}
