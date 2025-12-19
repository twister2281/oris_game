package game.ui;

import game.model.GameState;
import game.model.Player;
import game.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class CompassPanel extends JPanel {
    private GameState gameState;
    private int playerId;
    private double currentAngle;
    private double targetAngle;
    private Timer animationTimer;
    
    public CompassPanel() {
        setPreferredSize(new Dimension(Constants.COMPASS_PANEL_SIZE, Constants.COMPASS_PANEL_SIZE));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createTitledBorder("Compass"));
        
        currentAngle = 0;
        targetAngle = 0;
        
        // Таймер для плавной анимации стрелки
        animationTimer = new Timer(16, e -> {
            updateAnimation();
            repaint();
        });
        animationTimer.start();
    }
    
    public void setGameState(GameState gameState, int playerId) {
        this.gameState = gameState;
        this.playerId = playerId;
    }
    
    private void updateAnimation() {
        // Плавное вращение стрелки к целевому углу
        double diff = targetAngle - currentAngle;
        
        // Нормализуем разницу в диапазон [-180, 180]
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        
        // Плавное движение (интерполяция)
        currentAngle += diff * 0.1;
        
        // Нормализуем угол
        while (currentAngle >= 360) currentAngle -= 360;
        while (currentAngle < 0) currentAngle += 360;
    }
    
    private void calculateTargetAngle() {
        if (gameState == null || !gameState.isGameStarted()) {
            targetAngle = 0;
            return;
        }
        
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            targetAngle = 0;
            return;
        }
        
        int playerX = player.getX();
        int playerY = player.getY();
        int exitX = gameState.getExitX();
        int exitY = gameState.getExitY();
        
        // Вычисляем угол к выходу
        double dx = exitX - playerX;
        double dy = exitY - playerY;
        
        // Угол в радианах (0 = вверх, по часовой стрелке)
        double angleRad = Math.atan2(dx, -dy);
        // Конвертируем в градусы
        targetAngle = Math.toDegrees(angleRad);
        
        // Нормализуем в диапазон [0, 360)
        if (targetAngle < 0) {
            targetAngle += 360;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(getWidth(), getHeight()) / 2 - 20;
        
        // Рисуем круг компаса
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Рисуем метки направлений
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        
        String[] directions = {"N", "E", "S", "W"};
        int[] angles = {0, 90, 180, 270};
        
        for (int i = 0; i < directions.length; i++) {
            double angleRad = Math.toRadians(angles[i]);
            int x = centerX + (int)((radius - 15) * Math.sin(angleRad));
            int y = centerY - (int)((radius - 15) * Math.cos(angleRad));
            
            String dir = directions[i];
            int textWidth = fm.stringWidth(dir);
            int textHeight = fm.getHeight();
            g2d.drawString(dir, x - textWidth / 2, y + textHeight / 4);
        }
        
        // Обновляем целевой угол
        calculateTargetAngle();
        
        // Рисуем стрелку
        AffineTransform oldTransform = g2d.getTransform();
        
        // Перемещаем в центр и поворачиваем
        g2d.translate(centerX, centerY);
        g2d.rotate(Math.toRadians(currentAngle));
        
        // Рисуем стрелку (треугольник)
        int arrowSize = radius - 10;
        int[] xPoints = {0, -arrowSize / 3, arrowSize / 3};
        int[] yPoints = {-arrowSize, arrowSize / 2, arrowSize / 2};
        
        g2d.setColor(new Color(200, 0, 0));
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 3);
        
        g2d.setTransform(oldTransform);
        
        // Рисуем центр компаса
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillOval(centerX - 5, centerY - 5, 10, 10);
    }
}







