package com.minelittlepony.unicopia.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PhysicsBodyProjectileEntity extends PersistentProjectileEntity implements FlyingItemEntity {

    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(PhysicsBodyProjectileEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Boolean> BOUNCY = DataTracker.registerData(PhysicsBodyProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public PhysicsBodyProjectileEntity(EntityType<PhysicsBodyProjectileEntity> type, World world) {
        super(type, world);
    }

    public PhysicsBodyProjectileEntity(World world) {
        this(UEntities.MUFFIN, world);
    }

    public PhysicsBodyProjectileEntity(World world, @Nullable LivingEntity thrower) {
        super(UEntities.MUFFIN, thrower, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
        getDataTracker().startTracking(BOUNCY, false);
    }

    public void setStack(ItemStack stack) {
        getDataTracker().set(ITEM, stack);
    }

    @Override
    public ItemStack getStack() {
        return getDataTracker().get(ITEM);
    }

    @Override
    protected ItemStack asItemStack() {
        return getStack();
    }

    public void setBouncy() {
        getDataTracker().set(BOUNCY, true);
    }

    public boolean isBouncy() {
        return getDataTracker().get(BOUNCY);
    }

    @Override
    public void tick() {
        super.tick();
        if (inGround) {
            Vec3d vel = getVelocity();
            vel = vel.multiply(0, 1, 0);

            move(MovementType.SELF, vel);

            setVelocity(vel.multiply(0.3));
            addVelocity(0, -0.025, 0);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (isBouncy()) {
            setVelocity(getVelocity().multiply(-0.1, -0.3, -0.1));
            setYaw(getYaw() + 180);
            prevYaw += 180;
            return;
        }
        super.onEntityHit(hit);
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        BlockState state = world.getBlockState(hit.getBlockPos());

        if (getVelocity().length() > 0.2F) {
            boolean ownerCanModify = Caster.of(getOwner()).filter(pony -> pony.canModifyAt(hit.getBlockPos())).isPresent();

            if (ownerCanModify && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                if ((!isBouncy() || world.random.nextInt(200) == 0) && state.isIn(UTags.FRAGILE)) {
                    world.breakBlock(hit.getBlockPos(), true);
                }
            }

            if (isBouncy()) {
                Direction.Axis side = hit.getSide().getAxis();

                if (side == Direction.Axis.X) {
                    setVelocity(getVelocity().multiply(-0.4, 0.3, 0.3));
                }

                if (side == Direction.Axis.Y) {
                    setVelocity(getVelocity().multiply(0.3, -0.4, 0.3));
                }

                if (side == Direction.Axis.Z) {
                    setVelocity(getVelocity().multiply(0.3, 0.3, -0.4));
                }
            } else {
                super.onBlockHit(hit);
            }
        } else {
            super.onBlockHit(hit);
        }

        setSound(state.getSoundGroup().getStepSound());
        world.playSoundFromEntity(null, this, state.getSoundGroup().getStepSound(), SoundCategory.BLOCKS, 1, 1);
        emitGameEvent(GameEvent.STEP);
    }

    @Override
    protected SoundEvent getHitSound() {
        return isBouncy() ? SoundEvents.BLOCK_NOTE_BLOCK_BANJO : SoundEvents.BLOCK_STONE_HIT;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            nbt.put("Item", stack.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setStack(ItemStack.fromNbt(nbt.getCompound("Item")));
    }
}