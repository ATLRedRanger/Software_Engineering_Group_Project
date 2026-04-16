package com.playfair.backend;

public class ChallengeRecord {
    private final int id;
    private final String title;
    private final String ciphertext;
    private final String hint1;
    private final String hint2;
    private final String difficulty;
    private final String omittedLetter;
    private final String answerKey;
    private final int gridId;
    private final boolean active;

    public ChallengeRecord(
            int id,
            String title,
            String ciphertext,
            String hint1,
            String hint2,
            String difficulty,
            String omittedLetter,
            String answerKey,
            int gridId,
            boolean active
    ) {
        this.id = id;
        this.title = title;
        this.ciphertext = ciphertext;
        this.hint1 = hint1;
        this.hint2 = hint2;
        this.difficulty = difficulty;
        this.omittedLetter = omittedLetter;
        this.answerKey = answerKey;
        this.gridId = gridId;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public String getHint1() {
        return hint1;
    }

    public String getHint2() {
        return hint2;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getOmittedLetter() {
        return omittedLetter;
    }

    public String getAnswerKey() {
        return answerKey;
    }

    public int getGridId() {
        return gridId;
    }

    public boolean isActive() {
        return active;
    }
}