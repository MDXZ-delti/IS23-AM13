package it.polimi.ingsw.client;

import it.polimi.ingsw.client.view.GameView;
import it.polimi.ingsw.server.model.GameModel;

public class GameController {
    private final GameModel gameModel;
    private final GameView gameView;
    private final Client client;

    public GameController(GameModel gameModel, GameView gameView, Client client) {
        this.gameModel = gameModel;
        this.gameView = gameView;
        this.client = client;
        gameView.setController(this);
    }

    public void startGame() {
        gameModel.start();
    }

    public void login(String username) throws Exception {
        client.login(username);
    }

    public void showLoginScreen() {
        gameView.showLogin();
    }

    public int showAgeScreen() {
        return gameView.promptAge();
    }

    public boolean showFirstGamescreen() {
        return gameView.promptFirstGame();
    }
}
