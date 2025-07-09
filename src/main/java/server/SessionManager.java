package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.ClientCallback;
import common.WhiteboardInterface;

/**
 * The SessionManager class manages the whiteboard session, including user registration,
 * drawing actions, and communication between clients and the server.
 */
public class SessionManager extends UnicastRemoteObject implements WhiteboardInterface, Serializable {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static SessionManager instance;
    private final List<String> activeUsers = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<String, ClientCallback> clients = new ConcurrentHashMap<>();
    private final List<String> pendingUsers = Collections.synchronizedList(new ArrayList<>());
    private String whiteboardState = "";
    private String managerUsername;
    private ClientCallback managerCallback;

    /**
     * Constructs a new SessionManager.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    private SessionManager() throws RemoteException {
        super();
        LOGGER.log(Level.INFO, "SessionManager instance created");
    }

    /**
     * Gets the singleton instance of the SessionManager.
     *
     * @return the singleton instance
     * @throws RemoteException if a remote communication error occurs
     */
    public static synchronized SessionManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SessionManager();
        }
        LOGGER.log(Level.INFO, "SessionManager instance accessed");
        return instance;
    }

    /**
     * Registers a user with the whiteboard session.
     *
     * @param username the username to register
     * @param client the client callback
     * @param isManager whether the user is a manager
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void registerUser(String username, ClientCallback client, boolean isManager) throws RemoteException {
        if (username == null || username.isEmpty()) {
            LOGGER.log(Level.WARNING, "Server: Attempt to register user with null or empty username");
            throw new IllegalArgumentException("Server: Username cannot be null or empty");
        }
        if (isManager) {
            managerUsername = username;
            managerCallback = client;
            activeUsers.add(username);
            // Register client callback stub to server hashmap
            registerCallback(username, client);
            updateAllClientsUserList();
            LOGGER.log(Level.INFO, "Server: Manager with username '{0}' registered and added to active users", username);
            LOGGER.log(Level.INFO, "Server: List of active users: {0}", activeUsers);
            LOGGER.log(Level.INFO, "Server: List of callback stub: {0}", new Object[]{this.clients.keySet()});
        } else {
            pendingUsers.add(username);
            registerCallback(username, client);
            notifyManagerForApproval(username);
            LOGGER.log(Level.INFO, "Server: Pending user {0} added and awaiting approval", username);
            LOGGER.log(Level.INFO, "Server: List of pending user: {0}", pendingUsers);
        }
        LOGGER.log(Level.INFO, "List of callback stub: {0}", new Object[]{this.clients.keySet()});
    }

    /**
     * Notifies the manager for user approval.
     *
     * @param username the username to notify the manager about
     * @throws RemoteException if a remote communication error occurs
     */
    private void notifyManagerForApproval(String username) throws RemoteException {
        if (managerCallback != null) {
            managerCallback.notifyManager(username);
        }
    }

    /**
     * Approves a pending user to join the whiteboard session.
     *
     * @param username the username of the user to approve
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void approveClient(String username) throws RemoteException {
        LOGGER.log(Level.INFO, "Trying to approve user: {0}", username);
        if (pendingUsers.remove(username)) {
            activeUsers.add(username);
            ClientCallback clientCallback = clients.get(username);
            if (clientCallback != null) {
                clientCallback.setApproved(true);
                clientCallback.updateUserList(activeUsers.toArray(new String[0])); // Update user list
                LOGGER.log(Level.INFO, "Approved pending user {0} and moved to active users", username);
            } else {
                LOGGER.log(Level.WARNING, "No callback registered for approved user {0}", username);
            }
        } else {
            LOGGER.log(Level.WARNING, "Attempt to approve non-existing pending user {0}", username);
        }
        updateAllClientsUserList();
    }

    /**
     * Refuses a pending user to join the whiteboard session.
     *
     * @param username the username of the user to refuse
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void refuseClient(String username) throws RemoteException {
        pendingUsers.remove(username);
        //unregisterCallback(username, client);
        LOGGER.log(Level.INFO, "User refused: {0}", username);
        System.exit(1);
    }

    /**
     * Updates all clients with the current list of active users.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    private void updateAllClientsUserList() throws RemoteException {
        String[] userList = activeUsers.toArray(new String[0]);
        for (ClientCallback client : clients.values()) {
            try {
                client.updateUserList(userList);
            } catch (RemoteException e) {
                LOGGER.log(Level.SEVERE, "Failed to update user list for client", e);
            }
        }
    }

    /**
     * Handles a drawing action by updating the whiteboard state and notifying all clients.
     *
     * @param action the drawing action to handle
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void canvasAction(String action) throws RemoteException {
        whiteboardState += action + "\n";
        LOGGER.log(Level.INFO, "Server: Drawing action received: {0}", action);
        for (ClientCallback client : clients.values()) {
            try {
                client.updateCanvas(action);
                LOGGER.log(Level.INFO, "Server: Sent drawing action to client");
            } catch (RemoteException e) {
                LOGGER.log(Level.SEVERE, "Failed to update canvas for a client", e);
            }
        }
        LOGGER.log(Level.INFO, "Drew action: {0} and updated all clients", action);
    }

    /**
     * Clears the canvas by updating the whiteboard state and notifying all clients.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void clearCanvas() throws RemoteException {
        whiteboardState = ""; 
        for (ClientCallback client : clients.values()) {
            try {
                client.updateCanvasClear();
            } catch (RemoteException e) {
                System.out.println("Error broadcasting clear canvas to client.");
            }
        }
    }

    /**
     * Sends a message to all clients.
     *
     * @param message the message to send
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void sendMessage(String message) throws RemoteException {
        for (ClientCallback client : clients.values()) {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                LOGGER.log(Level.SEVERE, "Failed to send message to client", e);
            }
        }
    }

    /**
     * Kicks a user out of the whiteboard session.
     *
     * @param username the username of the user to kick out
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void kickUser(String username) throws RemoteException {
        if (activeUsers.remove(username)) {
            ClientCallback clientCallback = clients.remove(username);
            if (clientCallback != null) {
                clientCallback.notify("You have been kicked out by the manager.");
                clientCallback.deregister();
            }
            updateAllClientsUserList();
            LOGGER.log(Level.INFO, "User {0} has been kicked out by the manager", username);
        } else {
            LOGGER.log(Level.WARNING, "Attempt to kick out non-existing user {0}", username);
        }
    }

    /**
     * Creates a new whiteboard session.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void createNewBoard() throws RemoteException {
        whiteboardState = "";
        clearCanvas();
    }

    /**
     * Opens a whiteboard session from a file.
     *
     * @param filePath the path to the file to open
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void openBoard(String filePath) throws RemoteException {
        try {
            whiteboardState = new String(Files.readAllBytes(Paths.get(filePath)));
            for (String action : whiteboardState.split("\n")) {
                canvasAction(action);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening board from file: " + filePath, e);
            throw new RemoteException("Error opening board from file: " + filePath, e);
        }
    }

    /**
     * Saves the whiteboard session to a file.
     *
     * @param filePath the path to the file to save
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void saveBoard(String filePath) throws RemoteException {
        try {
            Files.write(Paths.get(filePath), whiteboardState.getBytes());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving board to file: " + filePath, e);
            throw new RemoteException("Error saving board to file: " + filePath, e);
        }
    }

    /**
     * Closes the whiteboard session.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void closeBoard() throws RemoteException {
        for (ClientCallback client : clients.values()) {
            try {
                client.notify("The whiteboard session is closing. You will be disconnected.");
                client.deregister();
            } catch (RemoteException e) {
                LOGGER.log(Level.SEVERE, "Failed to notify client about closing", e);
            }
        }

        whiteboardState = "";
        clearCanvas();

        System.exit(0);
    }

    /**
     * Registers a client callback stub to the server.
     *
     * @param username the username of the client
     * @param callback the client callback
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void registerCallback(String username, ClientCallback callback) throws RemoteException {
        clients.put(username, callback);
        LOGGER.log(Level.INFO, "Client callback registered: {0}", username);
    }

    /**
     * Gets the current whiteboard state.
     *
     * @return the current whiteboard state
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized String getWhiteboardState() throws RemoteException {
        return whiteboardState;
    }

    /**
     * Deregisters a client callback stub from the server.
     *
     * @param username the username of the client to deregister
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public synchronized void deregisterCallback(String username) throws RemoteException {
        clients.remove(username);
        activeUsers.remove(username);
        LOGGER.log(Level.INFO, "Client deregistered: {0}", username);
        updateAllClientsUserList();
    }
}
