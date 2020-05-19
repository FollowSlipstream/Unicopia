package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Affine;
import com.minelittlepony.unicopia.magic.AttachedMagicEffect;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.network.EffectSync;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;

public class Creature implements Ponylike<LivingEntity>, Caster<LivingEntity> {

    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    public static void boostrap() {}

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final Physics physics = new EntityPhysics<>(this);

    private final LivingEntity entity;

    public Creature(LivingEntity entity) {
        this.entity = entity;

        entity.getDataTracker().startTracking(EFFECT, new CompoundTag());
    }

    @Override
    public Race getSpecies() {
        return Race.HUMAN;
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public void setSpecies(Race race) {
    }

    @Override
    public void setEffect(MagicEffect effect) {
        effectDelegate.set(effect);
    }

    @Override
    public <T extends MagicEffect> T getEffect(Class<T> type, boolean update) {
        return effectDelegate.get(type, update);
    }

    @Override
    public boolean hasEffect() {
        return effectDelegate.has();
    }

    @Override
    public void tick() {
        if (hasEffect()) {
            AttachedMagicEffect effect = getEffect(AttachedMagicEffect.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isClient()) {
                    effect.renderOnPerson(this);
                }

                if (!effect.updateOnPerson(this)) {
                    setEffect(null);
                }
            }
        }
    }

    @Override
    public void setOwner(LivingEntity owner) {

    }

    @Override
    public LivingEntity getOwner() {
        return entity;
    }

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public void setCurrentLevel(int level) {
    }

    @Override
    public Affinity getAffinity() {
        if (getOwner() instanceof Affine) {
            return ((Affine)getOwner()).getAffinity();
        }
        return Affinity.NEUTRAL;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        MagicEffect effect = getEffect();

        if (effect != null) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(effect));
        }
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.contains("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
        physics.fromNBT(compound);
    }
}