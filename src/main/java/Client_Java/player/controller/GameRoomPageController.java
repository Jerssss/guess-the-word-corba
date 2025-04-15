package Client_Java.player.controller;

import Client_Java.player.model.GameRoomPageModel;
import Client_Java.player.view.GameRoomPageView;
import java.util.List;

public class GameRoomPageController {
    private final GameRoomPageView view;
    private final GameRoomPageModel model;
    private final int playerID;
    private final String sessionToken;

    public GameRoomPageController(GameRoomPageView view, GameRoomPageModel model,
                                  int playerID, String sessionToken) {
        this.view = view;
        this.model = model;
        this.playerID = playerID;
        this.sessionToken = sessionToken;
        initialize();
    }

    private void initialize() {
        // Initialize letter buttons and other UI elements through view methods
    }

    private void handleLetterGuess(char letter) {
        String result = model.guessLetter(playerID, sessionToken, letter);
        if (result.startsWith("Correct")) {
            updateBlanks(letter);
            if (isWordComplete()) {
                model.submitWord(playerID, sessionToken, model.getCurrentWord());
            }
        } else {
            model.decrementAttempts();
        }
    }

    private void updateBlanks(char letter) {
        List<Integer> positions = model.getLetterPositions(letter);
        view.updateBlanks(model.getCurrentWord(), model.getAllLetterPositions());
    }

    private boolean isWordComplete() {
        int wordLength = model.getCurrentWord().length();
        for (int i = 0; i < wordLength; i++) {
            if (view.isBlankEmpty(i)) {
                return false;
            }
        }
        return true;
    }
}