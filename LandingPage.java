package com.playfair.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;


//UI Components

public class LandingPage {

    private ScrollPane scrollPane;
    private VBox contentContainer;
    private Runnable onEnterAction;

    public LandingPage() {
        buildContent();
    }
    // Set enter action (Called from PlayfairUI)
    public void setOnEnterAction(Runnable action) {
        this.onEnterAction = action;
    }
    // Build main content
    private void buildContent() {
        contentContainer = new VBox(80);
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setPadding(new Insets(40, 60, 80, 60));
        contentContainer.setMaxWidth(1200);

        buildHeroSection();
        buildCipherModeSection();
        buildChallengeModeSection();
        buildFooterSection();

        scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("transparent-scroll");
        scrollPane.setContent(contentContainer);

        animateEntrance();
    }
    // HERO SECTION (Title, Description, Enter Button)
    // Playfair Cipher title
    // Description text explaining the app
    // subtext small text
    // Enter button with hover scale effect
    // When clicked, triggers onEnterAction to switch to Tool tab

    private void buildHeroSection() {
        VBox heroSection = new VBox(20);
        heroSection.setAlignment(Pos.CENTER_LEFT);
        heroSection.setMaxWidth(800);
        heroSection.setPadding(new Insets(60, 0, 40, 0));

        Text playfairText = new Text("Playfair");
        playfairText.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 72));
        playfairText.setFill(Color.WHITE);

        Text cipherText = new Text("Cipher");
        cipherText.setFont(Font.font("SF Pro Display", FontWeight.THIN, 72));
        cipherText.setFill(Color.WHITE);

        Text description = new Text(
                "Encrypt and decrypt messages using the classic Playfair cipher.\n" +
                        "Challenge yourself with interactive puzzles and improve your\n" +
                        "cryptography skills."
        );
        description.setFont(Font.font("SF Pro Text", FontWeight.THIN, 18));
        description.setFill(Color.WHITE);
        description.setTextAlignment(TextAlignment.LEFT);
        description.setLineSpacing(8);

        Text smallText = new Text("Master the art of classical cryptography");
        smallText.setFont(Font.font("SF Pro Text", FontWeight.THIN, 13));
        smallText.setFill(Color.WHITE);

        
        Button enterButton = new Button("Enter");
        enterButton.getStyleClass().add("glass-action-button");
        enterButton.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 16));
        enterButton.setOnAction(e -> {
            animateButtonClick(enterButton);
            if (onEnterAction != null) {
                onEnterAction.run();
            }
        });

        enterButton.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), enterButton);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        enterButton.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), enterButton);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        heroSection.getChildren().addAll(playfairText, cipherText, description, smallText, enterButton);
        contentContainer.getChildren().add(heroSection);
    }
    // Cipher mode (Glass Cards explaining features)
    private void buildCipherModeSection() {
        // Section title
        Text sectionTitle = new Text("CIPHER MODE.");
        sectionTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 48));
        sectionTitle.setFill(Color.WHITE);
        sectionTitle.setStyle("-fx-font-style: italic;");

        VBox titleBox = new VBox(sectionTitle);
        titleBox.setPadding(new Insets(40, 0, 20, 0));
        contentContainer.getChildren().add(titleBox);

        String matrixText =
                "⚡ UNIQUE GRID EVERY TIME\n" +
                        "Your keyword creates a one-of-a-kind 5x5 cipher grid. No duplicates. No patterns.\n\n" +
                        "WHY IT MATTERS\n" +
                        "Wrong key = can't decrypt. Your key is the only way in.\n\n" +
                        "25 LETTERS. 1 OMISSION.\n" +
                        "We merge J with I to fit the alphabet perfectly.";

        VBox matrixCard = createGlassCard("5×5 MATRIX GENERATION", matrixText);
        contentContainer.getChildren().add(matrixCard);

        String encryptionText =
                "🔒 TYPE → ENCRYPT → UNREADABLE\n" +
                        "One click. Your message becomes secret code.\n\n" +
                        "HOW IT WORKS\n" +
                        "Split text into pairs, then apply 3 simple grid rules:\n" +
                        "• Same row? Shift right\n" +
                        "• Same column? Shift down\n" +
                        "• Rectangle? Swap corners";

        VBox encryptionCard = createGlassCard("ENCRYPTION", encryptionText);
        contentContainer.getChildren().add(encryptionCard);

        String decryptionText =
                "🔓 PASTE → ENTER KEY → READABLE\n" +
                        "Got a secret message? Decrypt it instantly.\n\n" +
                        "EXACT REVERSE\n" +
                        "Same grid. Opposite moves:\n" +
                        "• Shift left for rows\n" +
                        "• Shift up for columns\n" +
                        "• Swap corners for rectangles";

        VBox decryptionCard = createGlassCard("DECRYPTION", decryptionText);
        contentContainer.getChildren().add(decryptionCard);
    }

    // Challenge mode (Glass Cards explaining features)
    private void buildChallengeModeSection() {
        Text sectionTitle = new Text("CHALLENGE MODE.");
        sectionTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 48));
        sectionTitle.setFill(Color.WHITE);
        sectionTitle.setStyle("-fx-font-style: italic;");

        VBox titleBox = new VBox(sectionTitle);
        titleBox.setPadding(new Insets(60, 0, 20, 0));
        contentContainer.getChildren().add(titleBox);

        String difficultyText =
                "📊 STREAK = DIFFICULTY\n" +
                        "Solve puzzles. Build streak. Level up.\n\n" +
                        "EASY (Streak 0-2): Simple words, obvious hints\n" +
                        "MEDIUM (Streak 3-5): Tricky keys, cryptic clues\n" +
                        "HARD (Streak 6+): Expert mode. Minimal hints.";

        VBox difficultyCard = createGlassCard("DIFFICULTY LEVELS", difficultyText);
        contentContainer.getChildren().add(difficultyCard);

        String interactiveText =
                "🎯 GUESS THE KEY → SCRAMBLED GRID → SOLVE\n" +
                        "Drag and drop letters into the right spots.\n\n" +
                        "VISUAL FEEDBACK\n" +
                        "✅ Green = correct placement\n" +
                        "❌ Red = wrong spot\n" +
                        "💙 Teal = matching pairs";

        VBox interactiveCard = createGlassCard("INTERACTIVE GRID", interactiveText);
        contentContainer.getChildren().add(interactiveCard);

        String streakText =
                "🏆 KEEP YOUR STREAK ALIVE\n" +
                        "Solve correctly → Streak increases\n" +
                        "Get it wrong → Streak resets to zero\n\n" +
                        "💾 AUTO-SAVED LOCALLY\n" +
                        "Pick up where you left off.\n" +
                        "Difficulty adjusts automatically. Always challenged.";

        VBox streakCard = createGlassCard("STREAK SYSTEM", streakText);
        contentContainer.getChildren().add(streakCard);
    }

    // glass cards
    private VBox createGlassCard(String title, String description) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(35));
        card.setMaxWidth(900);
        card.setMinWidth(900);
        card.setPrefWidth(900);
        card.getStyleClass().add("glass-card");

        Text titleText = new Text(title);
        titleText.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        titleText.setFill(Color.WHITE);
        titleText.setStyle("-fx-letter-spacing: 1px;");

        Text descText = new Text(description);
        descText.setFont(Font.font("SF Pro Text", FontWeight.NORMAL, 16));
        descText.setFill(Color.WHITE);
        descText.setWrappingWidth(830);
        descText.setLineSpacing(8);

        card.getChildren().addAll(titleText, descText);

        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return card;
    }

    // Footer section Get started button
    private void buildFooterSection() {
        VBox footer = new VBox(30);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(80, 0, 60, 0));

        // Button Style
        Button getStartedBtn = new Button("Get Started");
        getStartedBtn.getStyleClass().add("glass-action-button");
        getStartedBtn.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 18));
        getStartedBtn.setStyle("-fx-padding: 16px 48px; -fx-font-size: 18px;");

        getStartedBtn.setOnAction(e -> {
            animateButtonClick(getStartedBtn);
            if (onEnterAction != null) {
                onEnterAction.run();
            }
        });

        getStartedBtn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), getStartedBtn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        getStartedBtn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), getStartedBtn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        footer.getChildren().add(getStartedBtn);
        contentContainer.getChildren().add(footer);
    }
    // Animations
    private void animateEntrance() {
        contentContainer.setOpacity(0);
        contentContainer.setTranslateY(20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), contentContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), contentContainer);
        slideIn.setFromY(20);
        slideIn.setToY(0);

        fadeIn.play();
        slideIn.play();
    }

    private void animateButtonClick(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setOnFinished(e -> {
            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), button);
            st2.setToX(1.0);
            st2.setToY(1.0);
            st2.play();
        });
        st.play();
    }
    // getter
    public ScrollPane getView() {
        return scrollPane;
    }
}
