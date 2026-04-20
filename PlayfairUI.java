

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
        // Adjust position horizontally
        toggleContainer.setTranslateX(10);


        toggleContainer.getChildren().addAll(cipherToggle, challengeToggle);


        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);


        // Guest and streak commented out
        topBar.getChildren().addAll(leftSpacer, toggleContainer, rightSpacer);


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
        helpTitle.setStyle("-fx-font-size: 24px;");


        String helpText =
                "What is the Playfair Cipher?\n\n" +
                        "The Playfair cipher is a digraph substitution cipher invented in 1854 by Charles Wheatstone.\n" +
                        "Unlike simple ciphers that encrypt single letters, Playfair encrypts pairs of letters (digraphs),\n" +
                        "making it much harder to break using traditional frequency analysis. It was used by the British military in\n" +
                        "World War I and by the United States Army during World War II.\n\n\n" +
                        "How to Use the Cipher Tool:\n\n" +
                        "1. Enter a cipher key (e.g., PLAYFAIR, APPLE)\n" +
                        "2. Select a missing letter (usually J)\n" +
                        "3. Click 'Generate Grid'\n" +
                        "4. Type your message\n" +
                        "5. Click 'ENCRYPT' or 'DECRYPT'\n\n" +
                        "Note: I and J share the same cell\n\n\n" +
                        "How to Play Challenge Mode:\n\n" +
                        "1. Click 'CHALLENGE MODE' toggle to switch to challenge mode\n" +
                        "2. Click 'START' to begin a new challenge\n" +
                        "3. You will see an encoded message and a cipher key\n" +
                        "4. Use the hints to figure out what the decoded message should be\n" +
                        "5. DRAG AND DROP letters on the grid to arrange it correctly based on the key\n" +
                        "6. Click 'CHECK GRID' to verify your arrangement\n" +
                        "   - You get 3 attempts before a 'SKIP TO DECODE' button appears\n" +
                        "7. Once the grid is correct or you skip, move to the decode phase\n" +
                        "8. Each pair in the encoded message will highlight in different colors on the grid\n" +
                        "9. Type the decoded message in the text box and click 'SUBMIT DECODED'\n" +
                        "   - You get 3 attempts before 'SHOW ANSWER' appears\n" +
                        "10. Click 'NEXT PUZZLE' to try another challenge!\n\n" +
                        "Tip: The colors on the grid help you see which letters belong to each encoded pair!\n";


        Label instructions = new Label(helpText);
        instructions.setStyle("-fx-text-fill: #BDC7D0; -fx-font-size: 16px; -fx-wrap-text: true;");
        instructions.setWrapText(true);


        ScrollPane scrollPane = new ScrollPane(instructions);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        helpContent.getChildren().addAll(helpTitle, scrollPane);
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


        Label keyLabel = new Label("Cipher Key:");
        keyLabel.getStyleClass().add("glass-subtitle");


        keyField = new TextField("APPLE");
        keyField.getStyleClass().add("glass-input");


        Label missingLabel = new Label("Missing Letter:");
        missingLabel.getStyleClass().add("glass-subtitle");


        missingLetterCombo = new ComboBox<>();
        missingLetterCombo.getItems().addAll("J", "X", "Q", "Y", "Z");
        missingLetterCombo.setValue("J");
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


        vbox.getChildren().addAll(keyLabel, keyField, missingLabel, missingLetterCombo, messageLabel, inputArea, generateBtn);
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


        Label ijNote = new Label("Note: I and J share the same cell");
        ijNote.setStyle("-fx-text-fill: #159398; -fx-font-size: 11px; -fx-padding: 5 0 0 0;");


        vbox.getChildren().addAll(gridTitle, gridPane, ijNote);
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
        outputArea.getStyleClass().add("glass-text-area");


        vbox.getChildren().addAll(outputTitle, outputArea);
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


    private void encryptMessage() {
        performCipherAction(1);
    }


    private void decryptMessage() {
        performCipherAction(-1);
    }


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


            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    gridLabels[row][col].setText(String.valueOf(grid[row][col]));
                }
            }


            String fixedMessage = PlayfairDecrypt.ReplaceLettersInMessage(message, missingLetter);
            List<String> digrams = PlayfairDecrypt.DigramMessage(fixedMessage);
            String result = PlayfairDecrypt.ProcessMessage(digrams, grid, direction);


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
        missingLetterCombo.setValue("J");
        inputArea.clear();
        outputArea.clear();
        generateGrid();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }
}









