package game.ui;

import game.client.GameClient;
import game.model.GameState;
import game.model.Maze;
import game.model.Player;
import game.server.GameServer;
import game.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class GameWindow extends JFrame implements GameClient.ClientMessageListener {
    private GamePanel gamePanel;
    private CompassPanel compassPanel;
    private InfoPanel infoPanel;
    
    private GameServer server;
    private GameClient client;
    private GameState gameState;
    private int playerId;
    private boolean isServer;
    private boolean gameStarted;
    
    public GameWindow(boolean isServer) {
        this.isServer = isServer;
        this.gameStarted = false;
        this.gameState = new GameState();
        
        initializeUI();
        setupNetwork();
        setupKeyboard();
    }
    
    private void initializeUI() {
        setTitle("Maze Race - " + (isServer ? "Server" : "Client"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Создаём панели
        gamePanel = new GamePanel();
        compassPanel = new CompassPanel();
        infoPanel = new InfoPanel();
        
        // Размещаем панели
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(compassPanel, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(Constants.COMPASS_PANEL_SIZE + 20, Constants.GAME_PANEL_HEIGHT));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(gamePanel, BorderLayout.CENTER);
        centerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(centerPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Обработка закрытия окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }
    
    private void setupNetwork() {
        if (isServer) {
            // Запускаем сервер
            try {
                server = new GameServer(Constants.DEFAULT_PORT);
                server.addServerPlayer();
                playerId = 1;
                
                // Устанавливаем listener для уведомлений о завершении игры
                server.setGameEndListener((winnerId, time) -> {
                    SwingUtilities.invokeLater(() -> {
                        onGameEnd(winnerId, time);
                    });
                });
                
                // Запускаем сервер в отдельном потоке
                new Thread(() -> {
                    server.start();
                }).start();
                
                infoPanel.setConnectionStatus(true);
                infoPanel.setStatus("Server started. Waiting for client...");
                
                // Периодически проверяем инициализацию игры
                new Thread(() -> {
                    while (server != null && !gameStarted) {
                        try {
                            Thread.sleep(500);
                            GameState serverState = server.getGameState();
                            if (serverState != null && serverState.isGameStarted()) {
                                SwingUtilities.invokeLater(() -> {
                                    initializeServerGameState(serverState);
                                });
                                break;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }).start();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error starting server: " + e.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error starting server: " + e.getMessage());
            }
        } else {
            // Подключаемся как клиент
            String host = JOptionPane.showInputDialog(this, "Enter server address:", "localhost");
            if (host == null || host.trim().isEmpty()) {
                host = "localhost";
            }
            
            client = new GameClient(host, Constants.DEFAULT_PORT, this);
            client.start();
            
            infoPanel.setConnectionStatus(true);
            infoPanel.setStatus("Connecting to server...");
        }
    }
    
    private void initializeServerGameState(GameState serverState) {
        this.gameState = serverState;
        playerId = 1;
        
        if (gameState != null && gameState.isGameStarted()) {
            // Обновляем UI
            gamePanel.setGameState(gameState, playerId);
            compassPanel.setGameState(gameState, playerId);
            
            gameStarted = true;
            infoPanel.startTimer();
            infoPanel.setStatus("Game started! Use arrow keys or WASD to move.");
            
            gamePanel.requestFocus();
            repaint();
        }
    }
    
    private void setupKeyboard() {
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameStarted) {
                    return;
                }
                
                String direction = null;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        direction = "UP";
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        direction = "DOWN";
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        direction = "LEFT";
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        direction = "RIGHT";
                        break;
                }
                
                if (direction != null) {
                    handleMove(direction);
                }
            }
        });
    }
    
    private void handleMove(String direction) {
        if (isServer) {
            // Сервер обрабатывает движение локально
            handleServerMove(direction);
        } else {
            // Клиент отправляет движение на сервер
            if (client != null) {
                client.sendMove(direction);
            }
        }
    }
    
    private void handleServerMove(String direction) {
        if (gameState == null || !gameState.isGameStarted()) {
            return;
        }
        
        Player player = gameState.getPlayer(playerId);
        if (player == null || player.isFinished()) {
            return;
        }
        
        Maze maze = gameState.getMaze();
        if (maze.canMove(player.getX(), player.getY(), direction)) {
            int newX = player.getX();
            int newY = player.getY();
            
            switch (direction) {
                case "UP":
                    newY--;
                    break;
                case "DOWN":
                    newY++;
                    break;
                case "LEFT":
                    newX--;
                    break;
                case "RIGHT":
                    newX++;
                    break;
            }
            
            player.setPosition(newX, newY);
            player.setDirection(direction);
            
            // Отправляем обновление позиции клиентам
            if (server != null) {
                server.broadcastPosition(playerId, newX, newY, direction);
            }
            
            if (gameState.checkWin(playerId, newX, newY)) {
                infoPanel.showWinner(playerId, gameState.getElapsedTime());
                infoPanel.stopTimer();
                if (server != null) {
                    server.broadcastGameEnd(playerId, gameState.getElapsedTime());
                }
            }
            
            gamePanel.repaint();
            compassPanel.repaint();
        }
    }
    
    // Реализация ClientMessageListener
    @Override
    public void onGameStart(int pid, long mazeSeed, int startX, int startY, int exitX, int exitY) {
        SwingUtilities.invokeLater(() -> {
            playerId = pid;
            
            // Инициализируем состояние игры
            Maze maze = new Maze(Constants.MAZE_WIDTH, Constants.MAZE_HEIGHT, mazeSeed);
            gameState.initialize(maze, exitX, exitY);
            
            Player player = new Player(pid, startX, startY);
            gameState.addPlayer(player);
            
            // Обновляем UI
            gamePanel.setGameState(gameState, playerId);
            compassPanel.setGameState(gameState, playerId);
            
            gameStarted = true;
            infoPanel.startTimer();
            infoPanel.setStatus("Game started! Use arrow keys or WASD to move.");
            
            gamePanel.requestFocus();
            repaint();
        });
    }
    
    @Override
    public void onPositionUpdate(int pid, int x, int y, String direction) {
        SwingUtilities.invokeLater(() -> {
            if (gameState != null && !gameState.isGameEnded()) {
                Player player = gameState.getPlayer(pid);
                if (player != null && !player.isFinished()) {
                    player.setPosition(x, y);
                    player.setDirection(direction);
                    
                    gamePanel.repaint();
                    compassPanel.repaint();
                }
            }
        });
    }
    
    @Override
    public void onGameEnd(int winnerId, long time) {
        SwingUtilities.invokeLater(() -> {
            handleGameEnd(winnerId, time);
        });
    }
    
    private void handleGameEnd(int winnerId, long time) {
        gameStarted = false;
        infoPanel.showWinner(winnerId, time);
        infoPanel.stopTimer();
        
        if (gameState != null) {
            Player winner = gameState.getPlayer(winnerId);
            if (winner != null) {
                winner.finish(time);
            }
        }
        
        gamePanel.repaint();
    }
    
    public void updateGameState(GameState newState) {
        this.gameState = newState;
        if (gameState != null && gameState.isGameStarted()) {
            gamePanel.setGameState(gameState, playerId);
            compassPanel.setGameState(gameState, playerId);
        }
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    private void cleanup() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stopClient();
        }
    }
}

