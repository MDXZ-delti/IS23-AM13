package it.polimi.ingsw.client.view;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.commons.Message;
import it.polimi.ingsw.server.model.Board;
import it.polimi.ingsw.server.model.Bookshelf;
import it.polimi.ingsw.server.model.Item;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface GameView {

    String insertError = "The index must be between 0 and " + (Bookshelf.getColumns() - 1) + ": "; // TODO: make sure to load settings before this
    String insertUsernamePrompt = "Please, insert your username: ";
    String insertAgePrompt = "Please, insert your age: ";
    String firstGameQuestion = "Is this the first time you play this game?";
    String insertUsernameAgainPrompt = "Please, insert your username again: ";
    String welcomeMessage = "Welcome to...\n";
    String insertNumberOfPlayersPrompt = "Please, insert the total number of players: ";

    static void cleanScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    void loginProcedure();

    // Parameter is useless in CLI but needed in GUI
    void startView(Client client);

    void showMessage(String message);

    /**
     * Allows the pick of the items from the board and sends the message to the server
     */
    void showPick();

    void showBoard(Board board);

    void showBookshelf(Bookshelf bookshelf);

    void showStartGame();

    boolean showRearrange(List<Item> items) throws IOException;

    int promptInsert();

    void showEndGame(List<String> winners);

    List<Integer> rearrange(List<Item> items) throws IOException;

    void showCurrentScore(int score);

    void showDisconnection();

    void waitingRoom();

    void startGame(Message myGame);

    void endGame();

    void setClient(Client client);

    void pickMyBookshelf(HashMap<Bookshelf, String> bookshelves);

    void pickOtherBookshelf(HashMap<Bookshelf, String> bookshelves);

    void showOtherBookshelf(Bookshelf bookshelf, String name);

    void usernameError();

    void completeLoginError();

    void playerNumberError();

    void playerChoice();

    void showLastRound();

    void showGameAlreadyStarted();

    void showRemovePlayer();

    void showDisconnection(List<String> disconnectedPlayers);
}
