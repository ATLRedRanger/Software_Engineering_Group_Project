package com.playfair.ui;

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

    private TextField keyField;
    private ComboBox<String> missingLetterCombo;
    private TextArea inputArea;
    private TextArea outputArea;
    private Label[][] gridLabels = new Label[5][5];

    private BorderPane mainLayout;
    private Stage primaryStage;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private ScrollPane homeContent;
    private VBox toolContent;
    private VBox helpContent;
    private StackPane contentArea;
    private LandingPage landingPage;

    private VBox cipherView;
    private VBox challengeView;
    private ChallengeMode challengeMode;

    private Label cipherToggle;
    private Label challengeToggle;

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

        Region background = new Region();
        background.setStyle("-fx-background-color: linear-gradient(to bottom right, #021F2C, #0A2A38, #021520);");
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

        Button minBtn = new Button("─");
        minBtn.getStyleClass().addAll("window-button");
        minBtn.setOnAction(e -> stage.setIconified(true));

        Button maxBtn = new Button("□");
        maxBtn.getStyleClass().addAll("window-button");
        maxBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("window-button", "window-close");
        closeBtn.setOnAction(e -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        controls.getChildren().addAll(spacer, minBtn, maxBtn, closeBtn);

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
        homeBtn.setOnAction(e -> { setActiveTab(homeBtn, tabButtons); showHome(); });

        Button toolBtn = new Button("TOOL");
        toolBtn.getStyleClass().add("tab-button");
        toolBtn.setOnAction(e -> { setActiveTab(toolBtn, tabButtons); showTool(); });

        Button helpBtn = new Button("HELP");
        helpBtn.getStyleClass().add("tab-button");
        helpBtn.setOnAction(e -> { setActiveTab(helpBtn, tabButtons); showHelp(); });

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
        topBar.setPadding(new Insets(10, 20, 20, 20));

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
        userInfoBox.setStyle("-fx-background-color: rgba(2, 31, 44, 0.5); -fx-background-radius: 30; -fx-padding: 8 18;");

        usernameLabel = new Label("Guest");
        usernameLabel.setStyle("-fx-text-fill: #E3F4EB; -fx-font-weight: bold;");
        streakLabel = new Label("🔥 0");
        streakLabel.setStyle("-fx-text-fill: #159398;");

        userInfoBox.getChildren().addAll(usernameLabel, streakLabel);
        topBar.getChildren().addAll(leftSpacer, toggleContainer, rightSpacer, userInfoBox);

        cipherView = createCipherView();
        challengeMode = new ChallengeMode();
        challengeView = challengeMode.getView();

        ScrollPane cipherScrollPane = new ScrollPane(cipherView);
        cipherScrollPane.setFitToWidth(true);
        cipherScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        toolContent.getChildren().addAll(topBar, cipherScrollPane);
    }

    private void showCipherMode() {
        toolContent.getChildren().remove(1);
        ScrollPane sp = new ScrollPane(cipherView);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        toolContent.getChildren().add(sp);
    }

    private void showChallengeMode() {
        toolContent.getChildren().remove(1);
        ScrollPane sp = new ScrollPane(challengeView);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        toolContent.getChildren().add(sp);
    }

    private void createHelpContent() {
        helpContent = new VBox(20);
        helpContent.setAlignment(Pos.TOP_CENTER);
        helpContent.setPadding(new Insets(40));
        helpContent.getStyleClass().add("glass-panel");

        Label helpTitle = new Label("Help & Instructions");
        helpTitle.getStyleClass().add("glass-title");

        Label instructions = new Label("1. Enter Key\n2. Select Missing Letter\n3. Generate Grid\n4. Type Message\n5. Encrypt (Shift Right/Down) or Decrypt (Shift Left/Up).");
        instructions.setStyle("-fx-text-fill: #BDC7D0;");
        helpContent.getChildren().addAll(helpTitle, instructions);
    }

    private void showHome() { contentArea.getChildren().setAll(homeContent); }
    private void showTool() { contentArea.getChildren().setAll(toolContent); }
    private void showHelp() { contentArea.getChildren().setAll(helpContent); }

    private VBox createCipherView() {
        VBox view = new VBox(20);
        view.setAlignment(Pos.TOP_CENTER);
        view.getStyleClass().add("glass-panel");

        HBox contentLayout = new HBox(20, createInputSection(), createGridSection(), createOutputSection());
        contentLayout.setAlignment(Pos.CENTER);

        view.getChildren().addAll(contentLayout, createButtonSection());
        return view;
    }

    private VBox createInputSection() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(280);
        vbox.getStyleClass().add("section-bg");

        keyField = new TextField("APPLE");
        keyField.getStyleClass().add("glass-input");

        missingLetterCombo = new ComboBox<>();
        missingLetterCombo.getItems().addAll("J", "X", "Q", "Y", "Z");
        missingLetterCombo.setValue("J");
        missingLetterCombo.getStyleClass().add("glass-combo");

        inputArea = new TextArea();
        inputArea.setPromptText("Enter message...");
        inputArea.getStyleClass().add("glass-text-area");

        Button generateBtn = new Button("Generate Grid");
        generateBtn.setMaxWidth(Double.MAX_VALUE);
        generateBtn.getStyleClass().add("glass-action-button");
        generateBtn.setOnAction(e -> generateGrid());

        vbox.getChildren().addAll(new Label("Cipher Key:"), keyField, new Label("Missing:"), missingLetterCombo, new Label("Message:"), inputArea, generateBtn);
        return vbox;
    }

    private VBox createGridSection() {
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.getStyleClass().add("section-bg");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8); gridPane.setVgap(8);
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
        vbox.getChildren().addAll(new Label("5x5 Cipher Grid"), gridPane);
        return vbox;
    }

    private VBox createOutputSection() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(320);
        vbox.getStyleClass().add("section-bg");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.getStyleClass().add("glass-text-area");

        vbox.getChildren().addAll(new Label("Results"), outputArea);
        return vbox;
    }

    private HBox createButtonSection() {
        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 0, 0, 0));

        Button encBtn = new Button("ENCRYPT");
        encBtn.setOnAction(e -> encryptMessage());

        Button decBtn = new Button("DECRYPT");
        decBtn.setOnAction(e -> decryptMessage());

        Button clrBtn = new Button("CLEAR");
        clrBtn.setOnAction(e -> clearAll());

        hbox.getChildren().addAll(encBtn, decBtn, clrBtn);
        hbox.getChildren().forEach(n -> ((Button)n).getStyleClass().add("glass-action-button"));
        return hbox;
    }

    private void generateGrid() {
        String key = keyField.getText().trim().toUpperCase();
        String missingLetter = missingLetterCombo.getValue();
        if (key.isEmpty()) return;

        try {
            List<Character> cleanKey = PlayfairDecrypt.CleanKey(key);
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, missingLetter);
            char[][] grid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    gridLabels[row][col].setText(String.valueOf(grid[row][col]));
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Grid generation failed.");
        }
    }

    // UPDATED: Logic specifically for Encrypt (+1 Shift)
    private void encryptMessage() {
        performCipherAction(1);
    }

    // UPDATED: Logic specifically for Decrypt (-1 Shift)
    private void decryptMessage() {
        performCipherAction(-1);
    }

    // Consistently handles both directions
    private void performCipherAction(int direction) {
        String message = inputArea.getText().trim().toUpperCase().replaceAll("[^A-Z]", "");
        if (message.isEmpty()) {
            showAlert("Error", "Please enter a message.");
            return;
        }

        try {
            String key = keyField.getText().trim().toUpperCase().replace(" ", "");
            String missingLetter = missingLetterCombo.getValue();

            List<Character> cleanKey = PlayfairDecrypt.CleanKey(key);
            String rearrangedAlpha = PlayfairDecrypt.RearrangeAlphabet(cleanKey, missingLetter);
            char[][] grid = PlayfairDecrypt.PopulateGrid(rearrangedAlpha);

            // Update Grid UI
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    gridLabels[row][col].setText(String.valueOf(grid[row][col]));
                }
            }

            // Standardize and Digram
            String fixedMessage = PlayfairDecrypt.ReplaceLettersInMessage(message, missingLetter);
            List<String> digrams = PlayfairDecrypt.DigramMessage(fixedMessage);

            // Call the shared process method with the direction (+1 or -1)
            String result = PlayfairDecrypt.ProcessMessage(digrams, grid, direction);

            // Format result
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < result.length(); i += 2) {
                formatted.append(result, i, Math.min(i + 2, result.length())).append(" ");
            }

            outputArea.setText((direction == 1 ? "Encrypted: " : "Decrypted: ") + formatted.toString().trim());

        } catch (Exception e) {
            showAlert("Error", "Action failed: " + e.getMessage());
        }
    }

    private void clearAll() {
        keyField.setText("APPLE");
        inputArea.clear();
        outputArea.clear();
        generateGrid();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}