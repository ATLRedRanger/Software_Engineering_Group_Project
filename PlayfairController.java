package com.example.playfaircipher;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import java.util.List;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class PlayfairController {

    @FXML private TextField keyField;
    @FXML private ComboBox<String> omitCombo;
    @FXML private TextField messageField;
    @FXML private TextArea resultArea;
    @FXML private GridPane visualGrid;

    private char[][] currentGrid;

    @FXML
    public void initialize() {
        // Populate the dropdown with the alphabet (minus X as per your Python logic)
        String alpha = "ABCDEFGHIJJKLMNOPQRSTUVWYZ";
        for (char c : "ABCDEFGHIKLMNOPQRSTUVWXYZ".toCharArray()) {
            omitCombo.getItems().add(String.valueOf(c));
        }
        omitCombo.getSelectionModel().select("J"); // Default
    }

    @FXML
    private void handleGenerateGrid() {
        String keyInput = keyField.getText().toUpperCase().replaceAll("[^A-Z]", "");
        String omitted = omitCombo.getValue();

        if (keyInput.isEmpty()) {
            resultArea.setText("Please enter a valid key first.");
            return;
        }

        // Use the logic from the translated class
        List<Character> cleanKey = PlayfairDecryptor.cleanKey(keyInput);
        String newAlpha = PlayfairDecryptor.rearrangeAlphabet(cleanKey, omitted);
        currentGrid = PlayfairDecryptor.populateGrid(newAlpha);

        updateVisualGrid();
        resultArea.setText("Grid generated successfully with omitted letter: " + omitted);
    }

    private void updateVisualGrid() {
        visualGrid.getChildren().clear();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(40, 40);
                cell.setStyle("-fx-border-color: black; -fx-background-color: #fff;");
                cell.getChildren().add(new Text(String.valueOf(currentGrid[row][col])));
                visualGrid.add(cell, col, row);
            }
        }
    }

    @FXML
    private void handleDecrypt() {
        if (currentGrid == null) {
            resultArea.setText("Generate a grid first!");
            return;
        }

        String msg = messageField.getText().toUpperCase().replaceAll("[^A-Z]", "");
        String omitted = omitCombo.getValue();

        // 1. Replace omitted letter
        String fixedMessage = PlayfairDecryptor.replaceLettersInMessage(msg, omitted);

        // 2. Digram
        List<String> digrams = PlayfairDecryptor.digramMessage(fixedMessage);

        // 3. Decrypt
        String result = PlayfairDecryptor.decryptMessage(digrams, currentGrid);

        resultArea.setText(result);
    }
}