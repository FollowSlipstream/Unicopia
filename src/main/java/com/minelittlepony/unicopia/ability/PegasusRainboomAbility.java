package com.minelittlepony.unicopia.ability;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.util.math.Vec3d;

/**
 * Pegasus ability to perform rainbooms
 */
public class PegasusRainboomAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 59;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 60;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canInteractWithClouds();
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {

        if (!player.getMaster().isCreative() && player.getMagicalReserves().getMana().getPercentFill() < 0.2F) {
            return null;
        }

        if (player.getPhysics().isFlying() && !SpellType.RAINBOOM.isOn(player)) {
            return Hit.INSTANCE;
        }

        return null;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return player.getMagicalReserves().getMana().getMax() * 0.9F;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type) {

        if (type == ActivationType.TAP && player.getPhysics().isFlying()) {
            player.getPhysics().dashForward((float)player.getReferenceWorld().random.nextTriangular(1, 0.3F));
            return true;
        }

        return false;
    }

    @Override
    public void apply(Pony player, Hit data) {

        if (!player.getMaster().isCreative() && player.getMagicalReserves().getMana().getPercentFill() < 0.2F) {
            return;
        }

        if (player.getPhysics().isFlying() && !SpellType.RAINBOOM.isOn(player)) {
            player.getMagicalReserves().getMana().multiply(0.1F);
            player.addParticle(new OrientedBillboardParticleEffect(UParticles.RAINBOOM_RING, player.getPhysics().getMotionAngle()), player.getOriginVector(), Vec3d.ZERO);
            SpellType.RAINBOOM.withTraits().apply(player);
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(6);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
