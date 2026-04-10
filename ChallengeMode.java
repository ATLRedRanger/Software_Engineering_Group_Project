package com.playfair.ui;

import com.playfair.backend.PlayfairDecrypt;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.*;
import java.util.*;

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


    // FALLBACK SYSTEM PUZZLES (TEMPORARY PUZZLES)
    //For UI Testing

    private List<SimplePuzzle> puzzles = new ArrayList<>();
    private int puzzleIndex = 0;

    // Temporary puzzle data structure
    private class SimplePuzzle {
        String encoded;
        String key;
        String hint1;
        String hint2;
        String decoded;

        SimplePuzzle(String encoded, String key, String hint1, String hint2, String decoded) {
            this.encoded = encoded;
            this.key = key;
            this.hint1 = hint1;
            this.hint2 = hint2;
            this.decoded = decoded;
        }
    }

    //constructor loads fallback puzzles, builds ui
    public ChallengeMode() {
        loadPuzzles();
        createChallengeView();
        loadRandomPuzzle();
        updatePhase(1);
    }
    
//temporary puzzles
    private void loadPuzzles() {
        puzzles.add(new SimplePuzzle("BM OD ZB", "PLAYFAIR", "Starts with P", "Contains LAY", "HIDETH"));
        puzzles.add(new SimplePuzzle("BM OD ZB XD", "PLAYFAIR", "Starts with P", "Contains LAY", "HIDETHEG"));
        puzzles.add(new SimplePuzzle("YT RS TU XA", "APPLE", "Starts with A", "Ends with E", "HELLOWORLD"));
        puzzles.add(new SimplePuzzle("XQ PL MR YZ", "ZEBRA", "Starts with Z", "Has E and A", "TESTMESX"));
        puzzles.add(new SimplePuzzle("KX JE YU RE BE ZW EH E", "APPLE", "5-letter fruit", "A common red fruit", "APPLE"));
        puzzles.add(new SimplePuzzle("AB CD EF GH IJ", "SECRET", "Think about row shifts", "Classic Playfair rule", "SECRET"));
    }

    private void loadRandomPuzzle() {
        Random rand = new Random();
        SimplePuzzle p = puzzles.get(rand.nextInt(puzzles.size()));

        currentEncodedMessage = p.encoded;
        correctKey = p.key;
        hint1 = p.hint1;
        hint2 = p.hint2;
        decodedAnswer = p.decoded;
        
//generates grid using backend playfairdecrpyt
        String cleanKeyStr = correctKey.toUpperCase().replaceAll("\\s+", "");
        List<Character> cleanKey = new ArrayList<>();
        for (char ch : cleanKeyStr.toCharArray()) {
            if (!cleanKey.contains(ch)) cleanKey.add(ch);
        }

        String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, "J");
        correctGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

        encodedMessageLabel.setText("🔐 Encoded: " + currentEncodedMessage);
        hintLabel.setText("📌 Hint 1: " + hint1);

        //reset state for new puzzle
        wrongAttempts = 0;
        gridWrongAttempts = 0;
        hint2Shown = false;
        decodedTextArea.clear();

        // Clear all grid styles
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
    }

    //grid styling
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

    //UI for challenge mode screen
    // Creates title, How to Play section, instruction label, encoded message
    // Builds 5x5 draggable grid container
    // Adds hint label, key input, buttons (SUBMIT KEY, GET HINT 2, CHECK GRID, SKIP TO DECODE)
    // Adds decoded message section, SUBMIT DECODED, SHOW ANSWER, NEXT PUZZLE buttons

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
                int r = row, c = col;
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

        // Decoded message section 
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
                double cx = l.getLocalToSceneTransform().getTx() + l.getWidth()/2;
                double cy = l.getLocalToSceneTransform().getTy() + l.getHeight()/2;
                if (Math.hypot(dropX - cx, dropY - cy) < 50) {
                    target = l;
                    tr = r; tc = c;
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

    // PHASE 1: GUESS THE KEY
    // Compares user's key guess with correctKey
    // If correct → moves to Phase 3 (Arrange Grid)
    // If wrong → shows HINT 2 button

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
        showAlert("Hint 2 Revealed", "Use this hint to guess the key!");
    }

    // PHASE MANAGEMENT (Switches between 1, 3, 4, 5)
    // Phase 1: Guess key (blank grid, hint 1, key input)
    // Phase 3: Arrange grid (scrambled letters, drag-drop, CHECK GRID button)
    // Phase 4: Decode message (highlighted pairs, decoded text area, SUBMIT DECODED)
    // Phase 5: Complete (success message, NEXT PUZZLE button)

    private void updatePhase(int phase) {
        currentPhase = phase;
        Label dragInstruction = (Label)((VBox)gridPane.getParent()).getChildren().get(1);

        switch(phase) {
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
                for (int r = 0; r < 5; r++)
                    for (int c = 0; c < 5; c++)
                        gridLabels[r][c].setText("?");
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
                instructionLabel.setText("3. Type the decoded message below using the highlighted pairs");
                hintLabel.setText("");
                checkGridBtn.setVisible(false);
                skipToDecodeBtn.setVisible(false);
                decodedTextArea.setVisible(true);
                submitDecodedBtn.setVisible(true);
                showAnswerBtn.setVisible(true);
                dragInstruction.setVisible(false);
                decodedTextArea.clear();

                //  highlight pairs
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

    // PHASE 3: CHECK GRID ARRANGEMENT
    private void checkGrid() {
        boolean correct = true;

        // Clear any previous temp styles
        clearAllTempStyles();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                String text = gridLabels[r][c].getText();
                String expected = String.valueOf(correctGrid[r][c]);
                if (!text.equals(expected)) {
                    correct = false;
                    gridLabels[r][c].setStyle("-fx-background-color: rgba(231, 76, 60, 0.3); -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-font-size: 20px; -fx-font-weight: bold;");
                } else {
                    gridLabels[r][c].setStyle("-fx-background-color: rgba(46, 204, 113, 0.3); -fx-border-color: #27AE60; -fx-border-width: 2; -fx-font-size: 20px; -fx-font-weight: bold;");
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
    // PHASE 4: HIGHLIGHT PAIRS & DECODE
    private void highlightPairs() {
        String[] pairs = currentEncodedMessage.split(" ");

        // remove all existing highlighted styles
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
                // Reset to default style
                resetGridCellStyle(r, c);
            }
        }

        //  apply new highlights
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
                showAlert("Incorrect", "You've used all attempts. Click SHOW ANSWER to see the correct answer.");
            } else {
                showAlert("Incorrect", "Wrong. You have " + (3 - wrongAttempts) + " attempt(s) left.");
            }
        }
    }

    private void showAnswer() {
        showAlert("Answer", "The correct decoded message is: " + decodedAnswer);
    }

    // PHASE 5: NEXT PUZZLE
    private void nextPuzzle() {
        // Clear all styles and highlighted cells before loading new puzzle
        clearAllTempStyles();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
            }
        }

        loadRandomPuzzle();
        currentPhase = 1;
        wrongAttempts = 0;
        gridWrongAttempts = 0;
        hint2Shown = false;
        updatePhase(1);
    }
    // ALERT DIALOG
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
