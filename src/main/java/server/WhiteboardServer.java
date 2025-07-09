package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The WhiteboardServer class initializes and starts the RMI registry for the whiteboard application.
 * It binds the SessionManager instance to the registry so that clients can connect to the server.
 */
public class WhiteboardServer {
    private static final Logger LOGGER = Logger.getLogger(WhiteboardServer.class.getName());

    /**
     * The main method to start the WhiteboardServer.
     *
     * @param args command-line arguments: server IP address and server port
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java server.WhiteboardServer <serverIPAddress> <serverPort>");
            System.exit(1);
        }
        String serverIPAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try {
            SessionManager sessionManager = SessionManager.getInstance();
            Registry registry = LocateRegistry.createRegistry(serverPort);
            registry.rebind("Whiteboard", sessionManager);
            System.out.println("Whiteboard server ready at " + serverIPAddress + ":" + serverPort);
            LOGGER.log(Level.INFO, "Whiteboard server ready at {0}:{1}", new Object[]{serverIPAddress, serverPort});
        } catch (RemoteException e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Server exception", e);
        }
    }
}

