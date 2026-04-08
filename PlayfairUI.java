/*
* Jessica DeWitt - Start working on the UI/UX.
* Should be able to input a key, a letter to omit, a 5x5 grid and space for decoded phrase.
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.List;


public class PlayfairUI extends Application {


   // UI stuff for the main cipher tool
   private TextField keyField;
   private ComboBox<String> missingLetterCombo;
   private TextArea inputArea;
   private TextArea outputArea;
   private Label[][] gridLabels = new Label[5][5];


   // navigation between modes
   private BorderPane mainLayout;
   private VBox cipherView;
   private VBox challengeView;
   private ChallengeMode challengeMode;
   private Stage primaryStage;


   // Toggle buttons
   private Label cipherToggle;
   private Label challengeToggle;


   @Override
   public void start(Stage stage) {
       this.primaryStage = stage;
       stage.initStyle(StageStyle.TRANSPARENT);
       stage.setTitle("Playfair Cipher");


       // Background gradient
       Region background = new Region();
       background.setStyle(
               "-fx-background-color: linear-gradient(to bottom right, #021F2C, #0A2A38, #021520);"
       );


       mainLayout = new BorderPane();
       mainLayout.setPadding(new Insets(20));
       mainLayout.getStyleClass().add("glass-panel");


       // Top bar with toggle buttons
       HBox topBar = createTopBar();
       mainLayout.setTop(topBar);


       // Set up both modes
       cipherView = createCipherView();
       challengeMode = new ChallengeMode();
       challengeView = challengeMode.getView();


       // Start with cipher view
       mainLayout.setCenter(cipherView);


       // Stack background and content
       StackPane root = new StackPane();
       root.getChildren().addAll(background, mainLayout);


       Scene scene = new Scene(root, 1200, 800);
       scene.setFill(Color.TRANSPARENT);
       scene.getStylesheets().add(getClass().getResource("/com/playfair/ui/glass-style.css").toExternalForm());


       primaryStage.setScene(scene);
       primaryStage.show();


       generateGrid();
   }


   private HBox createTopBar() {
       HBox topBar = new HBox();
       topBar.setAlignment(Pos.CENTER);
       topBar.setPadding(new Insets(0, 0, 20, 0));


       // Left spacer
       Region leftSpacer = new Region();
       HBox.setHgrow(leftSpacer, Priority.ALWAYS);


       // Toggle container
       HBox toggleContainer = new HBox(4);
       toggleContainer.setAlignment(Pos.CENTER);
       toggleContainer.getStyleClass().add("toggle-container");


       cipherToggle = new Label("CIPHER MODE");
       cipherToggle.getStyleClass().addAll("toggle-option", "selected");
       cipherToggle.setOnMouseClicked(e -> {
           cipherToggle.getStyleClass().add("selected");
           challengeToggle.getStyleClass().remove("selected");
           mainLayout.setCenter(cipherView);
       });


       challengeToggle = new Label("CHALLENGE MODE");
       challengeToggle.getStyleClass().add("toggle-option");
       challengeToggle.setOnMouseClicked(e -> {
           challengeToggle.getStyleClass().add("selected");
           cipherToggle.getStyleClass().remove("selected");


           // Create fresh ChallengeMode instance
           challengeMode = new ChallengeMode();
           challengeView = challengeMode.getView();


           ScrollPane scrollPane = new ScrollPane();
           scrollPane.setContent(challengeView);
           scrollPane.setFitToWidth(true);
           scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
           scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
           scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
           mainLayout.setCenter(scrollPane);
       });


       toggleContainer.getChildren().addAll(cipherToggle, challengeToggle);


       // Right spacer
       Region rightSpacer = new Region();
       HBox.setHgrow(rightSpacer, Priority.ALWAYS);


       // Exit button
       Button exitBtn = new Button("✕");
       exitBtn.getStyleClass().add("exit-button");
       exitBtn.setOnAction(e -> primaryStage.close());


       topBar.getChildren().addAll(leftSpacer, toggleContainer, rightSpacer, exitBtn);


       return topBar;
   }


   private VBox createCipherView() {
       VBox cipherView = new VBox(20);
       cipherView.setAlignment(Pos.TOP_CENTER);
       cipherView.getStyleClass().add("glass-panel");


       // Title
       HBox titleSection = new HBox();
       titleSection.setAlignment(Pos.CENTER);
       Label title = new Label("Playfair Cipher");
       title.getStyleClass().add("glass-title");
       titleSection.getChildren().add(title);


       // Three column layout
       HBox contentLayout = new HBox(20);
       contentLayout.setAlignment(Pos.CENTER);


       VBox inputSection = createInputSection();
       VBox gridSection = createGridSection();
       VBox outputSection = createOutputSection();


       contentLayout.getChildren().addAll(inputSection, gridSection, outputSection);


       // Bottom buttons
       HBox buttonSection = createButtonSection();


       cipherView.getChildren().addAll(titleSection, contentLayout, buttonSection);
       return cipherView;
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


   private void encryptMessage() {
       String message = inputArea.getText().trim().toUpperCase();
       if (message.isEmpty()) {
           showAlert("Error", "Please enter a message to encrypt");
           return;
       }


       message = message.replaceAll("[^A-Z]", "");
       outputArea.setText("Encrypted: " + message);
   }


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
