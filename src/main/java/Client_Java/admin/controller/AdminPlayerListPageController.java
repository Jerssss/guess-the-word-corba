package Client_Java.admin.controller;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.model.AdminPlayerListPageModel;
import Client_Java.admin.view.AdminPlayerListPageView;
import Shared_Files.PlayerAccount;
import AdminIDL.AccountCurrentlyActiveException;
import AdminIDL.AccountNotFoundException;
import AdminIDL.NotLoggedInException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.Date;
import java.util.List;

public class AdminPlayerListPageController {

    private final AdminPlayerListPageModel model;
    private final AdminPlayerListPageView view;
    private ObservableList<PlayerAccount> playerData;
    private FilteredList<PlayerAccount> filteredData;

    public AdminPlayerListPageController(AdminPlayerListPageModel model, AdminPlayerListPageView view) {
        this.model = model;
        this.view = view;
        this.playerData = FXCollections.observableArrayList();

        System.out.println("[DEBUG] Current stage before setting return button action: " + AdminClient_Java.getStage());

        loadPlayers();
        setupSearchFilter();
        setupEditPlayerCallback();
        setupDeletePlayerCallback();
        view.setActionReturnButton(event -> view.showAdminMainMenu(AdminClient_Java.getLoggidInAdmin()));
    }

    public void loadPlayers() {
        List<PlayerAccount> players = model.fetchPlayers();
        if (players != null) {
            Platform.runLater(() -> {
                playerData.setAll(players);
                view.updateTable(playerData);
                System.out.println("[INFO] Loaded " + players.size() + " players.");
                System.out.println("[DEBUG] Player data loaded: " + playerData);
                System.out.println("[DEBUG] Filtered data size: " + filteredData.size());
            });
        } else {
            System.err.println("[ERROR] Failed to load players.");
        }
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(playerData, p -> true);
        Platform.runLater(() -> {
            view.playersTable.setItems(filteredData);
        });
        view.searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            model.searchPlayers(newValue, playerData, filteredData);
            Platform.runLater(() -> {
                view.playersTable.refresh();
                System.out.println("[DEBUG] TableView refreshed, filteredData size: " + filteredData.size());
            });
        });
    }

    private void setupEditPlayerCallback() {
        view.setOnEditPlayerCallback(updatedPlayer -> {
            boolean success = model.editPlayer(updatedPlayer.getPlayerId(), updatedPlayer.getPassword());
            if (success) {
                System.out.println("[INFO] Player updated successfully: " + updatedPlayer.getUsername());
                loadPlayers();
            } else {
                System.err.println("[ERROR] Failed to update player: " + updatedPlayer.getUsername());
            }
        });
    }

    private void setupDeletePlayerCallback() {
        view.setOnDeletePlayerCallback(player -> {
            boolean success = false;
            try {
                System.out.println("[DEBUG] Attempting to delete player: " + player.getUsername());
                success = model.removePlayer(player.getPlayerId());
            } catch (AccountCurrentlyActiveException e) {
                System.err.println("[ERROR] Cannot delete player: Account is currently active: " + player.getUsername());
                System.out.println("[DEBUG] Caught AccountCurrentlyActiveException, triggering alert");
                Platform.runLater(() -> view.showErrorAlert("Cannot Delete Player",
                        "Player " + player.getUsername() + " is currently active and cannot be deleted."));
            } catch (AccountNotFoundException e) {
                System.err.println("[ERROR] Cannot delete player: Account not found: " + player.getUsername());
                System.out.println("[DEBUG] Caught AccountNotFoundException, triggering alert");
                Platform.runLater(() -> view.showErrorAlert("Cannot Delete Player",
                        "Player " + player.getUsername() + " was not found."));
            } catch (NotLoggedInException e) {
                System.err.println("[ERROR] Cannot delete player: Not logged in: " + player.getUsername());
                System.out.println("[DEBUG] Caught NotLoggedInException, triggering alert");
                Platform.runLater(() -> view.showErrorAlert("Cannot Delete Player",
                        "You are not logged in. Please log in and try again."));
            } catch (Exception e) {
                System.err.println("[ERROR] Unexpected error deleting player: " + player.getUsername());
                e.printStackTrace();
                System.out.println("[DEBUG] Caught unexpected exception, triggering alert");
                Platform.runLater(() -> view.showErrorAlert("Unexpected Error",
                        "An unexpected error occurred while deleting player " + player.getUsername() + "."));
            }

            if (success) {
                System.out.println("[INFO] Player deleted successfully: " + player.getUsername());
                loadPlayers();
            }
        });
    }
}