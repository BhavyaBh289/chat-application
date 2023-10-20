
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

public class UDPClient {
    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private String username;

    private JTextArea chatTextArea;
    private JTextField inputTextField;
    private JButton sendButton;
    private JFrame frame;

    public UDPClient() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            initGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGUI() {
        frame = new JFrame(username + "'s chat window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        chatTextArea = new JTextArea("Enter /quit to exit\n");
        chatTextArea.setEditable(false);

        // Increase font size
        Font font = new Font("Arial", Font.PLAIN, 16);
        chatTextArea.setFont(font);

        // Create a rounded border and set padding
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10), // Padding
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 2, 2, 2, Color.GRAY), // Outer border
                        BorderFactory.createMatteBorder(10, 10, 10, 10, new Color(240, 240, 240)) // Inner background
                ));
        chatTextArea.setBorder(roundedBorder);

        frame.add(new JScrollPane(chatTextArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputTextField = new JTextField();
        sendButton = new JButton("Send");
        inputTextField.setPreferredSize(new Dimension(200, 30));
        sendButton.setPreferredSize(new Dimension(100, 30));

        inputPanel.add(inputTextField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.setVisible(true);
        inputTextField.requestFocus();
    }

    public void sendMessage() {
        String message = inputTextField.getText();
        if (!message.isEmpty()) {
            if (message.equalsIgnoreCase("/quit")) {
                String quitMessage = "/quit " + username;
                sendToServer(quitMessage);
                socket.close();
                System.exit(0);
            } else {
                String formattedMessage = username + ": " + message;
                sendToServer(formattedMessage);
                inputTextField.setText("");
            }
        }
    }

    private void sendToServer(String message) {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient() {
        username = JOptionPane.showInputDialog("Enter your username:");
        frame.setTitle(username + "'s chat window");

        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username cannot be empty.");
            System.exit(0);
        }

        String joinMessage = "/join " + username;
        sendToServer(joinMessage);

        Thread receiverThread = new Thread(() -> {
            try {
                while (true) {
                    byte[] receiveData = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    SwingUtilities.invokeLater(() -> {
                        chatTextArea.append(receivedMessage + "\n");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UDPClient client = new UDPClient();
            client.startClient();
        });
    }
}
