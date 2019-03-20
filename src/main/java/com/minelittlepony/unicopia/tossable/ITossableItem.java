package com.minelittlepony.unicopia.tossable;

import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.item.IDispensable;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public interface ITossableItem extends ITossable<ItemStack>, IDispensable {

    default boolean canBeThrown(ItemStack stack) {
        return true;
    }

    @Override
    default ActionResult<ItemStack> dispenseStack(IBlockSource source, ItemStack stack) {

        if (canBeThrown(stack)) {
            stack = toss(source.getWorld(), BlockDispenser.getDispensePosition(source), (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING), stack);

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    default void toss(World world, ItemStack itemstack, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        world.playSound(null, player.posX, player.posY, player.posZ, getThrowSound(itemstack), SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            EntityProjectile projectile = new EntityProjectile(world, player);

            projectile.setItem(itemstack);
            projectile.setThrowDamage(getThrowDamage(itemstack));
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5F, 1);

            world.spawnEntity(projectile);
        }

        player.addStat(StatList.getObjectUseStats(itemstack.getItem()));
    }

    default ItemStack toss(World world, IPosition pos, EnumFacing facing, ItemStack stack) {
        EntityProjectile iprojectile = new EntityProjectile(world, pos.getX(), pos.getY(), pos.getZ());

        iprojectile.setItem(stack);
        iprojectile.setThrowDamage(getThrowDamage(stack));

        iprojectile.shoot(facing.getXOffset(), facing.getYOffset() + 0.1F, facing.getZOffset(), 1.1F, 6);

        world.spawnEntity(iprojectile);

        stack.shrink(1);

        return stack;
    }
}
