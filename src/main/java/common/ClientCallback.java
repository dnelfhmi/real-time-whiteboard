package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The ClientCallback interface defines the methods that a client must implement
 * to receive callbacks from the whiteboard server.
 */
public interface ClientCallback extends Remote {
    void updateUserList(String[] users) throws RemoteException;
    void updateCanvas(String action) throws RemoteException;
    void notify(String message) throws RemoteException;
    void notifyManager(String message) throws RemoteException;
    void deregister() throws RemoteException;
    void receiveMessage(String message) throws RemoteException;
    void registerUser(String username, ClientCallback client, boolean isManager) throws RemoteException;
    void setApproved(boolean approved) throws RemoteException;
    void updateCanvasClear() throws RemoteException;
}
