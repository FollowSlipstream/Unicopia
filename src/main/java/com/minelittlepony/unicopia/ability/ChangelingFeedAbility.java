package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particles.ParticleUtils;
import com.minelittlepony.unicopia.particles.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

/**
 * Changeling ability to restore health from mobs
 */
public class ChangelingFeedAbility implements Ability<Ability.Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.feed";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_O;
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 5;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return canFeed(player) ? 15 : 80;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies == Race.CHANGELING;
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {
        if (canFeed(player)) {
            if (!getTargets(player).isEmpty()) {
                return new Hit();
            }
        }

        return null;
    }

    private boolean canFeed(Pony player) {
        return player.getOwner().getHealth() < player.getOwner().getMaximumHealth() || player.getOwner().canConsume(false);
    }

    private boolean canDrain(Entity e) {
        return (e instanceof LivingEntity)
            && (e instanceof CowEntity
            || e instanceof AbstractTraderEntity
            || e instanceof PlayerEntity
            || e instanceof SheepEntity
            || e instanceof PigEntity
            || e instanceof HostileEntity);
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    protected List<LivingEntity> getTargets(Pony player) {
        List<Entity> list = VecHelper.getWithinRange(player.getOwner(), 3, this::canDrain);

        Entity looked = VecHelper.getLookedAtEntity(player.getOwner(), 17);
        if (looked != null && !list.contains(looked) && canDrain(looked)) {
            list.add(looked);
        }

        return list.stream().map(i -> (LivingEntity)i).collect(Collectors.toList());
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.getOwner();

        float maximumHealthGain = player.getMaximumHealth() - player.getHealth();
        int maximumFoodGain = player.canConsume(false) ? (20 - player.getHungerManager().getFoodLevel()) : 0;

        if (maximumHealthGain > 0 || maximumFoodGain > 0) {

            float healAmount = 0;

            for (LivingEntity i : getTargets(iplayer)) {
                healAmount += drainFrom(player, i);
            }

            int foodAmount = (int)Math.floor(Math.min(healAmount / 3, maximumFoodGain));

            if (foodAmount > 0) {
                healAmount -= foodAmount;
                player.getHungerManager().add(foodAmount, 0.125f);
            }

            if (healAmount > 0) {
                player.heal(Math.min(healAmount, maximumHealthGain));
            }
        }
    }

    public float drainFrom(PlayerEntity changeling, LivingEntity living) {
        DamageSource d = MagicalDamageSource.causePlayerDamage("feed", changeling);

        float damage = living.getHealth()/2;

        if (damage > 0) {
            living.damage(d, damage);
        }

        ParticleUtils.spawnParticles(UParticles.CHANGELING_MAGIC, living, 7);

        if (changeling.hasStatusEffect(StatusEffects.NAUSEA)) {
            living.addStatusEffect(changeling.removeStatusEffectInternal(StatusEffects.NAUSEA));
        } else if (changeling.getEntityWorld().random.nextInt(2300) == 0) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 1));
        }

        if (living instanceof PlayerEntity) {
            damage ++;
            damage *= 1.6F;

            if (!changeling.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                changeling.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 13000, 1));
            }
        }

        return damage;
    }

    @Override
    public void preApply(Pony player) {
        player.addExertion(6);
    }

    @Override
    public void postApply(Pony player) {
        player.spawnParticles(ParticleTypes.HEART, 1);
    }
}