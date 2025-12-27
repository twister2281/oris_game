package game.model;


public class GridMazeGenerator implements MazeGenerator {
    
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
        
        // Основной путь от (0,0) вправо и вниз
        // Горизонтальный путь в первой строке
        for (int x = 0; x < width; x++) {
            gameCells[0][x] = false;
        }
        
        // Вертикальный путь в последнем столбце
        for (int y = 0; y < height; y++) {
            gameCells[y][width - 1] = false;
        }
        
        // Дополнительные проходы для создания альтернативных маршрутов
        // Вертикальные проходы через каждые 8-10 столбцов
        for (int col = 8; col < width; col += 8) {
            for (int y = 0; y < height; y++) {
                gameCells[y][col] = false;
            }
        }
        
        // Горизонтальные проходы через каждые 8-10 строк
        for (int row = 8; row < height; row += 8) {
            for (int x = 0; x < width; x++) {
                gameCells[row][x] = false;
            }
        }
        
        // Гарантируем проходимость стартовых позиций
        gameCells[0][0] = false;
        gameCells[height - 1][width - 1] = false;
        
        return gameCells;
    }
    
    @Override
    public String getName() {
        return "Сетка";
    }
}

