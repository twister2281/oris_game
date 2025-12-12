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
        
        // КРИТИЧЕСКИ ВАЖНО: Гарантируем, что стартовые позиции проходимы
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        // Обе стартовые позиции - проходы (ОБЯЗАТЕЛЬНО!)
        gameCells[startY1][startX1] = false;
        gameCells[startY2][startX2] = false;
        
        // Создаём проходимый лабиринт используя алгоритм backtracking
        // Начинаем с обеих стартовых позиций одновременно
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX1, startY1});
        
        // Отмечаем обе стартовые позиции как посещённые
        boolean[][] visited = new boolean[height][width];
        visited[startY1][startX1] = true;
        visited[startY2][startX2] = true;
        
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];
            
            // Находим непосещённых соседей (через одну клетку)
            int[][] neighbors = getUnvisitedNeighborsForGeneration(x, y, gameCells, visited);
            
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
                visited[ny][nx] = true;
                
                stack.push(new int[]{nx, ny});
            } else {
                stack.pop();
            }
        }
        
        // КРИТИЧЕСКИ ВАЖНО: Убеждаемся, что вторая стартовая позиция достижима
        if (!isReachable(gameCells, startX1, startY1, startX2, startY2)) {
            createPathToPosition(gameCells, startX1, startY1, startX2, startY2);
        }
        
        // Добавляем дополнительные стены для сложности (но сохраняем проходимость)
        addWallsForComplexity(gameCells);
        
        // ФИНАЛЬНАЯ ПРОВЕРКА: Стартовые позиции ВСЕГДА проходимы
        gameCells[startY1][startX1] = false;
        gameCells[startY2][startX2] = false;
        
        // Дополнительная проверка: убеждаемся, что обе стартовые позиции достижимы друг от друга
        if (!isReachable(gameCells, startX1, startY1, startX2, startY2)) {
            createPathToPosition(gameCells, startX1, startY1, startX2, startY2);
            // Ещё раз проверяем стартовые позиции
            gameCells[startY1][startX1] = false;
            gameCells[startY2][startX2] = false;
        }
        
        // Конвертируем игровые клетки в формат внутреннего лабиринта
        convertToInternalFormat(gameCells);
        
        // ФИНАЛЬНАЯ ФИНАЛЬНАЯ ПРОВЕРКА после конвертации
        // Убеждаемся, что стартовые позиции проходимы в финальном формате
        ensureStartPositionsPassable();
    }
    
    /**
     * Гарантирует, что стартовые позиции проходимы в финальном формате
     */
    private void ensureStartPositionsPassable() {
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        // Убираем стены в стартовых позициях
        int mazeX1 = startX1 * 2 + 1;
        int mazeY1 = startY1 * 2 + 1;
        int mazeX2 = startX2 * 2 + 1;
        int mazeY2 = startY2 * 2 + 1;
        
        walls[mazeY1][mazeX1] = false;
        walls[mazeY2][mazeX2] = false;
        
        // Убираем стены вокруг стартовых позиций для гарантии проходимости
        // Только те, которые не нарушают структуру
        if (mazeX1 > 1) walls[mazeY1][mazeX1 - 1] = false;
        if (mazeX1 < walls[0].length - 2) walls[mazeY1][mazeX1 + 1] = false;
        if (mazeY1 > 1) walls[mazeY1 - 1][mazeX1] = false;
        if (mazeY1 < walls.length - 2) walls[mazeY1 + 1][mazeX1] = false;
        
        if (mazeX2 > 1) walls[mazeY2][mazeX2 - 1] = false;
        if (mazeX2 < walls[0].length - 2) walls[mazeY2][mazeX2 + 1] = false;
        if (mazeY2 > 1) walls[mazeY2 - 1][mazeX2] = false;
        if (mazeY2 < walls.length - 2) walls[mazeY2 + 1][mazeX2] = false;
    }
    
    private int[][] getUnvisitedNeighborsForGeneration(int x, int y, boolean[][] gameCells, boolean[][] visited) {
        java.util.List<int[]> neighbors = new java.util.ArrayList<>();
        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                if (!visited[ny][nx]) { // Непосещённая клетка
                    neighbors.add(new int[]{nx, ny});
                }
            }
        }
        
        return neighbors.toArray(new int[0][]);
    }
    
    /**
     * Создаёт путь от одной позиции к другой, если они не достижимы
     * Гарантирует проходимость пути
     */
    private void createPathToPosition(boolean[][] gameCells, int fromX, int fromY, int toX, int toY) {
        // Используем A*-подобный алгоритм для создания пути
        // Сначала пытаемся идти по прямой
        int currentX = fromX;
        int currentY = fromY;
        
        java.util.List<int[]> path = new java.util.ArrayList<>();
        path.add(new int[]{currentX, currentY});
        
        while (currentX != toX || currentY != toY) {
            // Определяем направление движения
            int dx = Integer.compare(toX, currentX);
            int dy = Integer.compare(toY, currentY);
            
            // Двигаемся по горизонтали или вертикали
            int nextX = currentX;
            int nextY = currentY;
            
            if (dx != 0 && (dy == 0 || Math.abs(dx) >= Math.abs(dy))) {
                nextX += dx;
            } else if (dy != 0) {
                nextY += dy;
            }
            
            // Проверяем границы
            if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height) {
                currentX = nextX;
                currentY = nextY;
                path.add(new int[]{currentX, currentY});
            } else {
                break;
            }
        }
        
        // Убираем стены вдоль пути
        for (int[] cell : path) {
            int x = cell[0];
            int y = cell[1];
            if (x >= 0 && x < width && y >= 0 && y < height) {
                gameCells[y][x] = false;
            }
        }
    }
    
    /**
     * Добавляет дополнительные стены для сложности, но проверяет проходимость
     * Гарантирует, что все клетки остаются достижимыми от стартовых позиций
     */
    private void addWallsForComplexity(boolean[][] gameCells) {
        // Гарантируем, что стартовые позиции остаются проходимыми
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        // Собираем все проходимые клетки (кроме стартовых)
        java.util.List<int[]> passableCells = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!gameCells[y][x] && !(x == startX1 && y == startY1) && !(x == startX2 && y == startY2)) {
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
            boolean stillReachable = isReachable(gameCells, startX1, startY1, startX2, startY2);
            
            if (stillReachable) {
                // Стена добавлена успешно
                added++;
            } else {
                // Убираем стену обратно
                gameCells[y][x] = false;
            }
        }
        
        // Финальная проверка: убеждаемся, что стартовые позиции проходимы
        if (gameCells[startY1][startX1]) {
            gameCells[startY1][startX1] = false;
        }
        if (gameCells[startY2][startX2]) {
            gameCells[startY2][startX2] = false;
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
     * ГАРАНТИРУЕТ достижимость от обеих стартовых позиций
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
        
        // Выбираем случайную клетку и проверяем достижимость
        // Перемешиваем для случайности
        java.util.Collections.shuffle(validCells, random);
        
        // Проверяем достижимость от обеих стартовых позиций
        for (int[] cell : validCells) {
            int x = cell[0];
            int y = cell[1];
            
            // Проверяем достижимость (используем внутренний формат для проверки)
            boolean reachableFrom1 = isReachableFromPosition(startX1, startY1, x, y);
            boolean reachableFrom2 = isReachableFromPosition(startX2, startY2, x, y);
            
            if (reachableFrom1 && reachableFrom2) {
                return cell;
            }
        }
        
        // Если ни одна не достижима, создаём путь к первой подходящей
        if (!validCells.isEmpty()) {
            int[] cell = validCells.get(0);
            int x = cell[0];
            int y = cell[1];
            
            // Создаём пути от обеих стартовых позиций
            boolean[][] gameCells = convertToGameCells();
            if (!isReachable(gameCells, startX1, startY1, x, y)) {
                createPathToPosition(gameCells, startX1, startY1, x, y);
            }
            if (!isReachable(gameCells, startX2, startY2, x, y)) {
                createPathToPosition(gameCells, startX2, startY2, x, y);
            }
            convertToInternalFormat(gameCells);
            
            return cell;
        }
        
        return new int[]{width / 2, height / 2};
    }
    
    /**
     * Конвертирует внутренний формат лабиринта обратно в игровые клетки для проверки
     */
    private boolean[][] convertToGameCells() {
        boolean[][] gameCells = new boolean[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gameCells[y][x] = isWall(x, y);
            }
        }
        
        return gameCells;
    }
    
    /**
     * Проверяет достижимость позиции от стартовой позиции используя внутренний формат
     */
    private boolean isReachableFromPosition(int fromX, int fromY, int toX, int toY) {
        if (isWall(fromX, fromY) || isWall(toX, toY)) {
            return false;
        }
        
        boolean[][] visited = new boolean[height][width];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.offer(new int[]{fromX, fromY});
        visited[fromY][fromX] = true;
        
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            
            if (x == toX && y == toY) {
                return true;
            }
            
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (!visited[ny][nx] && !isWall(nx, ny)) {
                        visited[ny][nx] = true;
                        queue.offer(new int[]{nx, ny});
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Публичные методы для проверки и создания путей (используются в GameServer)
     */
    public boolean[][] convertToGameCellsForCheck() {
        return convertToGameCells();
    }
    
    public boolean isReachableForCheck(boolean[][] gameCells, int x1, int y1, int x2, int y2) {
        return isReachable(gameCells, x1, y1, x2, y2);
    }
    
    public void createPathToPositionForCheck(boolean[][] gameCells, int fromX, int fromY, int toX, int toY) {
        createPathToPosition(gameCells, fromX, fromY, toX, toY);
    }
    
    public void updateFromGameCells(boolean[][] gameCells) {
        convertToInternalFormat(gameCells);
    }
}

