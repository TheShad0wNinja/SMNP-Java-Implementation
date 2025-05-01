package manager;

import main.Message;
import main.Message.PDUType;
import main.OID;
import util.ConnectionUtil;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Manager {
    public static final int POLLING_PORT = 5000;
    public static final int TRAP_PORT = 5001;
    private final ConnectionUtil pollingConnection;
    private final ConnectionUtil trapConnection;
    private boolean running;
    private final ConcurrentHashMap<String, AgentInfo> agents;
    private final Object pollingLock = new Object();

    private record AgentInfo(int port, InetAddress address, String description) {}

    public Manager() throws SocketException {
        pollingConnection = new ConnectionUtil(POLLING_PORT);
        trapConnection = new ConnectionUtil(TRAP_PORT);
        running = true;
        agents = new ConcurrentHashMap<>();
    }

    public void start() {
        // Start a thread to listen to trap messages
        Thread trapListenerThread = new Thread(this::listenForTraps);
        trapListenerThread.start();

        // Start a thread to listen to responses from sent requests
        Thread responseListenerThread = new Thread(this::listenForResponses);
        responseListenerThread.start();
    }

    private void listenForTraps() {
        while (running) {
            try {
                Message trap = trapConnection.receiveMessage();
                new Thread(() -> handleTrap(trap)).start();
            } catch (Exception e) {
                if (running)
                    System.err.println("Error receiving trap: " + e.getMessage());
            }
        }
    }

    private void listenForResponses() {
        while (running) {
            try {
                Message response = pollingConnection.receiveMessage();
                handleResponse(response);
                synchronized (pollingLock) {
                    pollingLock.notify(); // Notify that we received a response
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error receiving response: " + e.getMessage());
                }
            }
        }
    }

    protected void handleTrap(Message trap) {
        if (trap.pduType() != PDUType.TRAP) {
            System.out.println("Received trap: " + trap);
            return;
        }

        System.out.println("Received trap: " + trap);

        // Handle Different Trap Types
        if (trap.oid().equals(OID.CONNECTION_STATUS)) {
            try {
                String[] parts = trap.value().split(":");
                InetAddress agentAddress = InetAddress.getByName(parts[0]);
                int agentPort = Integer.parseInt(parts[1]);
                
                agents.put(trap.value(), new AgentInfo(agentPort, agentAddress, "Agent at " + trap.value()));
                System.out.println("New agent connected: " + trap.value());
            } catch (Exception e) {
                System.err.println("Error handling connection request: " + e.getMessage());
            }
        }
    }

    protected void handleResponse(Message response) {
        System.out.println("Received response: " + response);
    }

    public void pollDevices() {
        if (!running) return;
        
        try {
            for (var agentInfo : agents.values()) {

                // Poll each OID and wait for response
                String[] oids = {
                    OID.SYSTEM_DESCRIPTION,
                    OID.SYSTEM_UPTIME,
                    OID.CPU_USAGE,
                    OID.MEMORY_USAGE,
                    OID.INTERFACE_STATUS
                };
                
                for (String oid : oids) {
                    synchronized (pollingLock) {
                        pollOID(agentInfo.address, agentInfo.port, oid);
                        pollingLock.wait(5000); // Wait up to 5 seconds for response
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Error during polling: " + e.getMessage());
            }
        }
    }

    private void pollOID(InetAddress agentAddress, int agentPort, String oid) throws IOException {
        Message pollMessage = new Message("public", PDUType.GET_REQUEST, oid, "");
        pollingConnection.sendMessage(pollMessage, agentAddress, agentPort);
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        try {
            Manager manager = new Manager();
            manager.start();
            
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.err.println("Error starting manager: " + e.getMessage());
        }
    }
}
