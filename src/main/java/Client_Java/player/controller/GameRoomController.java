package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.implementation.GameCallbackServiceImpl;
import GameIDL.*;
import PlayerCallBackIDL.GameCallBackService;
import PlayerCallBackIDL.GameCallBackServiceHelper;
import PlayerCallBackIDL.GameCallBackServicePOA;
import javafx.application.Platform;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRoomController {
    private final GameRoomModel model;
    private final GameRoomView view;
    private final int playerId;
    private final String sessionToken;
    private final String gameToken;

    private int roundNumber = 0;
    private String secretWord = "";
    private int remainingLives;
    private int wrongCount = 0;
    private final List<String> blankLabels = new ArrayList<>();
    private boolean endPopupShowing = false;
    private boolean isRoundInitialized = false;
    private boolean isRoundActive = false; // Track if round is active
    private final Map<String, Integer> winCounts = new HashMap<>();
    private long roundStartTime;

    public GameRoomController(
            GameRoomModel model,
            GameRoomView view,
            int playerId,
            String sessionToken,
            String gameToken
    ) throws WrongPolicy, ServantNotActive, NotLoggedInException {
        this.model = model;
        this.view = view;
        this.playerId = playerId;
        this.sessionToken = sessionToken;
        this.gameToken = gameToken;

        view.setController(this);

        remainingLives = model.getNumberOfLives(sessionToken);
        view.updateLifeCount(remainingLives);
        view.updateRoundLabel(roundNumber);

        GameCallbackServiceImpl servantImpl = new GameCallbackServiceImpl(this);
        GameCallBackServicePOA poaServant = servantImpl;
        Object cbRef = PlayerClient_Java.getClientModel().registerGameCallback(poaServant);
        GameCallBackService cbStub = GameCallBackServiceHelper.narrow(cbRef);
        model.registerCallback(cbStub);

        handleGameStart();
    }

    public void handleGameStart() {
        System.out.println("[DEBUG] handleGameStart() → request Round 1");
        model.startRound(gameToken, 1, playerId, sessionToken);
    }

    public void notifyRoundStartFromCallback(int newRound) {
        if (endPopupShowing) {
            view.scheduleRetryRoundStart(newRound, 1);
            return;
        }
        if (newRound <= roundNumber) return;

        roundNumber = newRound;
        Platform.runLater(() -> view.showRoundStartScene(newRound));
    }

    public void handleServerRoundStart(int roundNum) {
        System.out.println("[DEBUG] handleServerRoundStart for round " + roundNum);
        roundStartTime = System.currentTimeMillis();
        secretWord = model.getRandomWord(gameToken, roundNum, playerId, sessionToken);
        if (secretWord == null || secretWord.isEmpty()) {
            System.err.println("[ERROR] secretWord is null or empty, skipping round setup");
            view.showRoundEndPopup(null, "Error: No word available", true);
            return;
        }
        System.out.println("[DEBUG] secretWord: " + secretWord);
        remainingLives = model.getNumberOfLives(sessionToken);
        wrongCount = 0;
        view.updateLifeCount(remainingLives);
        view.updateRoundLabel(roundNum);
        view.resetAlphabetButtons();
        setupBlanks();
        isRoundInitialized = true;
        isRoundActive = true;
        view.enableAlphabetButtons();
        view.startCountdown(model.getRoundDuration(sessionToken));
    }

    private void setupBlanks() {
        blankLabels.clear();
        System.out.println("[DEBUG] Setting up blanks for secretWord: " + secretWord);
        for (int i = 0; i < secretWord.length(); i++) {
            blankLabels.add("_");
        }
        System.out.println("[DEBUG] blankLabels after setup: " + blankLabels);
        view.setupBlanks(secretWord.length());
    }

    public void handleGuess(char letter) {
        if (!isRoundInitialized || !isRoundActive) {
            System.err.println("[ERROR] Round not initialized or ended, ignoring guess for letter " + letter);
            return;
        }
        try {
            System.out.println("[DEBUG] Processing guess for letter: " + letter);
            view.disableLetterButton(letter);
            long guessTimeLong = System.currentTimeMillis() - roundStartTime;
            int guessTime = guessTimeLong > Integer.MAX_VALUE || guessTimeLong < Integer.MIN_VALUE
                    ? Integer.MAX_VALUE
                    : (int) guessTimeLong;
            List<Integer> hits = model.guessLetter(gameToken, playerId, sessionToken, letter, guessTime);

            if (blankLabels.isEmpty()) {
                System.err.println("[ERROR] blankLabels is empty, cannot process guess");
                return;
            }

            if (hits.isEmpty()) {
                remainingLives--;
                wrongCount++;
                view.updateLifeCount(remainingLives);
                view.showWrongLetter(letter);
                if (remainingLives <= 0) {
                    view.disableAlphabetButtons();
                    isRoundActive = false;
                }
            } else {
                boolean updated = false;
                for (Integer idx : hits) {
                    if (idx >= 0 && idx < blankLabels.size()) {
                        blankLabels.set(idx, String.valueOf(letter));
                        updated = true;
                    } else {
                        System.err.println("[ERROR] Invalid index " + idx + " for blankLabels size " + blankLabels.size());
                    }
                }
                if (updated) {
                    view.updateBlanks(blankLabels);
                    view.showCorrectLetter(letter);
                    boolean won = blankLabels.stream().noneMatch(l -> "_".equals(l));
                    if (won) {
                        view.disableAlphabetButtons();
                        isRoundActive = false;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error processing guess for letter " + letter + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onRoundTimeExpired() {
        isRoundActive = false;
        view.disableAlphabetButtons();
    }

    public void showRoundEnd(String winnerName, String secretWord) {
        if (endPopupShowing) return;
        endPopupShowing = true;
        isRoundActive = false;
        view.disableAlphabetButtons();
        view.showRoundEndPopup(winnerName, secretWord, true);
    }

    public void showGameEnd(String champion) {
        isRoundActive = false;
        view.disableAlphabetButtons();
        view.showGameEndPopup(champion);
        try {
            ViewNavigator.goToLobby();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to navigate to lobby: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onEndPopupClosed() {
        endPopupShowing = false;
        isRoundInitialized = false;
    }

    public void onRoundTimeExpiredFromView() {
        onRoundTimeExpired();
    }
}