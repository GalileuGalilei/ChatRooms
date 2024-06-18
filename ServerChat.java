import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private Map<String, IRoomChat> rooms;
    private Registry registry;
    private JFrame frame;
    private JPanel roomPanel;
    private JTextArea messageArea;
    private String host;

    public ServerChat(String host) throws RemoteException {
        roomList = new ArrayList<>();
        rooms = new HashMap<>();
        setupUI();
        this.host = host;
    }

    private void setupUI() throws RemoteException {
        frame = new JFrame("Server Chat");
        
        // Message area to show user messages
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPaneMessage = new JScrollPane(messageArea);
        scrollPaneMessage.setPreferredSize(new Dimension(200, 100));
        frame.add(scrollPaneMessage, BorderLayout.EAST);

        // Room panel to show available rooms
        roomPanel = new JPanel();
        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(roomPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Control panel to create rooms
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        final JTextField roomNameField = new JTextField();
        JButton createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String roomName = roomNameField.getText().trim();
                    if (!roomName.isEmpty()) {
                        createRoom(roomName);
                        updateRoomList();
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        controlPanel.add(roomNameField, BorderLayout.CENTER);
        controlPanel.add(createRoomButton, BorderLayout.EAST);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        updateRoomList();
    }

    private void updateRoomList() throws RemoteException {
        roomPanel.removeAll();
        for (String room : getRooms()) {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            
            JLabel roomLabel = new JLabel(room);
            panel.add(roomLabel);
    
            // Close room button
            JButton closeButton = new JButton("Close");
            final String roomName = room;
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        closeRoom(roomName);
                        updateRoomList();
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            panel.add(closeButton);
            panel.setSize(new Dimension(500, 50));
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
            roomPanel.add(panel);
            
        }
        roomPanel.revalidate();
        roomPanel.repaint();
    }
    
    

    public synchronized ArrayList<String> getRooms() throws RemoteException {
        return new ArrayList<>(roomList);
    }

    public synchronized void createRoom(String roomName) throws RemoteException {
        if (!roomList.contains(roomName)) {
            IRoomChat newRoom = new RoomChat(roomName);
            roomList.add(roomName);
            rooms.put(roomName, newRoom);
            try {
                Naming.rebind(host + '/' + roomName, newRoom);
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateRoomList();
        }
    }

    public IRoomChat getRoom(String roomName) throws RemoteException {
        return rooms.get(roomName);
    }

    public synchronized void closeRoom(String roomName) throws RemoteException {
        if (roomList.contains(roomName)) 
        {
            IRoomChat room = rooms.get(roomName);
            room.closeRoom();
            roomList.remove(roomName);
            rooms.remove(roomName);
            try {
                Naming.unbind(host + '/' + roomName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String host = "rmi://192.168.199.105:2020";
        int port = 2020;

        try {
            LocateRegistry.createRegistry(port);
            IServerChat server = new ServerChat(host);
            Naming.rebind(host + '/' + "Servidor", server);
            System.out.println("Server is ready at adress");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
