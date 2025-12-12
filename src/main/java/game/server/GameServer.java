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
        System.out.println("Server started on port " + port);
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
        System.out.println("Waiting for server player to be ready...");
    }
    
    private void waitForClient() {
        try {
            System.out.println("Waiting for client to connect...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
            
            ClientHandler clientHandler = new ClientHandler(clientSocket, this, 2);
            clients.add(clientHandler);
            clientHandler.start();
        } catch (IOException e) {
            System.err.println("Error accepting client: " + e.getMessage());
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
        
        // Генерируем лабиринт
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
        
        // Генерируем случайный выход на проходимой клетке с минимальным расстоянием 10-15
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = Constants.MAZE_WIDTH - 1;
        int startY2 = Constants.MAZE_HEIGHT - 1;
        int[] exitPos = maze.findRandomPassableCell(startX1, startY1, startX2, startY2);
        int exitX = exitPos[0];
        int exitY = exitPos[1];
        
        // КРИТИЧЕСКАЯ ПРОВЕРКА: Убеждаемся, что финиш достижим от обеих стартовых позиций
        // Если нет, создаём пути
        boolean[][] gameCells = maze.convertToGameCellsForCheck();
        if (!maze.isReachableForCheck(gameCells, startX1, startY1, exitX, exitY)) {
            maze.createPathToPositionForCheck(gameCells, startX1, startY1, exitX, exitY);
        }
        if (!maze.isReachableForCheck(gameCells, startX2, startY2, exitX, exitY)) {
            maze.createPathToPositionForCheck(gameCells, startX2, startY2, exitX, exitY);
        }
        maze.updateFromGameCells(gameCells);
        
        gameState.initialize(maze, exitX, exitY);
        gameInitialized = true;
        
        // Отправляем информацию о начале игры всем клиентам
        for (ClientHandler client : clients) {
            client.sendGameStart();
        }
        
        System.out.println("Game initialized!");
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
        System.out.println("Client disconnected: Player " + client.getPlayerId());
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
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
}

