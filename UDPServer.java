
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    public static final int SERVER_PORT = 12345;
    public static final int BUFFER_SIZE = 1024;

    private Map<String, Integer> clientPorts = new HashMap<>();
    private Map<String, InetAddress> clientaddr = new HashMap<>();

    public void startServer() {
        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("Server is running on port " + SERVER_PORT);

            while (true) {
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress senderaddr = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();

                if (message.startsWith("/join")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) {
                        String username = parts[1];
                        clientPorts.put(username, senderPort);
                        clientaddr.put(username,senderaddr);
                        String joiningMsg = username + " joined chat";
                        for (Map.Entry<String, Integer> entry : clientPorts.entrySet()) {
                            if (entry.getValue() != senderPort | clientaddr.get(entry.getKey())!=senderaddr) {
                                sendToClient(socket, joiningMsg, clientaddr.get(entry.getKey()), entry.getValue());
                            }
                        }
                    }
                } else if (message.startsWith("/quit")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) {
                        String username = parts[1];
                        clientPorts.remove(username);
                        clientaddr.remove(username);
                        String leftMsg = username + " left the chat";
                        for (Map.Entry<String, Integer> entry : clientPorts.entrySet()) {

                            sendToClient(socket, leftMsg, clientaddr.get(entry.getKey()), entry.getValue());

                        }
                    }
                } else {
                    for (Map.Entry<String, Integer> entry : clientPorts.entrySet()) {

                        sendToClient(socket, message, clientaddr.get(entry.getKey()), entry.getValue());

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToClient(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort)
            throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.startServer();
    }
}
