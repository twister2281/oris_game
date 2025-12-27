package game.server;

import game.model.GameState;
import game.model.Maze;
import game.model.Player;
import game.utils.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private GameState gameState;
    private boolean gameInitialized;
    private int nextPlayerId;
    private GameEndListener gameEndListener;
    
    public interface GameEndListener {
        void onGameEnd(int winnerId, long time);
    }
    
    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }
    
    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        nextPlayerId = 1;
        gameInitialized = false;
        System.out.println("Сервер запущен на порту " + port);
    }
    
    public void start() {
        // Ждём первого игрока (сервер)
        waitForServerPlayer();
        
        // Ждём второго игрока (клиент)
        waitForClient();
        
        // Инициализируем игру когда оба подключены
        initializeGame();
    }
    
    private void waitForServerPlayer() {
        // Серверный игрок уже здесь, создаём для него игрока с ID 1
        System.out.println("Ожидание готовности серверного игрока...");
    }
    
    private void waitForClient() {
        try {
            System.out.println("Ожидание подключения клиента...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Клиент подключен: " + clientSocket.getRemoteSocketAddress());
            
            ClientHandler clientHandler = new ClientHandler(clientSocket, this, 2);
            clients.add(clientHandler);
            clientHandler.start();
        } catch (IOException e) {
            System.err.println("Ошибка принятия клиента: " + e.getMessage());
        }
    }
    
    public void addServerPlayer() {
        // Создаём игрока для сервера (ID 1)
        if (gameState == null) {
            gameState = new GameState();
        }
        
        // Позиция будет установлена при инициализации игры
        Player serverPlayer = new Player(1, 0, 0);
        gameState.addPlayer(serverPlayer);
    }
    
    private void initializeGame() {
        if (gameInitialized || clients.size() < 1) {
            return;
        }
        
        // Создаём лабиринт с использованием seed для синхронизации с клиентом
        long seed = System.currentTimeMillis();
        Maze maze = new Maze(Constants.MAZE_WIDTH, Constants.MAZE_HEIGHT, seed);
        
        if (gameState == null) {
            gameState = new GameState();
        }
        
        // Устанавливаем стартовые позиции игроков
        Player player1 = gameState.getPlayer(1);
        if (player1 == null) {
            player1 = new Player(1, 0, 0);
            gameState.addPlayer(player1);
        }
        player1.setPosition(0, 0);
        
        Player player2 = gameState.getPlayer(2);
        if (player2 == null) {
            player2 = new Player(2, Constants.MAZE_WIDTH - 1, Constants.MAZE_HEIGHT - 1);
            gameState.addPlayer(player2);
        } else {
            player2.setPosition(Constants.MAZE_WIDTH - 1, Constants.MAZE_HEIGHT - 1);
        }
        
        // Генерируем случайную позицию финиша (минимум 10 блоков от стартовых позиций)
        int[] exitPos = maze.getFixedExitPosition();
        int exitX = exitPos[0];
        int exitY = exitPos[1];
        
        gameState.initialize(maze, exitX, exitY);
        gameInitialized = true;
        
        // Отправляем информацию о начале игры всем клиентам
        for (ClientHandler client : clients) {
            client.sendGameStart();
        }
        
        System.out.println("Игра инициализирована!");
    }
    
    public void broadcastPosition(int playerId, int x, int y, String direction) {
        // Обновляем позицию в gameState для серверного игрока
        if (gameState != null) {
            Player player = gameState.getPlayer(playerId);
            if (player != null) {
                player.setPosition(x, y);
                player.setDirection(direction);
            }
        }
        
        // Отправляем клиентам
        for (ClientHandler client : clients) {
            client.sendPositionUpdate(playerId, x, y, direction);
        }
    }
    
    public void broadcastGameEnd(int winnerId, long time) {
        // Уведомляем серверное окно
        if (gameEndListener != null) {
            gameEndListener.onGameEnd(winnerId, time);
        }
        
        // Отправляем клиентам
        for (ClientHandler client : clients) {
            client.sendGameEnd(winnerId, time);
        }
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Клиент отключен: Игрок " + client.getPlayerId());
    }
    
    public void stop() {
        try {
            for (ClientHandler client : clients) {
                client.stopHandler();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка остановки сервера: " + e.getMessage());
        }
    }
}

