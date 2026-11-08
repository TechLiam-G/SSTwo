package com.sstwo.client.mixin;

import com.sstwo.client.SSTwoClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void sstwo$hideExplosionParticles(
            ParticleOptions particleOptions,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            CallbackInfoReturnable<Particle> callback
    ) {
        if (SSTwoClient.hasNoExplosionParticles()
                && (particleOptions.getType() == ParticleTypes.EXPLOSION
                || particleOptions.getType() == ParticleTypes.EXPLOSION_EMITTER)) {
            callback.setReturnValue(null);
        }
    }
}
