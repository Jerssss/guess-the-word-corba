package Shared_Files;

import java.util.HashSet;
import java.util.Set;

public class GameSession {
    private String gameToken;
    private Set<Integer> playerIDs;
    private Set<String> usedWords;
    private int currentRound;
    private boolean isGameActive;

    public GameSession(String gameToken) {
        this.gameToken = gameToken;
        this.playerIDs = new HashSet<>();
        this.usedWords = new HashSet<>();
        this.currentRound = 0;
        this.isGameActive = true;
    }

    public String getGameToken() {
        return gameToken;
    }

    public Set<Integer> getPlayerIDs() {
        return playerIDs;
    }

    public void addPlayer(int playerID) {
        this.playerIDs.add(playerID);
    }

    public Set<String> getUsedWords() {
        return usedWords;
    }

    public void addUsedWord(String word) {
        this.usedWords.add(word);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void incrementRound() {
        this.currentRound++;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public void endGame() {
        this.isGameActive = false;
    }
}
