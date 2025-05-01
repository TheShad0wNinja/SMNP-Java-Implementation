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

    /**
     * A function used to send a serializable object to another UDP client
     * @param msg The Serializable object
     * @param address The address of the receiver
     * @param port The port of the receiver
     * @param <T> The type of the serializable object to be sent
     */
    public <T extends Serializable> void sendMessage(T msg, InetAddress address, int port) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(baos);

        objectStream.writeObject(msg);
        objectStream.flush();

        byte[] bytes = baos.toByteArray();

        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, address, port);
        socket.send(datagramPacket);
    }

    /**
     * @return Returns the data of type <T>
     * @param <T> The type of data that will be received
     */
    public <T extends Serializable> T receiveMessage() throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, 1024);
        socket.receive(datagramPacket);

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream objectStream = new ObjectInputStream(bais);

        return (T) objectStream.readObject();
    }
}
