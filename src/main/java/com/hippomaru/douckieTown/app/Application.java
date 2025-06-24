package com.hippomaru.douckieTown.app;

import com.hippomaru.douckieTown.model.map.CellGrid;

import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        CellGrid map = new CellGrid("src/main/resources/map_v1.txt");

        GameManager manager = new GameManager(map);

        var tempCoords = manager.getTRAFFIC_MEMBER_COORDS().get("AGENTS_ON_INIT");
        tempCoords.forEach(
                (c -> System.out.println(map.getGrid()[c.x()][c.y()].getCarriedEntity())));

        while (!manager.isFinished()) {
            manager.doStep();
            TimeUnit.MILLISECONDS.sleep(100);
        };
        var successfulAgents = manager.getSuccessfulAgents();
        successfulAgents.forEach(System.out::println);
    }
}
