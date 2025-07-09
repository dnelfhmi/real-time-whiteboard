package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * WhiteboardInterface defines the methods for remote communication between
 * the whiteboard server and clients. It allows users to draw, register,
 * send messages, and manage the whiteboard session.
 */
public interface WhiteboardInterface extends Remote {
    void canvasAction(String action) throws RemoteException;
    void clearCanvas() throws RemoteException;
    void registerUser(String username, ClientCallback client, boolean isManager) throws RemoteException;
    void sendMessage(String message) throws RemoteException;
    void kickUser(String username) throws RemoteException;
    void createNewBoard() throws RemoteException;
    void openBoard(String filePath) throws RemoteException;
    void saveBoard(String filePath) throws RemoteException;
    void closeBoard() throws RemoteException;
    void approveClient(String username) throws RemoteException;
    void refuseClient(String username) throws RemoteException;
    void registerCallback(String username, ClientCallback callback) throws RemoteException;
    String getWhiteboardState() throws RemoteException;
    void deregisterCallback(String username) throws RemoteException;
}
