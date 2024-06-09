package tutorial;


import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
        
public class Server implements Hello {
        
    public Server() {}

    public String sayHello() {
        return "Hello, world!";
    }
        
    public static void main(String args[]) {

        try {
            Server obj = new Server();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.bind("Hello", stub);

            System.err.println("Server ready");

            //print the ip and port of the server
            System.out.println("Server IP: " + java.net.InetAddress.getLocalHost().getHostAddress());

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
