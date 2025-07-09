package client;

import common.WhiteboardInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The WhiteboardClient class connects to the whiteboard server and provides methods
 * to interact with the server, such as drawing, messaging, and managing the whiteboard session.
 */
public class WhiteboardClient {
    private WhiteboardInterface stub;
    private static final Logger LOGGER = Logger.getLogger(WhiteboardClient.class.getName());

    /**
     * Constructs a new WhiteboardClient and connects to the server.
     *
     * @param serverAddress the address of the whiteboard server
     * @param serverPort the port of the whiteboard server
     */
    public WhiteboardClient(String serverAddress, int serverPort) {
        connectToServer(serverAddress, serverPort);
    }

    /**
     * Connects to the whiteboard server using the provided address and port.
     *
     * @param serverAddress the address of the whiteboard server
     * @param serverPort the port of the whiteboard server
     */
    private void connectToServer(String serverAddress, int serverPort) {
        try {
            Registry registry = LocateRegistry.getRegistry(serverAddress, serverPort);
            stub = (WhiteboardInterface) registry.lookup("Whiteboard");
            LOGGER.log(Level.INFO, "Connected to Whiteboard server at {0}:{1}", new Object[]{serverAddress, serverPort});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to Whiteboard server", e);
        }
    }

    /**
     * Gets the stub for interacting with the whiteboard server.
     *
     * @return the WhiteboardInterface stub
     */
    public WhiteboardInterface getStub() {
        return stub;
    }
}

