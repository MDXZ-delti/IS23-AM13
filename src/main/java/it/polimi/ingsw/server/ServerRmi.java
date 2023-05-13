package it.polimi.ingsw.server;

import it.polimi.ingsw.commons.Message;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerRmi implements CommunicationInterface, ServerInterface {

    private final Message gameStatus = null;
    private Registry registry;
    private CommunicationInterface server;
    private CommunicationInterface stub;

    public ServerRmi() throws RemoteException {
        super();
    }

    @Override
    public void start() {
        try {
            server = new ServerRmi();
            stub = (CommunicationInterface) UnicastRemoteObject.exportObject(server, 0);

            registry = LocateRegistry.createRegistry(PORT_RMI);
            registry.rebind("CommunicationInterface", stub);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Unable to start the RMI server.");
        }

        System.out.println("RMI server started on port " + PORT_RMI + ".");
    }

    @Override
    public void stop() {
        try {
            registry.unbind("CommunicationInterface");
            UnicastRemoteObject.unexportObject(server, true);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.err.println("Unable to stop the RMI server.");
        }
        System.out.println("RMI server stopped.");
    }

    public void setNewStatus() {
        //        gameStatus = parser.sendStartGame(controller.getCurrentPlayerPersonalGoal(), controller.getCommonGoals(), controller.getCurrentePlayerBookshelf(), controller.getBoard());
    }

    @Override
    public Message sendGame(int position) throws RemoteException {

        System.out.println("Sending game to " + position);
        return parser.sendStartGame(controller.getPersonalGoalCard(position), controller.getCommonGoals(), controller.getBookshelf(position), controller.getBoard());
    }

    @Override
    public Message sendUpdate(Message message) throws RemoteException {
        setNewStatus();
        return parser.sendUpdate("update", controller.getCurrentePlayerBookshelf(), controller.getBoard(), controller.getCurrentPlayerScore());
    }
}
