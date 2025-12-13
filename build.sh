#!/bin/bash

# Создаём директорию для скомпилированных классов
mkdir -p bin

# Компилируем все Java файлы
echo "Compiling Java files..."
find src/main/java -name "*.java" > sources.txt
javac -d bin -sourcepath src/main/java @sources.txt
rm sources.txt

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "To run the game, use: java -cp bin game.Main"
else
    echo "Compilation failed!"
    exit 1
fi



