package util;

import java.net.*;
import java.io.*;

/**
 * A class used to easily manage sending and receiving of UDP datagrams
 */
public class ConnectionUtil {
    private DatagramSocket socket = null;
    private boolean socketReady = false;

    /**
     * @param port The port the datagram socket will bind to
     */
    public ConnectionUtil(int port) {
        try {
            socket = new DatagramSocket(port);
            socketReady = true;
        } catch (SocketException e) {
            System.out.println("Unable to create socket");
        }
    }

    public ConnectionUtil() {
        try {
            socket = new DatagramSocket();
            socketReady = true;
        } catch (SocketException e) {
            System.out.println("Unable to create socket");
        }
    }

    public <T extends Serializable> void sendMessage(T msg, InetAddress address, int port) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(baos);

        objectStream.writeObject(msg);
        objectStream.flush();

        byte[] bytes = baos.toByteArray();

        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, address, port);
        socket.send(datagramPacket);
    }

    public <T extends Serializable> T receiveMessage() throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, 1024);
        socket.receive(datagramPacket);

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream objectStream = new ObjectInputStream(bais);

        return (T) objectStream.readObject();
    }
}
