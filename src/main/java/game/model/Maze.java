package game.model;

import java.util.Random;
import java.util.Stack;

public class Maze {
    private boolean[][] walls; // true = стена, false = проход
    private int width;
    private int height;
    private Random random;
    private long seed;
    
    public Maze(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.random = new Random(seed);
        this.walls = new boolean[height * 2 + 1][width * 2 + 1];
        generate();
    }
    
    public Maze(int width, int height) {
        this(width, height, System.currentTimeMillis());
    }
    
    private void generate() {
        // Инициализация: все клетки - стены
        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < walls[i].length; j++) {
                walls[i][j] = true;
            }
        }
        
        // Используем алгоритм рекурсивного backtracking
        Stack<int[]> stack = new Stack<>();
        int startX = 1;
        int startY = 1;
        
        // Начальная клетка
        walls[startY][startX] = false;
        stack.push(new int[]{startX, startY});
        
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];
            
            // Находим непосещённых соседей
            int[][] neighbors = getUnvisitedNeighbors(x, y);
            
            if (neighbors.length > 0) {
                // Выбираем случайного соседа
                int[] next = neighbors[random.nextInt(neighbors.length)];
                int nx = next[0];
                int ny = next[1];
                
                // Убираем стену между текущей и следующей клеткой
                int wallX = (x + nx) / 2;
                int wallY = (y + ny) / 2;
                walls[wallY][wallX] = false;
                walls[ny][nx] = false;
                
                stack.push(new int[]{nx, ny});
            } else {
                stack.pop();
            }
        }
    }
    
    private int[][] getUnvisitedNeighbors(int x, int y) {
        java.util.List<int[]> neighbors = new java.util.ArrayList<>();
        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx > 0 && nx < width * 2 && ny > 0 && ny < height * 2) {
                if (walls[ny][nx]) {
                    neighbors.add(new int[]{nx, ny});
                }
            }
        }
        
        return neighbors.toArray(new int[0][]);
    }
    
    public boolean isWall(int x, int y) {
        // Конвертируем координаты игрока в координаты лабиринта
        int mazeX = x * 2 + 1;
        int mazeY = y * 2 + 1;
        
        if (mazeX < 0 || mazeX >= walls[0].length || mazeY < 0 || mazeY >= walls.length) {
            return true; // Вне границ - стена
        }
        
        return walls[mazeY][mazeX];
    }
    
    public boolean canMove(int x, int y, String direction) {
        int newX = x;
        int newY = y;
        
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
        
        // Проверяем границы
        if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
            return false;
        }
        
        return !isWall(newX, newY);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public long getSeed() {
        return seed;
    }
    
    public boolean[][] getWalls() {
        return walls;
    }
}

