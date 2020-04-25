package com.minelittlepony.unicopia.particles;

import java.util.Optional;
import java.util.function.Consumer;
import com.minelittlepony.unicopia.magic.Caster;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ParticleHandle {

    private Optional<Attachment> particleEffect = Optional.empty();

    public Optional<Attachment> ifAbsent(ParticleSource source, Consumer<ParticleSpawner> constructor) {
        particleEffect.filter(Attachment::isStillAlive).orElseGet(() -> {
            if (source.getWorld().isClient) {
                constructor.accept(this::addParticle);
            }
            return null;
        });

        return particleEffect;
    }

    @Environment(EnvType.CLIENT)
    private void addParticle(ParticleEffect effect, Vec3d pos, Vec3d vel) {
        Particle p = MinecraftClient.getInstance().particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);

        if (p instanceof Attachment) {
            particleEffect = Optional.ofNullable((Attachment)p);
        }
    }

    public interface Attachment {

        boolean isStillAlive();

        void attach(Caster<?> caster);

        void setAttribute(int key, Object value);
    }
}