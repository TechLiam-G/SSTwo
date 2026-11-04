package com.sstwo.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SSTwoScreen extends Screen {
    public SSTwoScreen() {
        super(Component.literal("SSTwo"));
    }

    @Override
    protected void init() {
        addRenderableWidget(new SSTwoButton(
                (width - 230) / 2,
                (height - 30) / 2,
                230,
                30,
                Component.literal("SSTwo Settings"),
                () -> minecraft.setScreenAndShow(new SSTwoSettingsScreen(this))
        ));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 130;
        int top = height / 2 - 55;
        graphics.fill(left - 2, top - 2, left + 262, top + 57, 0xD9090D18);
        graphics.fill(left, top, left + 260, top + 55, 0xDD171D2B);
        graphics.fill(left, top, left + 260, top + 2, 0xFF55D6BE);
    }
}
