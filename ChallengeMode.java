package com.playfair.ui;

import com.playfair.backend.ChallengeRecord;
import com.playfair.backend.ChallengeRepository;
import com.playfair.backend.PlayfairDecrypt;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import java.util.*;

public class ChallengeMode {

    private VBox challengeView;
    private GridPane gridPane;
    private Label[][] gridLabels;
    private Label encodedMessageLabel;
    private Label hintLabel;
    private Label cipherKeyLabel;
    private Label decodedLabel;
    private Button startBtn;
    private Button hint2Btn;
    private Button checkGridBtn;
    private Button submitDecodedBtn;
    private Button nextPuzzleBtn;
    private Button showAnswerBtn;
    private Button skipToDecodeBtn;
    private TextArea decodedTextArea;
    private Label instructionLabel;
    private TitledPane howToPlayPane;

    private String currentEncodedMessage;
    private String correctKey;
    private String hint1;
    private String hint2;
    private String decodedAnswer;
    private char[][] correctGrid;
    private int currentPhase = 1;
    private boolean hint2Shown = false;
    private int wrongAttempts = 0;
    private int gridWrongAttempts = 0;

    private Label draggedLabel = null;
    private double dragOffsetX, dragOffsetY;
    private int sourceRow = -1, sourceCol = -1;
    private boolean[][] highlightedCells = new boolean[5][5];

    private int lastChallengeId = -1;
    private String currentDifficulty = "easy";

    private final ChallengeRepository repo = new ChallengeRepository();

    // Neon/Bright colors for grid highlighting,highly visible on dark background
    private final String[] pairColors = {
            "#FFFFFF",  // white
            "#FF00FF",  // neon pink/magenta
            "#00FFFF",  // cyan
            "#FFFF00",  // neon yellow
            "#FF4500",  // neon orange
            "#00FF00",  // neon green
            "#9B59B6",  // bright purple
            "#FF1493",  // deep pink
            "#00CED1",  // dark turquoise
            "#FFA500",  // orange
            "#7B68EE",  // medium purple
            "#32CD32"   // lime green
    };

    public ChallengeMode() {
        createChallengeView();
        loadRandomPuzzle();
        updatePhase(1);
    }

    public void setDifficulty(String difficulty) {
        this.currentDifficulty = difficulty;
    }

    private void loadRandomPuzzle() {
        try {
            ChallengeRecord record = repo.getRandomChallengeByDifficulty(currentDifficulty, lastChallengeId);

            if (record == null) {
                showAlert("No Challenges", "No challenges found for difficulty: " + currentDifficulty);
                return;
            }

            lastChallengeId = record.getId();
            currentEncodedMessage = record.getCiphertext();
            hint1 = record.getHint1();
            hint2 = record.getHint2();
            decodedAnswer = record.getAnswerKey();

            // Fetch a grid key
            String gridKey = repo.getRandomGridKey();
            if (gridKey == null) {
                showAlert("Error", "No grid keys found in database.");
                return;
            }
            correctKey = gridKey;

            // Build the correct grid
            String omitted = record.getOmittedLetter() != null ? record.getOmittedLetter() : "J";
            String cleanKeyStr = correctKey.toUpperCase().replaceAll("\\s+", "");
            List<Character> cleanKey = new ArrayList<>();
            for (char ch : cleanKeyStr.toCharArray()) {
                if (!cleanKey.contains(ch)) cleanKey.add(ch);
            }
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, omitted);
            correctGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            // Update UI labels
            encodedMessageLabel.setText("🔐 Encoded: " + currentEncodedMessage);
            cipherKeyLabel.setText("🔑 Cipher Key: " + correctKey);
            hintLabel.setText("📌 Hint 1: " + hint1);

            // Reset state
            wrongAttempts = 0;
            gridWrongAttempts = 0;
            hint2Shown = false;
            decodedTextArea.clear();

            for (int r = 0; r < 5; r++)
                for (int c = 0; c < 5; c++) {
                    highlightedCells[r][c] = false;
                    resetGridCellStyle(r, c);
                    gridLabels[r][c].getStyleClass().removeAll("highlighted", "cell-correct", "cell-wrong");
                }

            if (skipToDecodeBtn != null) skipToDecodeBtn.setVisible(false);

        } catch (Exception e) {
            showAlert("Database Error", "Failed to load puzzle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetGridCellStyle(int row, int col) {
        gridLabels[row][col].setStyle(
                "-fx-border-color: rgba(21, 147, 152, 0.4); " +
                        "-fx-border-width: 2; " +
                        "-fx-background-color: rgba(2, 31, 44, 0.7); " +
                        "-fx-text-fill: white; " +
                        "-fx-alignment: center; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-radius: 16;"
        );
    }

    private void clearAllTempStyles() {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++) {
                resetGridCellStyle(r, c);
                gridLabels[r][c].getStyleClass().removeAll("highlighted", "cell-correct", "cell-wrong");
            }
    }

    private void createChallengeView() {
        challengeView = new VBox(8);
        challengeView.setPadding(new Insets(10, 15, 15, 15));
        challengeView.setAlignment(Pos.TOP_CENTER);
        challengeView.getStyleClass().add("glass-panel");

        Label title = new Label("CHALLENGE MODE");
        title.getStyleClass().add("glass-title");

        // How to play section
        VBox howToContent = new VBox(10);
        howToContent.setPadding(new Insets(10));
        Label howToTitle = new Label("How to Play Playfair Challenge:");
        howToTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #E3F4EB; -fx-font-size: 14px;");
        String[] steps = {
                "1. You are given the cipher key and an encoded message",
                "2. Use the hints to figure out what the decoded message is",
                "3. Click START to see the scrambled grid",
                "4. DRAG AND DROP letters to arrange the grid correctly",
                "5. Click 'CHECK GRID' when you think it's correct",
                "6. After 3 wrong attempts, 'SKIP TO DECODE' button appears",
                "7. Pairs will light up in DIFFERENT NEON COLORS to help you decode",
                "8. Type the decoded message and submit to complete!"
        };
        VBox stepsBox = new VBox(5);
        for (String step : steps) {
            Label stepLabel = new Label("  • " + step);
            stepLabel.setWrapText(true);
            stepLabel.setStyle("-fx-text-fill: #BDC7D0;");
            stepsBox.getChildren().add(stepLabel);
        }
        howToContent.getChildren().addAll(howToTitle, stepsBox);
        howToPlayPane = new TitledPane("📖 How to Play", howToContent);
        howToPlayPane.setExpanded(false);
        howToPlayPane.getStyleClass().add("glass-panel");
        howToPlayPane.setMaxHeight(Control.USE_PREF_SIZE);

        instructionLabel = new Label("Use the key and hints to decode the message!");
        instructionLabel.getStyleClass().add("glass-subtitle");
        instructionLabel.setMaxWidth(Double.MAX_VALUE);
        instructionLabel.setAlignment(Pos.CENTER);
        instructionLabel.setPadding(new Insets(5, 0, 5, 0));

        // Encoded message label 
        encodedMessageLabel = new Label();
        encodedMessageLabel.getStyleClass().add("glass-subtitle");
        encodedMessageLabel.setMaxWidth(Double.MAX_VALUE);
        encodedMessageLabel.setAlignment(Pos.CENTER);
        encodedMessageLabel.setPadding(new Insets(5, 0, 5, 0));

        // Cipher key display label
        cipherKeyLabel = new Label();
        cipherKeyLabel.getStyleClass().add("glass-subtitle");
        cipherKeyLabel.setMaxWidth(Double.MAX_VALUE);
        cipherKeyLabel.setAlignment(Pos.CENTER);
        cipherKeyLabel.setStyle("-fx-text-fill: #159398; -fx-font-weight: bold; -fx-font-size: 16px;");
        cipherKeyLabel.setPadding(new Insets(5, 0, 5, 0));

        // Grid container
        VBox gridContainer = new VBox(8);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.setPadding(new Insets(5, 0, 5, 0));

        gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setAlignment(Pos.CENTER);
        gridLabels = new Label[5][5];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Label label = new Label("?");
                label.setMinSize(65, 65);
                label.getStyleClass().add("glass-cell");
                int r = row, c = col;
                label.setOnMousePressed(e -> handleDragStart(e, label, r, c));
                label.setOnMouseDragged(e -> handleDrag(e, label));
                label.setOnMouseReleased(e -> handleDragEnd(e, label, r, c));
                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        Label dragInstruction = new Label("✏️ Drag and drop letters to rearrange them");
        dragInstruction.setStyle("-fx-text-fill: #159398; -fx-padding: 5; -fx-font-size: 12px; -fx-font-weight: bold;");
        dragInstruction.setVisible(false);
        gridContainer.getChildren().addAll(gridPane, dragInstruction);

        hintLabel = new Label();
        hintLabel.getStyleClass().add("glass-hint");
        hintLabel.setMaxWidth(Double.MAX_VALUE);
        hintLabel.setAlignment(Pos.CENTER);
        hintLabel.setWrapText(true);
        hintLabel.setPadding(new Insets(5, 0, 5, 0));

        // START button
        startBtn = new Button("▶ START");
        startBtn.getStyleClass().addAll("glass-action-button");
        startBtn.setPadding(new Insets(8, 20, 8, 20));
        startBtn.setOnAction(e -> updatePhase(3));

        hint2Btn = new Button("🔍 GET HINT 2");
        hint2Btn.getStyleClass().addAll("glass-action-button");
        hint2Btn.setPadding(new Insets(8, 20, 8, 20));
        hint2Btn.setVisible(true);

        checkGridBtn = new Button("✅ CHECK GRID");
        checkGridBtn.getStyleClass().addAll("glass-action-button");
        checkGridBtn.setPadding(new Insets(8, 20, 8, 20));
        checkGridBtn.setVisible(false);

        skipToDecodeBtn = new Button("⏩ SKIP TO DECODE");
        skipToDecodeBtn.getStyleClass().addAll("glass-action-button");
        skipToDecodeBtn.setPadding(new Insets(8, 20, 8, 20));
        skipToDecodeBtn.setVisible(false);
        skipToDecodeBtn.setOnAction(e -> {
            clearAllTempStyles();
            showAlert("Skipping", "Moving to decode phase...");
            updatePhase(4);
        });

        // Decoded message section
        decodedLabel = new Label("Decoded Message:");
        decodedLabel.getStyleClass().add("glass-subtitle");
        decodedLabel.setPadding(new Insets(5, 0, 5, 0));
        decodedLabel.setVisible(false);
        decodedLabel.setTranslateY(-50);


        decodedTextArea = new TextArea();
        decodedTextArea.setPromptText("Type the decoded message here (spaces optional)");
        decodedTextArea.setPrefRowCount(2);
        decodedTextArea.setWrapText(true);
        decodedTextArea.getStyleClass().add("glass-text-area");
        decodedTextArea.setVisible(false);
        decodedTextArea.setMaxWidth(400);
        decodedTextArea.setPrefHeight(60);
        decodedTextArea.setTranslateY(-50);


        submitDecodedBtn = new Button("🎯 SUBMIT DECODED");
        submitDecodedBtn.getStyleClass().addAll("glass-action-button");
        submitDecodedBtn.setPadding(new Insets(8, 20, 8, 20));
        submitDecodedBtn.setVisible(false);
        submitDecodedBtn.setTranslateX(80);
        submitDecodedBtn.setTranslateY(-20);

        showAnswerBtn = new Button("💡 SHOW ANSWER");
        showAnswerBtn.getStyleClass().addAll("glass-action-button");
        showAnswerBtn.setPadding(new Insets(8, 20, 8, 20));
        showAnswerBtn.setVisible(false);
        showAnswerBtn.setTranslateX(80);
        showAnswerBtn.setTranslateY(-20);

        nextPuzzleBtn = new Button("NEXT PUZZLE ▶");
        nextPuzzleBtn.getStyleClass().addAll("glass-action-button");
        nextPuzzleBtn.setPadding(new Insets(8, 20, 8, 20));
        nextPuzzleBtn.setVisible(false);
        nextPuzzleBtn.setTranslateY(-150);

        // Button containers
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10, 0, 5, 0));
        buttonContainer.getChildren().addAll(startBtn, hint2Btn, checkGridBtn, skipToDecodeBtn);

        HBox decodeButtonContainer = new HBox(10);
        decodeButtonContainer.setAlignment(Pos.CENTER);
        decodeButtonContainer.setPadding(new Insets(5, 0, 5, 0));
        decodeButtonContainer.getChildren().addAll(submitDecodedBtn, showAnswerBtn);

        challengeView.getChildren().addAll(
                title, howToPlayPane, instructionLabel,
                encodedMessageLabel, cipherKeyLabel,
                gridContainer, hintLabel,
                buttonContainer,
                decodedLabel, decodedTextArea, decodeButtonContainer, nextPuzzleBtn
        );

        hint2Btn.setOnAction(e -> showHint2());
        checkGridBtn.setOnAction(e -> checkGrid());
        submitDecodedBtn.setOnAction(e -> checkDecoded());
        nextPuzzleBtn.setOnAction(e -> nextPuzzle());
        showAnswerBtn.setOnAction(e -> showAnswer());
    }

    // DRAG AND DROP HANDLERS

    private void handleDragStart(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3 || label.getText().equals("?")) return;
        clearAllTempStyles();
        draggedLabel = label;
        sourceRow = row;
        sourceCol = col;
        dragOffsetX = e.getX();
        dragOffsetY = e.getY();
        label.getStyleClass().add("dragged");
        label.toFront();
        e.consume();
    }

    private void handleDrag(MouseEvent e, Label label) {
        if (currentPhase != 3 || draggedLabel == null) return;
        double newX = e.getSceneX() - dragOffsetX - gridPane.getLocalToSceneTransform().getTx();
        double newY = e.getSceneY() - dragOffsetY - gridPane.getLocalToSceneTransform().getTy();
        label.setTranslateX(newX - label.getLayoutX());
        label.setTranslateY(newY - label.getLayoutY());
        e.consume();
    }

    private void handleDragEnd(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3 || draggedLabel == null) return;
        label.setTranslateX(0);
        label.setTranslateY(0);

        Label target = null;
        int tr = -1, tc = -1;
        double dropX = e.getSceneX(), dropY = e.getSceneY();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Label l = gridLabels[r][c];
                if (l == draggedLabel) continue;
                double cx = l.getLocalToSceneTransform().getTx() + l.getWidth() / 2;
                double cy = l.getLocalToSceneTransform().getTy() + l.getHeight() / 2;
                if (Math.hypot(dropX - cx, dropY - cy) < 50) {
                    target = l;
                    tr = r;
                    tc = c;
                    break;
                }
            }
            if (target != null) break;
        }

        if (target != null && !target.getText().equals("?")) {
            String temp = draggedLabel.getText();
            draggedLabel.setText(target.getText());
            target.setText(temp);
        }

        draggedLabel.getStyleClass().remove("dragged");
        updateLabelStyle(draggedLabel, sourceRow, sourceCol);
        if (target != null) {
            target.getStyleClass().remove("dragged");
            updateLabelStyle(target, tr, tc);
        }

        draggedLabel = null;
        sourceRow = -1;
        sourceCol = -1;
        e.consume();
    }

    private void updateLabelStyle(Label label, int row, int col) {
        label.getStyleClass().removeAll("glass-cell", "highlighted", "dragged");
        label.getStyleClass().add("glass-cell");
        if (isHighlighted(row, col)) label.getStyleClass().add("highlighted");
    }

    private boolean isHighlighted(int row, int col) {
        return highlightedCells[row][col];
    }

    private void showHint2() {
        hintLabel.setText("📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2);
        hint2Btn.setVisible(false);
        hint2Shown = true;
    }

    // PHASE MANAGEMENT
    private void updatePhase(int phase) {
        currentPhase = phase;

        switch (phase) {
            case 1:
                instructionLabel.setText("Use the key and hints to decode the message!");
                hintLabel.setText("📌 Hint 1: " + hint1);
                cipherKeyLabel.setVisible(true);
                startBtn.setVisible(true);
                hint2Btn.setVisible(true);
                hint2Btn.setDisable(false);
                checkGridBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                decodedLabel.setVisible(false);
                decodedTextArea.setVisible(false);
                submitDecodedBtn.setVisible(false);
                showAnswerBtn.setVisible(false);
                nextPuzzleBtn.setVisible(false);
                break;

            case 3:
                instructionLabel.setText("Arrange the grid correctly using the key: " + correctKey);
                hintLabel.setText(hint2Shown
                        ? "📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2
                        : "📌 Hint 1: " + hint1);
                startBtn.setVisible(false);
                hint2Btn.setVisible(true);
                checkGridBtn.setVisible(true);
                skipToDecodeBtn.setVisible(false);
                decodedLabel.setVisible(false);
                decodedTextArea.setVisible(false);
                submitDecodedBtn.setVisible(false);
                showAnswerBtn.setVisible(false);
                nextPuzzleBtn.setVisible(false);
                gridWrongAttempts = 0;
                clearAllTempStyles();

                List<String> letters = new ArrayList<>();
                for (int r = 0; r < 5; r++)
                    for (int c = 0; c < 5; c++)
                        letters.add(String.valueOf(correctGrid[r][c]));
                Collections.shuffle(letters);
                int idx = 0;
                for (int r = 0; r < 5; r++)
                    for (int c = 0; c < 5; c++)
                        gridLabels[r][c].setText(letters.get(idx++));
                break;

            case 4:
                instructionLabel.setText("Use the highlighted pairs to decode the message!");
                hintLabel.setText(hint2Shown
                        ? "📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2
                        : "📌 Hint 1: " + hint1);
                startBtn.setVisible(false);
                hint2Btn.setVisible(false);
                checkGridBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                decodedLabel.setVisible(true);
                decodedTextArea.setVisible(true);
                submitDecodedBtn.setVisible(true);
                showAnswerBtn.setVisible(false);
                nextPuzzleBtn.setVisible(false);
                decodedTextArea.clear();
                highlightGridPairs();
                break;

            case 5:
                instructionLabel.setText("🎉 Challenge Complete! Great job!");
                startBtn.setVisible(false);
                hint2Btn.setVisible(false);
                checkGridBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                decodedLabel.setVisible(false);
                decodedTextArea.setVisible(false);
                submitDecodedBtn.setVisible(false);
                showAnswerBtn.setVisible(false);
                nextPuzzleBtn.setVisible(true);
                clearAllTempStyles();
                for (int r = 0; r < 5; r++)
                    for (int c = 0; c < 5; c++) {
                        highlightedCells[r][c] = false;
                        gridLabels[r][c].getStyleClass().removeAll("highlighted");
                    }
                showAlert("🎉 Success!", "You decoded the message! Click NEXT PUZZLE to continue.");
                break;
        }
    }

    // PHASE 3: CHECK GRID
    private void checkGrid() {
        clearAllTempStyles();
        boolean correct = true;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                String text = gridLabels[r][c].getText();
                String expected = String.valueOf(correctGrid[r][c]);
                if (!text.equals(expected)) {
                    correct = false;
                    gridLabels[r][c].setStyle(
                            "-fx-background-color: rgba(231, 76, 60, 0.3); " +
                                    "-fx-border-color: #E74C3C; -fx-border-width: 2; " +
                                    "-fx-font-size: 20px; -fx-font-weight: bold;"
                    );
                } else {
                    gridLabels[r][c].setStyle(
                            "-fx-background-color: rgba(46, 204, 113, 0.3); " +
                                    "-fx-border-color: #27AE60; -fx-border-width: 2; " +
                                    "-fx-font-size: 20px; -fx-font-weight: bold;"
                    );
                }
            }
        }

        if (correct) {
            showAlert("Grid Correct!", "Moving to decode phase...");
            updatePhase(4);
        } else {
            gridWrongAttempts++;
            int remaining = 3 - gridWrongAttempts;
            if (gridWrongAttempts >= 3) {
                skipToDecodeBtn.setVisible(true);
                showAlert("Skip Available", "3 attempts used. Click SKIP TO DECODE to move on.");
            } else {
                showAlert("Incorrect Grid", "Some letters are wrong. " + remaining + " attempt(s) left.");
            }
        }
    }

    // PHASE 4: HIGHLIGHT GRID CELLS BASED ON ENCODED MESSAGE PAIRS
    private void highlightGridPairs() {
        String[] pairs = currentEncodedMessage.split(" ");

        // Clear all existing highlights and styles
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
                resetGridCellStyle(r, c);
            }
        }

        // Highlight each pair with a different neon color
        for (int pairIndex = 0; pairIndex < pairs.length; pairIndex++) {
            String pair = pairs[pairIndex];
            if (pair.length() == 2) {
                int[] p1 = findLetter(pair.charAt(0));
                int[] p2 = findLetter(pair.charAt(1));

                if (p1 != null && p2 != null) {
                    highlightedCells[p1[0]][p1[1]] = true;
                    highlightedCells[p2[0]][p2[1]] = true;

                    // Get color for this pair (cycle through colors)
                    String color = pairColors[pairIndex % pairColors.length];
                    String rgbColor = hexToRgb(color);

                    // Apply different neon color for each pair
                    String highlightStyle = "-fx-background-color: rgba(" + rgbColor + ", 0.4); " +
                            "-fx-border-color: " + color + "; " +
                            "-fx-border-width: 3; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold;";

                    gridLabels[p1[0]][p1[1]].setStyle(highlightStyle);
                    gridLabels[p2[0]][p2[1]].setStyle(highlightStyle);

                    // Add CSS class for any additional styling
                    gridLabels[p1[0]][p1[1]].getStyleClass().add("highlighted");
                    gridLabels[p2[0]][p2[1]].getStyleClass().add("highlighted");
                }
            }
        }
    }

    // Helper method to convert hex color to RGB string
    private String hexToRgb(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return r + ", " + g + ", " + b;
    }

    private int[] findLetter(char letter) {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                if (correctGrid[r][c] == letter)
                    return new int[]{r, c};
        return null;
    }

    private void checkDecoded() {
        String user = decodedTextArea.getText().trim().toUpperCase().replaceAll("\\s+", "");
        String expected = decodedAnswer.toUpperCase().replaceAll("\\s+", "");

        if (user.equals(expected)) {
            updatePhase(5);
        } else {
            wrongAttempts++;
            if (wrongAttempts >= 3) {
                showAnswerBtn.setVisible(true);
                showAlert("Incorrect", "3 attempts used. Click SHOW ANSWER to reveal it.");
            } else {
                showAlert("Incorrect", "Wrong! " + (3 - wrongAttempts) + " attempt(s) left.");
            }
        }
    }

    private void showAnswer() {
        showAlert("Answer", "The decoded message is: " + decodedAnswer);
    }

    // PHASE 5: NEXT PUZZLE
    private void nextPuzzle() {
        clearAllTempStyles();
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
            }
        loadRandomPuzzle();
        currentPhase = 1;
        wrongAttempts = 0;
        gridWrongAttempts = 0;
        hint2Shown = false;
        updatePhase(1);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return challengeView;
    }
}
