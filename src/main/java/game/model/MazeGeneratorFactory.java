package game.model;

import java.util.Random;

/**
 * Фабрика для создания генераторов лабиринта
 * Выбирает генератор на основе seed для синхронизации между сервером и клиентом
 */
public class MazeGeneratorFactory {
    
    /**
     * Создает генератор лабиринта на основе seed
     * Одинаковый seed всегда дает одинаковый генератор
     */
    public static MazeGenerator createGenerator(long seed) {
        Random random = new Random(seed);
        int choice = random.nextInt(3);
        
        switch (choice) {
            case 0:
                return new GridMazeGenerator();
            case 1:
                return new OpenMazeGenerator();
            case 2:
                return new PathMazeGenerator();
            default:
                return new GridMazeGenerator();
        }
    }
    
    /**
     * Создает случайный генератор лабиринта (использует текущее время как seed)
     */
    public static MazeGenerator createRandomGenerator() {
        return createGenerator(System.currentTimeMillis());
    }
    
    /**
     * Создает генератор по индексу
     * 0 = Grid, 1 = Open, 2 = Path
     */
    public static MazeGenerator createGenerator(int index) {
        switch (index) {
            case 0:
                return new GridMazeGenerator();
            case 1:
                return new OpenMazeGenerator();
            case 2:
                return new PathMazeGenerator();
            default:
                return new GridMazeGenerator();
        }
    }
}

