package com.playfair.ui;

import com.playfair.backend.ChallengeRecord;
import com.playfair.backend.ChallengeRepository;
import com.playfair.backend.PlayfairDecrypt;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChallengeMode {

    private VBox challengeView;
    private GridPane gridPane;
    private Label[][] gridLabels;
    private Label encodedMessageLabel;
    private Label hintLabel;
    private TextField keyInput;
    private Button submitKeyBtn;
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

    // Database + session state
    private final ChallengeRepository challengeRepository = new ChallengeRepository();
    private String selectedDifficulty = "easy";
    private int currentScore = 0;
    private int currentStreak = 0;
    private int lastChallengeId = -1;
    private int hintsUsedThisRound = 0;
    private ChallengeRecord currentChallenge;

    public ChallengeMode() {
        createChallengeView();
        chooseDifficulty();
        loadRandomPuzzle();
        updatePhase(1);
    }

    private void chooseDifficulty() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("easy", "easy", "medium", "hard");
        dialog.setTitle("Choose Difficulty");
        dialog.setHeaderText("Select challenge difficulty");
        dialog.setContentText("Difficulty:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(choice -> selectedDifficulty = choice);
    }

    private void loadRandomPuzzle() {
        try {
            currentChallenge = challengeRepository.getRandomChallengeByDifficulty(selectedDifficulty, lastChallengeId);

            if (currentChallenge == null) {
                showAlert("No Challenge Found", "No active challenge found for difficulty: " + selectedDifficulty);
                return;
            }

            lastChallengeId = currentChallenge.getId();
            currentEncodedMessage = currentChallenge.getCiphertext();
            correctKey = currentChallenge.getAnswerKey();
            hint1 = currentChallenge.getHint1();
            hint2 = currentChallenge.getHint2();

            // For now using answer_key as final answer too
            decodedAnswer = currentChallenge.getAnswerKey();

            String cleanKeyStr = correctKey.toUpperCase().replaceAll("\\s+", "");
            List<Character> cleanKey = new ArrayList<>();
            for (char ch : cleanKeyStr.toCharArray()) {
                if (!cleanKey.contains(ch)) {
                    cleanKey.add(ch);
                }
            }

            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, currentChallenge.getOmittedLetter());
            correctGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            encodedMessageLabel.setText("🔐 Encoded: " + currentEncodedMessage);
            hintLabel.setText("📌 Hint 1: " + hint1);

            wrongAttempts = 0;
            gridWrongAttempts = 0;
            hint2Shown = false;
            hintsUsedThisRound = 0;
            decodedTextArea.clear();

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    highlightedCells[r][c] = false;
                    resetGridCellStyle(r, c);
                    gridLabels[r][c].getStyleClass().removeAll("highlighted", "cell-correct", "cell-wrong");
                }
            }

            if (skipToDecodeBtn != null) {
                skipToDecodeBtn.setVisible(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load puzzle from database.");
        }
    }

    private int calculateScore() {
        int baseScore = 100;
        int hintPenalty = 25;
        return Math.max(0, baseScore - (hintsUsedThisRound * hintPenalty));
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
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                resetGridCellStyle(r, c);
                gridLabels[r][c].getStyleClass().removeAll("highlighted", "cell-correct", "cell-wrong");
            }
        }
    }

    private void createChallengeView() {
        challengeView = new VBox(15);
        challengeView.setPadding(new Insets(15));
        challengeView.setAlignment(Pos.TOP_CENTER);
        challengeView.getStyleClass().add("glass-panel");

        Label title = new Label("CHALLENGE MODE");
        title.getStyleClass().add("glass-title");

        VBox howToContent = new VBox(10);
        howToContent.setPadding(new Insets(10));

        Label howToTitle = new Label("How to Play Playfair Challenge:");
        howToTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #E3F4EB; -fx-font-size: 14px;");

        String[] steps = {
                "1. Guess the cipher key using the hints below",
                "2. You have 2 hints available",
                "3. Once key is correct, the grid will appear SCRAMBLED",
                "4. DRAG AND DROP letters to rearrange them correctly",
                "5. Click 'CHECK GRID' when you think it's correct",
                "6. After 3 wrong attempts, 'SKIP TO DECODE' button appears",
                "7. Pairs will light up in TEAL",
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

        instructionLabel = new Label("1. Guess the cipher key using the hints below");
        instructionLabel.getStyleClass().add("glass-subtitle");
        instructionLabel.setMaxWidth(Double.MAX_VALUE);
        instructionLabel.setAlignment(Pos.CENTER);

        encodedMessageLabel = new Label();
        encodedMessageLabel.getStyleClass().add("glass-subtitle");
        encodedMessageLabel.setMaxWidth(Double.MAX_VALUE);
        encodedMessageLabel.setAlignment(Pos.CENTER);

        VBox gridContainer = new VBox(10);
        gridContainer.setAlignment(Pos.CENTER);

        gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setAlignment(Pos.CENTER);
        gridLabels = new Label[5][5];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Label label = new Label("?");
                label.setMinSize(70, 70);
                label.getStyleClass().add("glass-cell");

                int r = row;
                int c = col;

                label.setOnMousePressed(e -> handleDragStart(e, label, r, c));
                label.setOnMouseDragged(e -> handleDrag(e, label));
                label.setOnMouseReleased(e -> handleDragEnd(e, label, r, c));

                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        Label dragInstruction = new Label("✏️ Drag and drop letters to rearrange them");
        dragInstruction.setStyle("-fx-text-fill: #159398; -fx-padding: 8; -fx-font-weight: bold;");
        dragInstruction.setVisible(false);

        gridContainer.getChildren().addAll(gridPane, dragInstruction);

        hintLabel = new Label();
        hintLabel.getStyleClass().add("glass-hint");
        hintLabel.setMaxWidth(Double.MAX_VALUE);
        hintLabel.setAlignment(Pos.CENTER);
        hintLabel.setWrapText(true);

        HBox keyBox = new HBox(10);
        keyBox.setAlignment(Pos.CENTER);

        keyInput = new TextField();
        keyInput.setPromptText("Enter key guess");
        keyInput.setPrefWidth(250);
        keyInput.getStyleClass().add("glass-input");

        submitKeyBtn = new Button("SUBMIT KEY");
        submitKeyBtn.getStyleClass().addAll("glass-action-button");

        keyBox.getChildren().addAll(keyInput, submitKeyBtn);

        hint2Btn = new Button("🔍 GET HINT 2");
        hint2Btn.getStyleClass().addAll("glass-action-button");
        hint2Btn.setVisible(false);

        checkGridBtn = new Button("✅ CHECK GRID");
        checkGridBtn.getStyleClass().addAll("glass-action-button");
        checkGridBtn.setVisible(false);

        skipToDecodeBtn = new Button("⏩ SKIP TO DECODE");
        skipToDecodeBtn.getStyleClass().addAll("glass-action-button");
        skipToDecodeBtn.setVisible(false);
        skipToDecodeBtn.setOnAction(e -> {
            clearAllTempStyles();
            showAlert("Skipping", "Moving to decode phase...");
            updatePhase(4);
        });

        Label decodedLabel = new Label("Decoded Message:");
        decodedLabel.getStyleClass().add("glass-subtitle");
        decodedLabel.setVisible(false);
        decodedLabel.setTranslateY(-40);

        decodedTextArea = new TextArea();
        decodedTextArea.setPromptText("Enter the decoded message here (spaces optional)");
        decodedTextArea.setPrefRowCount(3);
        decodedTextArea.setWrapText(true);
        decodedTextArea.getStyleClass().add("glass-text-area");
        decodedTextArea.setVisible(false);
        decodedTextArea.setMaxWidth(500);
        decodedTextArea.setTranslateY(-200);

        submitDecodedBtn = new Button("🎯 SUBMIT DECODED");
        submitDecodedBtn.getStyleClass().addAll("glass-action-button");
        submitDecodedBtn.setVisible(false);
        submitDecodedBtn.setTranslateY(-150);

        showAnswerBtn = new Button("💡 SHOW ANSWER");
        showAnswerBtn.getStyleClass().addAll("glass-action-button");
        showAnswerBtn.setVisible(false);
        showAnswerBtn.setTranslateY(-150);

        nextPuzzleBtn = new Button("NEXT PUZZLE");
        nextPuzzleBtn.getStyleClass().addAll("glass-action-button");
        nextPuzzleBtn.setVisible(false);
        nextPuzzleBtn.setTranslateY(-300);

        challengeView.getChildren().addAll(
                title, howToPlayPane, instructionLabel, encodedMessageLabel, gridContainer,
                hintLabel, keyBox, hint2Btn, checkGridBtn, skipToDecodeBtn,
                decodedLabel, decodedTextArea, submitDecodedBtn, showAnswerBtn, nextPuzzleBtn
        );

        submitKeyBtn.setOnAction(e -> checkKey());
        hint2Btn.setOnAction(e -> showHint2());
        checkGridBtn.setOnAction(e -> checkGrid());
        submitDecodedBtn.setOnAction(e -> checkDecoded());
        nextPuzzleBtn.setOnAction(e -> nextPuzzle());
        showAnswerBtn.setOnAction(e -> showAnswer());
    }

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

        if (isHighlighted(row, col)) {
            label.getStyleClass().add("highlighted");
        }
    }

    private boolean isHighlighted(int row, int col) {
        return highlightedCells[row][col];
    }

    private void checkKey() {
        String guess = keyInput.getText().trim().toUpperCase().replaceAll("\\s+", "");
        String expected = correctKey.toUpperCase().replaceAll("\\s+", "");

        if (guess.equals(expected)) {
            currentPhase = 3;
            updatePhase(3);
        } else {
            if (!hint2Shown) hint2Btn.setVisible(true);
            showAlert("Incorrect Key", "Try again or click HINT 2 for another clue!");
        }
    }

    private void showHint2() {
        hintLabel.setText("📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2);
        hint2Btn.setVisible(false);
        hint2Shown = true;
        hintsUsedThisRound = 1;
        showAlert("Hint 2 Revealed", "Use this hint to guess the key!");
    }

    private void updatePhase(int phase) {
        currentPhase = phase;
        Label dragInstruction = (Label) ((VBox) gridPane.getParent()).getChildren().get(1);

        switch (phase) {
            case 1:
                instructionLabel.setText("1. Guess the cipher key using the hints below");
                hintLabel.setText("📌 Hint 1: " + hint1);
                keyInput.clear();
                keyInput.setDisable(false);
                submitKeyBtn.setDisable(false);
                hint2Btn.setVisible(false);
                checkGridBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                decodedTextArea.setVisible(false);
                submitDecodedBtn.setVisible(false);
                showAnswerBtn.setVisible(false);
                nextPuzzleBtn.setVisible(false);
                dragInstruction.setVisible(false);
                hint2Shown = false;
                clearAllTempStyles();

                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 5; c++) {
                        gridLabels[r][c].setText("?");
                    }
                }
                break;

            case 3:
                instructionLabel.setText("2. Drag and drop letters to arrange the grid correctly");
                hintLabel.setText("✅ Key accepted! Now arrange the grid correctly.");
                keyInput.setDisable(true);
                submitKeyBtn.setDisable(true);
                checkGridBtn.setVisible(true);
                dragInstruction.setVisible(true);
                skipToDecodeBtn.setVisible(false);
                gridWrongAttempts = 0;
                clearAllTempStyles();

                List<String> letters = new ArrayList<>();
                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 5; c++) {
                        letters.add(String.valueOf(correctGrid[r][c]));
                    }
                }

                Collections.shuffle(letters);

                int idx = 0;
                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 5; c++) {
                        gridLabels[r][c].setText(letters.get(idx++));
                    }
                }
                break;

            case 4:
                instructionLabel.setText("3. Type the decoded message below using the highlighted pairs");
                hintLabel.setText("");
                checkGridBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                decodedTextArea.setVisible(true);
                submitDecodedBtn.setVisible(true);
                showAnswerBtn.setVisible(true);
                dragInstruction.setVisible(false);
                decodedTextArea.clear();
                highlightPairs();
                break;

            case 5:
                instructionLabel.setText("🎉 Challenge Complete! Great job!");
                decodedTextArea.setVisible(false);
                submitDecodedBtn.setVisible(false);
                showAnswerBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                nextPuzzleBtn.setVisible(true);
                clearAllTempStyles();

                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 5; c++) {
                        highlightedCells[r][c] = false;
                        gridLabels[r][c].getStyleClass().removeAll("highlighted");
                    }
                }

                showAlert("Success!", "You solved the puzzle! Click NEXT PUZZLE to continue.");
                break;
        }
    }

    private void checkGrid() {
        boolean correct = true;
        clearAllTempStyles();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                String text = gridLabels[r][c].getText();
                String expected = String.valueOf(correctGrid[r][c]);

                if (!text.equals(expected)) {
                    correct = false;
                    gridLabels[r][c].setStyle(
                            "-fx-background-color: rgba(231, 76, 60, 0.3); " +
                            "-fx-border-color: #E74C3C; " +
                            "-fx-border-width: 2; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold;"
                    );
                } else {
                    gridLabels[r][c].setStyle(
                            "-fx-background-color: rgba(46, 204, 113, 0.3); " +
                            "-fx-border-color: #27AE60; " +
                            "-fx-border-width: 2; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold;"
                    );
                }
            }
        }

        if (correct) {
            showAlert("Grid Correct!", "Moving to next phase...");
            updatePhase(4);
        } else {
            gridWrongAttempts++;
            int remaining = 3 - gridWrongAttempts;

            if (gridWrongAttempts >= 3) {
                skipToDecodeBtn.setVisible(true);
                showAlert("Skip Available", "You've used 3 attempts. Click 'SKIP TO DECODE' to move to the decoding phase.");
            } else {
                showAlert("Incorrect Grid", "Some letters are wrong. You have " + remaining + " attempt(s) left before you can skip.");
            }
        }
    }

    private void highlightPairs() {
        String[] pairs = currentEncodedMessage.split(" ");

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
                resetGridCellStyle(r, c);
            }
        }

        for (String pair : pairs) {
            if (pair.length() == 2) {
                int[] p1 = findLetter(pair.charAt(0));
                int[] p2 = findLetter(pair.charAt(1));

                if (p1 != null && p2 != null) {
                    highlightedCells[p1[0]][p1[1]] = true;
                    highlightedCells[p2[0]][p2[1]] = true;
                    gridLabels[p1[0]][p1[1]].getStyleClass().add("highlighted");
                    gridLabels[p2[0]][p2[1]].getStyleClass().add("highlighted");
                }
            }
        }
    }

    private int[] findLetter(char letter) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (correctGrid[r][c] == letter) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private void checkDecoded() {
        String user = decodedTextArea.getText().trim().toUpperCase().replaceAll("\\s+", "");
        String expected = decodedAnswer.toUpperCase().replaceAll("\\s+", "");

        if (user.equals(expected)) {
            int earned = calculateScore();
            currentScore += earned;
            currentStreak++;

            showAlert(
                    "Correct!",
                    "You solved it.\n\n" +
                    "Difficulty: " + selectedDifficulty +
                    "\nPoints earned: " + earned +
                    "\nTotal score: " + currentScore +
                    "\nCurrent streak: " + currentStreak
            );

            updatePhase(5);
        } else {
            wrongAttempts++;
            if (wrongAttempts >= 3) {
                currentStreak = 0;
                showAnswerBtn.setVisible(true);
                showAlert(
                        "Incorrect",
                        "You've used all attempts.\n" +
                        "Your streak reset to 0.\n" +
                        "Click SHOW ANSWER to see the correct answer."
                );
            } else {
                showAlert("Incorrect", "Wrong. You have " + (3 - wrongAttempts) + " attempt(s) left.");
            }
        }
    }

    private void showAnswer() {
        showAlert(
                "Answer",
                "The correct answer is: " + decodedAnswer +
                "\n\nSession score: " + currentScore +
                "\nCurrent streak: " + currentStreak +
                "\nDifficulty: " + selectedDifficulty
        );
    }

    private void nextPuzzle() {
        clearAllTempStyles();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
            }
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Next Puzzle");
        alert.setHeaderText("Choose next action");
        alert.setContentText("Do you want to keep the same difficulty?");

        ButtonType keepSame = new ButtonType("Keep Same");
        ButtonType changeDifficulty = new ButtonType("Change Difficulty");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(keepSame, changeDifficulty, cancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == changeDifficulty) {
                chooseDifficulty();
            } else if (result.get() == cancel) {
                return;
            }
        }

        wrongAttempts = 0;
        gridWrongAttempts = 0;
        hint2Shown = false;
        hintsUsedThisRound = 0;

        loadRandomPuzzle();
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