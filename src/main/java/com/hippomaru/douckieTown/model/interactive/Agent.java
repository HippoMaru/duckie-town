package com.hippomaru.douckieTown.model.interactive;

import com.hippomaru.douckieTown.model.map.Cell;
import com.hippomaru.douckieTown.model.map.CellGrid;
import com.hippomaru.douckieTown.model.map.CellType;
import com.hippomaru.douckieTown.model.map.Coords;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Getter
@Setter
@ToString
public class Agent extends TrafficMember implements Runnable {
    private Coords[] chosenRoute;
    private int successStepsCounter = 0;
    private int stepsCounter = 0;
    private Coords currentCoords;
    private CellGrid map;

    private volatile boolean running = true;
    private volatile boolean stepRequested = false;
    private final Object stepLock = new Object();
    private CountDownLatch stepLatch;

    @Override
    public void run() {
        while (running) {
            synchronized (stepLock) {
                while (!stepRequested && running) {
                    try {
                        stepLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!running) break;
                stepRequested = false;
            }

            synchronized (map) {
                Coords nextCoords = getNextCoords();
                if (nextCoords == null) {
                    stepsCounter++;
                } else {
                    Cell curCell = map.getGrid()[currentCoords.x()][currentCoords.y()];
                    Cell nextCell = map.getGrid()[nextCoords.x()][nextCoords.y()];
                    if (nextCell.getCarriedEntity() == null) {
                        curCell.setCarriedEntity(null);
                        nextCell.setCarriedEntity(this);
                        currentCoords = nextCoords;
                        successStepsCounter++;
                    }
                    stepsCounter++;
                }
            }

            if (stepLatch != null) {
                stepLatch.countDown();
                stepLatch = null;
            }
        }
    }

    public void requestStep() {
        synchronized (stepLock) {
            stepRequested = true;
            stepLock.notify();
        }
    }

    public void prepareStep(CountDownLatch latch) {
        this.stepLatch = latch;
    }

    public void stop() {
        running = false;
        synchronized (stepLock) {
            stepLock.notify();
        }
    }

    public void analyzeMap(CellGrid map, Coords start, List<Coords> finishCoords) {
        // Размеры карты
        int width = map.getWidth();
        int height = map.getHeight();

        boolean[][] visited = new boolean[height][width];

        Queue<Coords> queue = new LinkedList<>();

        Map<Coords, Coords> parentMap = new HashMap<>();

        queue.add(start);
        visited[start.y()][start.x()] = true;
        parentMap.put(start, null);
        Coords finish = null;

        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        while (!queue.isEmpty()) {
            Coords current = queue.poll();

            if (finishCoords.contains(current)) {
                finish = current;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nx = current.x() + dx[i];
                int ny = current.y() + dy[i];

                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }

                Cell cell = map.getGrid()[nx][ny];
                if (visited[ny][nx] || cell.getCellType() == CellType.WALL) {
                    continue;
                }

                Coords neighbor = new Coords(nx, ny);
                queue.add(neighbor);
                visited[ny][nx] = true;
                parentMap.put(neighbor, current);
            }
        }

        if (finish != null) {
            List<Coords> path = new ArrayList<>();
            Coords current = finish;

            while (current != null) {
                path.add(current);
                current = parentMap.get(current);
            }

            Collections.reverse(path);
            this.chosenRoute = path.toArray(new Coords[0]);

        } else {
            this.chosenRoute = new Coords[0];
        }
    }

    public Coords getNextCoords() {
        return chosenRoute[successStepsCounter + 1];
    }
}
