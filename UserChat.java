import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String usrName;
    private IServerChat server;
    private IRoomChat currentRoom;
    private Registry registry;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    protected UserChat(String usrName, IServerChat server, Registry registry) throws RemoteException {
        this.usrName = usrName;
        this.server = server;
        this.registry = registry;
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("Chat Room");
        chatArea = new JTextArea();
        inputField = new JTextField();

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage(inputField.getText());
                    inputField.setText("");
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.add(new JScrollPane(chatArea), "Center");
        frame.add(inputField, "South");

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void deliverMsg(String senderName, String msg) throws RemoteException {
        chatArea.append(senderName + ": " + msg + "\n");
    }

    public void listRooms() throws RemoteException {
        ArrayList<String> rooms = server.getRooms();
        JOptionPane.showMessageDialog(frame, "Available rooms: " + rooms);
    }

    public void joinRoom(String roomName) throws RemoteException {
        if (currentRoom != null) {
            currentRoom.leaveRoom(usrName);
        }

        try {
            currentRoom = (IRoomChat) registry.lookup(roomName);
        } catch (Exception e) {
            currentRoom = null;
            e.printStackTrace();
        }

        if (currentRoom == null) {
            JOptionPane.showMessageDialog(frame, "Room not found.");
            return;
        }

        currentRoom.joinRoom(usrName, this);
        chatArea.setText(""); // Clear the chat area for the new room
    }

    public void sendMessage(String msg) throws RemoteException {
        if (currentRoom != null) {
            currentRoom.sendMsg(usrName, msg);
        } else {
            JOptionPane.showMessageDialog(frame, "You are not in a room.");
        }
    }

    public static void main(String[] args) {
        String host = "26.3.100.186";
        int port = 3000;

        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            IServerChat server = (IServerChat) registry.lookup("Servidor");

            String usrName = JOptionPane.showInputDialog("Enter your name:");
            if (usrName == null || usrName.trim().isEmpty()) {
                System.exit(0);
            }

            UserChat client = new UserChat(usrName, server, registry);
            registry.rebind(usrName, client);

            while (true) {
                String[] options = {"List rooms", "Create room", "Join room"};
                int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "Chat Client",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                switch (choice) {
                    case 0:
                        client.listRooms();
                        break;
                    case 1:
                        String roomName = JOptionPane.showInputDialog("Enter room name to create:");
                        if (roomName != null && !roomName.trim().isEmpty()) {
                            server.createRoom(roomName);
                        }
                        break;
                    case 2:
                        roomName = JOptionPane.showInputDialog("Enter room name to join:");
                        if (roomName != null && !roomName.trim().isEmpty()) {
                            client.joinRoom(roomName);
                        }
                        break;
                    default:
                        System.exit(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
