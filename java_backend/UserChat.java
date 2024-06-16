package java_backend;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
//package java-backend;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String usrName;
    private IServerChat server;
    private IRoomChat currentRoom;
    private Registry registry;
    private PrintWriter out;

    protected UserChat(String usrName, IServerChat server, PrintWriter out, Registry registry) throws RemoteException 
    {
        this.usrName = usrName;
        this.server = server;
        this.registry = registry;
        this.out = out;
    }

    public void deliverMsg(String senderName, String msg) throws RemoteException 
    {
        this.out.println("message:" + senderName + ">>  " + msg);
    }

    public void listRooms() throws RemoteException 
    {
        ArrayList<String> rooms = server.getRooms();

        String roomList = "rooms: ";
        for (String room : rooms) 
        {
            roomList += room + ",";
        }

        this.out.println(roomList);
    }

    public void joinRoom(String roomName) throws RemoteException
    {
        if (currentRoom != null) 
        {
            currentRoom.leaveRoom(usrName);
        }

        try 
        {    
            currentRoom = (IRoomChat) registry.lookup(roomName);
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

    public static void main(String[] args) 
    {
        String localhost = "localhost";
        int electronPort = 3000;
        String serverHost = "26.3.100.186";
        int serverPort = 1099;

        try 
        {
            Socket socket = new ServerSocket(electronPort).accept();
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connectted to electronPort");

            Registry remoteRegistry = LocateRegistry.getRegistry(serverHost, serverPort);
            IServerChat server = (IServerChat) remoteRegistry.lookup("Servidor");
            UserChat client = null;

            while (in.hasNextLine())
            {
                String line = in.nextLine();
                String parts[] = line.split(":");
                String command = parts[0];
                String argument = parts[1];

                switch (command) 
                {
                    case "name":
                        client = new UserChat(argument, server, out, remoteRegistry);
                        System.out.println("User " + argument + " connected.");
                        break;

                    case "room":
                        if (client != null) 
                        {
                            client.joinRoom(argument);
                            System.out.println("User " + client.usrName + " joined room " + argument);
                        }
                        break;

                    case "msg":
                        if (client != null) 
                        {
                            client.sendMessage(argument);
                            System.out.println("User " + client.usrName + " sent message: " + argument);
                        }
                        break;
                
                    default:
                        System.out.println("Invalid command.");
                        break;
                }
            }

            socket.close();
            in.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
