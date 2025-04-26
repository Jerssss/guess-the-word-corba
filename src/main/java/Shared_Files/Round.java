package Shared_Files;

public class Round {
    private String word;
    private long startTimeMillis;
    private long durationMillis;
    private boolean isRoundOver;

    public Round(String word, long durationMillis) {
        this.word = word;
        this.durationMillis = durationMillis;
        this.startTimeMillis = System.currentTimeMillis();
        this.isRoundOver = false;
    }

    public String getWord() {
        return word;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public boolean isRoundOver() {
        return isRoundOver || (System.currentTimeMillis() - startTimeMillis) > durationMillis;
    }

    public void endRound() {
        this.isRoundOver = true;
    }
}
