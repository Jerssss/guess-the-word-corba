package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.implementation.GameCallbackServiceImpl;
import GameIDL.NotLoggedInException;
import PlayerCallBackIDL.GameCallBackService;
import PlayerCallBackIDL.GameCallBackServiceHelper;
import PlayerCallBackIDL.GameCallBackServicePOA;
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
    private final int totalRounds;

    private int roundNumber = 0;
    private String secretWord = "";
    private int remainingLives;
    private int wrongCount = 0;
    private final List<String> blankLabels = new ArrayList<>(); // Track blank label states as strings
    private boolean endPopupShowing = false;
    private final Map<String, Integer> winCounts = new HashMap<>();

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
        this.totalRounds = model.getTotalRounds(sessionToken);

        // Register controller with view
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
            view.scheduleRetryRoundStart(newRound, 1); // Delegate retry to view
            return;
        }
        if (newRound <= roundNumber) return;

        roundNumber = newRound;
        view.showRoundStartScene(newRound);
    }

    public void handleServerRoundStart(int roundNum) {
        System.out.println("[DEBUG] handleServerRoundStart for round " + roundNum);
        secretWord = model.getRandomWord(gameToken, roundNum, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);
        wrongCount = 0;
        view.updateLifeCount(remainingLives);
        view.updateRoundLabel(roundNum);
        view.resetAlphabetButtons();
        view.enableAlphabetButtons();
        setupBlanks();
        view.startCountdown(model.getRoundDuration(sessionToken));
    }

    private void setupBlanks() {
        blankLabels.clear();
        for (int i = 0; i < secretWord.length(); i++) {
            blankLabels.add("_");
        }
        view.setupBlanks(secretWord.length());
    }

    public void handleGuess(char letter) {
        try {
            view.disableLetterButton(letter);
            List<Integer> hits = model.guessLetter(gameToken, playerId, sessionToken, letter);

            if (hits.isEmpty()) {
                remainingLives--;
                wrongCount++;
                view.updateLifeCount(remainingLives);
                view.showWrongLetter(letter);
                if (remainingLives <= 0) {
                    view.disableAlphabetButtons();
                }
            } else {
                hits.forEach(idx -> blankLabels.set(idx, String.valueOf(letter)));
                view.updateBlanks(blankLabels);
                view.showCorrectLetter(letter);
                boolean won = blankLabels.stream().noneMatch(l -> "_".equals(l));
                if (won) {
                    view.disableAlphabetButtons();
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error processing guess for letter " + letter + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onRoundTimeExpired() {
        view.disableAlphabetButtons();
    }

    public void showRoundEnd(String winnerName, String secretWord) {
        if (endPopupShowing) return;
        endPopupShowing = true;
        view.showRoundEndPopup(winnerName, secretWord, roundNumber < totalRounds);
    }

    public void showGameEnd(String champion) {
        view.showGameEndPopup(champion);
    }

    public void onEndPopupClosed() {
        endPopupShowing = false;
    }

    public void onRoundTimeExpiredFromView() {
        onRoundTimeExpired();
    }
}