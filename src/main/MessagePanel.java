package main;

import javax.swing.*;
import java.awt.*;

public class MessagePanel extends JPanel {
    private JTextArea messageArea;
    private JScrollPane scrollPane;

    public MessagePanel(String title) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(title));

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    public void clear() {
        messageArea.setText("");
    }
} 