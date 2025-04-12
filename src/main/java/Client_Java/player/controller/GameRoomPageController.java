package Client_Java.player.controller;

import Client_Java.player.model.GameRoomPageModel;
import Client_Java.player.view.GameRoomPageView;

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
        view.getLifeCountLabel().textProperty().bind(model.remainingAttemptsProperty().asString());

//TODO:   //Set up event handlers
//        view.getBlank().setOnMouseClicked(event -> model.guessLetter('A')); // Example: Guess the letter 'A'
//        view.getBlank1().setOnMouseClicked(event -> model.guessLetter('B')); // Example: Guess the letter 'B'
    }

    public void handleReturnToHome() {
        //TODO: Logic to return to the home screen
    }
}