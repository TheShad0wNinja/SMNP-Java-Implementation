package manager;

import main.Message;
import main.Message.PDUType;
import main.OID;
import main.MessagePanel;
import util.ConnectionUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ManagerUI extends JFrame {
    private Manager manager;
    private MessagePanel messagePanel;
    private JPanel controlPanel;
    private JButton startButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton pollButton;
    private JLabel statusLabel;
    private boolean isRunning;

    public ManagerUI() {
        setTitle("SNMP Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create components
        messagePanel = new MessagePanel("Messages");
        controlPanel = new JPanel();
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        clearButton = new JButton("Clear");
        pollButton = new JButton("Poll Devices");
        statusLabel = new JLabel("Status: Stopped");

        // Setup control panel
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(pollButton);
        controlPanel.add(clearButton);
        controlPanel.add(statusLabel);
        stopButton.setEnabled(false);
        pollButton.setEnabled(false);

        // Add components to frame
        add(messagePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Add action listeners
        startButton.addActionListener(e -> startManager());
        stopButton.addActionListener(e -> stopManager());
        clearButton.addActionListener(e -> messagePanel.clear());
        pollButton.addActionListener(e -> pollDevices());

        // Initialize manager & override the handle functions to display data in the UI
        try {
            manager = new Manager() {
                @Override
                protected void handleTrap(Message trap) {
                    super.handleTrap(trap);
                    SwingUtilities.invokeLater(() -> 
                        messagePanel.addMessage("Received trap: " + trap));
                }

                @Override
                protected void handleResponse(Message response) {
                    super.handleResponse(response);
                    SwingUtilities.invokeLater(() -> 
                        messagePanel.addMessage("Received response: " + response));
                }
            };
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(this, "Error initializing manager: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startManager() {
        if (!isRunning) {
            manager.start();
            isRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            pollButton.setEnabled(true);
            statusLabel.setText("Status: Running");
            messagePanel.addMessage("Manager started");
        }
    }

    private void stopManager() {
        if (isRunning) {
            manager.stop();
            isRunning = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            pollButton.setEnabled(false);
            statusLabel.setText("Status: Stopped");
            messagePanel.addMessage("Manager stopped");
        }
    }

    private void pollDevices() {
        if (isRunning) {
            messagePanel.addMessage("Polling devices...");
            manager.pollDevices();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManagerUI ui = new ManagerUI();
            ui.setVisible(true);
        });
    }
} 