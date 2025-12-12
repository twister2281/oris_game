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
        // Создаём массив для игровых клеток (не внутренних координат лабиринта)
        // true = стена, false = проход
        boolean[][] gameCells = new boolean[height][width];
        
        // Инициализация: все клетки - стены
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                gameCells[i][j] = true; // Все клетки - стены
            }
        }
        
        // Используем алгоритм рекурсивного backtracking для создания проходимого лабиринта
        Stack<int[]> stack = new Stack<>();
        int startX = 0;
        int startY = 0;
        
        // Начальная клетка - проход
        gameCells[startY][startX] = false;
        stack.push(new int[]{startX, startY});
        
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];
            
            // Находим непосещённых соседей (через одну клетку)
            int[][] neighbors = getUnvisitedNeighborsSimple(x, y, gameCells);
            
            if (neighbors.length > 0) {
                // Выбираем случайного соседа
                int[] next = neighbors[random.nextInt(neighbors.length)];
                int nx = next[0];
                int ny = next[1];
                
                // Убираем стену между текущей и следующей клеткой
                int wallX = (x + nx) / 2;
                int wallY = (y + ny) / 2;
                gameCells[wallY][wallX] = false; // Убираем стену между клетками
                gameCells[ny][nx] = false; // Делаем следующую клетку проходом
                
                stack.push(new int[]{nx, ny});
            } else {
                stack.pop();
            }
        }
        
        // Добавляем дополнительные стены для сложности (но сохраняем проходимость)
        addWallsForComplexity(gameCells);
        
        // Конвертируем игровые клетки в формат внутреннего лабиринта
        convertToInternalFormat(gameCells);
    }
    
    private int[][] getUnvisitedNeighborsSimple(int x, int y, boolean[][] gameCells) {
        java.util.List<int[]> neighbors = new java.util.ArrayList<>();
        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                if (gameCells[ny][nx]) { // Если это стена (непосещённая)
                    neighbors.add(new int[]{nx, ny});
                }
            }
        }
        
        return neighbors.toArray(new int[0][]);
    }
    
    /**
     * Добавляет дополнительные стены для сложности, но проверяет проходимость
     * Гарантирует, что все клетки остаются достижимыми от стартовых позиций
     */
    private void addWallsForComplexity(boolean[][] gameCells) {
        // Собираем все проходимые клетки (кроме стартовых)
        java.util.List<int[]> passableCells = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!gameCells[y][x] && !(x == 0 && y == 0) && !(x == width - 1 && y == height - 1)) {
                    passableCells.add(new int[]{x, y});
                }
            }
        }
        
        // Добавляем случайные стены (около 25% от проходимых клеток)
        int wallsToAdd = Math.max(5, passableCells.size() / 4);
        int attempts = 0;
        int added = 0;
        
        // Перемешиваем список для случайности
        java.util.Collections.shuffle(passableCells, random);
        
        for (int[] cell : passableCells) {
            if (added >= wallsToAdd || attempts >= passableCells.size()) {
                break;
            }
            
            attempts++;
            int x = cell[0];
            int y = cell[1];
            
            // Временно ставим стену
            gameCells[y][x] = true;
            
            // Проверяем, что обе стартовые позиции могут достичь друг друга
            // (это гарантирует, что все клетки достижимы)
            boolean stillReachable = isReachable(gameCells, 0, 0, width - 1, height - 1);
            
            if (stillReachable) {
                // Стена добавлена успешно
                added++;
            } else {
                // Убираем стену обратно
                gameCells[y][x] = false;
            }
        }
    }
    
    /**
     * Проверяет, достижима ли точка (x2, y2) из точки (x1, y1) используя BFS
     */
    private boolean isReachable(boolean[][] gameCells, int x1, int y1, int x2, int y2) {
        if (gameCells[y1][x1] || gameCells[y2][x2]) {
            return false; // Старт или финиш - стена
        }
        
        boolean[][] visited = new boolean[height][width];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.offer(new int[]{x1, y1});
        visited[y1][x1] = true;
        
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            
            if (x == x2 && y == y2) {
                return true; // Достигли цели
            }
            
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (!visited[ny][nx] && !gameCells[ny][nx]) { // Проход и не посещён
                        visited[ny][nx] = true;
                        queue.offer(new int[]{nx, ny});
                    }
                }
            }
        }
        
        return false; // Не достигли цели
    }
    
    /**
     * Конвертирует игровые клетки в формат внутреннего лабиринта
     */
    private void convertToInternalFormat(boolean[][] gameCells) {
        // Инициализация внутреннего формата: все стены
        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < walls[i].length; j++) {
                walls[i][j] = true;
            }
        }
        
        // Заполняем проходы
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!gameCells[y][x]) { // Если это проход
                    int mazeX = x * 2 + 1;
                    int mazeY = y * 2 + 1;
                    walls[mazeY][mazeX] = false;
                }
            }
        }
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
     * с минимальным расстоянием от стартовых позиций (10-15 клеток)
     */
    public int[] findRandomPassableCell(int startX1, int startY1, int startX2, int startY2) {
        java.util.List<int[]> passableCells = new java.util.ArrayList<>();
        
        // Собираем все проходимые клетки
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isWall(x, y)) {
                    passableCells.add(new int[]{x, y});
                }
            }
        }
        
        if (passableCells.isEmpty()) {
            return new int[]{width / 2, height / 2};
        }
        
        // Фильтруем клетки с минимальным расстоянием 10-15 от стартовых позиций
        int minDistance = 10;
        int maxDistance = 15;
        java.util.List<int[]> validCells = new java.util.ArrayList<>();
        
        for (int[] cell : passableCells) {
            int x = cell[0];
            int y = cell[1];
            
            // Исключаем стартовые позиции
            if ((x == startX1 && y == startY1) || (x == startX2 && y == startY2)) {
                continue;
            }
            
            // Вычисляем расстояние до обеих стартовых позиций
            int dist1 = Math.abs(x - startX1) + Math.abs(y - startY1);
            int dist2 = Math.abs(x - startX2) + Math.abs(y - startY2);
            
            // Минимальное расстояние должно быть не менее minDistance
            int minDist = Math.min(dist1, dist2);
            
            if (minDist >= minDistance && minDist <= maxDistance) {
                validCells.add(cell);
            }
        }
        
        // Если нет клеток в нужном диапазоне, ищем с минимальным расстоянием >= 10
        if (validCells.isEmpty()) {
            for (int[] cell : passableCells) {
                int x = cell[0];
                int y = cell[1];
                
                if ((x == startX1 && y == startY1) || (x == startX2 && y == startY2)) {
                    continue;
                }
                
                int dist1 = Math.abs(x - startX1) + Math.abs(y - startY1);
                int dist2 = Math.abs(x - startX2) + Math.abs(y - startY2);
                int minDist = Math.min(dist1, dist2);
                
                if (minDist >= minDistance) {
                    validCells.add(cell);
                }
            }
        }
        
        // Если всё ещё нет, берём любую кроме стартовых
        if (validCells.isEmpty()) {
            for (int[] cell : passableCells) {
                int x = cell[0];
                int y = cell[1];
                if (!(x == startX1 && y == startY1) && !(x == startX2 && y == startY2)) {
                    validCells.add(cell);
                }
            }
        }
        
        if (validCells.isEmpty()) {
            return new int[]{width / 2, height / 2};
        }
        
        return validCells.get(random.nextInt(validCells.size()));
    }
}

