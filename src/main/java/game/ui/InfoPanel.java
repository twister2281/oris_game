package game.ui;

import game.utils.Constants;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    private long startTime;
    private boolean gameStarted;
    private Timer timer;
    
    public InfoPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.INFO_PANEL_HEIGHT));
        setBackground(new Color(240, 240, 240));
        
        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        statusLabel = new JLabel("Waiting for game to start...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        connectionLabel = new JLabel("Disconnected");
        connectionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        connectionLabel.setForeground(Color.RED);
        
        add(timeLabel);
        add(Box.createHorizontalStrut(30));
        add(statusLabel);
        add(Box.createHorizontalStrut(30));
        add(connectionLabel);
        
        timer = new Timer(100, e -> updateTime());
    }
    
    public void startTimer() {
        gameStarted = true;
        startTime = System.currentTimeMillis();
        timer.start();
    }
    
    public void stopTimer() {
        timer.stop();
    }
    
    public void updateTime() {
        if (gameStarted) {
            long elapsed = System.currentTimeMillis() - startTime;
            long seconds = elapsed / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        }
    }
    
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
    
    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionLabel.setText("Connected");
            connectionLabel.setForeground(new Color(0, 150, 0));
        } else {
            connectionLabel.setText("Disconnected");
            connectionLabel.setForeground(Color.RED);
        }
    }
    
    public void showWinner(int winnerId, long time) {
        timer.stop();
        long seconds = time / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (winnerId > 0) {
            statusLabel.setText(String.format("Player %d won! Time: %02d:%02d", winnerId, minutes, seconds));
            statusLabel.setForeground(new Color(0, 150, 0));
        } else {
            statusLabel.setText("Game ended");
        }
    }
}







