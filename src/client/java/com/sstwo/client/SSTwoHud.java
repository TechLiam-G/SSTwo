package com.sstwo.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class SSTwoHud {
    private static final int TEXT_COLOR = 0xFFE5F5F2;
    private SSTwoHud() {
    }

    static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("sstwo", "information_hud"),
                SSTwoHud::render
        );
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanillaCrosshair ->
                (graphics, deltaTracker) -> {
                    if (ZoomSettings.getCrosshairMode() == 0) {
                        vanillaCrosshair.extractRenderState(graphics, deltaTracker);
                    } else {
                        renderCustomCrosshair(graphics);
                    }
                });
    }

    private static void renderCustomCrosshair(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.screen() != null) {
            return;
        }

        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        if (ZoomSettings.getCrosshairMode() == 1) {
            drawPvpCrosshair(graphics, centerX, centerY);
        } else {
            drawBridgingCrosshair(graphics, centerX, centerY);
        }
    }

    // Outlined short arms remain visible against both bright and dark scenes.
    private static void drawPvpCrosshair(GuiGraphicsExtractor graphics, int x, int y) {
        int outline = 0xE6000000;
        int color = 0xFFF4F7FF;
        graphics.fill(x - 1, y - 6, x + 2, y - 2, outline);
        graphics.fill(x - 1, y + 2, x + 2, y + 6, outline);
        graphics.fill(x - 6, y - 1, x - 2, y + 2, outline);
        graphics.fill(x + 2, y - 1, x + 6, y + 2, outline);
        graphics.fill(x, y - 5, x + 1, y - 2, color);
        graphics.fill(x, y + 2, x + 1, y + 5, color);
        graphics.fill(x - 5, y, x - 2, y + 1, color);
        graphics.fill(x + 2, y, x + 5, y + 1, color);
        graphics.fill(x, y, x + 1, y + 1, 0xFF55D6BE);
    }

    // A compact T marker helps align the center while placing blocks.
    private static void drawBridgingCrosshair(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 6, x + 2, y + 6, 0xE6000000);
        graphics.fill(x - 6, y - 1, x + 6, y + 2, 0xE6000000);
        graphics.fill(x - 5, y, x + 6, y + 1, 0xFFFFD166);
        graphics.fill(x, y - 5, x + 1, y + 6, 0xFFFFD166);
        graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFFFFFF);
    }

    private static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.screen() != null) {
            return;
        }

        List<String> lines = new ArrayList<>(4);
        if (ZoomSettings.hasCoordinatesHud()) {
            var position = client.player.blockPosition();
            lines.add(String.format(Locale.ROOT, "XYZ: %d  %d  %d",
                    position.getX(), position.getY(), position.getZ()));
        }
        if (ZoomSettings.hasFpsHud()) {
            lines.add("FPS: " + client.getFps());
        }
        if (ZoomSettings.hasDirectionHud()) {
            lines.add("Facing: " + client.player.getDirection().getName().toUpperCase(Locale.ROOT));
        }
        if (ZoomSettings.hasZoomHud()) {
            lines.add("Zoom: " + (ZoomController.isZoomEnabled() ? "ON" : "OFF"));
        }
        lines.addAll(CheatController.getOreLines());

        if (lines.isEmpty()) {
            return;
        }

        int padding = ZoomSettings.isCompactHud() ? 5 : 10;
        int lineHeight = ZoomSettings.isCompactHud() ? 11 : 14;
        int panelWidth = lines.stream().mapToInt(client.font::width).max().orElse(0) + padding * 2;
        int panelHeight = lines.size() * lineHeight + 4;
        boolean right = ZoomSettings.getHudPosition() % 2 == 1;
        boolean bottom = ZoomSettings.getHudPosition() >= 2;
        int x = right ? graphics.guiWidth() - panelWidth - 8 : 8;
        int y = bottom ? graphics.guiHeight() - panelHeight - 8 : 8;
        int alpha = Math.round(255 * ZoomSettings.getHudOpacity() / 100.0f);
        int panelColor = (alpha << 24) | 0x00090D18;

        for (String line : lines) {
            graphics.fill(x, y, x + panelWidth, y + lineHeight, panelColor);
            graphics.fill(x, y, x + 2, y + lineHeight, 0xFF55D6BE);
            graphics.text(client.font, line, x + padding, y + (lineHeight - 9) / 2, TEXT_COLOR);
            y += lineHeight;
        }
    }
}
