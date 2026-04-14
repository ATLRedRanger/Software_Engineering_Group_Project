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
    private Button hint2Btn;
    private Button checkGridBtn;
    private Button submitDecodedBtn;
    private Button nextPuzzleBtn;
    private Button showAnswerBtn;
    private Button skipToDecodeBtn;
    private Button backBtn;
    private TextArea decodedTextArea;
    private Label instructionLabel;
    private TitledPane howToPlayPane;

    // FIX: Keep a direct reference to gridContainer so we never call getParent()
    private VBox gridContainer;

    // FIX: Keep a direct reference to dragInstruction so visibility can be toggled
    private Label dragInstruction;

    // Difficulty selection
    private Button easyBtn;
    private Button mediumBtn;
    private Button hardBtn;
    private Button startGameBtn;
    private VBox difficultyBox;

    // Button containers
    private HBox gameButtonContainer;
    private HBox decodeButtonContainer;

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

    // FIX: Track drag source by data, not by label reference that can go stale
    private Label draggedLabel = null;
    private int sourceRow = -1, sourceCol = -1;

    // FIX: Store initial scene coords at drag start for accurate delta calculation
    private double dragStartSceneX, dragStartSceneY;
    private double dragStartTranslateX, dragStartTranslateY;

    private boolean[][] highlightedCells = new boolean[5][5];

    private int lastChallengeId = -1;
    private String currentDifficulty = "easy";

    // FIX: Callback set by PlayfairUI to reset the ScrollPane to the top on phase changes
    private Runnable scrollToTopAction;

    private final ChallengeRepository repo = new ChallengeRepository();

    public ChallengeMode() {
        createChallengeView();
        updatePhase(1);
    }

    /**
     * FIX: Called by PlayfairUI after construction to wire up scroll reset behavior.
     * ChallengeMode does not need a direct reference to the ScrollPane.
     */
    public void setScrollToTopAction(Runnable action) {
        this.scrollToTopAction = action;
    }

    /** Scroll the containing ScrollPane back to the top (safe to call from any phase). */
    private void scrollToTop() {
        if (scrollToTopAction != null) {
            scrollToTopAction.run();
        }
    }

    public void setDifficulty(String difficulty) {
        this.currentDifficulty = difficulty;
    }

    private void loadRandomPuzzle() {
        try {
            System.out.println("=== LOADING PUZZLE ===");
            System.out.println("Difficulty: " + currentDifficulty);
            System.out.println("Last Challenge ID: " + lastChallengeId);

            ChallengeRecord record = repo.getRandomChallengeByDifficulty(currentDifficulty, lastChallengeId);

            if (record == null) {
                System.out.println("❌ Record is NULL!");
                showAlert("No Challenges", "No challenges found for difficulty: " + currentDifficulty);
                return;
            }

            System.out.println("✅ Record found: " + record.getTitle());
            System.out.println("   Grid ID: " + record.getGridId());

            lastChallengeId = record.getId();
            currentEncodedMessage = record.getCiphertext();
            hint1 = record.getHint1();
            hint2 = record.getHint2();
            decodedAnswer = record.getAnswerKey();

            String gridKey = repo.getGridKey(record.getGridId());
            System.out.println("   Grid Key from database: " + gridKey);

            if (gridKey == null) {
                System.out.println("❌ Grid Key is NULL!");
                showAlert("Error", "Could not find grid key for this challenge.");
                return;
            }
            correctKey = gridKey;

            String omitted = record.getOmittedLetter() != null ? record.getOmittedLetter() : "J";
            String cleanKeyStr = correctKey.toUpperCase().replaceAll("\\s+", "");
            List<Character> cleanKey = new ArrayList<>();
            for (char ch : cleanKeyStr.toCharArray()) {
                if (!cleanKey.contains(ch)) cleanKey.add(ch);
            }
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, omitted);
            correctGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            encodedMessageLabel.setText("🔐 Encoded: " + currentEncodedMessage);
            cipherKeyLabel.setText("🔑 Cipher Key: " + correctKey);
            hintLabel.setText("📌 Hint 1: " + hint1);

            wrongAttempts = 0;
            gridWrongAttempts = 0;
            hint2Shown = false;
            decodedTextArea.clear();

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    highlightedCells[r][c] = false;
                    applyDefaultCellStyle(r, c);
                }
            }

            if (skipToDecodeBtn != null) skipToDecodeBtn.setVisible(false);

            System.out.println("✅ Puzzle loaded successfully!");
            System.out.println("========================");

        } catch (Exception e) {
            System.out.println("❌ ERROR in loadRandomPuzzle: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to load puzzle: " + e.getMessage());
        }
    }

    /**
     * FIX: Unified cell style reset using CSS classes only.
     */
    private void applyDefaultCellStyle(int row, int col) {
        Label cell = gridLabels[row][col];
        cell.setStyle("");
        cell.getStyleClass().removeAll("highlighted", "cell-correct", "cell-wrong", "dragged");
        if (!cell.getStyleClass().contains("glass-cell")) {
            cell.getStyleClass().add("glass-cell");
        }
        if (highlightedCells[row][col]) {
            cell.getStyleClass().add("highlighted");
        }
    }

    private void clearAllTempStyles() {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Label cell = gridLabels[r][c];
                cell.setStyle("");
                cell.getStyleClass().removeAll("cell-correct", "cell-wrong", "dragged");
            }
        }
    }

    /**
     * Helper: show or hide a node AND remove it from layout calculations.
     * FIX: setVisible(false) alone still reserves space in the VBox, creating
     * large invisible gaps. setManaged(false) tells the layout engine to ignore
     * the node entirely, so it takes up zero height when hidden.
     */
    private static void setVisibleAndManaged(javafx.scene.Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }

    private void createChallengeView() {
        challengeView = new VBox(8);
        challengeView.setPadding(new Insets(10));
        challengeView.setAlignment(Pos.TOP_CENTER);
        // FIX: Removed "glass-panel" style class — challengeView is already inside
        // toolContent which has glass-panel. Having it on both causes double padding
        // and a visible extra gap at the top of Challenge Mode.

        Label title = new Label("CHALLENGE MODE");
        title.getStyleClass().add("glass-title");
        title.setPadding(new Insets(0, 0, 5, 0));

        // How to play collapsible section
        VBox howToContent = new VBox(10);
        howToContent.setPadding(new Insets(10));
        Label howToTitle = new Label("How to Play Playfair Challenge:");
        howToTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #E3F4EB; -fx-font-size: 14px;");
        String[] steps = {
                "1. Choose your difficulty (Easy, Medium, or Hard)",
                "2. Click START to begin",
                "3. You are given the cipher key and an encoded message",
                "4. Use the hints to figure out what the decoded message is",
                "5. DRAG AND DROP letters to arrange the grid correctly",
                "6. Click 'CHECK GRID' when you think it's correct",
                "7. After 3 wrong attempts, 'SKIP TO DECODE' button appears",
                "8. Pairs will light up in TEAL to help you decode",
                "9. Type the decoded message and submit to complete!"
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

        // Difficulty selection
        difficultyBox = new VBox(15);
        difficultyBox.setAlignment(Pos.CENTER);
        difficultyBox.setPadding(new Insets(20));
        difficultyBox.setStyle("-fx-background-color: rgba(2, 31, 44, 0.5); -fx-background-radius: 20; -fx-border-color: rgba(21, 147, 152, 0.3); -fx-border-width: 1; -fx-border-radius: 20;");

        Label difficultyTitle = new Label("CHOOSE YOUR DIFFICULTY");
        difficultyTitle.setStyle("-fx-text-fill: #E3F4EB; -fx-font-weight: bold; -fx-font-size: 18px;");

        HBox buttonRow = new HBox(20);
        buttonRow.setAlignment(Pos.CENTER);

        easyBtn = new Button("EASY");
        mediumBtn = new Button("MEDIUM");
        hardBtn = new Button("HARD");

        easyBtn.getStyleClass().add("glass-action-button");
        mediumBtn.getStyleClass().add("glass-action-button");
        hardBtn.getStyleClass().add("glass-action-button");

        String defaultStyle = "-fx-background-color: rgba(21, 147, 152, 0.4); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 30; -fx-cursor: hand;";
        String selectedStyle = "-fx-background-color: #159398; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(21, 147, 152, 0.5), 15, 0, 0, 0);";

        easyBtn.setStyle(selectedStyle);
        mediumBtn.setStyle(defaultStyle);
        hardBtn.setStyle(defaultStyle);

        easyBtn.setOnAction(e -> {
            easyBtn.setStyle(selectedStyle);
            mediumBtn.setStyle(defaultStyle);
            hardBtn.setStyle(defaultStyle);
            currentDifficulty = "easy";
        });
        mediumBtn.setOnAction(e -> {
            mediumBtn.setStyle(selectedStyle);
            easyBtn.setStyle(defaultStyle);
            hardBtn.setStyle(defaultStyle);
            currentDifficulty = "medium";
        });
        hardBtn.setOnAction(e -> {
            hardBtn.setStyle(selectedStyle);
            easyBtn.setStyle(defaultStyle);
            mediumBtn.setStyle(defaultStyle);
            currentDifficulty = "hard";
        });

        buttonRow.getChildren().addAll(easyBtn, mediumBtn, hardBtn);

        Label easyDesc = new Label("Easy: Simple words, obvious hints");
        Label mediumDesc = new Label("Medium: Tricky keys, cryptic clues");
        Label hardDesc = new Label("Hard: Expert mode, minimal hints");
        easyDesc.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 12px;");
        mediumDesc.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 12px;");
        hardDesc.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 12px;");

        VBox descBox = new VBox(5);
        descBox.setAlignment(Pos.CENTER);
        descBox.getChildren().addAll(easyDesc, mediumDesc, hardDesc);

        startGameBtn = new Button("START");
        startGameBtn.getStyleClass().add("glass-action-button");
        startGameBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 30; -fx-font-size: 16px; -fx-cursor: hand;");
        startGameBtn.setOnAction(e -> {
            loadRandomPuzzle();
            if (correctGrid != null) {
                updatePhase(3);
            }
        });

        difficultyBox.getChildren().addAll(difficultyTitle, buttonRow, descBox, startGameBtn);

        instructionLabel = new Label("Choose your difficulty and click START!");
        instructionLabel.getStyleClass().add("glass-subtitle");
        instructionLabel.setMaxWidth(Double.MAX_VALUE);
        instructionLabel.setAlignment(Pos.CENTER);
        instructionLabel.setPadding(new Insets(5, 0, 5, 0));

        encodedMessageLabel = new Label();
        encodedMessageLabel.getStyleClass().add("glass-subtitle");
        encodedMessageLabel.setMaxWidth(Double.MAX_VALUE);
        encodedMessageLabel.setAlignment(Pos.CENTER);
        encodedMessageLabel.setPadding(new Insets(5, 0, 5, 0));
        // FIX: setManaged(false) prevents hidden nodes from taking up layout space
        setVisibleAndManaged(encodedMessageLabel, false);

        cipherKeyLabel = new Label();
        cipherKeyLabel.getStyleClass().add("glass-subtitle");
        cipherKeyLabel.setMaxWidth(Double.MAX_VALUE);
        cipherKeyLabel.setAlignment(Pos.CENTER);
        cipherKeyLabel.setStyle("-fx-text-fill: #159398; -fx-font-weight: bold; -fx-font-size: 16px;");
        cipherKeyLabel.setPadding(new Insets(5, 0, 5, 0));
        // FIX: Same — remove from layout when hidden
        setVisibleAndManaged(cipherKeyLabel, false);

        hintLabel = new Label();
        hintLabel.getStyleClass().add("glass-hint");
        hintLabel.setMaxWidth(Double.MAX_VALUE);
        hintLabel.setAlignment(Pos.CENTER);
        hintLabel.setWrapText(true);
        hintLabel.setPadding(new Insets(5, 0, 5, 0));
        // FIX: Same
        setVisibleAndManaged(hintLabel, false);

        // FIX: Assign gridContainer to field — never use getParent() again
        gridContainer = new VBox(10);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.setPadding(new Insets(5, 0, 5, 0));
        // FIX: Same
        setVisibleAndManaged(gridContainer, false);

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

        // FIX: Assign dragInstruction to field so visibility can be controlled per phase
        dragInstruction = new Label("✏️ Drag and drop letters to rearrange them");
        dragInstruction.setStyle("-fx-text-fill: #159398; -fx-padding: 5; -fx-font-weight: bold");
        dragInstruction.setVisible(false);

        gridContainer.getChildren().addAll(gridPane, dragInstruction);

        // Game buttons (Phase 3)
        gameButtonContainer = new HBox(10);
        gameButtonContainer.setAlignment(Pos.CENTER);
        gameButtonContainer.setPadding(new Insets(10, 0, 10, 0));
        // FIX: Same
        setVisibleAndManaged(gameButtonContainer, false);

        hint2Btn = new Button("🔍 GET HINT 2");
        hint2Btn.getStyleClass().add("glass-action-button");

        checkGridBtn = new Button("✅ CHECK GRID");
        checkGridBtn.getStyleClass().add("glass-action-button");

        skipToDecodeBtn = new Button("⏩ SKIP TO DECODE");
        skipToDecodeBtn.getStyleClass().add("glass-action-button");
        skipToDecodeBtn.setOnAction(e -> {
            clearAllTempStyles();
            showAlert("Skipping", "Moving to decode phase...");
            updatePhase(4);
        });

        backBtn = new Button("◀ BACK");
        backBtn.getStyleClass().add("glass-action-button");
        backBtn.setOnAction(e -> {
            currentPhase = 1;
            updatePhase(1);
        });

        gameButtonContainer.getChildren().addAll(hint2Btn, checkGridBtn, skipToDecodeBtn, backBtn);

        // Decode buttons (Phase 4)
        decodeButtonContainer = new HBox(10);
        decodeButtonContainer.setAlignment(Pos.CENTER);
        decodeButtonContainer.setPadding(new Insets(10, 0, 10, 0));
        // FIX: Same
        setVisibleAndManaged(decodeButtonContainer, false);

        submitDecodedBtn = new Button("🎯 SUBMIT DECODED");
        submitDecodedBtn.getStyleClass().add("glass-action-button");

        showAnswerBtn = new Button("💡 SHOW ANSWER");
        showAnswerBtn.getStyleClass().add("glass-action-button");

        decodeButtonContainer.getChildren().addAll(submitDecodedBtn, showAnswerBtn);

        decodedLabel = new Label("Decoded Message:");
        decodedLabel.getStyleClass().add("glass-subtitle");
        decodedLabel.setPadding(new Insets(5, 0, 5, 0));
        // FIX: Same
        setVisibleAndManaged(decodedLabel, false);

        decodedTextArea = new TextArea();
        decodedTextArea.setPromptText("Type the decoded message here (spaces optional)");
        decodedTextArea.setPrefRowCount(3);
        decodedTextArea.setWrapText(true);
        decodedTextArea.getStyleClass().add("glass-text-area");
        decodedTextArea.setMaxWidth(500);
        decodedTextArea.setPrefHeight(80);
        // FIX: Same
        setVisibleAndManaged(decodedTextArea, false);

        nextPuzzleBtn = new Button("NEXT PUZZLE ▶");
        nextPuzzleBtn.getStyleClass().add("glass-action-button");
        nextPuzzleBtn.setPadding(new Insets(10, 0, 10, 0));
        // FIX: Same
        setVisibleAndManaged(nextPuzzleBtn, false);

        challengeView.getChildren().addAll(
                title,
                howToPlayPane,
                difficultyBox,
                instructionLabel,
                encodedMessageLabel,
                cipherKeyLabel,
                hintLabel,
                gridContainer,
                gameButtonContainer,
                decodedLabel,
                decodedTextArea,
                decodeButtonContainer,
                nextPuzzleBtn
        );

        hint2Btn.setOnAction(e -> showHint2());
        checkGridBtn.setOnAction(e -> checkGrid());
        submitDecodedBtn.setOnAction(e -> checkDecoded());
        nextPuzzleBtn.setOnAction(e -> nextPuzzle());
        showAnswerBtn.setOnAction(e -> showAnswer());
    }

    // ─── DRAG AND DROP ────────────────────────────────────────────────────────

    private void handleDragStart(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3 || label.getText().equals("?")) return;
        clearAllTempStyles();
        draggedLabel = label;
        sourceRow = row;
        sourceCol = col;

        dragStartSceneX = e.getSceneX();
        dragStartSceneY = e.getSceneY();
        dragStartTranslateX = label.getTranslateX();
        dragStartTranslateY = label.getTranslateY();

        label.getStyleClass().add("dragged");
        label.toFront();
        e.consume();
    }

    private void handleDrag(MouseEvent e, Label label) {
        if (currentPhase != 3 || draggedLabel == null) return;

        double deltaX = e.getSceneX() - dragStartSceneX;
        double deltaY = e.getSceneY() - dragStartSceneY;
        label.setTranslateX(dragStartTranslateX + deltaX);
        label.setTranslateY(dragStartTranslateY + deltaY);

        e.consume();
    }

    private void handleDragEnd(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3 || draggedLabel == null) return;

        label.setTranslateX(0);
        label.setTranslateY(0);

        Label target = null;
        int tr = -1, tc = -1;
        double dropX = e.getSceneX();
        double dropY = e.getSceneY();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Label candidate = gridLabels[r][c];
                if (candidate == draggedLabel) continue;
                javafx.geometry.Bounds bounds = candidate.localToScene(candidate.getBoundsInLocal());
                double cx = bounds.getCenterX();
                double cy = bounds.getCenterY();
                if (Math.hypot(dropX - cx, dropY - cy) < (bounds.getWidth() / 2 + 10)) {
                    target = candidate;
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
        applyDefaultCellStyle(sourceRow, sourceCol);
        if (target != null) {
            target.getStyleClass().remove("dragged");
            applyDefaultCellStyle(tr, tc);
        }

        draggedLabel = null;
        sourceRow = -1;
        sourceCol = -1;
        e.consume();
    }

    private void showHint2() {
        hintLabel.setText("📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2);
        hint2Btn.setVisible(false);
        hint2Shown = true;
    }

    // ─── PHASE MANAGEMENT ─────────────────────────────────────────────────────

    private void updatePhase(int phase) {
        currentPhase = phase;

        // FIX: Always scroll back to top when the phase changes so the user
        // sees the title/top of the view, not wherever they last scrolled to.
        scrollToTop();

        switch (phase) {
            case 1:
                setVisibleAndManaged(difficultyBox, true);
                instructionLabel.setText("Choose your difficulty and click START!");
                setVisibleAndManaged(encodedMessageLabel, false);
                setVisibleAndManaged(cipherKeyLabel, false);
                setVisibleAndManaged(gridContainer, false);
                setVisibleAndManaged(hintLabel, false);
                setVisibleAndManaged(gameButtonContainer, false);
                setVisibleAndManaged(decodeButtonContainer, false);
                setVisibleAndManaged(decodedLabel, false);
                setVisibleAndManaged(decodedTextArea, false);
                setVisibleAndManaged(nextPuzzleBtn, false);
                break;

            case 3:
                if (correctGrid == null) {
                    showAlert("Error", "Failed to load puzzle. Please try again.");
                    return;
                }
                setVisibleAndManaged(difficultyBox, false);
                instructionLabel.setText("Arrange the grid correctly using the key: " + correctKey);
                setVisibleAndManaged(encodedMessageLabel, true);
                setVisibleAndManaged(cipherKeyLabel, true);
                setVisibleAndManaged(gridContainer, true);
                dragInstruction.setVisible(true);
                setVisibleAndManaged(hintLabel, true);
                setVisibleAndManaged(gameButtonContainer, true);
                hint2Btn.setVisible(true);
                checkGridBtn.setVisible(true);
                skipToDecodeBtn.setVisible(false);
                backBtn.setVisible(true);
                setVisibleAndManaged(decodeButtonContainer, false);
                setVisibleAndManaged(decodedLabel, false);
                setVisibleAndManaged(decodedTextArea, false);
                setVisibleAndManaged(nextPuzzleBtn, false);
                gridWrongAttempts = 0;
                clearAllTempStyles();

                // Shuffle grid letters
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
                setVisibleAndManaged(gridContainer, true);
                dragInstruction.setVisible(false);
                setVisibleAndManaged(gameButtonContainer, false);
                setVisibleAndManaged(decodeButtonContainer, true);
                submitDecodedBtn.setVisible(true);
                showAnswerBtn.setVisible(false);
                backBtn.setVisible(true);
                setVisibleAndManaged(decodedLabel, true);
                setVisibleAndManaged(decodedTextArea, true);
                setVisibleAndManaged(nextPuzzleBtn, false);
                decodedTextArea.clear();
                highlightPairs();
                break;

            case 5:
                instructionLabel.setText("🎉 Challenge Complete! Great job!");
                setVisibleAndManaged(decodedLabel, false);
                setVisibleAndManaged(decodedTextArea, false);
                setVisibleAndManaged(decodeButtonContainer, false);
                setVisibleAndManaged(gameButtonContainer, false);
                backBtn.setVisible(false);
                setVisibleAndManaged(nextPuzzleBtn, true);
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

    // ─── PHASE 3: CHECK GRID ─────────────────────────────────────────────────

    private void checkGrid() {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                gridLabels[r][c].setStyle("");
                gridLabels[r][c].getStyleClass().removeAll("cell-correct", "cell-wrong");
            }
        }

        boolean correct = true;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                String text = gridLabels[r][c].getText();
                String expected = String.valueOf(correctGrid[r][c]);
                if (!text.equals(expected)) {
                    correct = false;
                    gridLabels[r][c].getStyleClass().add("cell-wrong");
                } else {
                    gridLabels[r][c].getStyleClass().add("cell-correct");
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

    // ─── PHASE 4: HIGHLIGHT PAIRS ─────────────────────────────────────────────

    private void highlightPairs() {
        String[] pairs = currentEncodedMessage.split(" ");

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
                applyDefaultCellStyle(r, c);
            }
        }

        for (String pair : pairs) {
            if (pair.length() == 2) {
                int[] p1 = findLetter(pair.charAt(0));
                int[] p2 = findLetter(pair.charAt(1));
                if (p1 != null && p2 != null) {
                    highlightedCells[p1[0]][p1[1]] = true;
                    highlightedCells[p2[0]][p2[1]] = true;
                    if (!gridLabels[p1[0]][p1[1]].getStyleClass().contains("highlighted"))
                        gridLabels[p1[0]][p1[1]].getStyleClass().add("highlighted");
                    if (!gridLabels[p2[0]][p2[1]].getStyleClass().contains("highlighted"))
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
                showAlert("Incorrect", "3 attempts used. Click SHOW ANSWER to reveal it.");
            } else {
                showAlert("Incorrect", "Wrong! " + (3 - wrongAttempts) + " attempt(s) left.");
            }
        }
    }

    private void showAnswer() {
        showAlert("Answer", "The decoded message is: " + decodedAnswer);
    }

    // ─── PHASE 5: NEXT PUZZLE ─────────────────────────────────────────────────

    private void nextPuzzle() {
        clearAllTempStyles();
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++) {
                highlightedCells[r][c] = false;
                gridLabels[r][c].getStyleClass().removeAll("highlighted");
            }
        correctGrid = null;
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
