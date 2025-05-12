package Shared_Files;

/**
 * Represents a player account with credentials and stats.
 */
public class PlayerAccount {
    private final int playerID;
    private String name;
    private final String username;
    private String password;
    private int gameWins;

    /**
     * Constructor: sets the player ID and credentials.
     * @param playerID   unique ID assigned by server
     * @param username   login name
     * @param password   login password
     * @param gameWins   initial number of wins
     */
    public PlayerAccount(int playerID, String username, String password, int gameWins) {
        this.playerID = playerID;
        this.username = username;
        this.password = password;
        this.gameWins = gameWins;
    }

    public PlayerAccount(int playerID, String name, String username, String password, int gameWins) {
        this.playerID = playerID;
        this.name = name;
        this.username = username;
        this.password = password;
        this.gameWins = gameWins;
    }

    public int getPlayerId() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getGameWins() {
        return gameWins;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGameWins(int gameWins) {
        this.gameWins = gameWins;
    }
}
