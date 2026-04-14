package com.playfair.ui;

import com.playfair.backend.PlayfairDecrypt;
import javafx.application.Application;
import javafx.application.Platform;
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

    // UI components for cipher tool
    private TextField keyField;
    private ComboBox<String> missingLetterCombo;
    private TextArea inputArea;
    private TextArea outputArea;
    private Label[][] gridLabels = new Label[5][5];

    // Current grid state
    private char[][] currentGrid;

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

    // FIX: Reusable ScrollPanes — created once, never recreated
    private ScrollPane cipherScrollPane;
    private ScrollPane challengeScrollPane;

    // Toggle buttons
    private Label cipherToggle;
    private Label challengeToggle;

    // Username and Streak
    private HBox userInfoBox;
    private Label usernameLabel;
    private Label streakLabel;

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
                ((Button) node).getStyleClass().remove("tab-active");
            }
        }
        activeButton.getStyleClass().add("tab-active");
    }

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

    private void createToolContent() {
        toolContent = new VBox(15);
        toolContent.setAlignment(Pos.TOP_CENTER);
        toolContent.setPadding(new Insets(10));
        toolContent.getStyleClass().add("glass-panel");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10, 20, 10, 20));
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

        userInfoBox.setOnMouseEntered(e ->
                userInfoBox.setStyle("-fx-background-color: rgba(21, 147, 152, 0.2); -fx-background-radius: 30; -fx-padding: 8 18; -fx-border-color: rgba(21, 147, 152, 0.5); -fx-border-width: 1; -fx-border-radius: 30;")
        );
        userInfoBox.setOnMouseExited(e ->
                userInfoBox.setStyle("-fx-background-color: rgba(2, 31, 44, 0.5); -fx-background-radius: 30; -fx-padding: 8 18; -fx-border-color: rgba(21, 147, 152, 0.3); -fx-border-width: 1; -fx-border-radius: 30;")
        );
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

        cipherScrollPane = buildScrollPane(cipherView);
        challengeScrollPane = buildScrollPane(challengeView);

        challengeMode.setScrollToTopAction(() ->
                Platform.runLater(() -> challengeScrollPane.setVvalue(0.0))
        );

        toolContent.getChildren().addAll(topBar, cipherScrollPane);
    }

    private ScrollPane buildScrollPane(javafx.scene.Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private void showCipherMode() {
        replaceContentPane(cipherScrollPane);
        Platform.runLater(() -> cipherScrollPane.setVvalue(0.0));
    }

    private void showChallengeMode() {
        replaceContentPane(challengeScrollPane);
        Platform.runLater(() -> challengeScrollPane.setVvalue(0.0));
    }

    private void replaceContentPane(ScrollPane newPane) {
        if (toolContent.getChildren().size() > 1) {
            toolContent.getChildren().set(1, newPane);
        } else {
            toolContent.getChildren().add(newPane);
        }
    }

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
                        "2. Select a missing letter (usually J)\n" +
                        "3. Click 'Generate Grid'\n" +
                        "4. Type your message\n" +
                        "5. Click 'ENCRYPT' or 'DECRYPT'\n\n" +
                        "Note: Encryption is coming soon!"
        );
        instructions.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 14px; -fx-wrap-text: true;");

        helpContent.getChildren().addAll(helpTitle, instructions);
    }

    private void showHome() {
        contentArea.getChildren().setAll(homeContent);
    }

    private void showTool() {
        contentArea.getChildren().setAll(toolContent);
    }

    private void showHelp() {
        contentArea.getChildren().setAll(helpContent);
    }

    private VBox createCipherView() {
        VBox view = new VBox(20);
        view.setAlignment(Pos.TOP_CENTER);
        view.getStyleClass().add("glass-panel");

        HBox contentLayout = new HBox(20);
        contentLayout.setAlignment(Pos.CENTER);

        VBox inputSection = createInputSection();
        VBox gridSection = createGridSection();
        VBox outputSection = createOutputSection();

        contentLayout.getChildren().addAll(inputSection, gridSection, outputSection);
        HBox buttonSection = createButtonSection();

        view.getChildren().addAll(contentLayout, buttonSection);
        return view;
    }

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
        generateBtn.getStyleClass().add("glass-action-button");
        generateBtn.setOnAction(e -> generateGrid());

        vbox.getChildren().addAll(
                keyLabel, keyField,
                missingLabel, missingLetterCombo,
                messageLabel, inputArea,
                generateBtn
        );

        return vbox;
    }

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

    private HBox createButtonSection() {
        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 0, 0, 0));

        Button encryptBtn = new Button("ENCRYPT");
        encryptBtn.setPrefWidth(110);
        encryptBtn.getStyleClass().add("glass-action-button");
        encryptBtn.setOnAction(e -> encryptMessage());

        Button decryptBtn = new Button("DECRYPT");
        decryptBtn.setPrefWidth(110);
        decryptBtn.getStyleClass().add("glass-action-button");
        decryptBtn.setOnAction(e -> decryptMessage());

        Button clearBtn = new Button("CLEAR");
        clearBtn.setPrefWidth(110);
        clearBtn.getStyleClass().add("glass-action-button");
        clearBtn.setOnAction(e -> clearAll());

        hbox.getChildren().addAll(encryptBtn, decryptBtn, clearBtn);
        return hbox;
    }

    // ========== BACKEND METHOD CALLS ONLY ==========
    // These methods ONLY call PlayfairDecrypt methods

    private void generateGrid() {
        String key = keyField.getText().trim().toUpperCase().replaceAll("\\s+", "");
        String missingLetter = missingLetterCombo.getValue();

        if (key.isEmpty() || missingLetter == null) {
            showAlert("Error", "Please enter a key and select a missing letter");
            return;
        }

        try {
            List<Character> cleanKey = PlayfairDecrypt.CleanKey(key);
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, missingLetter);
            currentGrid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    gridLabels[row][col].setText(String.valueOf(currentGrid[row][col]));
                }
            }

            outputArea.setText("Grid generated successfully\nKey: " + key + "\nMissing letter: " + missingLetter);

        } catch (Exception e) {
            showAlert("Error", "Error generating grid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void encryptMessage() {
        String message = inputArea.getText().trim().toUpperCase().replaceAll("[^A-Z]", "");

        if (message.isEmpty()) {
            showAlert("Error", "Please enter a message to encrypt");
            return;
        }

        if (currentGrid == null) {
            showAlert("Error", "Please generate a grid first");
            return;
        }

        // NEEDS EncryptMessage() - currently not available
        outputArea.setText("⚠️ Encryption: needs EncryptMessage() method to PlayfairDecrypt");
    }

    private void decryptMessage() {
        String message = inputArea.getText().trim().toUpperCase().replaceAll("\\s+", "").replaceAll("[^A-Z]", "");

        if (message.isEmpty()) {
            showAlert("Error", "Please enter a message to decrypt");
            return;
        }

        if (currentGrid == null) {
            showAlert("Error", "Please generate a grid first");
            return;
        }

        try {
            List<String> digrams = PlayfairDecrypt.DigramMessage(message);
            String decrypted = PlayfairDecrypt.DecryptMessage(digrams, currentGrid);

            StringBuilder formattedDecrypted = new StringBuilder();
            for (int i = 0; i < decrypted.length(); i += 2) {
                if (i + 2 <= decrypted.length()) {
                    formattedDecrypted.append(decrypted, i, i + 2).append(" ");
                } else {
                    formattedDecrypted.append(decrypted.substring(i));
                }
            }

            outputArea.setText("Decrypted: " + formattedDecrypted.toString().trim());

        } catch (Exception e) {
            showAlert("Error", "Decryption failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearAll() {
        keyField.setText("APPLE");
        missingLetterCombo.setValue("J");
        inputArea.clear();
        outputArea.clear();
        generateGrid();
    }

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
