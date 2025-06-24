package com.hippomaru.douckieTown.app;

import com.hippomaru.douckieTown.model.map.CellGrid;

import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        CellGrid map = new CellGrid("src/main/resources/map_one_agent.txt");
        CellGrid map3 = new CellGrid("src/main/resources/map_three_agents.txt");
        CellGrid map5 = new CellGrid("src/main/resources/map_five_agents.txt");

        GameManager manager = new GameManager(map5, 10, 0.04);

        while (!manager.isFinished()) {
            manager.doStep();
            TimeUnit.MILLISECONDS.sleep(100);
        };
        var successfulAgents = manager.getSuccessfulAgents();
        successfulAgents.forEach(System.out::println);
    }
}
