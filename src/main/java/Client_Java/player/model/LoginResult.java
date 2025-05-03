package Client_Java.player.model;

import Shared_Files.PlayerAccount;

public class LoginResult {
    private final PlayerAccount account;
    private final String sessionToken;

    public LoginResult(PlayerAccount account, String sessionToken) {
        this.account      = account;
        this.sessionToken = sessionToken;
    }

    public PlayerAccount getAccount() { return account; }
    public String getSessionToken() { return sessionToken; }
}
