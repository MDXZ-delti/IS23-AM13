package it.polimi.ingsw.server;

import it.polimi.ingsw.server.model.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static it.polimi.ingsw.server.CommunicationInterface.PORT_SOCKET;

public class ServerTCP {
    private final ServerSocket serverSocket;
    public List<SocketClientHandler> clientHandlers;
    public HashMap<SocketClientHandler, Player> connectedPlayers;
    public Socket s = null;
    public ExecutorService executor;


    public ServerTCP() throws IOException {
        clientHandlers = new ArrayList<>();
        connectedPlayers = new HashMap<>();
        executor = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(PORT_SOCKET);
        System.out.println("Server socket started on port " + serverSocket.getLocalPort() + ".");
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection established.");
                    SocketClientHandler clientHandler = new SocketClientHandler(clientSocket);
                    executor.submit(clientHandler);
                    clientHandler.sendMessage("Welcome to the server!");
                    sendToAllExcept("A new player has joined the game!", clientHandler);
                    clientHandlers.add(clientHandler);
//                    clientHandler.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() throws RemoteException, NotBoundException {
        System.out.println("Stopping socket server...");
        sendToAll("Server is shutting down...");
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while closing socket server.");
        }
        closeAllConnections();
        executor.shutdownNow();
    }

    public void closeAllConnections() {
        for (SocketClientHandler clientHandler : clientHandlers) {
            clientHandler.close();
        }
    }

    public void sendToAll(String message) {
        for (SocketClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    public void sendToAllExcept(String message, SocketClientHandler clientHandler) {
        for (SocketClientHandler client : clientHandlers) {
            if (client != clientHandler) {
                client.sendMessage(message);
            }
        }
    }

    public int getConnectedPlayers() {
        return clientHandlers.size();
    }
}
