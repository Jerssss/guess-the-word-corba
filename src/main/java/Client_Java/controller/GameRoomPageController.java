package Client_Java.controller;

import Client_Java.model.GameRoomPageModel;
import Client_Java.view.GameRoomPageView;

public class GameRoomPageController {
    private final GameRoomPageView view;
    private final GameRoomPageModel model;

    public GameRoomPageController(GameRoomPageView view, GameRoomPageModel model) {
        this.view = view;
        this.model = model;
        initialize();
    }

    private void initialize() {
        // Bind model properties to the view
        view.getLifeCountLabel().textProperty().bind(model.lifeCountProperty().asString());

//TODO:   Set up event handlers
//        view.getBlank().setOnMouseClicked(event -> handleBlankClick('A')); // Example: Guess the letter 'A'
//        view.getBlank1().setOnMouseClicked(event -> handleBlankClick('B')); // Example: Guess the letter 'B'
    }

    private void handleBlankClick(char letter) {
        model.guessLetter(letter);
    }

    public void handleReturnToHome() {
        model.returnToHome();
    }
}