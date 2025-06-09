package com.hippomaru.douckieTown.app;

import com.hippomaru.douckieTown.model.interactive.Agent;
import com.hippomaru.douckieTown.model.interactive.Direction;
import com.hippomaru.douckieTown.model.interactive.TrafficMember;
import com.hippomaru.douckieTown.model.map.Coords;
import com.hippomaru.douckieTown.model.interactive.Transport;
import com.hippomaru.douckieTown.model.map.Cell;
import com.hippomaru.douckieTown.model.map.CellGrid;
import com.hippomaru.douckieTown.model.map.CellType;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@Setter
public class GameManager {

    private final static double TRANSPORT_SPAWN_PROBABILITY = 0.04;
    private final static double TRANSPORT_MOVE_PROBABILITY = 0.7;
    private final static int TRANSPORT_MAX_WAIT = 5;

    private List<Agent> successfulAgents = new ArrayList<>();
    private final CellGrid map;
    private final Map<CellType, List<Coords>> CELL_TYPES_COORDS;
    private final Map<String, List<Coords>> TRAFFIC_MEMBER_COORDS;
    private final Random random = new Random();

    // Графические компоненты
    private JFrame frame;
    private MapPanel mapPanel;
    private static final int CELL_SIZE = 30; // Размер ячейки в пикселях

    public GameManager(CellGrid map){
        this.map = map;

        // Инициализация карт с изменяемыми списками
        CELL_TYPES_COORDS = new EnumMap<>(CellType.class);
        for (CellType type : CellType.values()) {
            CELL_TYPES_COORDS.put(type, new ArrayList<>());
        }

        TRAFFIC_MEMBER_COORDS = new HashMap<>();
        TRAFFIC_MEMBER_COORDS.put("TRANSPORT", new ArrayList<>());
        TRAFFIC_MEMBER_COORDS.put("AGENT", new ArrayList<>());

        // Заполнение координат
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Cell cell = map.getGrid()[x][y];
                CELL_TYPES_COORDS.get(cell.getCellType()).add(cell.getCoords());
            }
        }

        // Инициализация графического интерфейса
        initGUI();

        spawnAgents();
        spawnTransport();
        updateVisualization(); // Первоначальная отрисовка
    }

    private void initGUI() {
        frame = new JFrame("Douckie Town Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mapPanel = new MapPanel();
        mapPanel.setPreferredSize(new Dimension(
                (int) map.getHeight() * CELL_SIZE,
                (int) map.getWidth() * CELL_SIZE
        ));

        frame.add(mapPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void doStep() {
        despawnAgents();
        despawnTransport();
        moveAgents();
        moveTransport();
        spawnTransport();
        updateVisualization(); // Обновляем визуализацию после шага
    }

    private void updateVisualization() {
        mapPanel.repaint();
    }

    // Внутренний класс для отрисовки карты
    private class MapPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawMap(g);
        }

        private void drawMap(Graphics g) {
            // Отрисовка фона ячеек
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    Cell cell = map.getGrid()[x][y];
                    drawCellBackground(g, cell, x, y);

                    // Отрисовка сущностей
                    if (cell.getCarriedEntity() != null) {
                        drawEntity(g, cell.getCarriedEntity(), x, y);
                    }
                }
            }
        }

        private void drawCellBackground(Graphics g, Cell cell, int x, int y) {
            Color color = switch (cell.getCellType()) {
                case WALL -> Color.GREEN;
                case AGENT_SPAWN -> Color.DARK_GRAY;
                case ROAD -> Color.LIGHT_GRAY;
                case FINISH -> Color.BLUE;
                case TRANSPORT_SPAWN -> new Color(139, 69, 19); // Коричневый
                default -> Color.WHITE;
            };

            g.setColor(color);
            g.fillRect(y * CELL_SIZE, x * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        private void drawEntity(Graphics g, TrafficMember entity, int x, int y) {
            if (entity instanceof Agent) {
                g.setColor(Color.BLACK);
            } else if (entity instanceof Transport) {
                g.setColor(Color.YELLOW);
            } else {
                return;
            }

            // Отрисовка круга (сущности)
            g.fillOval(
                    y * CELL_SIZE,
                    x * CELL_SIZE,
                    CELL_SIZE,
                    CELL_SIZE
            );
        }
    }

    public boolean isFinished(){
        return TRAFFIC_MEMBER_COORDS.get("AGENT").isEmpty();
    }

    private void spawnAgents(){
        for (Coords coords: CELL_TYPES_COORDS.get(CellType.AGENT_SPAWN)){
            Cell cell = map.getGrid()[coords.x()][coords.y()];
            Agent agent = new Agent();
            agent.setCurrentDirection(Direction.UP);
            agent.analyzeMap(map, coords, CELL_TYPES_COORDS.get(CellType.FINISH));
            cell.setCarriedEntity(agent);
            TRAFFIC_MEMBER_COORDS.get("AGENT").add(coords);
        }
    }
    private void spawnTransport(){
        for (Coords coords: CELL_TYPES_COORDS.get(CellType.TRANSPORT_SPAWN)){
            Cell cell = map.getGrid()[coords.x()][coords.y()];
            if (cell.getCarriedEntity() == null &&
                    random.nextDouble() < TRANSPORT_SPAWN_PROBABILITY){
                Transport transport = new Transport();
                List<Direction> possibleDirections = getPossibleDirections(coords);
                transport.chooseDirection(possibleDirections);
                cell.setCarriedEntity(transport);
                TRAFFIC_MEMBER_COORDS.get("TRANSPORT").add(coords);
            }
        }
    }

    private void despawnAgents(){
        for (Coords coords: CELL_TYPES_COORDS.get(CellType.FINISH)){
            Cell cell = map.getGrid()[coords.x()][coords.y()];
            if (cell.getCarriedEntity() instanceof Agent){
                successfulAgents.add((Agent) cell.getCarriedEntity());
                cell.setCarriedEntity(null);
                TRAFFIC_MEMBER_COORDS.get("AGENT").remove(coords);
            }
        }
    }
    
    private void despawnTransport(){
        List<Coords> tempCoords = List.copyOf(TRAFFIC_MEMBER_COORDS.get("TRANSPORT"));
        for (Coords coords: tempCoords){
            Cell cell = map.getGrid()[coords.x()][coords.y()];
            Transport transport = (Transport) cell.getCarriedEntity();
            if (transport.getStepsCounter() >= transport.getPossibleStepsCount()){
                cell.setCarriedEntity(null);
                TRAFFIC_MEMBER_COORDS.get("TRANSPORT").remove(coords);
            };
        }
    }

    private void moveAgents(){
        List<Coords> tempCoords = List.copyOf(TRAFFIC_MEMBER_COORDS.get("AGENT"));
        for (Coords coords: tempCoords){
            Cell curCell = map.getGrid()[coords.x()][coords.y()];
            Agent agent = (Agent) curCell.getCarriedEntity();
            Coords nextCoords = agent.getNextCoords();
            Cell nextCell = map.getGrid()[nextCoords.x()][nextCoords.y()];
            if (nextCell.getCarriedEntity() == null){
                TRAFFIC_MEMBER_COORDS.get("AGENT").remove(coords);
                TRAFFIC_MEMBER_COORDS.get("AGENT").add(nextCoords);
                curCell.setCarriedEntity(null);
                nextCell.setCarriedEntity(agent);
                agent.setSuccessStepsCounter(agent.getSuccessStepsCounter() + 1);
            }
            agent.setStepsCounter(agent.getStepsCounter() + 1);
        }
    }

    private void moveTransport(){
        List<Coords> tempCoords = List.copyOf(TRAFFIC_MEMBER_COORDS.get("TRANSPORT"));
        for (Coords coords: tempCoords){
            Cell curCell = map.getGrid()[coords.x()][coords.y()];
            Transport transport = (Transport) curCell.getCarriedEntity();
            if (!isPossibleToMove(transport.getNextCoords(coords)) || 
                    getPossibleDirections(coords).size() > 2 ||
                    transport.getWaitCounter() >= TRANSPORT_MAX_WAIT){
                transport.chooseDirection(getPossibleDirections(coords));
                transport.setWaitCounter(0);
            }
            Coords nextCoords = transport.getNextCoords(coords);
            if (random.nextDouble() < TRANSPORT_MOVE_PROBABILITY) {
                Cell nextCell = map.getGrid()[nextCoords.x()][nextCoords.y()];
                if (nextCell.getCarriedEntity() == null){
                    TRAFFIC_MEMBER_COORDS.get("TRANSPORT").remove(coords);
                    TRAFFIC_MEMBER_COORDS.get("TRANSPORT").add(nextCoords);
                    curCell.setCarriedEntity(null);
                    nextCell.setCarriedEntity(transport);
                }
                else {
                    transport.setWaitCounter(transport.getWaitCounter() + 1);
                }
            }
            transport.setStepsCounter(transport.getStepsCounter() + 1);
        }
    }


    private List<Direction> getPossibleDirections(Coords coords) {
        int x = coords.x();
        int y = coords.y();

        Map<Direction, Coords> neighbours = Map.of(
                Direction.LEFT, new Coords(x - 1, y),
                Direction.RIGHT, new Coords(x + 1, y),
                Direction.DOWN, new Coords(x, y - 1),
                Direction.UP, new Coords(x, y + 1)
        );

        return neighbours.entrySet().stream()
                .filter((entry -> isPossibleToMove(entry.getValue())))
                .map((Map.Entry::getKey))
                .toList();
    }

    private boolean isPossibleToMove(Coords coords){
        int x = coords.x();
        int y = coords.y();
        return x >= 0 && x < map.getWidth() &&
                y >= 0 && y < map.getHeight() &&
                map.getGrid()[x][y].getCellType() != CellType.WALL;
    }
}
