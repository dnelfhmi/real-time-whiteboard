package client;

import javafx.stage.Stage;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import gui.WhiteboardApp;

/**
 * CreateWhiteBoard is a JavaFX application that allows a user to create and manage a whiteboard.
 * The user becomes the manager of the whiteboard session.
 */
public class CreateWhiteBoard extends WhiteboardApp {

    private static final Logger LOGGER = Logger.getLogger(CreateWhiteBoard.class.getName());

    /**
     * Starts the JavaFX application for creating a whiteboard.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        try {
            controller = new ClientController(this, serverAddress, serverPort, username, true);
            LOGGER.log(Level.INFO, "Whiteboard manager started username {0} create a whiteboard", username);
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Error starting whiteboard", e);
            e.printStackTrace();
        }
    }

    /**
     * The main method to launch the CreateWhiteBoard application.
     *
     * @param args command-line arguments: server IP address, server port, and username
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java gui.CreateWhiteBoard <serverIPAddress> <serverPort> <username>");
            System.exit(1);
        }
        System.setProperty("isManager", "true");
        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        username = args[2];
        LOGGER.log(Level.INFO, "Launching CreateWhiteBoard with serverAddress={0}, serverPort={1}, username={2}",
                new Object[]{serverAddress, serverPort, username});
        try {
            launch(args);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error launching CreateWhiteBoard application", e);
            e.printStackTrace();
        }
    }
}
