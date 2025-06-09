package com.hippomaru.douckieTown.model.interactive;

import com.hippomaru.douckieTown.model.map.Coords;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TrafficMember {
    protected Direction currentDirection;
}
