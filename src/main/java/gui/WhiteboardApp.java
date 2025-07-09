package gui;

import drawing.CommandListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import client.ClientController;
import drawing.CanvasManager;

/**
 * The WhiteboardApp class is the main entry point for the whiteboard application.
 * It initializes the user interface and handles user interactions.
 */
public class WhiteboardApp extends Application implements CommandListener {
    private static final Logger LOGGER = Logger.getLogger(WhiteboardApp.class.getName());

    protected static ClientController controller;
    private CanvasManager canvasManager;
    private UIManager uiManager;
    protected static String serverAddress;
    protected static int serverPort;
    protected static String username;
    private Stage primaryStage;

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        boolean isManager = Boolean.parseBoolean(System.getProperty("isManager", "false"));
        if (isManager) {
            initializeMainUI(primaryStage);
        } else {
            // Setup a waiting screen for regular users
            Scene waitingScene = new Scene(new Label("Waiting for manager approval..."), 300, 200);
            primaryStage.setScene(waitingScene);
            primaryStage.show();
        }

        // Handle window close request
        primaryStage.setOnCloseRequest(event -> {
            try {
                if (controller != null) {
                    controller.deregisterClient();
                }
            } catch (RemoteException e) {
                LOGGER.log(Level.SEVERE, "Error deregistering client", e);
            }
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Gets the primary stage of the application.
     *
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Initializes the main user interface for the application.
     *
     * @param primaryStage the primary stage for this application
     */
    public void initializeMainUI(Stage primaryStage) {
        canvasManager = new CanvasManager(new Canvas(900, 650), this);
        uiManager = new UIManager(this, canvasManager);
        uiManager.initialize(primaryStage);
        primaryStage.setTitle("Whiteboard - " + username);
    }

    /**
     * Gets the client controller.
     *
     * @return the client controller
     */
    public ClientController getController() {
        return controller;
    }

    /**
     * Gets the username of the current user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * The main method to launch the WhiteboardApp application.
     *
     * @param args command-line arguments: server IP address, server port, and username
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java gui.WhiteboardApp <serverIPAddress> <serverPort> <username>");
            System.exit(1);
        }
        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        username = args[2];
        LOGGER.log(Level.INFO, "Launching WhiteboardApp with serverAddress={0}, serverPort={1}, username={2}",
                new Object[]{serverAddress, serverPort, username});
        launch(args);
    }

    /**
     * Handles the release of a drawing command.
     *
     * @param command the drawing command to handle
     */
    @Override
    public void onReleaseCommand(String command) {
        controller.sendCanvasAction(command);
    }

    /**
     * Updates the chat area with a new message.
     *
     * @param message the message to add to the chat area
     */
    public void updateChat(String message) {
        uiManager.updateChat(message);
    }

    /**
     * Updates the canvas with a given drawing command.
     *
     * @param drawCommand the drawing command to apply
     */
    public void updateCanvas(String drawCommand) {
        if (canvasManager != null) {
            canvasManager.updateCanvas(drawCommand);
        } else {
            LOGGER.log(Level.SEVERE, "CanvasManager is not initialized");
        }
    }

    /**
     * Updates the user list with the given array of usernames.
     *
     * @param users the array of usernames to display in the user list
     */
    public void updateUserList(String[] users) {
        uiManager.updateUserList(users);
    }

    /**
     * Shows an approval dialog for a user connection request.
     *
     * @param username the username of the user requesting connection
     */
    public void showApprovalDialog(String username) {
        uiManager.showApprovalDialog(username, decision -> {
            if (decision) {
                controller.approveUser(username);
            } else {
                controller.refuseUser(username);
            }
        });
    }

    /**
     * Clears the canvas.
     */
    public void clearCanvas() {
        canvasManager.clearCanvas();
    }

    /**
     * Shows a notification and exits the application.
     *
     * @param message the notification message to display
     */
    public void showNotificationAndExit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notification");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });
    }
}
