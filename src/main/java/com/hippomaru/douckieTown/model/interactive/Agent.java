package com.hippomaru.douckieTown.model.interactive;

import com.hippomaru.douckieTown.model.map.Cell;
import com.hippomaru.douckieTown.model.map.CellGrid;
import com.hippomaru.douckieTown.model.map.CellType;
import com.hippomaru.douckieTown.model.map.Coords;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@ToString
public class Agent extends TrafficMember {
    private Coords[] chosenRoute;
    private int successStepsCounter = 0;
    private int stepsCounter = 0;

    public void analyzeMap(CellGrid map, Coords start, List<Coords> finishCoords) {
        // Размеры карты
        int width = map.getWidth();
        int height = map.getHeight();

        // Массив для отслеживания посещенных клеток
        boolean[][] visited = new boolean[height][width];

        // Очередь для BFS (хранит координаты)
        Queue<Coords> queue = new LinkedList<>();

        // Карта для восстановления пути (ребенок -> родитель)
        Map<Coords, Coords> parentMap = new HashMap<>();

        // Инициализация BFS
        queue.add(start);
        visited[start.y()][start.x()] = true;
        parentMap.put(start, null);
        Coords finish = null;

        // Направления движения: ВВЕРХ, ВНИЗ, ВЛЕВО, ВПРАВО
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        while (!queue.isEmpty()) {
            Coords current = queue.poll();

            // Проверяем, достигли ли финиша
            if (finishCoords.contains(current)) {
                finish = current;
                break;
            }

            // Исследуем соседние клетки
            for (int i = 0; i < 4; i++) {
                int nx = current.x() + dx[i];
                int ny = current.y() + dy[i];

                // Проверка границ карты
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }

                // Пропускаем посещенные клетки и стены
                Cell cell = map.getGrid()[nx][ny];
                if (visited[ny][nx] || cell.getCellType() == CellType.WALL) {
                    continue;
                }

                // Добавляем в очередь
                Coords neighbor = new Coords(nx, ny);
                queue.add(neighbor);
                visited[ny][nx] = true;
                parentMap.put(neighbor, current);
            }
        }

        // Восстанавливаем путь, если финиш найден
        if (finish != null) {
            List<Coords> path = new ArrayList<>();
            Coords current = finish;

            // Идем от финиша к старту
            while (current != null) {
                path.add(current);
                current = parentMap.get(current);
            }

            // Разворачиваем путь (старт -> финиш)
            Collections.reverse(path);
            this.chosenRoute = path.toArray(new Coords[0]);

        } else {
            // Путь не найден
            this.chosenRoute = new Coords[0];
        }
    }

    public Coords getNextCoords() {
        return chosenRoute[successStepsCounter + 1];
    }
}
