package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.entity.player.Pony;

/**
 * Predicate for abilities to control whether a player can fly.
 *
 * This overrides what the race specifies.
 */
public interface FlightPredicate {
    FlightType getFlightType(Pony player);
}
