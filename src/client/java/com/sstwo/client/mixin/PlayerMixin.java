package com.sstwo.client.mixin;

import com.sstwo.client.SSTwoClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
abstract class PlayerMixin {
    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;causeExtraKnockback("
                            + "Lnet/minecraft/world/entity/Entity;F"
                            + "Lnet/minecraft/world/phys/Vec3;"
                            + "Lnet/minecraft/world/damagesource/DamageSource;FZ)V"
            ),
            index = 1
    )
    private float sstwo$scaleHitKnockback(float knockback) {
        return knockback * SSTwoClient.getHitKnockbackMultiplier();
    }
}
