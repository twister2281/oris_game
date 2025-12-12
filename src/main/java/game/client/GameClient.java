package game.client;

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

public class GameClient extends Thread {
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameState gameState;
    private ClientMessageListener listener;
    private boolean running;
    private int playerId;
    
    public interface ClientMessageListener {
        void onGameStart(int playerId, long mazeSeed, int startX, int startY, int exitX, int exitY);
        void onPositionUpdate(int playerId, int x, int y, String direction);
        void onGameEnd(int winnerId, long time);
    }
    
    public GameClient(String host, int port, ClientMessageListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        this.running = false;
        this.gameState = new GameState();
    }
    
    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = true;
            
            System.out.println("Connected to server: " + host + ":" + port);
            
            // Обрабатываем сообщения от сервера
            while (running) {
                Message message = ProtocolParser.receiveMessage(in);
                if (message == null) {
                    break;
                }
                
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error in client: " + e.getMessage());
            if (listener != null) {
                // Уведомляем о разрыве соединения
            }
        } finally {
            close();
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case GAME_START:
                handleGameStart(message);
                break;
            case PLAYER_POSITION:
                handlePositionUpdate(message);
                break;
            case GAME_END:
                handleGameEnd(message);
                break;
        }
    }
    
    private void handleGameStart(Message message) {
        String[] data = message.getData();
        if (data.length < 6) {
            return;
        }
        
        try {
            int pid = Integer.parseInt(data[0]);
            long seed = Long.parseLong(data[1]);
            int startX = Integer.parseInt(data[2]);
            int startY = Integer.parseInt(data[3]);
            int exitX = Integer.parseInt(data[4]);
            int exitY = Integer.parseInt(data[5]);
            
            this.playerId = pid;
            
            // Инициализируем состояние игры
            Maze maze = new Maze(Constants.MAZE_WIDTH, Constants.MAZE_HEIGHT, seed);
            gameState.initialize(maze, exitX, exitY);
            
            Player player = new Player(pid, startX, startY);
            gameState.addPlayer(player);
            
            if (listener != null) {
                listener.onGameStart(pid, seed, startX, startY, exitX, exitY);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid game start data: " + e.getMessage());
        }
    }
    
    private void handlePositionUpdate(Message message) {
        String[] data = message.getData();
        if (data.length < 4) {
            return;
        }
        
        try {
            int pid = Integer.parseInt(data[0]);
            int x = Integer.parseInt(data[1]);
            int y = Integer.parseInt(data[2]);
            String direction = data[3];
            
            Player player = gameState.getPlayer(pid);
            if (player != null) {
                player.setPosition(x, y);
                player.setDirection(direction);
                
                if (listener != null) {
                    listener.onPositionUpdate(pid, x, y, direction);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid position data: " + e.getMessage());
        }
    }
    
    private void handleGameEnd(Message message) {
        String[] data = message.getData();
        if (data.length < 2) {
            return;
        }
        
        try {
            int winnerId = Integer.parseInt(data[0]);
            long time = Long.parseLong(data[1]);
            
            if (listener != null) {
                listener.onGameEnd(winnerId, time);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid game end data: " + e.getMessage());
        }
    }
    
    public void sendMove(String direction) {
        if (out != null && running) {
            String[] data = {
                String.valueOf(playerId),
                direction
            };
            Message msg = new Message(MessageType.PLAYER_MOVE, data);
            ProtocolParser.sendMessage(out, msg);
        }
    }
    
    public void requestSync() {
        if (out != null && running) {
            String[] data = {String.valueOf(playerId)};
            Message msg = new Message(MessageType.SYNC_REQUEST, data);
            ProtocolParser.sendMessage(out, msg);
        }
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void stopClient() {
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
    }
}


