package game.model;

/**
 * Интерфейс для генераторов лабиринта
 */
public interface MazeGenerator {
    /**
     * Генерирует лабиринт в виде массива игровых клеток
     * @param width ширина лабиринта
     * @param height высота лабиринта
     * @return массив gameCells, где true = стена, false = проход
     */
    boolean[][] generate(int width, int height);
    
    /**
     * Возвращает название генератора
     */
    String getName();
}

