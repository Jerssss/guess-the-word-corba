package Shared_Files;

public class PlayerAccount {
    private int player_id;
    private String username;
    private String password;
    private int game_wins;

    public PlayerAccount(int pid, String uname, String pass, int wins) {
        this.player_id = pid;
        this.username = uname;
        this.password = pass;
        this.game_wins = wins;
    }

    public int getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getGame_wins() {
        return game_wins;
    }

    public void setGame_wins(int game_wins) {
        this.game_wins = game_wins;
    }
}
