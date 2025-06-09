package com.hippomaru.douckieTown.model.map;

import com.hippomaru.douckieTown.model.interactive.TrafficMember;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Cell {

    private final CellType cellType;
    private final Coords coords;

    private TrafficMember carriedEntity = null;

}

