package com.minelittlepony.unicopia.ability;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public interface Abilities {
    Registry<Ability<?>> REGISTRY = Registries.createSimple(Unicopia.id("abilities"));
    Map<AbilitySlot, Set<Ability<?>>> BY_SLOT = new EnumMap<>(AbilitySlot.class);
    BiFunction<AbilitySlot, Race, List<Ability<?>>> BY_SLOT_AND_RACE = Util.memoize((slot, race) -> {
        return BY_SLOT.computeIfAbsent(slot, s -> new LinkedHashSet<>())
                .stream()
                .filter(a -> a.canUse(race))
                .toList();
    });

    // unicorn / alicorn
    Ability<?> CAST = register(new UnicornCastingAbility(), "cast", AbilitySlot.PRIMARY);
    Ability<?> SHOOT = register(new UnicornProjectileAbility(), "shoot", AbilitySlot.PRIMARY);
    Ability<?> TELEPORT = register(new UnicornTeleportAbility(), "teleport", AbilitySlot.SECONDARY);
    Ability<?> GROUP_TELEPORT = register(new UnicornGroupTeleportAbility(), "teleport_group", AbilitySlot.SECONDARY);
    Ability<?> DISPELL = register(new UnicornDispellAbility(), "dispell", AbilitySlot.TERTIARY);

    // earth / alicorn
    Ability<?> KICK = register(new EarthPonyKickAbility(), "kick", AbilitySlot.PRIMARY);
    Ability<?> GROW = register(new EarthPonyGrowAbility(), "grow", AbilitySlot.SECONDARY);
    Ability<?> STOMP = register(new EarthPonyStompAbility(), "stomp", AbilitySlot.TERTIARY);

    // pegasus
    Ability<?> RAINBOOM = register(new PegasusRainboomAbility(), "rainboom", AbilitySlot.PRIMARY);
    Ability<?> CAPTURE_CLOUD = register(new PegasusCaptureStormAbility(), "capture_cloud", AbilitySlot.SECONDARY);

    // pegasus / bat / alicorn / changeling
    Ability<?> CARRY = register(new CarryAbility(), "carry", AbilitySlot.PASSIVE);

    // changeling
    Ability<?> DISGUISE = register(new ChangelingDisguiseAbility(), "disguise", AbilitySlot.SECONDARY);
    Ability<?> FEED = register(new ChangelingFeedAbility(), "feed", AbilitySlot.TERTIARY);

    // bat
    Ability<?> HANG = register(new BatPonyHangAbility(), "hang", AbilitySlot.SECONDARY);
    Ability<?> EEEE = register(new BatEeeeAbility(), "eee", AbilitySlot.TERTIARY);

    static <T extends Ability<?>> T register(T power, String name, AbilitySlot slot) {
        Identifier id = Unicopia.id(name);
        BY_SLOT.computeIfAbsent(slot, s -> new LinkedHashSet<>()).add(power);
        return Registry.register(REGISTRY, id, power);
    }

    static void bootstrap() {}
}
