package it.polimi.ingsw.server;

import it.polimi.ingsw.commons.Message;
import it.polimi.ingsw.utils.FullRoomException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;

public class SocketClientHandler implements Runnable, ServerCommunicationInterface {

    public final BufferedReader clientBufferedReader;
    private final Socket socket;
    public PrintStream clientPrintStream;
    public DataOutputStream dataOutputStream;
    public Thread listenThread;

    public SocketClientHandler(Socket socket) throws IOException {
        this.socket = socket;

        // To send data to the client
        try {
            clientPrintStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // To read data coming from the client
        try {
            clientBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // This is to send data to the server
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Unable to create output stream");
            throw new RuntimeException(e);
        }

        // Listen for messages coming from the client
        listenThread = new Thread(() -> {
            String clientString;
            while (true) {
                try {
                    synchronized (clientBufferedReader) {
                        clientString = clientBufferedReader.readLine();
                        JSONParser parser = new JSONParser();
                        JSONObject messageFromClient = null;
                        try {
                            messageFromClient = (JSONObject) parser.parse(clientString);
                        } catch (ParseException e) {
                            System.err.println("Unable to parse message from client");
                        }

                        Message message = new Message(messageFromClient);

                        try {
                            receiveMessageTcp(message, this);
                        } catch (IllegalAccessException | FullRoomException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (IOException e) {
                    System.out.println(socket.getInetAddress() + " disconnected, unable to read");
                    break;
                }
            }
        });
    }

    @Override
    public void run() {
        listenThread.start();
    }

    public void sendString(String message) {
        clientPrintStream.println(message);
    }

    public void close() {
        sendString("Connection closed.");
        listenThread.interrupt();
        clientPrintStream.close();
        try {
            clientBufferedReader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while closing buffered reader or socket.");
        }

        // terminate application
        System.exit(0);
    }

    @Override
    public void receiveMessageTcp(Message message, SocketClientHandler client) throws IllegalAccessException, RemoteException, FullRoomException {
        String category = message.getCategory();

        switch (category) {
            case "ping" -> controller.pingReceived(message.getUsername());
            case "numOfPlayers" -> {
                int numPlayer = message.getNumPlayer();
                String isOk = controller.checkNumPlayer(numPlayer);
                if (!isOk.equals("ok")) {
                    sendMessageToClient(new Message("numOfPlayersNotOK"));
                } else {
                    System.out.println("Number of players: " + numPlayer);
                    sendMessageToClient(new Message("waitingRoom"));
                }
            }
            case "pick" -> {
                if ("ok".equals(controller.pick(message.getPick()))) {
                    sendMessageToClient(new Message(controller.getPicked(message.getPick())));
                } else {
                    sendMessageToClient(new Message("pickRetry"));
                }
            }
            case "insert" -> {
                if (controller.checkInsert(message.getInsert())) {
                    sendUpdate(message);
                    controller.changeTurn();
                    turn();
                }
                // TODO: return an error message if the insert is not valid, otherwise the game will freeze
            }
            case "sort" -> controller.rearrangePicked(message.getSort());
            case "completeLogin" -> {
                String username = message.getUsername();
                int checkStatus = controller.checkUsername(username);
                if (checkStatus == 1) {
                    // The username is available, a new player can be added
                    sendMessageToClient(new Message("username", username));
                    controller.addPlayer(message.getUsername(), 0, message.getFirstGame());
                    System.out.println(message.getUsername() + " logged in.");
                    controller.addClient(message.getUsername(), client);
                    controller.startRoom();
                    if (controller.isFirst()) {
                        sendMessageToClient(new Message("chooseNumOfPlayer"));
                    } else {
                        sendMessageToClient(new Message("waitingRoom"));
                        if (controller.checkRoom()) {
                            startGame();
                            System.out.println("Game started.");
                        }
                    }
                } else if (checkStatus == 0) {
                    // The username has already been taken, retry
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    checkStatus = controller.checkUsername(username);
                    if (checkStatus == 0) {
                        System.out.println(username + " requested login, but the username is already taken.");
                        sendMessageToClient(new Message("usernameRetry"));
                    } else {
                        System.out.println(username + " reconnected.");
                    }
                } else {
                    // The username is already taken, but the player was disconnected and is trying to reconnect
                    System.out.println(username + " reconnected.");
                    sendMessageToClient(new Message("update", controller.getBookshelves(), controller.getBoard(), controller.getCurrentPlayerScore()));
                }
            }
            default -> System.out.println(message + " requested unknown");
        }
    }

    public void sendMessageToClient(Message message) {
        clientPrintStream.println(message.getJSONstring());
    }
}
