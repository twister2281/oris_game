package game.model;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private Maze maze;
    private Map<Integer, Player> players;
    private int exitX;
    private int exitY;
    private boolean gameStarted;
    private boolean gameEnded;
    private long startTime;
    private int winnerId;
    
    public GameState() {
        this.players = new HashMap<>();
        this.gameStarted = false;
        this.gameEnded = false;
    }
    
    public void initialize(Maze maze, int exitX, int exitY) {
        this.maze = maze;
        this.exitX = exitX;
        this.exitY = exitY;
        this.gameStarted = true;
        this.startTime = System.currentTimeMillis();
    }
    
    public Maze getMaze() {
        return maze;
    }
    
    public Map<Integer, Player> getPlayers() {
        return players;
    }
    
    public Player getPlayer(int id) {
        return players.get(id);
    }
    
    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }
    
    public int getExitX() {
        return exitX;
    }
    
    public int getExitY() {
        return exitY;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isGameEnded() {
        return gameEnded;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getElapsedTime() {
        if (gameStarted) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    public void endGame(int winnerId) {
        this.gameEnded = true;
        this.winnerId = winnerId;
    }
    
    public int getWinnerId() {
        return winnerId;
    }
    
    public boolean checkWin(int playerId, int x, int y) {
        if (x == exitX && y == exitY) {
            if (!gameEnded) {
                endGame(playerId);
                Player player = players.get(playerId);
                if (player != null) {
                    player.finish(getElapsedTime());
                }
            }
            return true;
        }
        return false;
    }
}


