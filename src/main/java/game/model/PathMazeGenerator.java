package game.model;

/**
 * Генератор лабиринта с основными путями и ответвлениями
 * Создает несколько основных путей с боковыми ответвлениями
 */
public class PathMazeGenerator implements MazeGenerator {
    
    @Override
    public boolean[][] generate(int width, int height) {
        boolean[][] gameCells = new boolean[height][width];
        
        // Инициализация: все клетки - стены
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                gameCells[i][j] = true;
            }
        }
        
        // ГАРАНТИРУЕМ: Стартовые позиции всегда проходимы
        gameCells[0][0] = false;
        gameCells[height - 1][width - 1] = false;
        
        // Основной путь от старта 1 (0,0) вправо, затем вниз
        for (int x = 0; x < width / 2; x++) {
            gameCells[0][x] = false;
        }
        for (int y = 0; y < height / 2; y++) {
            gameCells[y][width / 2] = false;
        }
        
        // Основной путь от старта 2 (width-1, height-1) влево, затем вверх
        for (int x = width / 2; x < width; x++) {
            gameCells[height - 1][x] = false;
        }
        for (int y = height / 2; y < height; y++) {
            gameCells[y][width / 2] = false;
        }
        
        // Центральный горизонтальный путь
        int centerY = height / 2;
        for (int x = 0; x < width; x++) {
            gameCells[centerY][x] = false;
        }
        
        // Центральный вертикальный путь
        int centerX = width / 2;
        for (int y = 0; y < height; y++) {
            gameCells[y][centerX] = false;
        }
        
        // Боковые ответвления от центральных путей
        // Горизонтальные ответвления
        for (int row : new int[]{height / 4, 3 * height / 4}) {
            if (row < height) {
                for (int x = width / 4; x < 3 * width / 4; x++) {
                    gameCells[row][x] = false;
                }
            }
        }
        
        // Вертикальные ответвления
        for (int col : new int[]{width / 4, 3 * width / 4}) {
            if (col < width) {
                for (int y = height / 4; y < 3 * height / 4; y++) {
                    gameCells[y][col] = false;
                }
            }
        }
        
        // Дополнительные соединительные пути
        // Диагональные проходы в углах
        for (int i = 0; i < Math.min(width / 3, height / 3); i++) {
            if (i < width && i < height) {
                gameCells[i][i] = false;
            }
            if (width - 1 - i >= 0 && height - 1 - i >= 0) {
                gameCells[height - 1 - i][width - 1 - i] = false;
            }
        }
        
        // Гарантируем проходимость стартовых позиций
        gameCells[0][0] = false;
        gameCells[height - 1][width - 1] = false;
        
        return gameCells;
    }
    
    @Override
    public String getName() {
        return "Пути";
    }
}

