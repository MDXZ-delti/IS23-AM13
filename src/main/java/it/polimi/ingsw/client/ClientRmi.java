package it.polimi.ingsw.client;

import it.polimi.ingsw.client.view.GameCliView;
import it.polimi.ingsw.client.view.GameView;
import it.polimi.ingsw.commons.Message;
import it.polimi.ingsw.server.CommunicationInterface;
import it.polimi.ingsw.utils.FullRoomException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import static it.polimi.ingsw.server.CommunicationInterface.HOSTNAME;
import static it.polimi.ingsw.server.CommunicationInterface.PORT_RMI;

public class ClientRmi extends Client {

    public Thread loginThread;
    GameView gameView = new GameCliView(); // TODO: this should be injected by the controller (cli or gui depending on user)
    GameController controller = new GameController(null, gameView);
    int myPosition;
    private Registry registry;
    private CommunicationInterface server;

    /**
     * Starts the client
     */
    public ClientRmi() {
        super();
    }

    @Override
    public void connect() throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry(HOSTNAME, PORT_RMI);
        server = (CommunicationInterface) registry.lookup("CommunicationInterface");
    }

    /**
     * Starts the login procedure, asks the user his info and sends them to the server.
     */
    @Override
    public void login() {
        int age;
        boolean firstGame;
        try {
            controller.startGame();
            String username = controller.showLoginScreen();
            String responseMessage = parser.getMessage(server.sendMessage(parser.sendUsername(username))); // This message will be a JSON
            // TODO: parse the JSON (now it's plain text)
            while ("retry".equals(responseMessage)) {
                System.out.println("Username already taken. Retry.");
                username = controller.showLoginScreen();
                responseMessage = parser.getMessage(server.sendMessage(parser.sendUsername(username)));
            }
            age = controller.showAgeScreen();

            gameView.showMessage(responseMessage);
            String ageResponse = parser.getMessage(server.sendMessage(parser.sendAge(age)));
            if (!ageResponse.startsWith("ok")) {
                System.out.println("Remember that you need to be supervised by an adult to play this game.");
            }
            firstGame = controller.showFirstGameScreen();
            int nextStep = parser.getPosition(server.sendMessage(parser.sendFirstGame(firstGame)));
            if (nextStep == 1) {
                int numPlayer = controller.showNumberOfPlayersScreen();
                String numPlayerResponse = parser.getMessage(server.sendMessage(parser.sendNumPlayer(numPlayer)));
                while (numPlayerResponse.startsWith("retry")) {
                    System.out.println("Illegal number of players. Retry.");
                    numPlayer = controller.showNumberOfPlayersScreen();
                    numPlayerResponse = parser.getMessage(server.sendMessage(parser.sendNumPlayer(numPlayer)));
                }
                //end of login
            }
            myPosition = nextStep;
            System.out.println("Your position is " + myPosition);
            waitingRoom();
        } catch (RemoteException e) {
            throw new RuntimeException(e); // TODO: handle this exception
        } catch (FullRoomException | IOException | ParseException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitingRoom() throws FullRoomException, IOException, ParseException, IllegalAccessException {
        System.out.println("Waiting for other players to join...");
        String response = parser.getMessage(server.sendMessage(parser.sendReady()));
        while (response == null) {
            response = parser.getMessage(server.sendMessage(parser.sendReady()));
        }
        startGame();
    }

    public void startGame() throws FullRoomException, IOException, ParseException, IllegalAccessException {
        Message myGame = server.sendMessage(parser.sendPosition(myPosition));
        controller.showPersonalGoal(parser.getPersonalGoal(myGame));
        controller.showCommonGoal(parser.getCardsType(myGame), parser.getCardOccurrences(myGame), parser.getCardSize(myGame), parser.getCardHorizontal(myGame));
        //        System.out.println("Game started!");
        //TODO: show bookshelf and board
        controller.showBoard(parser.getBoard(myGame));
        controller.showBookshelf(parser.getBookshelf(myGame));
        waitForTurn();
    }

    /**
     * Waits for the turn of the player. It depends on the number received by the server:
     * <ul>
     *     <li>-1: the game is over</li>
     *     <li>0: it's not the player's turn</li>
     *     <li>1: it's the player's turn</li>
     * </ul>
     */
    public void waitForTurn() throws IOException, IllegalAccessException, ParseException, FullRoomException {
        int myTurn = 0;
        while (myTurn != 1) {
            if (myTurn == -1) {
                endGame();
                break;
            } else {
                myTurn = parser.getTurn(server.sendMessage(parser.sendTurn("turn", myPosition)));
            }
        }
        if (myTurn == 1) {
            myTurn();
        }
    }

    /**
     * Shows the board and asks the user to pick some tiles, then, if the pick is valid, asks the user to rearrange the tiles (if the player want),
     * then asks the user to choose a column to place the tiles in. at the end of the turn, the player returns to the waiting room.
     */
    public void myTurn() throws FullRoomException, IOException, IllegalAccessException, ParseException {
        // Sends the message to server to get the board
        Message currentBoard = server.sendMessage(parser.sendMessage("board"));
        controller.showBoard(parser.getBoard(currentBoard));
        //shows and returns the pick
        List<Integer> pick = controller.showPickScreen();
        Message myPick = parser.sendPick(pick.get(0), pick.get(1), pick.get(2), pick.get(3));
        Message isMyPickOk = server.sendMessage(myPick);

        while (!"picked".equals(parser.getMessage(isMyPickOk))) {
            System.out.println("Pick not ok,please retry");
            pick = controller.showPickScreen();
            myPick = parser.sendPick(pick.get(0), pick.get(1), pick.get(2), pick.get(3));
            isMyPickOk = server.sendMessage(myPick);
        }
        System.out.println("Pick ok");

        if (controller.showRearrangeScreen()) {
            server.sendMessage(parser.sendRearrange(controller.rearrangeScreen(parser.getPicked(isMyPickOk), parser.getPickedSize(myPick))));
        }

        Message myInsert = server.sendMessage(parser.sendInsert(controller.showInsertScreen()));
        while (!"update".equals(parser.getMessage(myInsert))) {
            gameView.showMessage(gameView.insertError);
            myInsert = server.sendMessage(parser.sendInsert(controller.showInsertScreen()));
        }

        controller.showBookshelf(parser.getBookshelf(myInsert));
        controller.showScore(parser.getScore(myInsert));
        //        controller.showBoard(parser.getBoard(myInsert));

        waitForTurn();
    }

    /**
     * Ends the game
     */
    public void endGame() throws FullRoomException, RemoteException, IllegalAccessException {
        Message winners = server.sendMessage(parser.sendMessage("endGame"));
        controller.showEndGame(parser.getWinners(winners));
    }
}
