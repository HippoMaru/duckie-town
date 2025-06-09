package com.hippomaru.douckieTown.model.map;

import java.util.HashMap;
import java.util.Map;

public enum CellType {
    ROAD, WALL, AGENT_SPAWN, TRANSPORT_SPAWN, FINISH, UNKNOWN;

    private final static Map<Integer, CellType> CELL_TYPE_CODES = Map.of(
            0, ROAD,
            1, WALL,
            2, AGENT_SPAWN,
            3, TRANSPORT_SPAWN,
            4, FINISH
    );

    public static CellType getTypeByCode(int code){
        if (!CELL_TYPE_CODES.containsKey(code)) return UNKNOWN;
        return CELL_TYPE_CODES.get(code);
    }
}
