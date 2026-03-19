/*
 * Jessica DeWitt - UI/Front-end Development
 * Main UI for the Playfair Cipher app - handles both cipher tool and challenge mode
 */
package com.playfair.ui;

import com.playfair.backend.CipherGrid;
import com.playfair.backend.PlayfairDecrypt;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.List;

public class PlayfairUI extends Application {

    // UI stuff for the main cipher tool
    private TextField keyField;              // where user types the cipher key
    private ComboBox<String> missingLetterCombo;  // dropdown for picking which letter to leave out
    private TextArea inputArea;              // message to encrypt/decrypt goes here
    private TextArea outputArea;              // shows results
    private Label[][] gridLabels = new Label[5][5];  // the 5x5 grid display

    // navigation between modes
    private BorderPane mainLayout;
    private VBox cipherView;
    private VBox challengeView;
    private ChallengeMode challengeMode;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Playfair Cipher");

        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");

        // buttons to switch between cipher tool and challenge mode
        HBox navBar = createNavigationBar();
        mainLayout.setTop(navBar);

        // set up both modes
        cipherView = createCipherView();
        challengeMode = new ChallengeMode();
        challengeView = challengeMode.getView();

        // start with the main cipher tool
        mainLayout.setCenter(cipherView);

        Scene scene = new Scene(mainLayout, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.show();

        // show default grid on startup
        generateGrid();
    }

    // creates the two buttons at the top for switching modes
    private HBox createNavigationBar() {
        HBox navBar = new HBox(10);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(0, 0, 20, 0));

        Button cipherBtn = new Button("CIPHER MODE");
        cipherBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        cipherBtn.setOnAction(e -> mainLayout.setCenter(cipherView));

        Button challengeBtn = new Button("CHALLENGE MODE");
        challengeBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        challengeBtn.setOnAction(e -> {
            challengeMode = new ChallengeMode(); // fresh start each time
            challengeView = challengeMode.getView();

            // make it scrollable in case the content gets long
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(challengeView);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #f0f0f0; -fx-background-color: #f0f0f0;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            mainLayout.setCenter(scrollPane);
        });

        navBar.getChildren().addAll(cipherBtn, challengeBtn);
        return navBar;
    }

    // builds the main cipher tool screen
    private VBox createCipherView() {
        VBox cipherView = new VBox(20);
        cipherView.setAlignment(Pos.TOP_CENTER);

        // title at the top
        HBox titleSection = new HBox();
        titleSection.setAlignment(Pos.CENTER);
        Label title = new Label("Playfair Cipher");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleSection.getChildren().add(title);

        // three column layout - inputs, grid, results
        HBox contentLayout = new HBox(20);
        contentLayout.setAlignment(Pos.CENTER);

        VBox inputSection = createInputSection();
        VBox gridSection = createGridSection();
        VBox outputSection = createOutputSection();

        contentLayout.getChildren().addAll(inputSection, gridSection, outputSection);

        // action buttons at the bottom
        HBox buttonSection = createButtonSection();

        cipherView.getChildren().addAll(titleSection, contentLayout, buttonSection);
        return cipherView;
    }

    // left panel - where user enters key, missing letter, and message
    private VBox createInputSection() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(250);
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 15;");

        Label keyLabel = new Label("Cipher Key:");
        keyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        keyField = new TextField();
        keyField.setPromptText("Enter key (e.g., APPLE, PLAYFAIR)");
        keyField.setText("APPLE");

        Label missingLabel = new Label("Missing Letter:");
        missingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        missingLetterCombo = new ComboBox<>();
        missingLetterCombo.getItems().addAll("J", "X", "Q", "Y", "Z");
        missingLetterCombo.setValue("J");
        missingLetterCombo.setPrefWidth(80);

        Label messageLabel = new Label("Message:");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        inputArea = new TextArea();
        inputArea.setPromptText("Enter message to encrypt/decrypt");
        inputArea.setPrefRowCount(5);
        inputArea.setWrapText(true);

        Button generateBtn = new Button("Generate Grid");
        generateBtn.setMaxWidth(Double.MAX_VALUE);
        generateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        generateBtn.setOnAction(e -> generateGrid());

        vbox.getChildren().addAll(
                keyLabel, keyField,
                missingLabel, missingLetterCombo,
                messageLabel, inputArea,
                generateBtn
        );

        return vbox;
    }

    // center panel - shows the 5x5 grid
    private VBox createGridSection() {
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);

        Label gridTitle = new Label("5x5 Cipher Grid");
        gridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setAlignment(Pos.CENTER);

        // create all 25 cells, start with question marks
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Label label = new Label("?");
                label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                label.setMinSize(70, 70);
                label.setStyle(
                        "-fx-border-color: black;" +
                                "-fx-border-width: 2;" +
                                "-fx-background-color: white;" +
                                "-fx-alignment: center;"
                );

                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        vbox.getChildren().addAll(gridTitle, gridPane);
        return vbox;
    }

    // right panel - shows encryption/decryption results
    private VBox createOutputSection() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(300);
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 15;");

        Label outputTitle = new Label("Results");
        outputTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label resultLabel = new Label("Processed Message:");
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(10);
        outputArea.setWrapText(true);
        outputArea.setPromptText("Result will appear here...");

        vbox.getChildren().addAll(outputTitle, resultLabel, outputArea);

        return vbox;
    }

    // bottom buttons for encrypt/decrypt/clear
    private HBox createButtonSection() {
        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 0, 0, 0));

        Button encryptBtn = new Button("ENCRYPT");
        encryptBtn.setPrefWidth(100);
        encryptBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        encryptBtn.setOnAction(e -> encryptMessage());

        Button decryptBtn = new Button("DECRYPT");
        decryptBtn.setPrefWidth(100);
        decryptBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        decryptBtn.setOnAction(e -> decryptMessage());

        Button clearBtn = new Button("CLEAR");
        clearBtn.setPrefWidth(100);
        clearBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        clearBtn.setOnAction(e -> clearAll());

        hbox.getChildren().addAll(encryptBtn, decryptBtn, clearBtn);

        return hbox;
    }

    // updates the grid when user changes key or missing letter
    private void generateGrid() {
        String key = keyField.getText().trim().toUpperCase();
        String missingLetter = missingLetterCombo.getValue();

        if (key.isEmpty()) {
            showAlert("Error", "Please enter a key");
            return;
        }

        if (missingLetter == null || missingLetter.isEmpty()) {
            showAlert("Error", "Please select a missing letter");
            return;
        }

        try {
            // call backend to generate the grid
            List<Character> cleanKey = CipherGrid.CleanKey(key);
            String rearrangedAlpha = CipherGrid.RearrangeAlphabet(cleanKey, missingLetter);
            CipherGrid.PopulateGrid(rearrangedAlpha);
            String[][] grid = CipherGrid.getGrid();

            // update the display
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    if (grid[row][col] != null) {
                        gridLabels[row][col].setText(grid[row][col]);
                    }
                }
            }

            outputArea.setText("Grid generated successfully\nKey: " + key + "\nMissing letter: " + missingLetter);

        } catch (Exception e) {
            showAlert("Error", "Error generating grid: " + e.getMessage());
        }
    }

    // placeholder for now and will encrypt when backend is there.
    private void encryptMessage() {
        String message = inputArea.getText().trim().toUpperCase();
        if (message.isEmpty()) {
            showAlert("Error", "Please enter a message to encrypt");
            return;
        }

        message = message.replaceAll("[^A-Z]", "");
        outputArea.setText("Encrypted: " + message);
    }

    // decrypts message using the PlayfairDecrypt backend
    private void decryptMessage() {
        String message = inputArea.getText().trim().toUpperCase();
        if (message.isEmpty()) {
            showAlert("Error", "Please enter a message to decrypt");
            return;
        }

        String processedMessage = message.replaceAll("\\s+", "").replaceAll("[^A-Z]", "");

        try {
            String key = keyField.getText().trim().toUpperCase();
            String missingLetter = missingLetterCombo.getValue();
            key = key.replaceAll("\\s+", "");

            List<Character> cleanKey = PlayfairDecrypt.CleanKey(key);
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, missingLetter);
            char[][] grid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            // update grid display
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    gridLabels[row][col].setText(String.valueOf(grid[row][col]));
                }
            }

            List<String> digrams = PlayfairDecrypt.DigramMessage(processedMessage);
            String decrypted = PlayfairDecrypt.DecryptMessage(digrams, grid);

            // add spaces every 2 letters for readability
            String formattedDecrypted = "";
            for (int i = 0; i < decrypted.length(); i += 2) {
                if (i + 2 <= decrypted.length()) {
                    formattedDecrypted += decrypted.substring(i, i + 2) + " ";
                }
            }

            outputArea.setText("Decrypted: " + formattedDecrypted.trim());

        } catch (Exception e) {
            showAlert("Error", "Decryption failed: " + e.getMessage());
        }
    }

    // resets everything to default values
    private void clearAll() {
        keyField.setText("APPLE");
        missingLetterCombo.setValue("J");
        inputArea.clear();
        outputArea.clear();
        generateGrid();
    }

    // shows error messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}