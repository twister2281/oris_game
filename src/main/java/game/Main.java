package game;

import game.ui.GameWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            // Спрашиваем, запускать как сервер или клиент
            int choice = JOptionPane.showOptionDialog(
                null,
                "Запустить как:",
                "Гонка в лабиринте",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Сервер", "Клиент"},
                "Сервер"
            );
            
            boolean isServer = (choice == JOptionPane.YES_OPTION || choice == JOptionPane.CLOSED_OPTION);
            
            GameWindow window = new GameWindow(isServer);
            window.setVisible(true);
        });
    }
}

