package com.playfair.ui;

import com.playfair.backend.CipherGrid;
import com.playfair.backend.PlayfairDecrypt;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.List;

public class PlayfairUI extends Application {

    // UI  for the main cipher tool
    private TextField keyField;
    private ComboBox<String> missingLetterCombo;
    private TextArea inputArea;
    private TextArea outputArea;
    private Label[][] gridLabels = new Label[5][5];

    // Navigation
    private BorderPane mainLayout;
    private Stage primaryStage;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    // Tab content containers
    private ScrollPane homeContent;
    private VBox toolContent;
    private VBox helpContent;
    private StackPane contentArea;
    private LandingPage landingPage;

    // Tool tab subviews
    private VBox cipherView;
    private VBox challengeView;
    private ChallengeMode challengeMode;

    // Toggle buttons
    private Label cipherToggle;
    private Label challengeToggle;

    // Username and Streak
    private HBox userInfoBox;
    private Label usernameLabel;
    private Label streakLabel;
   
//Application entry point
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Playfair Cipher");

        StackPane root = new StackPane();
        root.setAlignment(Pos.TOP_LEFT);

        root.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });
        root.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                stage.setMaximized(!stage.isMaximized());
            }
        });

        Region background = new Region();
        background.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #021F2C, #0A2A38, #021520);"
        );
        background.setPrefSize(1200, 800);

        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20, 20, 20, 20));
        mainLayout.getStyleClass().add("glass-panel");

        HBox windowControls = createWindowControls(stage);
        HBox navBar = createNavigationBar();

        createHomeContent();
        createToolContent();
        createHelpContent();

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        VBox topSection = new VBox(5);
        topSection.setAlignment(Pos.TOP_CENTER);
        topSection.getChildren().addAll(windowControls, navBar);
        mainLayout.setTop(topSection);
        mainLayout.setCenter(contentArea);

        showHome();

        root.getChildren().addAll(background, mainLayout);

        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/com/playfair/ui/glass-style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        generateGrid();
    }

   // WINDOW CONTROLS (Minimize, Maximize, Close)
    private HBox createWindowControls(Stage stage) {
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_RIGHT);
        controls.setPadding(new Insets(10, 20, 0, 0));
        controls.setMaxWidth(Double.MAX_VALUE);

        Button minBtn = new Button("─");
        minBtn.getStyleClass().addAll("window-button");
        minBtn.setOnAction(e -> stage.setIconified(true));

        Button maxBtn = new Button("□");
        maxBtn.getStyleClass().addAll("window-button");
        maxBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("window-button", "window-close");
        closeBtn.setOnAction(e -> stage.close());

        controls.getChildren().addAll(minBtn, maxBtn, closeBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        controls.getChildren().add(0, spacer);

        return controls;
    }

    // NAVIGATION BAR (App Title + Home/Tool/Help Tabs)
    private HBox createNavigationBar() {
        HBox navBar = new HBox();
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setSpacing(20);
        navBar.getStyleClass().add("nav-bar");

        Label appTitle = new Label("Playfair Cipher");
        appTitle.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tabButtons = new HBox(15);
        tabButtons.setAlignment(Pos.CENTER_RIGHT);

        Button homeBtn = new Button("HOME");
        homeBtn.getStyleClass().addAll("tab-button", "tab-active");
        homeBtn.setOnAction(e -> {
            setActiveTab(homeBtn, tabButtons);
            showHome();
        });

        Button toolBtn = new Button("TOOL");
        toolBtn.getStyleClass().add("tab-button");
        toolBtn.setOnAction(e -> {
            setActiveTab(toolBtn, tabButtons);
            showTool();
        });

        Button helpBtn = new Button("HELP");
        helpBtn.getStyleClass().add("tab-button");
        helpBtn.setOnAction(e -> {
            setActiveTab(helpBtn, tabButtons);
            showHelp();
        });

        tabButtons.getChildren().addAll(homeBtn, toolBtn, helpBtn);
        navBar.getChildren().addAll(appTitle, spacer, tabButtons);

        return navBar;
    }

    private void setActiveTab(Button activeButton, HBox tabButtons) {
        for (javafx.scene.Node node : tabButtons.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.getStyleClass().remove("tab-active");
            }
        }
        activeButton.getStyleClass().add("tab-active");
    }
 // Creates LandingPage and sets action for Enter button to switch to Tool tab
    private void createHomeContent() {
        landingPage = new LandingPage();
        landingPage.setOnEnterAction(() -> {
            VBox topSection = (VBox) mainLayout.getTop();
            HBox navBar = (HBox) topSection.getChildren().get(1);
            HBox tabButtons = (HBox) navBar.getChildren().get(2);
            Button toolBtn = (Button) tabButtons.getChildren().get(1);
            setActiveTab(toolBtn, tabButtons);
            showTool();
        });
        homeContent = landingPage.getView();
    }


// Creates top bar with centered toggle and right-aligned user info
        // CIPHER MODE and CHALLENGE MODE toggle buttons
        // Wraps Cipher Mode in ScrollPane
   
    private void createToolContent() {
        toolContent = new VBox(15);
        toolContent.setAlignment(Pos.TOP_CENTER);
        toolContent.setPadding(new Insets(10));
        toolContent.getStyleClass().add("glass-panel");

        
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10, 20, 20, 20));
        topBar.setMaxWidth(Double.MAX_VALUE);

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        HBox toggleContainer = new HBox(4);
        toggleContainer.setAlignment(Pos.CENTER);
        toggleContainer.getStyleClass().add("toggle-container");
        toggleContainer.setTranslateX(80); 

        cipherToggle = new Label("CIPHER MODE");
        cipherToggle.getStyleClass().addAll("toggle-option", "selected");
        cipherToggle.setOnMouseClicked(e -> {
            cipherToggle.getStyleClass().add("selected");
            challengeToggle.getStyleClass().remove("selected");
            showCipherMode();
        });

        challengeToggle = new Label("CHALLENGE MODE");
        challengeToggle.getStyleClass().add("toggle-option");
        challengeToggle.setOnMouseClicked(e -> {
            challengeToggle.getStyleClass().add("selected");
            cipherToggle.getStyleClass().remove("selected");
            showChallengeMode();
        });

        toggleContainer.getChildren().addAll(cipherToggle, challengeToggle);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        userInfoBox = new HBox(12);
        userInfoBox.setAlignment(Pos.CENTER_RIGHT);
        userInfoBox.setStyle("-fx-background-color: rgba(2, 31, 44, 0.5); -fx-background-radius: 30; -fx-padding: 8 18; -fx-border-color: rgba(21, 147, 152, 0.3); -fx-border-width: 1; -fx-border-radius: 30;");
        userInfoBox.setCursor(javafx.scene.Cursor.HAND);

        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 14px;");

        usernameLabel = new Label("Guest");
        usernameLabel.setStyle("-fx-text-fill: #E3F4EB; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label separator = new Label("|");
        separator.setStyle("-fx-text-fill: #159398; -fx-font-size: 14px;");

        streakLabel = new Label("🔥 0");
        streakLabel.setStyle("-fx-text-fill: #159398; -fx-font-weight: bold; -fx-font-size: 14px;");

        userInfoBox.getChildren().addAll(userIcon, usernameLabel, separator, streakLabel);

        userInfoBox.setOnMouseEntered(e -> {
            userInfoBox.setStyle("-fx-background-color: rgba(21, 147, 152, 0.2); -fx-background-radius: 30; -fx-padding: 8 18; -fx-border-color: rgba(21, 147, 152, 0.5); -fx-border-width: 1; -fx-border-radius: 30;");
        });
        userInfoBox.setOnMouseExited(e -> {
            userInfoBox.setStyle("-fx-background-color: rgba(2, 31, 44, 0.5); -fx-background-radius: 30; -fx-padding: 8 18; -fx-border-color: rgba(21, 147, 152, 0.3); -fx-border-width: 1; -fx-border-radius: 30;");
        });

        userInfoBox.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Info");
            alert.setHeaderText(null);
            alert.setContentText("Username: Guest\nStreak: 0\n\nDatabase connection will be added later.");
            alert.showAndWait();
        });

        topBar.getChildren().addAll(leftSpacer, toggleContainer, rightSpacer, userInfoBox);

        cipherView = createCipherView();
        challengeMode = new ChallengeMode();
        challengeView = challengeMode.getView();

       //scrollpane for cipher mode
        
        ScrollPane cipherScrollPane = new ScrollPane();
        cipherScrollPane.setContent(cipherView);
        cipherScrollPane.setFitToWidth(true);
        cipherScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        cipherScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cipherScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        toolContent.getChildren().addAll(topBar, cipherScrollPane);
    }

    private void showCipherMode() {
        toolContent.getChildren().remove(1);
        
        ScrollPane cipherScrollPane = new ScrollPane();
        cipherScrollPane.setContent(cipherView);
        cipherScrollPane.setFitToWidth(true);
        cipherScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        cipherScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cipherScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        toolContent.getChildren().add(cipherScrollPane);
    }

    private void showChallengeMode() {
        toolContent.getChildren().remove(1);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(challengeView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        toolContent.getChildren().add(scrollPane);
    }


    // Shows how to use Cipher Mode and Challenge Mode
        // Explains Playfair cipher rules
   
    private void createHelpContent() {
        helpContent = new VBox(20);
        helpContent.setAlignment(Pos.TOP_CENTER);
        helpContent.setPadding(new Insets(40));
        helpContent.getStyleClass().add("glass-panel");

        Label helpTitle = new Label("Help & Instructions");
        helpTitle.getStyleClass().add("glass-title");

        Label instructions = new Label(
                "How to Use the Playfair Cipher Tool:\n\n" +
                        "1. Enter a cipher key (e.g., PLAYFAIR, APPLE)\n" +
                        "2. Select a missing letter (usually J - I and J share a cell)\n" +
                        "3. Click 'Generate Grid' to create the 5x5 cipher matrix\n" +
                        "4. Type your message in the Message box\n" +
                        "5. Click 'ENCRYPT' to encode or 'DECRYPT' to decode\n\n" +
                        "Challenge Mode:\n" +
                        "- Guess the cipher key using the hints\n" +
                        "- Drag and drop letters to arrange the grid correctly\n" +
                        "- Use highlighted pairs to decode the message\n" +
                        "- You have 3 attempts before the answer is revealed\n\n" +
                        "Playfair Cipher Rules:\n" +
                        "- Same row: Shift left (decrypt) / right (encrypt)\n" +
                        "- Same column: Shift up (decrypt) / down (encrypt)\n" +
                        "- Different row/col: Swap corners of the rectangle"
        );
        instructions.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 14px; -fx-wrap-text: true;");

        helpContent.getChildren().addAll(helpTitle, instructions);
    }

    private void showHome() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeContent);
    }

    private void showTool() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(toolContent);
    }

    private void showHelp() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(helpContent);
    }

// Cipher Mode UI Threecolumn layout: Input, Grid , Output
        // Bottom row: ENCRYPT, DECRYPT, CLEAR buttons
   
    private VBox createCipherView() {
        VBox cipherView = new VBox(20);
        cipherView.setAlignment(Pos.TOP_CENTER);
        cipherView.getStyleClass().add("glass-panel");

        HBox contentLayout = new HBox(20);
        contentLayout.setAlignment(Pos.CENTER);

        VBox inputSection = createInputSection();
        VBox gridSection = createGridSection();
        VBox outputSection = createOutputSection();

        contentLayout.getChildren().addAll(inputSection, gridSection, outputSection);
        HBox buttonSection = createButtonSection();

        cipherView.getChildren().addAll(contentLayout, buttonSection);
        return cipherView;
    }

 // Cipher Key text field
        // Missing Letter dropdown (J, X, Q, Y, Z)
        // Message text area
        // Generate Grid button

   
    private VBox createInputSection() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(280);
        vbox.getStyleClass().add("section-bg");

        Label keyLabel = new Label("Cipher Key:");
        keyLabel.getStyleClass().add("glass-subtitle");

        keyField = new TextField();
        keyField.setPromptText("Enter key (e.g., APPLE, PLAYFAIR)");
        keyField.setText("APPLE");
        keyField.getStyleClass().add("glass-input");

        Label missingLabel = new Label("Missing Letter:");
        missingLabel.getStyleClass().add("glass-subtitle");

        missingLetterCombo = new ComboBox<>();
        missingLetterCombo.getItems().addAll("J", "X", "Q", "Y", "Z");
        missingLetterCombo.setValue("J");
        missingLetterCombo.setPrefWidth(100);
        missingLetterCombo.getStyleClass().add("glass-combo");

        Label messageLabel = new Label("Message:");
        messageLabel.getStyleClass().add("glass-subtitle");

        inputArea = new TextArea();
        inputArea.setPromptText("Enter message to encrypt/decrypt");
        inputArea.setPrefRowCount(5);
        inputArea.setWrapText(true);
        inputArea.getStyleClass().add("glass-text-area");

        Button generateBtn = new Button("Generate Grid");
        generateBtn.setMaxWidth(Double.MAX_VALUE);
        generateBtn.getStyleClass().addAll("glass-action-button");
        generateBtn.setOnAction(e -> generateGrid());

        vbox.getChildren().addAll(
                keyLabel, keyField,
                missingLabel, missingLetterCombo,
                messageLabel, inputArea,
                generateBtn
        );

        return vbox;
    }

// 5x5 grid of glass cells
   // Each cell starts with "?" until grid is generated
   
    private VBox createGridSection() {
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.getStyleClass().add("section-bg");

        Label gridTitle = new Label("5x5 Cipher Grid");
        gridTitle.getStyleClass().add("glass-subtitle");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setAlignment(Pos.CENTER);

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Label label = new Label("?");
                label.setMinSize(70, 70);
                label.getStyleClass().add("glass-cell");

                gridLabels[row][col] = label;
                gridPane.add(label, col, row);
            }
        }

        vbox.getChildren().addAll(gridTitle, gridPane);
        return vbox;
    }

   // Read-only text area showing encryption/decryption results
    private VBox createOutputSection() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(320);
        vbox.getStyleClass().add("section-bg");

        Label outputTitle = new Label("Results");
        outputTitle.getStyleClass().add("glass-subtitle");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(10);
        outputArea.setWrapText(true);
        outputArea.setPromptText("Result will appear here...");
        outputArea.getStyleClass().add("glass-text-area");

        vbox.getChildren().addAll(outputTitle, outputArea);
        return vbox;
    }

// ENCRYPT button (placeholder - need backend)
        // DECRYPT button (working)
        // CLEAR button (resets all fields)
   
    private HBox createButtonSection() {
        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 0, 0, 0));

        Button encryptBtn = new Button("ENCRYPT");
        encryptBtn.setPrefWidth(110);
        encryptBtn.getStyleClass().addAll("glass-action-button");
        encryptBtn.setOnAction(e -> encryptMessage());

        Button decryptBtn = new Button("DECRYPT");
        decryptBtn.setPrefWidth(110);
        decryptBtn.getStyleClass().addAll("glass-action-button");
        decryptBtn.setOnAction(e -> decryptMessage());

        Button clearBtn = new Button("CLEAR");
        clearBtn.setPrefWidth(110);
        clearBtn.getStyleClass().addAll("glass-action-button");
        clearBtn.setOnAction(e -> clearAll());

        hbox.getChildren().addAll(encryptBtn, decryptBtn, clearBtn);
        return hbox;
    }

   // GRID GENERATION (Calls Backend)
   // Takes key and missing letter, calls CipherGrid methods
        // Updates the 5x5 grid display
   
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
            List<Character> cleanKey = CipherGrid.CleanKey(key);
            String rearrangedAlpha = CipherGrid.RearrangeAlphabet(cleanKey, missingLetter);
            CipherGrid.PopulateGrid(rearrangedAlpha);
            String[][] grid = CipherGrid.getGrid();

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

// ENCRYPT MESSAGE (Placeholder - need Backend)
   
    private void encryptMessage() {
        String message = inputArea.getText().trim().toUpperCase();
        if (message.isEmpty()) {
            showAlert("Error", "Please enter a message to encrypt");
            return;
        }

        message = message.replaceAll("[^A-Z]", "");
        outputArea.setText("Encrypted: " + message);
    }

// Calls PlayfairDecrypt methods to decrypt the message
        // Formats output with spaces every 2 letters

   
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

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    gridLabels[row][col].setText(String.valueOf(grid[row][col]));
                }
            }

            List<String> digrams = PlayfairDecrypt.DigramMessage(processedMessage);
            String decrypted = PlayfairDecrypt.DecryptMessage(digrams, grid);

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

  // CLEAR ALL FIELDS
   
    private void clearAll() {
        keyField.setText("APPLE");
        missingLetterCombo.setValue("J");
        inputArea.clear();
        outputArea.clear();
        generateGrid();
    }

   // ALERT DIALOG
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
