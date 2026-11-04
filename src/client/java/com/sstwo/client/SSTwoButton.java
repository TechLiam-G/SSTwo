package com.sstwo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class SSTwoButton extends AbstractButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            "sstwo", "textures/gui/button_atlas.png"
    );
    private static final int TEXTURE_WIDTH = 384;
    private static final int TEXTURE_HEIGHT = 32;
    private Runnable action;

    SSTwoButton(int x, int y, int width, int height, Component label, Runnable action) {
        super(x, y, width, height, label);
        this.action = action;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    void setAction(Runnable action) {
        this.action = action;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int state = !active ? 2 : isHoveredOrFocused() ? 1 : 0;
        int color = !active ? 0xFFB7C2C9 : isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFE5F5F2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                getX(), getY(),
                state * 128, 0,
                width, height,
                128, 32,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
        graphics.centeredText(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + width / 2,
                getY() + (height - 9) / 2,
                color
        );
    }
}
