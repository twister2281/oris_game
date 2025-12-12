package game.server;

import game.model.GameState;
import game.model.Maze;
import game.model.Player;
import game.protocol.Message;
import game.protocol.MessageType;
import game.protocol.ProtocolParser;
import game.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private GameServer server;
    private PrintWriter out;
    private BufferedReader in;
    private int playerId;
    private boolean running;
    
    public ClientHandler(Socket socket, GameServer server, int playerId) {
        this.socket = socket;
        this.server = server;
        this.playerId = playerId;
        this.running = true;
        
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            // Отправляем информацию о начале игры
            sendGameStart();
            
            // Обрабатываем сообщения от клиента
            while (running) {
                Message message = ProtocolParser.receiveMessage(in);
                if (message == null) {
                    break;
                }
                
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            close();
        }
    }
    
    public void sendGameStart() {
        GameState gameState = server.getGameState();
        if (gameState != null && gameState.isGameStarted()) {
            Player player = gameState.getPlayer(playerId);
            if (player != null) {
                Maze maze = gameState.getMaze();
                String[] data = {
                    String.valueOf(playerId),
                    String.valueOf(maze.getSeed()),
                    String.valueOf(player.getX()),
                    String.valueOf(player.getY()),
                    String.valueOf(gameState.getExitX()),
                    String.valueOf(gameState.getExitY())
                };
                Message msg = new Message(MessageType.GAME_START, data);
                ProtocolParser.sendMessage(out, msg);
            }
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case PLAYER_MOVE:
                handlePlayerMove(message);
                break;
            case SYNC_REQUEST:
                sendGameStart();
                break;
        }
    }
    
    private void handlePlayerMove(Message message) {
        String[] data = message.getData();
        if (data.length < 2) {
            return;
        }
        
        try {
            int pid = Integer.parseInt(data[0]);
            String direction = data[1];
            
            if (pid == playerId) {
                GameState gameState = server.getGameState();
                Player player = gameState.getPlayer(pid);
                
                if (player != null && !player.isFinished()) {
                    // Проверяем возможность движения
                    if (gameState.getMaze().canMove(player.getX(), player.getY(), direction)) {
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
                        
                        // Проверяем победу
                        if (gameState.checkWin(pid, newX, newY)) {
                            // Отправляем сообщение о конце игры всем
                            server.broadcastGameEnd(pid, gameState.getElapsedTime());
                        } else {
                            // Отправляем обновление позиции всем клиентам
                            server.broadcastPosition(pid, newX, newY, direction);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid player ID in move message");
        }
    }
    
    public void sendMessage(Message message) {
        ProtocolParser.sendMessage(out, message);
    }
    
    public void sendPositionUpdate(int pid, int x, int y, String direction) {
        String[] data = {
            String.valueOf(pid),
            String.valueOf(x),
            String.valueOf(y),
            direction
        };
        Message msg = new Message(MessageType.PLAYER_POSITION, data);
        sendMessage(msg);
    }
    
    public void sendGameEnd(int winnerId, long time) {
        String[] data = {
            String.valueOf(winnerId),
            String.valueOf(time)
        };
        Message msg = new Message(MessageType.GAME_END, data);
        sendMessage(msg);
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void stopHandler() {
        running = false;
    }
    
    private void close() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        server.removeClient(this);
    }
}

