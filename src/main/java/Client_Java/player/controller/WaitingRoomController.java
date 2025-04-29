    package Client_Java.player.controller;

    import Client_Java.player.PlayerClient_Model;
    import Client_Java.player.PlayerClient_Java;
    import Client_Java.player.model.WaitingRoomModel;
    import Client_Java.player.view.WaitingRoomView;
    import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;

    import Server_Java.implementation.GameCallbackServiceImpl;
    import javafx.application.Platform;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.scene.control.Label;

    import java.util.concurrent.Executors;
    import java.util.concurrent.ScheduledExecutorService;
    import java.util.concurrent.TimeUnit;

    /**
     * Controller for the waiting room: joins lobby, registers game callback,
     * waits until minimum players then counts down.
     */
    public class WaitingRoomController {
        private final WaitingRoomModel model;
        private final WaitingRoomView view;
        private final int playerId;
        private final String sessionToken;
        private String gameToken;

        private ScheduledExecutorService scheduler;
        private int countdown;
        private int minimumPlayers;
        private int initialCountdown;

        @FXML private Label countdownLabel;
        @FXML private Label playerCountLabel;

        public WaitingRoomController(WaitingRoomModel model, WaitingRoomView view,
                                     int playerId, String sessionToken) {
            this.model = model;
            this.view = view;
            this.playerId = playerId;
            this.sessionToken = sessionToken;
            initialize();
        }

        private void initialize() {
            // 1. Join the lobby and get a game token
            this.gameToken = model.joinLobby(playerId, sessionToken);
            if (gameToken == null) {
                System.err.println("[WaitingRoom] Failed to join lobby.");
                return;
            }

            // 2. Register game callback for start notifications
            GameCallbackServiceImpl callbackImpl = new GameCallbackServiceImpl();
            GameCallBackService callbackStub = PlayerClient_Model.registerGameCallback(callbackImpl);
            model.registerCallback(playerId, gameToken, sessionToken, callbackStub);

            // 3. Fetch settings
            countdown = model.getSetting("countdown_to_game_start", sessionToken);
            minimumPlayers = model.getSetting("minimum_players", sessionToken);

            // 4. Initialize UI
            view.setWaitingPlayersCount(0);
            view.setRemainingTime(countdown);
            view.setActionCancelButton(this::handleCancel);

            // 5. Start polling until minimum players joined
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::pollJoin, 1, 1, TimeUnit.SECONDS);
        }

        private void pollJoin() {
            int joined = model.getNumberOfPlayersJoined(playerId, sessionToken);
            Platform.runLater(() -> view.setWaitingPlayersCount(joined));

            if (joined >= minimumPlayers) {
                scheduler.shutdownNow();
                // Start countdown phase
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(this::pollCountdown, 1, 1, TimeUnit.SECONDS);
            }
        }

        private void pollCountdown() {
            countdown--;
            Platform.runLater(() -> view.setRemainingTime(countdown));

            if (countdown <= 0) {
                scheduler.shutdownNow();
                onReadyToStart();
            }
        }

        /**
         * Called when countdown finishes or callback triggers game start.
         */
        private void onReadyToStart() {
            System.out.println("[WaitingRoom] Game starting now.");
            // TODO: Transition to actual game view
        }

        private void handleCancel(ActionEvent event) {
            // stop the scheduler
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }
            // tell the server we're leaving
            model.leaveLobby(playerId, gameToken, sessionToken);

            // go back to lobby screen
            PlayerClient_Java.navigateToLobby();
        }

    }
