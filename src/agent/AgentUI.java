package agent;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.SocketException;
import main.Message;
import main.Message.PDUType;
import main.OID;

public class AgentUI extends Agent {
    private JFrame frame;
    private JTextArea messageArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton sendTrapButton;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public AgentUI() throws SocketException {
        super();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("SNMP Agent");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Create message area
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Agent");
        stopButton = new JButton("Stop Agent");
        clearButton = new JButton("Clear Messages");
        sendTrapButton = new JButton("Send Trap");
        
        startButton.addActionListener(e -> startAgent());
        stopButton.addActionListener(e -> stopAgent());
        clearButton.addActionListener(e -> messageArea.setText(""));
        sendTrapButton.addActionListener(e -> sendTrap());
        
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(sendTrapButton);
        buttonPanel.add(clearButton);
        
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        // Disable stop and trap buttons initially
        stopButton.setEnabled(false);
        sendTrapButton.setEnabled(false);
    }

    private void startAgent() {
        if (!isRunning.get()) {
            isRunning.set(true);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            sendTrapButton.setEnabled(true);
            new Thread(() -> {
                try {
                    start();
                    appendMessage("Agent started successfully");
                } catch (Exception e) {
                    appendMessage("Error starting agent: " + e.getMessage());
                    isRunning.set(false);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    sendTrapButton.setEnabled(false);
                }
            }).start();
        }
    }

    private void stopAgent() {
        if (isRunning.get()) {
            isRunning.set(false);
            stop();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            sendTrapButton.setEnabled(false);
            appendMessage("Agent stopped");
        }
    }

    private void sendTrap() {
        if (isRunning.get()) {
            try {
                Message trap = new Message("public", PDUType.TRAP, OID.ERROR_TYPE, "Test trap from agent");
                sendTrap(trap);
                appendMessage("Sent trap: " + trap);
            } catch (Exception e) {
                appendMessage("Error sending trap: " + e.getMessage());
            }
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    @Override
    protected void handleRequest(Message request) {
        appendMessage("Received request: " + request);
        super.handleRequest(request);
    }

    @Override
    protected void monitorSystem() {
        appendMessage("System monitoring started");
        super.monitorSystem();
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            AgentUI agentUI = new AgentUI();
            agentUI.show();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Failed to initialize agent: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 