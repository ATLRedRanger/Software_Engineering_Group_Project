package com.example.playfaircipher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // This line loads your FXML file
        // Ensure "PlayfairUI.fxml" is in the same folder/package as this class
        Parent root = FXMLLoader.load(getClass().getResource("/PlayfairUI.fxml"));

        primaryStage.setTitle("Playfair Cipher Tool");
        primaryStage.setScene(new Scene(root, 450, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}