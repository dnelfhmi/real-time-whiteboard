package client;

import javafx.application.Platform;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.ClientCallback;
import gui.WhiteboardApp;

/**
 * The ClientController class manages the interaction between the client GUI and the whiteboard server.
 * It handles user registration, drawing commands, messages, board management actions.
 */
public class ClientController extends UnicastRemoteObject implements ClientCallback, Serializable {
    private static final Logger LOGGER = Logger.getLogger(ClientController.class.getName());
    private final WhiteboardApp app;
    private final WhiteboardClient client;
    private final boolean isManager;
    private boolean isApproved = false;
    private final Object lock = new Object();

    /**
     * Gets the associated WhiteboardApp instance.
     *
     * @return the WhiteboardApp instance
     */
    public WhiteboardApp getApp() {
        return app;
    }

    /**
     * Constructs a new ClientController.
     *
     * @param app the WhiteboardApp instance associated with this controller
     * @param serverAddress the address of the whiteboard server
     * @param serverPort the port of the whiteboard server
     * @param username the username of the client
     * @param isManager whether the client is a manager
     * @throws RemoteException if a remote communication error occurs
     */
    public ClientController(WhiteboardApp app, String serverAddress, int serverPort, String username, boolean isManager) throws RemoteException {
        super();
        this.app = app;
        this.client = new WhiteboardClient(serverAddress, serverPort);
        this.isManager = isManager;
        this.registerUser(username, this, isManager);
    }

    /**
     * Sets the approval status of the client.
     *
     * @param approved the approval status
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void setApproved(boolean approved) throws RemoteException {
        synchronized (lock) {
            this.isApproved = approved;
            lock.notifyAll();
        }
        if (approved) {
            Platform.runLater(() -> {
                app.initializeMainUI(app.getPrimaryStage());
                fetchAndApplyWhiteboardState();
            });
        }
    }

    /**
     * Registers the user with the whiteboard server.
     *
     * @param username the username to register
     * @param client the client callback
     * @param isManager whether the client is a manager
     * @throws RemoteException if a remote communication error occurs
     */
    public void registerUser(String username, ClientCallback client, boolean isManager) throws RemoteException {
        try {
            LOGGER.log(Level.INFO, "Client: Registering user: {0}, Controller type: {1}, isManager: {2}", new Object[]{username, this.getClass().getSimpleName(), isManager});
            this.client.getStub().registerUser(username, client, isManager);
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Error registering user: " + username, e);
            e.printStackTrace();
        }
    }

    /**
     * Notifies the manager for user approval.
     *
     * @param username the username to notify the manager about
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void notifyManager(String username) throws RemoteException {
        Platform.runLater(() -> {
            app.showApprovalDialog(username);
        });
    }

    /**
     * Requests approval for a user.
     *
     * @param username the username to request approval for
     * @throws RemoteException if a remote communication error occurs
     */
    public void requestApproval(String username) throws RemoteException {
        client.getStub().approveClient(username);
        LOGGER.log(Level.INFO, "Client: Requested approval for user: {0}", username);
    }

    /**
     * Approves a user.
     *
     * @param username the username to approve
     */
    public void approveUser(String username) {
        try {
            client.getStub().approveClient(username);
            this.isApproved = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refuses a user.
     *
     * @param username the username to refuse
     */
    public void refuseUser(String username) {
        try {
            client.getStub().refuseClient(username);
            this.isApproved = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the canvas.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateCanvasClear() throws RemoteException {
        Platform.runLater(() -> {
            app.clearCanvas();
        });
    }

    /**
     * Waits for approval.
     *
     * @return true if approved, false if interrupted
     */
    public boolean waitForApproval() {
        synchronized (lock) {
            while (!isApproved) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Updates the user list in the GUI.
     *
     * @param users the array of usernames to update
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateUserList(String[] users) throws RemoteException {
        Platform.runLater(() -> {
            app.updateUserList(users);
        });
    }

    /**
     * Sends a canvas action to the server.
     *
     * @param action the canvas action to send
     */
    public void sendCanvasAction(String action) {
        try {
            client.getStub().canvasAction(action);
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Error sending draw action to server", e);
        }
    }

    /**
     * Updates the canvas with a given action.
     *
     * @param action the canvas action to apply
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateCanvas(String action) throws RemoteException {
        LOGGER.log(Level.INFO, "Client: Received drawing action: {0}", action);
        Platform.runLater(() -> {
            app.updateCanvas(action);
            LOGGER.log(Level.INFO, "Client: Updated canvas with action: {0}", action);
        });
    }

    /**
     * Clears the canvas on the server and updates the local state.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    public void clearCanvas() throws RemoteException {
        client.getStub().clearCanvas();
        Platform.runLater(() -> app.clearCanvas());
    }

    /**
     * Notifies the client with a message and exits.
     *
     * @param message the notification message
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void notify(String message) throws RemoteException {
        app.showNotificationAndExit(message);
    }

    /**
     * Deregisters the client and performs client-side cleanup.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void deregister() throws RemoteException {
        Platform.runLater(() -> app.showNotificationAndExit("The whiteboard session is closing. You will be disconnected."));
    }

    /**
     * Receives a chat message and updates the chat in the GUI.
     *
     * @param message the chat message to receive
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void receiveMessage(String message) throws RemoteException {
        Platform.runLater(() -> app.updateChat(message));
    }

    /**
     * Sends a chat message to the server.
     *
     * @param message the chat message to send
     * @throws RemoteException if a remote communication error occurs
     */
    public void sendMessage(String message) throws RemoteException {
        String fullMessage = app.getUsername() + ": " + message;
        client.getStub().sendMessage(fullMessage);
    }

    /**
     * Fetches the current state of the whiteboard from the server and applies it locally.
     */
    public void fetchAndApplyWhiteboardState() {
        try {
            String state = client.getStub().getWhiteboardState();
            if (state != null && !state.isEmpty()) {
                String[] actions = state.split("\n");
                for (String action : actions) {
                    updateCanvas(action);
                }
            }
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Error fetching whiteboard state", e);
        }
    }

    /**
     * Creates a new whiteboard if the client is a manager.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    public void createNewBoard() throws RemoteException {
        if (isManager) {
            client.getStub().createNewBoard();
        } else {
            LOGGER.log(Level.WARNING, "Only the manager can create a new board.");
        }
    }

    /**
     * Opens an existing whiteboard from a file if the client is a manager.
     *
     * @param filePath the path to the file to open
     * @throws RemoteException if a remote communication error occurs
     */
    public void openBoard(String filePath) throws RemoteException {
        if (isManager) {
            client.getStub().openBoard(filePath);
        } else {
            LOGGER.log(Level.WARNING, "Only the manager can open a board.");
        }
    }

    /**
     * Saves the current whiteboard to a file if the client is a manager.
     *
     * @param filePath the path to the file to save
     * @throws RemoteException if a remote communication error occurs
     */
    public void saveBoard(String filePath) throws RemoteException {
        if (isManager) {
            client.getStub().saveBoard(filePath);
        } else {
            LOGGER.log(Level.WARNING, "Only the manager can save a board.");
        }
    }

    /**
     * Closes the current whiteboard if the client is a manager.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    public void closeBoard() throws RemoteException {
        if (isManager) {
            client.getStub().closeBoard();
        } else {
            LOGGER.log(Level.WARNING, "Only the manager can close a board.");
        }
    }

    /**
     * Kicks a user from the whiteboard session if the client is a manager.
     *
     * @param username the username of the user to kick
     * @throws RemoteException if a remote communication error occurs
     */
    public void kickUser(String username) throws RemoteException {
        if (isManager) {
            client.getStub().kickUser(username);
        } else {
            LOGGER.log(Level.WARNING, "Only the manager can kick out a user.");
        }
    }

    /**
     * Deregisters the client and removes it from the active users list.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    public void deregisterClient() throws RemoteException {
        client.getStub().deregisterCallback(app.getUsername());
    }
}

