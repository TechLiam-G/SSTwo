package com.sstwo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

final class ZoomController {
    private static boolean zoomTargetEnabled;
    private static Integer previousFov;

    private ZoomController() {
    }

    static boolean isZoomEnabled() {
        return zoomTargetEnabled;
    }

    static void tick(Minecraft client, KeyMapping zoomKey) {
        if (client.player == null || client.level == null) {
            disableImmediately(client);
            return;
        }

        if (client.gui.screen() != null) {
            setZoomEnabled(client, false);
        } else if (ZoomSettings.isHoldToZoom()) {
            setZoomEnabled(client, zoomKey.isDown());
        } else if (zoomKey.consumeClick()) {
            setZoomEnabled(client, !zoomTargetEnabled);
        }

        updateFov(client);
    }

    static void adjust(Minecraft client, int amount) {
        ZoomSettings.setZoomFov(ZoomSettings.getZoomFov() + amount);
    }

    static void setZoomEnabled(Minecraft client, boolean enabled) {
        if (enabled == zoomTargetEnabled) {
            return;
        }
        if (enabled && previousFov == null) {
            previousFov = client.options.fov().get();
        }
        zoomTargetEnabled = enabled;
        if (!ZoomSettings.isSmoothZoom()) {
            updateFov(client);
        }
    }

    static void disableImmediately(Minecraft client) {
        if (previousFov != null) {
            client.options.fov().set(previousFov);
        }
        previousFov = null;
        zoomTargetEnabled = false;
    }

    private static void updateFov(Minecraft client) {
        if (previousFov == null) {
            return;
        }

        int targetFov = zoomTargetEnabled
                ? Math.min(previousFov, ZoomSettings.getZoomFov())
                : previousFov;
        int currentFov = client.options.fov().get();
        if (!ZoomSettings.isSmoothZoom()) {
            client.options.fov().set(targetFov);
        } else if (currentFov != targetFov) {
            int difference = targetFov - currentFov;
            int step = Math.max(1, Math.abs(difference) / 4);
            client.options.fov().set(currentFov + Integer.signum(difference) * step);
        }

        if (!zoomTargetEnabled && client.options.fov().get() == previousFov) {
            previousFov = null;
        }
    }
}
