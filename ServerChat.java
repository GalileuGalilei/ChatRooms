import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ServerChat extends UnicastRemoteObject implements IServerChat
{
    private ArrayList<String> roomList;
    private Map<String, IRoomChat> rooms;
    
    public ServerChat() throws RemoteException
    {
        roomList = new ArrayList<>();
        rooms = new HashMap<>();
    }
    
    public synchronized ArrayList<String> getRooms() throws RemoteException 
    {
        return new ArrayList<>(roomList);
    }

    public synchronized void createRoom(String roomName) throws RemoteException 
    {
        if (!roomList.contains(roomName)) 
        {
            IRoomChat newRoom = new RoomChat(roomName);
            roomList.add(roomName);
            rooms.put(roomName, newRoom);
        }
    }

    public IRoomChat getRoom(String roomName) throws RemoteException 
    {
        return rooms.get(roomName);
    }

    public static void main(String[] args) 
    {
        int port = 1099;
        String host = "localhost";

        try 
        {
            IServerChat server = new ServerChat();
            LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://" + host + '/', server);
            System.out.println("Server is ready.");
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}