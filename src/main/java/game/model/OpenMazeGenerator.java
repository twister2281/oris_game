package game.model;

/**
 * Генератор открытого лабиринта с минимальным количеством стен
 * Больше проходов, легче навигация
 */
public class OpenMazeGenerator implements MazeGenerator {
    
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
        
        // Создаем открытое поле: каждая вторая строка и столбец - проходы
        // Это создает сетку с большим количеством проходов
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Каждая четная строка - проход
                if (y % 2 == 0) {
                    gameCells[y][x] = false;
                }
                // Каждый четный столбец - проход
                if (x % 2 == 0) {
                    gameCells[y][x] = false;
                }
            }
        }
        
        // Добавляем дополнительные горизонтальные проходы для лучшей связности
        for (int row = 3; row < height; row += 4) {
            for (int x = 0; x < width; x++) {
                gameCells[row][x] = false;
            }
        }
        
        // Добавляем дополнительные вертикальные проходы
        for (int col = 3; col < width; col += 4) {
            for (int y = 0; y < height; y++) {
                gameCells[y][col] = false;
            }
        }
        
        // Гарантируем проходимость стартовых позиций
        gameCells[0][0] = false;
        gameCells[height - 1][width - 1] = false;
        
        return gameCells;
    }
    
    @Override
    public String getName() {
        return "Открытый";
    }
}

