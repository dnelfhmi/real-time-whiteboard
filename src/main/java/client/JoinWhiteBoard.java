package client;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import gui.WhiteboardApp;

/**
 * JoinWhiteBoard is a JavaFX application that allows a user to join an existing whiteboard session.
 * The user is not the manager of the whiteboard session.
 */
public class JoinWhiteBoard extends WhiteboardApp {

    private static final Logger LOGGER = Logger.getLogger(JoinWhiteBoard.class.getName());

    /**
     * Starts the JavaFX application for joining a whiteboard.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        try {
            controller = new ClientController(this, serverAddress, serverPort, username, false);
            new Thread(() -> {
                if (controller.waitForApproval()) {
                    Platform.runLater(() -> {
                        controller.getApp().initializeMainUI(primaryStage);
                        controller.fetchAndApplyWhiteboardState(); // Fetch and apply the current whiteboard state
                    });
                } else {
                    Platform.runLater(() -> showError(primaryStage, "Failed to join whiteboard or not approved."));
                }
            }).start();
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Error initializing client controller", e);
            Platform.runLater(() -> showError(primaryStage, "Failed to initialize connection."));
        }
    }

    /**
     * Displays an error message in the primary stage.
     *
     * @param primaryStage the primary stage for this application
     * @param message the error message to display
     */
    private void showError(Stage primaryStage, String message) {
        Scene errorScene = new Scene(new Label(message), 300, 200);
        primaryStage.setScene(errorScene);
    }

    /**
     * The main method to launch the JoinWhiteBoard application.
     *
     * @param args command-line arguments: server IP address, server port, and username
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java gui.JoinWhiteBoard <serverIPAddress> <serverPort> <username>");
            System.exit(1);
        }
        System.setProperty("isManager", "false");
        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        username = args[2];
        LOGGER.log(Level.INFO, "Launching JoinWhiteBoard with serverAddress={0}, serverPort={1}, username={2}",
                new Object[]{serverAddress, serverPort, username});
        launch(args);
    }
}
