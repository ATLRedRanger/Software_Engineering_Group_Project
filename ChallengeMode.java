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

    private int lastChallengeId = -1;
    private String currentDifficulty = "easy";

    private final ChallengeRepository repo = new ChallengeRepository();

    // Distinct colors for each digram pair
    private final String[] pairColors = {
            "#FF5733", "#33FF57", "#3357FF", "#F333FF", "#FFF333",
            "#33FFF3", "#FF8C00", "#8A2BE2", "#00FF7F", "#FF1493"
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
            if (record == null) return;

            lastChallengeId = record.getId();
            currentEncodedMessage = record.getCiphertext();
            hint1 = record.getHint1();
            hint2 = record.getHint2();

            correctKey = repo.getRandomGridKey();
            String omitted = (record.getOmittedLetter() != null) ? record.getOmittedLetter() : "J";

            List<Character> cleanKey = PlayfairDecrypt.CleanKey(correctKey.toUpperCase());
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, omitted);
            correctGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            // Decrypt the message so we can check the user's final answer
            List<String> digrams = PlayfairDecrypt.DigramMessage(currentEncodedMessage.replace(" ", ""));
            decodedAnswer = PlayfairDecrypt.ProcessMessage(digrams, correctGrid, -1);

            encodedMessageLabel.setText("🔐 Encoded: " + currentEncodedMessage);
            cipherKeyLabel.setText("🔑 Cipher Key: " + correctKey);
            hintLabel.setText("📌 Hint 1: " + hint1);

            resetUIState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetUIState() {
        wrongAttempts = 0;
        gridWrongAttempts = 0;
        hint2Shown = false;
        decodedTextArea.clear();
        if (skipToDecodeBtn != null) skipToDecodeBtn.setVisible(false);
        clearAllTempStyles();
    }

    private void createChallengeView() {
        challengeView = new VBox(12);
        challengeView.setPadding(new Insets(20));
        challengeView.setAlignment(Pos.TOP_CENTER);
        challengeView.getStyleClass().add("glass-panel");

        Label title = new Label("CHALLENGE MODE");
        title.getStyleClass().add("glass-title");

        instructionLabel = new Label("Build the grid, then decrypt the highlighted pairs!");
        instructionLabel.getStyleClass().add("glass-subtitle");

        encodedMessageLabel = new Label();
        encodedMessageLabel.getStyleClass().add("glass-subtitle");

        cipherKeyLabel = new Label();
        cipherKeyLabel.setStyle("-fx-text-fill: #159398; -fx-font-weight: bold; -fx-font-size: 16px;");

        setupGrid();

        hintLabel = new Label();
        hintLabel.getStyleClass().add("glass-hint");

        setupButtons();
        setupDecodeSection();

        HBox buttonContainer = new HBox(15, startBtn, hint2Btn, checkGridBtn, skipToDecodeBtn);
        buttonContainer.setAlignment(Pos.CENTER);

        HBox decodeButtonContainer = new HBox(15, submitDecodedBtn, showAnswerBtn);
        decodeButtonContainer.setAlignment(Pos.CENTER);

        challengeView.getChildren().addAll(
                title, instructionLabel, encodedMessageLabel, cipherKeyLabel,
                gridPane, hintLabel, buttonContainer,
                decodedLabel, decodedTextArea, decodeButtonContainer, nextPuzzleBtn
        );
    }

    private void setupGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
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
    }

    // --- FUNCTIONAL DRAG AND DROP SWAP ---
    private void handleDragStart(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3) return;
        draggedLabel = label;
        dragOffsetX = e.getX();
        dragOffsetY = e.getY();
        label.setOpacity(0.7);
        label.toFront();
    }

    private void handleDrag(MouseEvent e, Label label) {
        if (draggedLabel == null) return;
        double newX = e.getSceneX() - dragOffsetX - gridPane.getLocalToSceneTransform().getTx();
        double newY = e.getSceneY() - dragOffsetY - gridPane.getLocalToSceneTransform().getTy();
        label.setTranslateX(newX - label.getLayoutX());
        label.setTranslateY(newY - label.getLayoutY());
    }

    private void handleDragEnd(MouseEvent e, Label label, int row, int col) {
        if (draggedLabel == null) return;
        label.setTranslateX(0);
        label.setTranslateY(0);
        label.setOpacity(1.0);

        double dropX = e.getSceneX();
        double dropY = e.getSceneY();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Label target = gridLabels[r][c];
                if (target == label) continue;

                double targetX = target.getLocalToSceneTransform().getTx() + target.getWidth() / 2;
                double targetY = target.getLocalToSceneTransform().getTy() + target.getHeight() / 2;

                if (Math.hypot(dropX - targetX, dropY - targetY) < 35) {
                    String temp = label.getText();
                    label.setText(target.getText());
                    target.setText(temp);
                    break;
                }
            }
        }
        draggedLabel = null;
    }

    private void setupButtons() {
        startBtn = new Button("▶ START");
        startBtn.setOnAction(e -> updatePhase(3));

        hint2Btn = new Button("🔍 GET HINT 2");
        hint2Btn.setOnAction(e -> showHint2());

        checkGridBtn = new Button("✅ CHECK GRID");
        checkGridBtn.setOnAction(e -> checkGrid());

        skipToDecodeBtn = new Button("⏩ SKIP");
        skipToDecodeBtn.setOnAction(e -> updatePhase(4));
        skipToDecodeBtn.setVisible(false);

        nextPuzzleBtn = new Button("NEXT PUZZLE ▶");
        nextPuzzleBtn.setOnAction(e -> nextPuzzle());
        nextPuzzleBtn.setVisible(false);
    }

    private void setupDecodeSection() {
        decodedLabel = new Label("Type Decrypted Message:");
        decodedLabel.setVisible(false);
        decodedTextArea = new TextArea();
        decodedTextArea.setPrefRowCount(2);
        decodedTextArea.setMaxWidth(400);
        decodedTextArea.setVisible(false);

        submitDecodedBtn = new Button("🎯 SUBMIT");
        submitDecodedBtn.setOnAction(e -> checkDecoded());
        submitDecodedBtn.setVisible(false);

        showAnswerBtn = new Button("💡 SHOW ANSWER");
        showAnswerBtn.setOnAction(e -> showAnswer());
        showAnswerBtn.setVisible(false);
    }

    private void updatePhase(int phase) {
        currentPhase = phase;
        startBtn.setVisible(phase == 1);
        checkGridBtn.setVisible(phase == 3);

        decodedLabel.setVisible(phase == 4);
        decodedTextArea.setVisible(phase == 4);
        submitDecodedBtn.setVisible(phase == 4);

        if (phase == 3) scrambleGrid();
        if (phase == 4) highlightGridPairs(); // Highlights the Ciphertext pairs
        if (phase == 5) nextPuzzleBtn.setVisible(true);
    }

    private void scrambleGrid() {
        List<String> letters = new ArrayList<>();
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                letters.add(String.valueOf(correctGrid[r][c]));
        Collections.shuffle(letters);
        int idx = 0;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                gridLabels[r][c].setText(letters.get(idx++));
    }

    // --- HIGHLIGHTING LOGIC: Target the Ciphertext (Encoded Pairs) ---
    private void highlightGridPairs() {
        String cleanCipher = currentEncodedMessage.replace(" ", "");
        List<String> digrams = PlayfairDecrypt.DigramMessage(cleanCipher);
        clearAllTempStyles();

        for (int i = 0; i < digrams.size(); i++) {
            String pair = digrams.get(i);
            int[] p1 = findLetterInGrid(pair.charAt(0));
            int[] p2 = findLetterInGrid(pair.charAt(1));

            if (p1 != null && p2 != null) {
                String color = pairColors[i % pairColors.length];
                String style = "-fx-background-color: rgba(" + hexToRgb(color) + ", 0.4); -fx-border-color: " + color + "; -fx-border-width: 3;";

                // We highlight the EXACT locations of the encoded letters
                // The user must then perform the shift logic manually.
                gridLabels[p1[0]][p1[1]].setStyle(style);
                gridLabels[p2[0]][p2[1]].setStyle(style);
            }
        }
    }

    private int[] findLetterInGrid(char letter) {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                if (gridLabels[r][c].getText().equals(String.valueOf(letter)))
                    return new int[]{r, c};
        return null;
    }

    private void checkGrid() {
        boolean correct = true;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (!gridLabels[r][c].getText().equals(String.valueOf(correctGrid[r][c]))) {
                    correct = false;
                    gridLabels[r][c].setStyle("-fx-border-color: red;");
                } else {
                    gridLabels[r][c].setStyle("-fx-border-color: green; -fx-background-color: rgba(46, 204, 113, 0.1);");
                }
            }
        }
        if (correct) updatePhase(4);
        else if (++gridWrongAttempts >= 3) skipToDecodeBtn.setVisible(true);
    }

    private void checkDecoded() {
        String userRaw = decodedTextArea.getText().trim().toUpperCase().replaceAll("\\s+", "");
        String answerRaw = decodedAnswer.toUpperCase().replaceAll("\\s+", "");

        if (userRaw.equals(answerRaw)) {
            updatePhase(5);
            showAlert("Success!", "Excellent work! Message decrypted correctly.");
        } else {
            if (++wrongAttempts >= 3) showAnswerBtn.setVisible(true);
            showAlert("Incorrect", "That's not quite right. Try again!");
        }
    }

    private void clearAllTempStyles() {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                gridLabels[r][c].setStyle("-fx-border-color: rgba(21, 147, 152, 0.4); -fx-background-color: rgba(2, 31, 44, 0.7);");
    }

    private String hexToRgb(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return r + "," + g + "," + b;
    }

    private void showHint2() {
        hintLabel.setText("📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2);
        hint2Btn.setVisible(false);
    }

    private void showAnswer() { showAlert("Answer", "The decrypted message is: " + decodedAnswer); }
    private void nextPuzzle() { loadRandomPuzzle(); updatePhase(1); }
    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    public VBox getView() { return challengeView; }
}