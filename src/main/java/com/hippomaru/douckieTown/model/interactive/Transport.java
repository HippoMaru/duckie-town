package com.hippomaru.douckieTown.model.interactive;

import com.hippomaru.douckieTown.model.map.Coords;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Random;

@Getter
@Setter
public class Transport extends TrafficMember{
    private final Random random = new Random();
    private final int possibleStepsCount = random.nextInt(135) + 15;
    private int stepsCounter;

    public void chooseDirection(List<Direction> possibleDirections){
        currentDirection = possibleDirections.get(random.nextInt(possibleDirections.size()));
    }


    public Coords getNextCoords(Coords curCoords) {
        return switch (currentDirection){
            case UP -> new Coords(curCoords.x(), curCoords.y() + 1);
            case DOWN -> new Coords(curCoords.x(), curCoords.y() - 1);
            case LEFT -> new Coords(curCoords.x() - 1, curCoords.y());
            case RIGHT -> new Coords(curCoords.x() + 1, curCoords.y());
        };
    }
}
