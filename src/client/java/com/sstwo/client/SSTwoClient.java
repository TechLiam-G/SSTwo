package com.sstwo.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

public final class SSTwoClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("sstwo", "main")
    );
    private static final KeyMapping OPEN_MENU_KEY = registerKey(
            "key.sstwo.open_menu", InputConstants.KEY_RSHIFT
    );
    private static final KeyMapping ZOOM_KEY = registerKey(
            "key.sstwo.zoom", InputConstants.KEY_C
    );
    private static final KeyMapping ZOOM_IN_KEY = registerKey(
            "key.sstwo.zoom_in", InputConstants.KEY_LBRACKET
    );
    private static final KeyMapping ZOOM_OUT_KEY = registerKey(
            "key.sstwo.zoom_out", InputConstants.KEY_RBRACKET
    );
    private static final List<KeyMapping> CUSTOM_KEY_MAPPINGS = List.of(
            OPEN_MENU_KEY, ZOOM_KEY, ZOOM_IN_KEY, ZOOM_OUT_KEY
    );

    static List<KeyMapping> getCustomKeyMappings() {
        return CUSTOM_KEY_MAPPINGS;
    }

    private static KeyMapping registerKey(String translationKey, int defaultKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey,
                InputConstants.Type.KEYBOARD,
                defaultKey,
                CATEGORY
        ));
    }

    @Override
    public void onInitializeClient() {
        ZoomSettings.load();
        SSTwoHud.register();
        registerHitParticles();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU_KEY.consumeClick()) {
                client.setScreenAndShow(new SSTwoScreen());
            }
            while (ZOOM_IN_KEY.consumeClick()) {
                ZoomController.adjust(client, -ZoomSettings.getZoomStep());
            }
            while (ZOOM_OUT_KEY.consumeClick()) {
                ZoomController.adjust(client, ZoomSettings.getZoomStep());
            }
            ZoomController.tick(client, ZOOM_KEY);
            CheatController.tick(client);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(ZoomController::disableImmediately);
    }

    private static void registerHitParticles() {
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!ZoomSettings.hasHitParticles()
                    || player != Minecraft.getInstance().player
                    || !(world instanceof ClientLevel clientLevel)
                    || !(target instanceof LivingEntity livingTarget)) {
                return InteractionResult.PASS;
            }

            var random = ThreadLocalRandom.current();
            double centerY = livingTarget.getY() + livingTarget.getBbHeight() * 0.5;
            var particle = switch (ZoomSettings.getHitParticleStyle()) {
                case 1 -> ParticleTypes.PORTAL;
                case 2 -> ParticleTypes.END_ROD;
                case 3 -> ParticleTypes.ENCHANT;
                default -> ParticleTypes.CRIT;
            };
            for (int i = 0; i < 14; i++) {
                double x = livingTarget.getX() + (random.nextDouble() - 0.5) * livingTarget.getBbWidth();
                double y = centerY + (random.nextDouble() - 0.5) * livingTarget.getBbHeight();
                double z = livingTarget.getZ() + (random.nextDouble() - 0.5) * livingTarget.getBbWidth();
                clientLevel.addAlwaysVisibleParticle(
                        particle,
                        true,
                        x, y, z,
                        (random.nextDouble() - 0.5) * 0.2,
                        0.04 + random.nextDouble() * 0.18,
                        (random.nextDouble() - 0.5) * 0.2
                );
            }
            return InteractionResult.PASS;
        });
    }
}
