package Client_Java.admin.model;

import Shared_Files.PlayerAccount;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AdminPlayerListPageModel {

    private final ObservableList<PlayerAccount> playerList = FXCollections.observableArrayList();

    public ObservableList<PlayerAccount> getPlayerList() {
        return playerList;
    }

    public void addPlayer(PlayerAccount player) {
        playerList.add(player);
    }

    public void removePlayer(PlayerAccount player) {
        playerList.remove(player);
    }

    public void clearPlayers() {
        playerList.clear();
    }
}
