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
        
        // Добавляем дополнительные проходы для разнообразия (но сохраняем проходимость)
        addAdditionalPassages();
    }
    
    /**
     * Добавляет дополнительные проходы в лабиринт для разнообразия
     * Гарантирует, что лабиринт остаётся проходимым
     */
    private void addAdditionalPassages() {
        // Добавляем случайные проходы (около 10% от размера лабиринта)
        int additionalPassages = (width * height) / 10;
        
        for (int i = 0; i < additionalPassages; i++) {
            // Выбираем случайную позицию для стены
            int wallX = random.nextInt(width * 2 - 1) + 1;
            int wallY = random.nextInt(height * 2 - 1) + 1;
            
            // Проверяем, что это стена и не граничная
            if (walls[wallY][wallX] && 
                wallX > 1 && wallX < width * 2 - 1 &&
                wallY > 1 && wallY < height * 2 - 1) {
                
                // Проверяем, что по обе стороны от стены есть проходы
                boolean canRemove = false;
                
                // Горизонтальная стена
                if (wallY % 2 == 0 && wallX % 2 == 1) {
                    if (!walls[wallY - 1][wallX] && !walls[wallY + 1][wallX]) {
                        canRemove = true;
                    }
                }
                // Вертикальная стена
                else if (wallX % 2 == 0 && wallY % 2 == 1) {
                    if (!walls[wallY][wallX - 1] && !walls[wallY][wallX + 1]) {
                        canRemove = true;
                    }
                }
                
                if (canRemove) {
                    walls[wallY][wallX] = false;
                }
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
    
    /**
     * Находит случайную проходимую клетку в лабиринте
     */
    public int[] findRandomPassableCell() {
        java.util.List<int[]> passableCells = new java.util.ArrayList<>();
        
        // Собираем все проходимые клетки
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isWall(x, y)) {
                    passableCells.add(new int[]{x, y});
                }
            }
        }
        
        // Выбираем случайную, но не слишком близко к краям
        if (passableCells.isEmpty()) {
            return new int[]{width / 2, height / 2};
        }
        
        // Фильтруем клетки, которые не слишком близко к стартовым позициям
        java.util.List<int[]> validCells = new java.util.ArrayList<>();
        for (int[] cell : passableCells) {
            int x = cell[0];
            int y = cell[1];
            // Исключаем углы (стартовые позиции игроков)
            if (!(x == 0 && y == 0) && !(x == width - 1 && y == height - 1)) {
                validCells.add(cell);
            }
        }
        
        if (validCells.isEmpty()) {
            validCells = passableCells;
        }
        
        return validCells.get(random.nextInt(validCells.size()));
    }
}

