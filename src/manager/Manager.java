package manager;

import main.Message;
import main.Message.PDUType;
import main.OID;
import util.ConnectionUtil;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Manager {
    private static final int POLLING_PORT = 5000;
    private static final int TRAP_PORT = 5001;
    private ConnectionUtil pollingConnection;
    private ConnectionUtil trapConnection;
    private boolean running;
    private Thread trapListenerThread;
    private Thread responseListenerThread;
    private ConcurrentHashMap<InetAddress, AgentInfo> agents;
    private final Object pollingLock = new Object();

    private static class AgentInfo {
        public final int port;
        public final String description;

        public AgentInfo(int port, String description) {
            this.port = port;
            this.description = description;
        }
    }

    public Manager() throws SocketException {
        pollingConnection = new ConnectionUtil(POLLING_PORT);
        trapConnection = new ConnectionUtil(TRAP_PORT);
        running = true;
        agents = new ConcurrentHashMap<>();
    }

    public void start() {
        // Start trap listener thread
        trapListenerThread = new Thread(this::listenForTraps);
        trapListenerThread.start();
        
        // Start response listener thread
        responseListenerThread = new Thread(this::listenForResponses);
        responseListenerThread.start();
    }

    private void listenForTraps() {
        while (running) {
            try {
                Message trap = trapConnection.receiveMessage();
                handleTrap(trap);
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error receiving trap: " + e.getMessage());
                }
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
        System.out.println("Received trap: " + trap);
        
        if (trap.getPduType() == PDUType.TRAP && trap.getOid().equals(OID.CONNECTION_STATUS)) {
            try {
                String[] parts = trap.getValue().split(":");
                InetAddress agentAddress = InetAddress.getByName(parts[0]);
                int agentPort = Integer.parseInt(parts[1]);
                
                agents.put(agentAddress, new AgentInfo(agentPort, "Agent at " + trap.getValue()));
                System.out.println("New agent connected: " + trap.getValue());
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
            for (InetAddress agentAddress : agents.keySet()) {
                AgentInfo agentInfo = agents.get(agentAddress);
                
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
                        pollOID(agentAddress, agentInfo.port, oid);
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
