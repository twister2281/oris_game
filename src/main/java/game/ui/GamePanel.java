package game.ui;

import game.model.GameState;
import game.model.Maze;
import game.model.Player;
import game.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GamePanel extends JPanel {
    private GameState gameState;
    private int playerId;
    private int viewOffsetX;
    private int viewOffsetY;
    
    public GamePanel() {
        setPreferredSize(new Dimension(Constants.GAME_PANEL_WIDTH, Constants.GAME_PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        
        viewOffsetX = 0;
        viewOffsetY = 0;
    }
    
    public void setGameState(GameState gameState, int playerId) {
        this.gameState = gameState;
        this.playerId = playerId;
        updateViewOffset();
    }
    
    private void updateViewOffset() {
        if (gameState == null || !gameState.isGameStarted()) {
            return;
        }
        
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return;
        }
        
        // Центрируем вид на игроке
        int cellSize = Constants.CELL_SIZE;
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        viewOffsetX = panelWidth / 2 - player.getX() * cellSize - cellSize / 2;
        viewOffsetY = panelHeight / 2 - player.getY() * cellSize - cellSize / 2;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gameState == null || !gameState.isGameStarted()) {
            drawWaiting(g);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Maze maze = gameState.getMaze();
        Player player = gameState.getPlayer(playerId);
        
        if (maze == null || player == null) {
            return;
        }
        
        updateViewOffset();
        
        int cellSize = Constants.CELL_SIZE;
        int visibilityRadius = Constants.VISIBILITY_RADIUS;
        
        // Рисуем видимую часть лабиринта
        int startX = Math.max(0, player.getX() - visibilityRadius);
        int endX = Math.min(maze.getWidth(), player.getX() + visibilityRadius + 1);
        int startY = Math.max(0, player.getY() - visibilityRadius);
        int endY = Math.min(maze.getHeight(), player.getY() + visibilityRadius + 1);
        
        // Фон (невидимая область)
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Рисуем видимые клетки
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int screenX = x * cellSize + viewOffsetX;
                int screenY = y * cellSize + viewOffsetY;
                
                if (maze.isWall(x, y)) {
                    // Стена
                    g2d.setColor(new Color(100, 100, 150));
                    g2d.fillRect(screenX, screenY, cellSize, cellSize);
                    g2d.setColor(new Color(80, 80, 120));
                    g2d.drawRect(screenX, screenY, cellSize, cellSize);
                } else {
                    // Проход
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.fillRect(screenX, screenY, cellSize, cellSize);
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawRect(screenX, screenY, cellSize, cellSize);
                }
            }
        }
        
        // Рисуем выход
        int exitX = gameState.getExitX();
        int exitY = gameState.getExitY();
        
        if (exitX >= startX && exitX < endX && exitY >= startY && exitY < endY) {
            int screenX = exitX * cellSize + viewOffsetX;
            int screenY = exitY * cellSize + viewOffsetY;
            
            // Пульсирующий эффект для выхода
            long time = System.currentTimeMillis();
            int pulse = (int)(Math.sin(time / 200.0) * 5 + 5);
            
            g2d.setColor(new Color(255, 215, 0, 200));
            g2d.fillOval(screenX + pulse, screenY + pulse, 
                        cellSize - pulse * 2, cellSize - pulse * 2);
            
            g2d.setColor(new Color(255, 165, 0));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(screenX + pulse, screenY + pulse, 
                        cellSize - pulse * 2, cellSize - pulse * 2);
        }
        
        // Рисуем игрока
        if (player.getX() >= startX && player.getX() < endX && 
            player.getY() >= startY && player.getY() < endY) {
            
            int screenX = player.getX() * cellSize + viewOffsetX;
            int screenY = player.getY() * cellSize + viewOffsetY;
            
            // Тело игрока (круг)
            g2d.setColor(new Color(0, 150, 255));
            g2d.fillOval(screenX + 5, screenY + 5, cellSize - 10, cellSize - 10);
            
            // Обводка
            g2d.setColor(new Color(0, 100, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(screenX + 5, screenY + 5, cellSize - 10, cellSize - 10);
            
            // Направление (маленький треугольник)
            int centerX = screenX + cellSize / 2;
            int centerY = screenY + cellSize / 2;
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            
            switch (player.getDirection()) {
                case "UP":
                    xPoints = new int[]{centerX, centerX - 5, centerX + 5};
                    yPoints = new int[]{centerY - 8, centerY + 2, centerY + 2};
                    break;
                case "DOWN":
                    xPoints = new int[]{centerX, centerX - 5, centerX + 5};
                    yPoints = new int[]{centerY + 8, centerY - 2, centerY - 2};
                    break;
                case "LEFT":
                    xPoints = new int[]{centerX - 8, centerX + 2, centerX + 2};
                    yPoints = new int[]{centerY, centerY - 5, centerY + 5};
                    break;
                case "RIGHT":
                    xPoints = new int[]{centerX + 8, centerX - 2, centerX - 2};
                    yPoints = new int[]{centerY, centerY - 5, centerY + 5};
                    break;
            }
            
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
        
        // Рисуем границы видимости (опционально)
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                                      0, new float[]{5, 5}, 0));
        int centerScreenX = getWidth() / 2;
        int centerScreenY = getHeight() / 2;
        int visibilitySize = visibilityRadius * cellSize * 2;
        g2d.drawOval(centerScreenX - visibilitySize / 2, centerScreenY - visibilitySize / 2, 
                    visibilitySize, visibilitySize);
    }
    
    private void drawWaiting(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Waiting for game to start...";
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(text, x, y);
    }
}



