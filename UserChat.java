import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.*;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String usrName;
    private IServerChat server;
    private IRoomChat currentRoom;
    private String host;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private Container roomPanel;

    protected UserChat(String usrName, IServerChat server, String host) throws RemoteException {
        this.usrName = usrName;
        this.server = server;
        this.host = host;
        setupUI();
    }

    private void setupUI() throws RemoteException {
        frame = new JFrame("Server Chat");
        
        // Message area to show user messages

        Panel messagePanel = new Panel();
        messagePanel.setPreferredSize(new Dimension(200, 100));
        
        chatArea = new JTextArea();
        chatArea.setPreferredSize(new Dimension(200, 380));
        chatArea.setEditable(false);
        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(200, 20));
        //when enter is pressed
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        sendMessage(inputField.getText());
                        inputField.setText("");
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        messagePanel.add(chatArea, BorderLayout.NORTH);
        messagePanel.add(inputField, BorderLayout.SOUTH);
        frame.add(messagePanel, BorderLayout.EAST);

        // Room panel to show available rooms
        roomPanel = new JPanel();
        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(roomPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Control panel to create rooms
        JPanel controlPanel = new JPanel();
        //setflowlayout
        controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        final JTextField roomNameField = new JTextField();
        roomNameField.setPreferredSize(new Dimension(200, 30));
        JButton createRoomButton = new JButton("Create Rooms");
        createRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String roomName = roomNameField.getText().trim();
                    if (!roomName.isEmpty()) {
                        server.createRoom(roomName);
                        updateRoomList();
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton refreshRoomList = new JButton("Refresh Rooms");
        refreshRoomList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    updateRoomList();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        controlPanel.add(roomNameField);
        controlPanel.add(createRoomButton);
        controlPanel.add(refreshRoomList);
        frame.add(controlPanel, BorderLayout.SOUTH);


        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        updateRoomList();
    }

    private void updateRoomList() throws RemoteException {
        roomPanel.removeAll();
        for (String room : server.getRooms()) {
            final String roomName = room;
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            
            JLabel roomLabel = new JLabel(roomName);
            panel.add(roomLabel);
    
            // Join room button
            JButton joinButton = new JButton("Join");
            joinButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        joinRoom(roomName);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            panel.add(joinButton);
    
            // Leave room button
            JButton leaveButton = new JButton("Leave");
            leaveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        currentRoom.leaveRoom(usrName);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            panel.add(leaveButton);
            panel.setSize(new Dimension(500, 50));
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
            roomPanel.add(panel);
            
        }
        roomPanel.revalidate();
        roomPanel.repaint();
    }

    public void deliverMsg(String senderName, String msg) throws RemoteException {
        chatArea.append(senderName + ": " + msg + "\n");
    }

    public void listRooms() throws RemoteException {
        ArrayList<String> rooms = server.getRooms();
        String roomList = "Rooms:\n";
        for (String room : rooms) {
            roomList += room + "\n";
        }
        JOptionPane.showMessageDialog(frame, roomList);
    }

    public void joinRoom(String roomName) throws RemoteException {
        if (currentRoom != null) {
            currentRoom.leaveRoom(usrName);
        }

        try {
            System.out.println(host + '/' + roomName);
            currentRoom = (IRoomChat) Naming.lookup(host + '/' + roomName);
        } catch (Exception e) {
            currentRoom = null;
            e.printStackTrace();
        }

        if (currentRoom == null) {
            JOptionPane.showMessageDialog(frame, "Room " + roomName + " not found.");
            return;
        }

        currentRoom.joinRoom(usrName, (IUserChat)this);
        System.out.println("Joining room " + roomName);
        chatArea.setText(""); // Clear the chat area for the new room
    }

    public void sendMessage(String msg) throws RemoteException {
        if (currentRoom != null) {
            currentRoom.sendMsg(usrName, msg);
        } else {
            JOptionPane.showMessageDialog(frame, "You are not in a room.");
        }
    }

    public static void main(String[] args) 
    {
        String host = "rmi://192.168.199.152:2020";

        try {
            IServerChat server = (IServerChat) Naming.lookup(host + '/' + "Servidor");

            String usrName = JOptionPane.showInputDialog("Enter your name:");
            if (usrName == null || usrName.trim().isEmpty()) {
                System.exit(0);
            }

            UserChat client = new UserChat(usrName, server, host);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
