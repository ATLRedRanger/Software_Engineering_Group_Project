/*
 * Jessica DeWitt - UI/Front-end Development
 *  Front-end for challenge mode
 */

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
    private Label[][] gridLabels;  // using labels so we can drag them around
    private Label encodedMessageLabel;
    private Label hintLabel;
    private TextField keyInput;
    private Button submitKeyBtn;
    private Button hint2Btn;
    private Button checkGridBtn;
    private Button submitDecodedBtn;
    private Button newChallengeBtn;
    private HBox decodedInputBox;
    private TextField[] decodedFields;
    private Label instructionLabel;
    private TitledPane howToPlayPane;
    private HBox puzzleSelectorBox;
    private ToggleGroup puzzleGroup;
    private RadioButton[] puzzleButtons;

    private String currentEncodedMessage;
    private String correctKey;
    private String hint1;
    private String hint2;
    private String decodedAnswer;
    private char[][] correctGrid;
    private int currentPhase = 1;  // tracks where user is in the challenge
    private int currentPuzzleIndex = 0;
    private boolean hint2Shown = false;

    // drag and drop stuff
    private Label draggedLabel = null;
    private double dragOffsetX, dragOffsetY;
    private int sourceRow = -1;
    private int sourceCol = -1;

    // keeps track of which cells are highlighted yellow
    private boolean[][] highlightedCells = new boolean[5][5];

    // predefined puzzles - will be replaced with database later
    private Puzzle[] puzzles = {
            new Puzzle(
                    "BM OD ZB",
                    "PLAYFAIR",
                    "Starts with P",
                    "Contains LAY",
                    "HIDETH"
            ),
            new Puzzle(
                    "BM OD ZB XD",
                    "PLAYFAIR",
                    "Starts with P",
                    "Contains LAY",
                    "HIDETHEG"
            ),
            new Puzzle(
                    "BM OD ZB XD NA",
                    "PLAYFAIR",
                    "Starts with P",
                    "Contains LAY",
                    "HIDETHEGOL"
            ),
            new Puzzle(
                    "YT RS TU XA",
                    "APPLE",
                    "Starts with A",
                    "Ends with E",
                    "HELLOWORLD"
            ),
            new Puzzle(
                    "XQ PL MR YZ",
                    "ZEBRA",
                    "Starts with Z",
                    "Has E and A",
                    "TESTMESX"
            )
    };

    // simple class to hold puzzle data
    private class Puzzle {
        String encoded;
        String key;
        String hint1;
        String hint2;
        String decoded;

        Puzzle(String encoded, String key, String hint1, String hint2, String decoded) {
            this.encoded = encoded;
            this.key = key;
            this.hint1 = hint1;
            this.hint2 = hint2;
            this.decoded = decoded;
        }
    }

    public ChallengeMode() {
        createChallengeView();
        loadPuzzle(0);
        updatePhase(1);
    }

    // builds the whole challenge mode screen
    private void createChallengeView() {
        challengeView = new VBox(15);
        challengeView.setPadding(new Insets(15));
        challengeView.setAlignment(Pos.TOP_CENTER);
        challengeView.setStyle("-fx-background-color: #f0f0f0;");

        Label title = new Label("CHALLENGE MODE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2C3E50"));

        // radio buttons to pick which puzzle to try
        puzzleSelectorBox = new HBox(15);
        puzzleSelectorBox.setAlignment(Pos.CENTER);
        puzzleSelectorBox.setPadding(new Insets(10));
        puzzleSelectorBox.setStyle("-fx-background-color: #ECF0F1; -fx-border-radius: 5; -fx-padding: 10;");

        Label selectLabel = new Label("Select Puzzle:");
        selectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        puzzleGroup = new ToggleGroup();
        puzzleButtons = new RadioButton[puzzles.length];

        for (int i = 0; i < puzzles.length; i++) {
            int puzzleNum = i + 1;
            RadioButton rb = new RadioButton("Puzzle " + puzzleNum);
            rb.setToggleGroup(puzzleGroup);
            rb.setUserData(i);
            rb.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

            int index = i;
            rb.setOnAction(e -> {
                if (puzzleGroup.getSelectedToggle() != null) {
                    int selectedIndex = (int) ((RadioButton) puzzleGroup.getSelectedToggle()).getUserData();
                    loadPuzzle(selectedIndex);
                    updatePhase(1);
                }
            });

            puzzleButtons[i] = rb;
            puzzleSelectorBox.getChildren().add(rb);
        }

        puzzleButtons[0].setSelected(true);

        // collapsible section with instructions
        VBox howToContent = new VBox(10);
        howToContent.setPadding(new Insets(10));

        Label howToTitle = new Label("How to Play Playfair Challenge:");
        howToTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        String[] steps = {
                "1. Guess the cipher key using the hints",
                "2. You have 2 hints available",
                "3. Once key is correct, the grid will appear",
                "4. DRAG AND DROP letters to rearrange them",
                "5. Click 'CHECK GRID' when you think it's correct",
                "6. Pairs will light up in YELLOW",
                "7. Use the lit pairs to decode the message",
                "8. Fill in the decoded message boxes",
                "9. Submit to complete the challenge!"
        };

        VBox stepsBox = new VBox(5);
        for (String step : steps) {
            Label stepLabel = new Label("  • " + step);
            stepLabel.setWrapText(true);
            stepsBox.getChildren().add(stepLabel);
        }

        howToContent.getChildren().addAll(howToTitle, stepsBox);

        howToPlayPane = new TitledPane("📖 How to Play", howToContent);
        howToPlayPane.setExpanded(false);
        howToPlayPane.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-radius: 5;");

        // shows current step
        instructionLabel = new Label("1. Guess the cipher key using the hints below");
        instructionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        instructionLabel.setStyle("-fx-text-fill: #2C3E50; -fx-background-color: #EBF5FB; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #3498DB;");
        instructionLabel.setMaxWidth(Double.MAX_VALUE);
        instructionLabel.setAlignment(Pos.CENTER);

        // shows the encoded message to solve
        encodedMessageLabel = new Label();
        encodedMessageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        encodedMessageLabel.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 5;");
        encodedMessageLabel.setMaxWidth(Double.MAX_VALUE);
        encodedMessageLabel.setAlignment(Pos.CENTER);

        // container for the grid
        VBox gridContainer = new VBox(10);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #BDC3C7; -fx-border-radius: 5;");

        gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setAlignment(Pos.CENTER);

        gridLabels = new Label[5][5];

        // create all grid cells and set up drag events
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Label label = new Label("?");
                label.setMinSize(70, 70);
                label.setMaxSize(70, 70);
                label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                label.setAlignment(Pos.CENTER);
                label.setStyle(getDefaultLabelStyle());

                int r = row;
                int c = col;

                label.setOnMousePressed(e -> handleMousePressed(e, label, r, c));
                label.setOnMouseDragged(e -> handleMouseDragged(e, label));
                label.setOnMouseReleased(e -> handleMouseReleased(e, label, r, c));

                // hover effect - changes border color
                label.setOnMouseEntered(e -> {
                    if (!isHighlighted(r, c) && !label.equals(draggedLabel)) {
                        label.setStyle(getHoverLabelStyle());
                    }
                });

                label.setOnMouseExited(e -> {
                    if (!isHighlighted(r, c) && !label.equals(draggedLabel)) {
                        label.setStyle(getDefaultLabelStyle());
                    } else if (isHighlighted(r, c)) {
                        label.setStyle(getHighlightedLabelStyle());
                    }
                });

                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        gridContainer.getChildren().add(gridPane);

        Label dragInstruction = new Label("✏️ Drag and drop letters to rearrange them");
        dragInstruction.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        dragInstruction.setStyle("-fx-text-fill: #E67E22; -fx-background-color: #FDEBD0; -fx-padding: 8; -fx-border-radius: 5;");
        dragInstruction.setVisible(false);
        gridContainer.getChildren().add(dragInstruction);

        // shows hints
        hintLabel = new Label();
        hintLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        hintLabel.setStyle("-fx-background-color: #FFF3CD; -fx-padding: 12; -fx-border-color: #FFE69C; -fx-border-radius: 5;");
        hintLabel.setMaxWidth(Double.MAX_VALUE);
        hintLabel.setAlignment(Pos.CENTER);
        hintLabel.setWrapText(true);

        // key guess input
        HBox keyBox = new HBox(10);
        keyBox.setAlignment(Pos.CENTER);

        keyInput = new TextField();
        keyInput.setPromptText("Enter key guess");
        keyInput.setPrefWidth(250);
        keyInput.setStyle("-fx-padding: 10; -fx-font-size: 14px; -fx-background-radius: 5;");

        submitKeyBtn = new Button("SUBMIT KEY");
        submitKeyBtn.setStyle(getBlueButtonStyle());

        keyBox.getChildren().addAll(keyInput, submitKeyBtn);

        hint2Btn = new Button("🔍 GET HINT 2");
        hint2Btn.setStyle(getOrangeButtonStyle());
        hint2Btn.setVisible(false);

        checkGridBtn = new Button("✅ CHECK GRID");
        checkGridBtn.setStyle(getGreenButtonStyle());
        checkGridBtn.setVisible(false);

        newChallengeBtn = new Button("🔄 NEXT PUZZLE");
        newChallengeBtn.setStyle(getRedButtonStyle());
        newChallengeBtn.setVisible(false);

        // where user types the decoded message
        Label decodedLabel = new Label("Decoded Message:");
        decodedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        decodedLabel.setVisible(false);

        decodedInputBox = new HBox(10);
        decodedInputBox.setAlignment(Pos.CENTER);
        decodedInputBox.setVisible(false);
        decodedInputBox.setStyle("-fx-background-color: #D5F5E3; -fx-padding: 15; -fx-border-radius: 5; -fx-border-color: #27AE60;");

        submitDecodedBtn = new Button("🎯 SUBMIT DECODED");
        submitDecodedBtn.setStyle(getPurpleButtonStyle());
        submitDecodedBtn.setVisible(false);

        // put everything together
        challengeView.getChildren().addAll(
                title,
                puzzleSelectorBox,
                howToPlayPane,
                instructionLabel,
                encodedMessageLabel,
                gridContainer,
                hintLabel,
                keyBox,
                hint2Btn,
                checkGridBtn,
                newChallengeBtn,
                decodedLabel,
                decodedInputBox,
                submitDecodedBtn
        );

        setupActions();
    }

    // style helpers - keep all the styling in one place
    private String getDefaultLabelStyle() {
        return "-fx-border-color: #34495E; -fx-border-width: 2; -fx-background-color: white; -fx-text-fill: #2C3E50; -fx-alignment: center; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5;";
    }

    private String getHoverLabelStyle() {
        return "-fx-border-color: #3498DB; -fx-border-width: 3; -fx-background-color: #EBF5FB; -fx-text-fill: #2C3E50; -fx-alignment: center; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5;";
    }

    private String getDraggedLabelStyle() {
        return "-fx-border-color: #3498DB; -fx-border-width: 3; -fx-background-color: #EBF5FB; -fx-text-fill: #2C3E50; -fx-alignment: center; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);";
    }

    private String getHighlightedLabelStyle() {
        return "-fx-background-color: #F1C40F; -fx-border-color: #E67E22; -fx-border-width: 3; -fx-text-fill: #2C3E50; -fx-alignment: center; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5;";
    }

    private String getBlueButtonStyle() {
        return "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-font-size: 14px;";
    }

    private String getOrangeButtonStyle() {
        return "-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-font-size: 14px;";
    }

    private String getGreenButtonStyle() {
        return "-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-font-size: 14px;";
    }

    private String getRedButtonStyle() {
        return "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-font-size: 14px;";
    }

    private String getPurpleButtonStyle() {
        return "-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-font-size: 14px;";
    }

    private boolean isHighlighted(int row, int col) {
        return highlightedCells[row][col];
    }

    // drag and drop: pick up a letter
    private void handleMousePressed(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3 || label.getText().equals("?")) return;

        draggedLabel = label;
        sourceRow = row;
        sourceCol = col;

        dragOffsetX = e.getX();
        dragOffsetY = e.getY();

        label.setStyle(getDraggedLabelStyle());
        label.toFront();

        e.consume();
    }

    // letter is being dragged
    private void handleMouseDragged(MouseEvent e, Label label) {
        if (currentPhase != 3 || draggedLabel == null) return;

        double newX = e.getSceneX() - dragOffsetX - gridPane.getLocalToSceneTransform().getTx();
        double newY = e.getSceneY() - dragOffsetY - gridPane.getLocalToSceneTransform().getTy();

        label.setTranslateX(newX - label.getLayoutX());
        label.setTranslateY(newY - label.getLayoutY());

        e.consume();
    }

    // let go of the letter - see if it lands on another cell
    private void handleMouseReleased(MouseEvent e, Label label, int row, int col) {
        if (currentPhase != 3 || draggedLabel == null) return;

        label.setTranslateX(0);
        label.setTranslateY(0);

        Label targetLabel = null;
        int targetRow = -1;
        int targetCol = -1;

        double dropX = e.getSceneX();
        double dropY = e.getSceneY();

        // check if drop point is near another label's center
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Label l = gridLabels[r][c];
                if (l == draggedLabel) continue;

                double centerX = l.getLocalToSceneTransform().getTx() + l.getWidth() / 2;
                double centerY = l.getLocalToSceneTransform().getTy() + l.getHeight() / 2;

                double distance = Math.sqrt(Math.pow(dropX - centerX, 2) + Math.pow(dropY - centerY, 2));
                if (distance < 50) {
                    targetLabel = l;
                    targetRow = r;
                    targetCol = c;
                    break;
                }
            }
            if (targetLabel != null) break;
        }

        // swap letters if dropped on another cell
        if (targetLabel != null && !targetLabel.getText().equals("?")) {
            String tempText = draggedLabel.getText();
            draggedLabel.setText(targetLabel.getText());
            targetLabel.setText(tempText);
        }

        // update styles
        updateLabelStyle(draggedLabel, sourceRow, sourceCol);
        if (targetLabel != null) {
            updateLabelStyle(targetLabel, targetRow, targetCol);
        }

        draggedLabel = null;
        sourceRow = -1;
        sourceCol = -1;

        e.consume();
    }

    private void updateLabelStyle(Label label, int row, int col) {
        if (isHighlighted(row, col)) {
            label.setStyle(getHighlightedLabelStyle());
        } else {
            label.setStyle(getDefaultLabelStyle());
        }
    }

    // loads a puzzle by index
    private void loadPuzzle(int index) {
        if (index >= 0 && index < puzzles.length) {
            currentPuzzleIndex = index;
            Puzzle p = puzzles[index];

            currentEncodedMessage = p.encoded;
            correctKey = p.key;
            hint1 = p.hint1;
            hint2 = p.hint2;
            decodedAnswer = p.decoded;

            // get the correct grid from backend
            List<Character> cleanKey = PlayfairDecrypt.CleanKey(correctKey);
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, "J");
            correctGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            encodedMessageLabel.setText("🔐 Encoded: " + currentEncodedMessage);

            // create input fields for the decoded answer
            createDecodedFields(decodedAnswer.length());

            // clear any highlighted cells
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    highlightedCells[row][col] = false;
                }
            }
        }
    }

    // creates the right number of text fields for the decoded answer
    private void createDecodedFields(int length) {
        decodedInputBox.getChildren().clear();
        decodedFields = new TextField[length];

        for (int i = 0; i < length; i++) {
            TextField field = new TextField();
            field.setPrefWidth(50);
            field.setMaxWidth(50);
            field.setPrefHeight(50);
            field.setAlignment(Pos.CENTER);
            field.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            field.setStyle(
                    "-fx-border-color: #27AE60; " +
                            "-fx-border-width: 2; " +
                            "-fx-background-color: white; " +
                            "-fx-text-fill: #2C3E50; " +
                            "-fx-background-radius: 5; " +
                            "-fx-border-radius: 5;"
            );
            decodedFields[i] = field;
            decodedInputBox.getChildren().add(field);
        }
    }

    private void setupActions() {
        submitKeyBtn.setOnAction(e -> checkKey());
        hint2Btn.setOnAction(e -> showHint2());
        checkGridBtn.setOnAction(e -> checkGrid());
        submitDecodedBtn.setOnAction(e -> checkDecoded());
        newChallengeBtn.setOnAction(e -> nextPuzzle());
    }

    // checks if user's key guess is correct
    private void checkKey() {
        String guess = keyInput.getText().trim().toUpperCase().replaceAll("\\s+", "");

        if (guess.equals(correctKey)) {
            currentPhase = 3;
            updatePhase(3);
        } else {
            if (!hint2Shown) {
                hint2Btn.setVisible(true);
            }
            showAlert("❌ Incorrect Key", "Try again or click HINT 2 for another clue!");
        }
    }

    // shows the second hint
    private void showHint2() {
        hintLabel.setText("📌 Hint 1: " + hint1 + "\n📌 Hint 2: " + hint2);
        hint2Btn.setVisible(false);
        hint2Shown = true;
        showAlert("🔍 Hint 2 Revealed", "Use this hint to guess the key!");
    }

    // updates the UI based on what phase the user is in
    private void updatePhase(int phase) {
        currentPhase = phase;

        Label dragInstruction = (Label)((VBox)gridPane.getParent()).getChildren().get(1);

        switch(phase) {
            case 1:  // guessing the key
                instructionLabel.setText("1. Guess the cipher key using the hints below");
                instructionLabel.setStyle("-fx-text-fill: #2C3E50; -fx-background-color: #EBF5FB; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #3498DB;");
                hintLabel.setText("📌 Hint 1: " + hint1);
                keyInput.clear();
                keyInput.setDisable(false);
                submitKeyBtn.setDisable(false);
                hint2Btn.setVisible(false);
                checkGridBtn.setVisible(false);
                submitDecodedBtn.setVisible(false);
                newChallengeBtn.setVisible(false);
                decodedInputBox.setVisible(false);
                dragInstruction.setVisible(false);
                hint2Shown = false;

                // blank grid
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        gridLabels[row][col].setText("?");
                        gridLabels[row][col].setStyle(getDefaultLabelStyle());
                    }
                }
                break;

            case 3:  // arranging the grid
                instructionLabel.setText("2. Drag and drop letters to arrange the grid correctly");
                instructionLabel.setStyle("-fx-text-fill: #2C3E50; -fx-background-color: #D5F5E3; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #27AE60;");
                hintLabel.setText("✅ Key accepted! Now arrange the grid correctly.");
                keyInput.setDisable(true);
                submitKeyBtn.setDisable(true);
                hint2Btn.setVisible(false);
                checkGridBtn.setVisible(true);
                dragInstruction.setVisible(true);

                // show the correct letters but they can be rearranged
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        gridLabels[row][col].setText(String.valueOf(correctGrid[row][col]));
                        gridLabels[row][col].setStyle(getDefaultLabelStyle());
                    }
                }
                break;

            case 4:  // decoding
                instructionLabel.setText("3. Use the YELLOW highlighted pairs to decode the " + decodedAnswer.length() + "-letter message");
                instructionLabel.setStyle("-fx-text-fill: #2C3E50; -fx-background-color: #FDEBD0; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #E67E22;");
                hintLabel.setText("");

                checkGridBtn.setVisible(false);
                decodedInputBox.setVisible(true);
                submitDecodedBtn.setVisible(true);
                newChallengeBtn.setVisible(false);
                dragInstruction.setVisible(false);

                // reset grid styles
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        gridLabels[row][col].setStyle(getDefaultLabelStyle());
                    }
                }

                for (TextField field : decodedFields) {
                    field.clear();
                }

                highlightPairs();
                break;

            case 5:  // complete
                instructionLabel.setText("4. 🎉 Challenge Complete! Great job!");
                instructionLabel.setStyle("-fx-text-fill: #2C3E50; -fx-background-color: #E8DAEF; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #8E44AD;");
                hintLabel.setText("");

                decodedInputBox.setVisible(false);
                submitDecodedBtn.setVisible(false);
                newChallengeBtn.setVisible(true);
                dragInstruction.setVisible(false);
                showAlert("🎉 Success!", "You solved the puzzle!");
                break;
        }
    }

    // checks if the grid is arranged correctly
    private void checkGrid() {
        boolean isCorrect = true;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                String labelText = gridLabels[row][col].getText();
                String correctLetter = String.valueOf(correctGrid[row][col]);

                if (!labelText.equals(correctLetter)) {
                    isCorrect = false;
                    gridLabels[row][col].setStyle(
                            "-fx-background-color: #FFCDD2; " +
                                    "-fx-border-color: #E74C3C; " +
                                    "-fx-border-width: 2; " +
                                    "-fx-text-fill: #2C3E50; " +
                                    "-fx-alignment: center; " +
                                    "-fx-font-size: 24px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-border-radius: 5;"
                    );
                } else {
                    gridLabels[row][col].setStyle(
                            "-fx-background-color: #C8E6C9; " +
                                    "-fx-border-color: #27AE60; " +
                                    "-fx-border-width: 2; " +
                                    "-fx-text-fill: #2C3E50; " +
                                    "-fx-alignment: center; " +
                                    "-fx-font-size: 24px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-border-radius: 5;"
                    );
                }
            }
        }

        if (isCorrect) {
            showAlert("✅ Grid Correct!", "Moving to next phase...");
            updatePhase(4);
        } else {
            showAlert("❌ Incorrect Grid", "Some letters are in the wrong position. Keep trying!");
        }
    }

    // lights up the pairs from the encoded message
    private void highlightPairs() {
        String[] pairs = currentEncodedMessage.split(" ");

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                highlightedCells[row][col] = false;
            }
        }

        for (String pair : pairs) {
            if (pair.length() == 2) {
                int[] pos1 = findLetterPosition(pair.charAt(0));
                int[] pos2 = findLetterPosition(pair.charAt(1));

                if (pos1 != null && pos2 != null) {
                    highlightedCells[pos1[0]][pos1[1]] = true;
                    highlightedCells[pos2[0]][pos2[1]] = true;

                    gridLabels[pos1[0]][pos1[1]].setStyle(getHighlightedLabelStyle());
                    gridLabels[pos2[0]][pos2[1]].setStyle(getHighlightedLabelStyle());
                }
            }
        }
    }

    // finds where a letter is in the correct grid
    private int[] findLetterPosition(char letter) {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                if (correctGrid[row][col] == letter) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    // checks if the decoded message is correct
    private void checkDecoded() {
        StringBuilder userDecoded = new StringBuilder();
        for (TextField field : decodedFields) {
            if (!field.getText().trim().isEmpty()) {
                userDecoded.append(field.getText().trim().toUpperCase());
            }
        }

        String userInput = userDecoded.toString();

        if (userInput.equals(decodedAnswer)) {
            updatePhase(5);
        } else if (userInput.length() >= 4 && decodedAnswer.startsWith(userInput)) {
            showAlert("⚠️ Getting There", "You're on the right track! Keep going...");
        } else {
            showAlert("❌ Incorrect", "Decoded message is wrong. Try again using the highlighted pairs!");
        }
    }

    // moves to the next puzzle
    private void nextPuzzle() {
        int nextIndex = (currentPuzzleIndex + 1) % puzzles.length;
        puzzleButtons[nextIndex].setSelected(true);
        loadPuzzle(nextIndex);
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