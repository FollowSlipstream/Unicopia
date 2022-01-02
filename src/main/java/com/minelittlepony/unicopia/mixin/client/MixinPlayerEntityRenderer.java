package com.minelittlepony.unicopia.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer.FeatureRoot;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

@Mixin(PlayerEntityRenderer.class)
abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    @Nullable
    private AccessoryFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> accessories;

    MixinPlayerEntityRenderer() { super(null, null, 0); }

    @SuppressWarnings("unchecked")
    private AccessoryFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> getAccessories() {
        if (accessories == null) {
            accessories = features.stream()
                .filter(a -> a instanceof FeatureRoot)
                .map(a -> ((FeatureRoot<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>)a).getAccessories())
                .findFirst()
                .orElseGet(() -> new AccessoryFeatureRenderer<>(this));
        }
        return accessories;
    }

    @Inject(method = "renderArm", at = @At("RETURN"))
    private void onRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo info) {
        Arm a = this.getModel().leftArm == arm ? Arm.LEFT : Arm.RIGHT;
        getAccessories().renderArm(matrices, vertexConsumers, light, player, arm, a);
    }
}
