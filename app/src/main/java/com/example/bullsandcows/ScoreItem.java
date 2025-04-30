package com.example.bullsandcows;

public class ScoreItem {
    private String username;
    private int score;
    private long timestamp;

    public ScoreItem(String username, int score, long timestamp) {
        this.username = username;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
