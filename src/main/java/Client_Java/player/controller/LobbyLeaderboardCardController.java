package Client_Java.player.controller;

import Client_Java.player.model.LobbyLeaderboardCardModel;
import Client_Java.player.view.cards.LobbyLeaderboardCardView;

public class LobbyLeaderboardCardController {
    private final LobbyLeaderboardCardModel model;
    private final LobbyLeaderboardCardView view;

    public LobbyLeaderboardCardController(LobbyLeaderboardCardModel model, LobbyLeaderboardCardView view) {
        this.model = model;
        this.view = view;
        initialize();
    }

    private void initialize() {
        // Update view with model data
        view.setRank(model.getRank());
        view.setUsername(model.getUsername());
        view.setPoints(model.getPoints());
    }
}