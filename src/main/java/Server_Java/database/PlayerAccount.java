package Server_Java.database;

public class PlayerAccount {
    private int player_id;
    private String username;
    private String password;
    private int game_wins;

    // Constructor to create a new player account instance
    public PlayerAccount(int pid, String uname, String pass, int wins) {
        this.player_id = pid;
        this.username = uname;
        this.password = pass;
        this.game_wins = wins;
    }

    // returns the player id of the account
    public int getPlayer_id() {
        return player_id;
    }

    // assigns the player id of the account using the parameter
    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    // returns the username of the account
    public String getUsername() {
        return username;
    }

    // assigns username of the account using the parameter
    public void setUsername(String username) {
        this.username = username;
    }

    // returns the password of the account
    public String getPassword() {
        return password;
    }

    // assigns password of the account using the parameter
    public void setPassword(String password) {
        this.password = password;
    }

    // returns the number of wins of the account
    public int getGame_wins() {
        return game_wins;
    }

    // assigns the number of wins of the account using the parameter
    public void setGame_wins(int game_wins) {
        this.game_wins = game_wins;
    }
}
