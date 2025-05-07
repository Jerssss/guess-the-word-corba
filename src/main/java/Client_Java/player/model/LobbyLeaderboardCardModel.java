package Client_Java.player.model;

public class LobbyLeaderboardCardModel {
    private int rank;
    private String username;
    private int points;

    public LobbyLeaderboardCardModel(int rank, String username, int points) {
        this.rank = rank;
        this.username = username;
        this.points = points;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}