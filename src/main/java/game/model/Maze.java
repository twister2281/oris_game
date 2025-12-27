package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maze {
    private boolean[][] walls; // true = стена, false = проход
    private int width;
    private int height;
    private long seed;
    
    public Maze(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.walls = new boolean[height * 2 + 1][width * 2 + 1];
        generate();
    }
    
    public Maze(int width, int height) {
        this(width, height, System.currentTimeMillis());
    }
    
    private void generate() {
        // Используем фабрику для создания генератора на основе seed
        // Одинаковый seed даст одинаковый генератор для сервера и клиента
        MazeGenerator generator = MazeGeneratorFactory.createGenerator(seed);
        
        // Генерируем лабиринт
        boolean[][] gameCells = generator.generate(width, height);
        
        // ФИНАЛЬНАЯ ПРОВЕРКА: Стартовые позиции ВСЕГДА проходимы
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        gameCells[startY1][startX1] = false;
        gameCells[startY2][startX2] = false;
        
        // Конвертируем игровые клетки в формат внутреннего лабиринта
        convertToInternalFormat(gameCells);
        
        // ФИНАЛЬНАЯ ПРОВЕРКА после конвертации
        ensureStartPositionsPassable();
        
        System.out.println("Лабиринт сгенерирован с помощью: " + generator.getName() + " генератор (seed: " + seed + ")");
    }
    
    /**
     * Гарантирует, что стартовые позиции проходимы в финальном формате
     */
    private void ensureStartPositionsPassable() {
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        // Убираем стены в стартовых позициях (дополнительная гарантия)
        int mazeX1 = startX1 * 2 + 1;
        int mazeY1 = startY1 * 2 + 1;
        int mazeX2 = startX2 * 2 + 1;
        int mazeY2 = startY2 * 2 + 1;
        
        // Проверяем границы
        if (mazeX1 >= 0 && mazeX1 < walls[0].length && mazeY1 >= 0 && mazeY1 < walls.length) {
            walls[mazeY1][mazeX1] = false;
        }
        if (mazeX2 >= 0 && mazeX2 < walls[0].length && mazeY2 >= 0 && mazeY2 < walls.length) {
            walls[mazeY2][mazeX2] = false;
        }
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
                    
                    // Создаём проходы к соседним проходимым клеткам
                    // Вверх
                    if (y > 0 && !gameCells[y - 1][x]) {
                        walls[mazeY - 1][mazeX] = false;
                    }
                    // Вниз
                    if (y < height - 1 && !gameCells[y + 1][x]) {
                        walls[mazeY + 1][mazeX] = false;
                    }
                    // Влево
                    if (x > 0 && !gameCells[y][x - 1]) {
                        walls[mazeY][mazeX - 1] = false;
                    }
                    // Вправо
                    if (x < width - 1 && !gameCells[y][x + 1]) {
                        walls[mazeY][mazeX + 1] = false;
                    }
                }
            }
        }
        
        // КРИТИЧЕСКИ ВАЖНО: Гарантируем проходимость стартовых позиций
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        int mazeX1 = startX1 * 2 + 1;
        int mazeY1 = startY1 * 2 + 1;
        int mazeX2 = startX2 * 2 + 1;
        int mazeY2 = startY2 * 2 + 1;
        
        // Убираем стены в стартовых позициях
        walls[mazeY1][mazeX1] = false;
        walls[mazeY2][mazeX2] = false;
        
        // Убираем стены вокруг стартовых позиций для гарантии проходимости
        if (mazeX1 > 0) walls[mazeY1][mazeX1 - 1] = false;
        if (mazeX1 < walls[0].length - 1) walls[mazeY1][mazeX1 + 1] = false;
        if (mazeY1 > 0) walls[mazeY1 - 1][mazeX1] = false;
        if (mazeY1 < walls.length - 1) walls[mazeY1 + 1][mazeX1] = false;
        
        if (mazeX2 > 0) walls[mazeY2][mazeX2 - 1] = false;
        if (mazeX2 < walls[0].length - 1) walls[mazeY2][mazeX2 + 1] = false;
        if (mazeY2 > 0) walls[mazeY2 - 1][mazeX2] = false;
        if (mazeY2 < walls.length - 1) walls[mazeY2 + 1][mazeX2] = false;
    }
    
    public boolean isWall(int x, int y) {
        // Проверяем границы
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true; // Вне границ - стена
        }
        
        // Конвертируем координаты игрока в координаты лабиринта
        int mazeX = x * 2 + 1;
        int mazeY = y * 2 + 1;
        
        // Проверяем границы внутреннего формата
        if (mazeX < 0 || mazeX >= walls[0].length || mazeY < 0 || mazeY >= walls.length) {
            return true; // Вне границ - стена
        }
        
        return walls[mazeY][mazeX];
    }
    
    public boolean canMove(int x, int y, String direction) {
        int newX = x;
        int newY = y;
        
        switch (direction) {
            case "ВВЕРХ":
                newY--;
                break;
            case "ВНИЗ":
                newY++;
                break;
            case "ВЛЕВО":
                newX--;
                break;
            case "ВПРАВО":
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
     * Возвращает случайную позицию финиша
     * Финиш генерируется случайно, но минимум в 10 блоках от обеих стартовых позиций
     * Использует seed для детерминированности (одинаковый seed = одинаковый финиш)
     */
    public int[] getFixedExitPosition() {
        // Стартовые позиции игроков
        int startX1 = 0;
        int startY1 = 0;
        int startX2 = width - 1;
        int startY2 = height - 1;
        
        // Используем seed для детерминированной генерации
        Random random = new Random(seed);
        
        // Список всех подходящих позиций
        List<int[]> validPositions = new ArrayList<>();
        
        // Перебираем все клетки и находим подходящие
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Проверяем, что это не стартовая позиция
                if ((x == startX1 && y == startY1) || (x == startX2 && y == startY2)) {
                    continue;
                }
                
                // Вычисляем манхэттенское расстояние до обеих стартовых позиций
                int dist1 = Math.abs(x - startX1) + Math.abs(y - startY1);
                int dist2 = Math.abs(x - startX2) + Math.abs(y - startY2);
                
                // Проверяем, что минимум 10 блоков от обеих стартовых позиций
                if (dist1 >= 10 && dist2 >= 10) {
                    // Проверяем, что позиция проходима (не стена)
                    if (!isWall(x, y)) {
                        validPositions.add(new int[]{x, y});
                    }
                }
            }
        }
        
        // Если нашли подходящие позиции, выбираем случайную
        if (!validPositions.isEmpty()) {
            int index = random.nextInt(validPositions.size());
            return validPositions.get(index);
        }
        
        // Если не нашли подходящих позиций (маловероятно), ищем любую проходимую позицию
        // с минимальным расстоянием 10 от обеих стартовых позиций
        // Начинаем поиск от центра и расширяемся
        int centerX = width / 2;
        int centerY = height / 2;
        
        for (int radius = 0; radius < Math.max(width, height); radius++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    int x = centerX + dx;
                    int y = centerY + dy;
                    if (x >= 0 && x < width && y >= 0 && y < height) {
                        // Пропускаем стартовые позиции
                        if ((x == startX1 && y == startY1) || (x == startX2 && y == startY2)) {
                            continue;
                        }
                        // Проверяем проходимость
                        if (!isWall(x, y)) {
                            int dist1 = Math.abs(x - startX1) + Math.abs(y - startY1);
                            int dist2 = Math.abs(x - startX2) + Math.abs(y - startY2);
                            // Если минимум 10 блоков от обеих стартовых позиций
                            if (dist1 >= 10 && dist2 >= 10) {
                                return new int[]{x, y};
                            }
                        }
                    }
                }
            }
        }
        
        // Если все еще не нашли (крайне маловероятно), используем центр как последний fallback
        // даже если он не соответствует требованиям по расстоянию
        System.err.println("Предупреждение: не удалось найти подходящую позицию финиша с расстоянием >= 10, используется центр");
        return new int[]{centerX, centerY};
    }
    
    /**
     * Старый метод для совместимости - теперь возвращает фиксированную позицию
     */
    public int[] findRandomPassableCell(int startX1, int startY1, int startX2, int startY2) {
        return getFixedExitPosition();
    }
    
    /**
     * Конвертирует внутренний формат лабиринта обратно в игровые клетки для проверки
     */
    public boolean[][] convertToGameCellsForCheck() {
        boolean[][] gameCells = new boolean[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gameCells[y][x] = isWall(x, y);
            }
        }
        
        return gameCells;
    }
    
    /**
     * Проверяет, достижима ли точка (x2, y2) из точки (x1, y1) используя BFS
     */
    public boolean isReachableForCheck(boolean[][] gameCells, int x1, int y1, int x2, int y2) {
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
     * Создаёт путь от одной позиции к другой, если они не достижимы
     * Гарантирует проходимость пути
     */
    public void createPathToPositionForCheck(boolean[][] gameCells, int fromX, int fromY, int toX, int toY) {
        // Используем простой алгоритм для создания пути
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
    
    public void updateFromGameCells(boolean[][] gameCells) {
        convertToInternalFormat(gameCells);
    }
}
