import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    public RoomChat(String roomName) throws RemoteException 
    {
        this.roomName = roomName;
        this.userList = new HashMap<>();
    }

    public synchronized void sendMsg(String usrName, String msg) throws RemoteException 
    {
        for (IUserChat user : userList.values()) 
        {
            user.deliverMsg(usrName, msg);
        }
    }

    public synchronized void joinRoom(String usrName, IUserChat user) throws RemoteException 
    {
        userList.put(usrName, user);
    }

    public synchronized void leaveRoom(String usrName) throws RemoteException 
    {
        userList.remove(usrName);
    }

    public synchronized void closeRoom() throws RemoteException 
    {
        userList.clear();
    }

    public String getRoomName() throws RemoteException 
    {
        return roomName;
    }
}
