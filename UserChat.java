import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String usrName;
    private IServerChat server;
    private IRoomChat currentRoom;
    private String host;

    protected UserChat(String usrName, IServerChat server, String host) throws RemoteException 
    {
        this.usrName = usrName;
        this.server = server;
        this.host = host;
    }

    public void deliverMsg(String senderName, String msg) throws RemoteException 
    {
        System.out.println(senderName + ": " + msg);
    }

    public void listRooms() throws RemoteException 
    {
        ArrayList<String> rooms = server.getRooms();
        System.out.println("Available rooms: " + rooms);
    }

    public void joinRoom(String roomName) throws RemoteException
    {
        if (currentRoom != null) 
        {
            currentRoom.leaveRoom(usrName);
        }

        try 
        {    
            //stub
            currentRoom = (IRoomChat) Naming.lookup("rmi://" + host + '/' + roomName);
        } 
        catch (Exception e) 
        {
            currentRoom = null;
            e.printStackTrace();
        }

        if (currentRoom == null) 
        {
            System.out.println("Room not found.");
            return;
        }

        currentRoom.joinRoom(usrName, this);
        RoomUI(this, currentRoom);
    }

    public void sendMessage(String msg) throws RemoteException 
    {
        if (currentRoom != null) 
        {
            currentRoom.sendMsg(usrName, msg);
        } 
        else 
        {
            System.out.println("You are not in a room.");
        }
    } 

    private void RoomUI(UserChat self, IRoomChat room) throws RemoteException
    {
        System.out.print("type 'exit' to leave the room\n");
        Scanner scanner = new Scanner(System.in);
        while(true)
        {
            System.out.print(">> ");
            String msg = scanner.nextLine();

            if(msg.equals("exit"))
            {
                room.leaveRoom(self.usrName);
                break;
            }

            room.sendMsg(self.usrName, msg);
        }
    }

    public static void main(String[] args) 
    {
        String host = "localhost";

        try 
        {
            IServerChat server = (IServerChat) Naming.lookup("rmi://" + host + '/' + "Servidor");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String usrName = scanner.nextLine();

            UserChat client = new UserChat(usrName, server, host);

            while (true) {
                System.out.println("Choose an option:");
                System.out.println("1. List rooms\n2. Create room\n3. Join room");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        client.listRooms();
                        break;
                    case 2:
                        System.out.print("Enter room name to create: ");
                        String roomName = scanner.nextLine();
                        server.createRoom(roomName);
                        break;
                    case 3:
                        System.out.print("Enter room name to join: ");
                        roomName = scanner.nextLine();
                        client.joinRoom(roomName);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
