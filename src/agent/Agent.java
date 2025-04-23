package agent;

import main.Message;
import main.Message.PDUType;
import main.OID;
import util.ConnectionUtil;
import java.net.*;
import java.io.*;

public class Agent {
    private static final int BASE_PORT = 5002; // Start port for agents
    private static final int MAX_PORT_ATTEMPTS = 100; // Maximum number of ports to try
    private ConnectionUtil connection;
    private boolean running;
    private Thread requestListenerThread;
    private Thread monitoringThread;
    private InetAddress managerAddress;
    private boolean connected;
    private long startTime; // For uptime calculation
    private int agentPort; // The port this agent is using

    public Agent() throws SocketException {
        agentPort = findAvailablePort();
        connection = new ConnectionUtil(agentPort);
        running = true;
        connected = false;
        try {
            managerAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new SocketException("Failed to resolve localhost: " + e.getMessage());
        }
        startTime = System.currentTimeMillis();
    }

    public Agent(String managerHost) throws UnknownHostException, SocketException {
        agentPort = findAvailablePort();
        connection = new ConnectionUtil(agentPort);
        running = true;
        connected = false;
        managerAddress = InetAddress.getByName(managerHost);
        startTime = System.currentTimeMillis();
    }

    private int findAvailablePort() throws SocketException {
        for (int port = BASE_PORT; port < BASE_PORT + MAX_PORT_ATTEMPTS; port++) {
            try {
                DatagramSocket testSocket = new DatagramSocket(port);
                testSocket.close();
                return port;
            } catch (SocketException e) {
                // Port is in use, try next one
                continue;
            }
        }
        throw new SocketException("Could not find available port after " + MAX_PORT_ATTEMPTS + " attempts");
    }

    public void start() {
        // Send connection request
        try {
            sendConnectionRequest();
        } catch (IOException e) {
            System.err.println("Failed to send connection request: " + e.getMessage());
            return;
        }

        // Start request listener thread
        requestListenerThread = new Thread(this::listenForRequests);
        requestListenerThread.start();
        
        // Start monitoring thread
        monitoringThread = new Thread(this::monitorSystem);
        monitoringThread.start();
    }

    private void sendConnectionRequest() throws IOException {
        // Send a trap to notify the manager of our existence
        String localAddress = InetAddress.getLocalHost().getHostAddress();
        String agentInfo = localAddress + ":" + agentPort;
        Message connectionTrap = new Message("public", PDUType.TRAP, 
                                          OID.CONNECTION_STATUS, agentInfo);
        connection.sendMessage(connectionTrap, managerAddress, 5001); // Manager's trap port
        System.out.println("Sent connection request to manager from port " + agentPort);
    }

    private void listenForRequests() {
        while (running) {
            try {
                Message request = connection.receiveMessage();
                // Handle request in a new thread
                new Thread(() -> handleRequest(request)).start();
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error receiving request: " + e.getMessage());
                }
            }
        }
    }

    protected void handleRequest(Message request) {
        try {
            System.out.println("Received request: " + request);

            // Check if this is a connection confirmation
            if (request.getPduType() == PDUType.GET_RESPONSE && 
                request.getOid().equals(OID.CONNECTION_STATUS) &&
                request.getValue().equals("connected")) {
                connected = true;
                System.out.println("Successfully connected to manager");
                return;
            }

            // Handle regular requests based on OID
            String responseValue = getValueForOID(request.getOid());
            Message response = new Message("public", PDUType.GET_RESPONSE, 
                                         request.getOid(), responseValue);
            sendResponse(response, managerAddress);
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
        }
    }

    private String getValueForOID(String oid) {
        // Return appropriate values based on the OID
        switch (oid) {
            case OID.SYSTEM_DESCRIPTION:
                return "Demo SNMP Agent on port " + agentPort;
            case OID.SYSTEM_UPTIME:
                long uptime = (System.currentTimeMillis() - startTime) / 1000;
                return String.valueOf(uptime);
            case OID.SYSTEM_CONTACT:
                return "admin@demo.com";
            case OID.INTERFACE_COUNT:
                return "2";
            case OID.INTERFACE_STATUS:
                return "up";
            case OID.INTERFACE_SPEED:
                return "1000";
            case OID.CPU_USAGE:
                return String.valueOf((int)(Math.random() * 100));
            case OID.MEMORY_USAGE:
                return String.valueOf((int)(Math.random() * 100));
            case OID.DISK_USAGE:
                return String.valueOf((int)(Math.random() * 100));
            default:
                return "Unknown OID";
        }
    }

    private void sendResponse(Message message, InetAddress address) throws IOException {
        connection.sendMessage(message, address, 5000); // Manager's polling port
    }

    protected void monitorSystem() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (running) {
                    System.err.println("Error during monitoring: " + e.getMessage());
                }
            }
        }
    }

    public void sendTrap(Message trap) throws IOException {
        if (managerAddress != null) {
            connection.sendMessage(trap, managerAddress, 5001); // Manager's trap port
        }
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        try {
            Agent agent = new Agent("localhost");
            agent.start();
        } catch (Exception e) {
            System.err.println("Error starting agent: " + e.getMessage());
        }
    }
}
