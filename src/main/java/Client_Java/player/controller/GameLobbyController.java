package Client_Java.player.controller;

import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.LobbyLeaderboardCardModel;
import Client_Java.player.view.GameLobbyView;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.view.cards.LobbyLeaderboardCardView;
import Client_Java.player.SessionManager;
import Shared_Files.PlayerAccount;
import AuthenticationIDL.NotLoggedInException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;
import java.util.List;

/**
 * Controller for the game lobby screen.
 * Strict MVC: all navigation goes through ViewNavigator.
 */
public class GameLobbyController {
    private final GameLobbyModel model;
    private final GameLobbyView view;
    private final PlayerAccount player;

    public GameLobbyController(
            GameLobbyModel model,
            GameLobbyView view,
            PlayerAccount player
    ) {
        this.model = model;
        this.view = view;
        this.player = player;
        initialize();
    }

    private void initialize() {
        System.out.println("[DEBUG] Initializing GameLobbyController");
        view.setActionEnterGameButton(this::handleEnterGame);
        view.setActionQuitButton(this::handleQuitButton);
        view.setActionAboutButton(this::handleAbout);
        populateLeaderboard();
        updateCurrentUserInfo();
    }

    private void populateLeaderboard() {
        try {
            // Fetch leaderboard data from model
            String sessionToken = SessionManager.getSessionToken();
            List<LobbyLeaderboardCardModel> leaderboardModels = model.getLeaderboardData(sessionToken);
            System.out.println("[DEBUG] Fetched " + leaderboardModels.size() + " leaderboard entries");

            // Clear existing leaderboard cards
            view.getLeaderboardsFlowPane().getChildren().clear();
            System.out.println("[DEBUG] Cleared leaderboardsFlowPane");

            // Create and add leaderboard cards
            for (LobbyLeaderboardCardModel cardModel : leaderboardModels) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player/LobbyLeaderboardCard.fxml"));
                    System.out.println("[DEBUG] Loading /fxml/player/LobbyLeaderboardCard.fxml for " + cardModel.getUsername());
                    Node cardNode = loader.load();
                    LobbyLeaderboardCardView cardView = loader.getController();
                    if (cardView == null) {
                        System.err.println("[ERROR] LobbyLeaderboardCardView controller is null");
                    } else {
                        System.out.println("[DEBUG] LobbyLeaderboardCardView controller loaded successfully");
                    }
                    LobbyLeaderboardCardController cardController = new LobbyLeaderboardCardController(cardModel, cardView);
                    view.getLeaderboardsFlowPane().getChildren().add(cardNode);
                    System.out.println("[DEBUG] Added leaderboard card for " + cardModel.getUsername());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("[ERROR] Failed to load leaderboard card: " + e.getMessage());
                }
            }
            System.out.println("[DEBUG] Added " + view.getLeaderboardsFlowPane().getChildren().size() + " leaderboard cards");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ERROR] Failed to populate leaderboard: " + e.getMessage());
        }
    }

    private void updateCurrentUserInfo() {
        try {
            String username = player.getUsername();
            String sessionToken = SessionManager.getSessionToken();
            List<LobbyLeaderboardCardModel> leaderboard = model.getLeaderboardData(sessionToken);
            for (LobbyLeaderboardCardModel model : leaderboard) {
                if (model.getUsername().equals(username)) {
                    view.getCurrentUserRankLB().setText("# " + model.getRank());
                    view.getCurrentUserLB().setText(model.getUsername());
                    view.getCurrentUserPointsLB().setText(String.valueOf(model.getPoints()));
                    System.out.println("[DEBUG] Updated current user info: " + username);
                    return;
                }
            }
            // Fallback if player not in leaderboard
            view.getCurrentUserRankLB().setText("# N/A");
            view.getCurrentUserLB().setText(username);
            view.getCurrentUserPointsLB().setText("0");
            System.out.println("[DEBUG] Current user not in leaderboard, using fallback: " + username);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ERROR] Failed to update user info: " + e.getMessage());
        }
    }

    private void handleEnterGame(ActionEvent event) {
        try {
            ViewNavigator.goToWaitingRoom();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ERROR] Could not open waiting room: " + e.getMessage());
        }
    }

    private void handleQuitButton(ActionEvent event) {
        try {
            // Call logout on the AuthenticationService
            SessionManager.getAuthService().logout(
                    player.getPlayerId(),
                    SessionManager.getSessionToken()
            );
            // Clear session data
            SessionManager.setLoggedInPlayer(null);
            SessionManager.setSessionToken(null);
            // Navigate to login screen
            ViewNavigator.goToLogin();
        } catch (NotLoggedInException e) {
            // Even if session is invalid, still navigate to login
            try {
                ViewNavigator.goToLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("[ERROR] Error navigating to login: " + ex.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ERROR] Logout failed: " + e.getMessage());
        }
    }

    private void handleAbout(ActionEvent event) {
        System.out.println("[DEBUG] About button clicked");
        // Implement about dialog if needed
    }
}