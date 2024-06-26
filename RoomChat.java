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

    public void sendMsg(String usrName, String msg) throws RemoteException 
    {
        System.out.println("Mensagem enviada: " + msg);
        for (IUserChat user : userList.values()) 
        {
            user.deliverMsg(usrName, msg);
        }
    }

    public void joinRoom(String usrName, IUserChat user) throws RemoteException 
    {
        System.out.println("Usuario " + usrName + " entrou na sala " + roomName);
        userList.put(usrName, user);
    }

    public void leaveRoom(String usrName) throws RemoteException 
    {
        userList.remove(usrName);
    }

    public void closeRoom() throws RemoteException 
    {
        for (IUserChat user : userList.values()) 
        {
            user.deliverMsg("Servidor", "Sala fechada pelo servidor");
        }
        userList.clear();
    }

    public String getRoomName() throws RemoteException 
    {
        return roomName;
    }
}
