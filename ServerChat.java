import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerChat extends UnicastRemoteObject implements IServerChat
{
    private ArrayList<String> roomList;
    private Map<String, IRoomChat> rooms;
    Registry registry;
    
    public ServerChat(Registry registry) throws RemoteException
    {
        roomList = new ArrayList<>();
        rooms = new HashMap<>();
        this.registry = registry;

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
            try 
            {
                registry.rebind(roomName, newRoom);
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }

    public IRoomChat getRoom(String roomName) throws RemoteException 
    {
        return rooms.get(roomName);
    }

    public static void main(String[] args) 
    {
        int port = 3000;

        try 
        {
            Registry registry = LocateRegistry.createRegistry(port);
            IServerChat server = new ServerChat(registry);
            registry.rebind("Servidor", server);
            System.out.println("Server is ready.");
            System.out.println(registry);
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}