package com.sstwo.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class SSTwoSettingsScreen extends Screen {
    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 430;
    private static final int SIDEBAR_WIDTH = 116;
    private static final int ROW_SPACING = 25;

    private final Screen parent;
    private final List<OptionEntry> options = new ArrayList<>();
    private final SSTwoButton[] categoryButtons = new SSTwoButton[Category.values().length];
    private final Map<KeyMapping, SSTwoButton> keybindButtons = new HashMap<>();

    private Category selectedCategory = Category.ZOOM;
    private KeyMapping selectedKeyMapping;
    private EditBox searchBox;
    private String searchQuery = "";
    private int panelLeft;
    private int panelTop;
    private int contentLeft;
    private int contentTop;
    private int contentWidth;

    public SSTwoSettingsScreen(Screen parent) {
        super(Component.literal("SSTwo Settings"));
        this.parent = parent;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - PANEL_HEIGHT) / 2;
        int right = panelLeft + PANEL_WIDTH;
        int bottom = panelTop + PANEL_HEIGHT;

        // Draw the SSTwo panel frame, sidebar, and section separators.
        graphics.fill(panelLeft - 2, panelTop - 2, right + 2, bottom + 2, 0xD9090D18);
        graphics.fill(panelLeft, panelTop, right, bottom, 0xDD171D2B);
        graphics.fill(panelLeft, panelTop, right, panelTop + 2, 0xFF55D6BE);
        graphics.fill(panelLeft + SIDEBAR_WIDTH, panelTop + 42, panelLeft + SIDEBAR_WIDTH + 1, bottom - 12, 0xFF303C4B);
        graphics.centeredText(minecraft.font, "SSTWO  /  SETTINGS", width / 2, panelTop + 13, 0xFFE5F5F2);
        String heading = searchQuery.isBlank() ? selectedCategory.title : "Search results";
        graphics.text(minecraft.font, heading, contentLeft, panelTop + 35, 0xFF55D6BE);
    }

    @Override
    protected void init() {
        options.clear();
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - PANEL_HEIGHT) / 2;
        contentLeft = panelLeft + SIDEBAR_WIDTH + 14;
        contentTop = panelTop + 91;
        contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 28;

        addCategoryButtons();
        addSearchBox();
        addZoomOptions();
        addHudOptions();
        addCombatOptions();
        addGeneralOptions();
        addCheatOptions();
        addKeybindOptions();
        addRenderableWidget(new SSTwoButton(
                panelLeft + 14, panelTop + PANEL_HEIGHT - 38, SIDEBAR_WIDTH - 28, 24,
                Component.literal("Back"), () -> minecraft.setScreenAndShow(parent)
        ));
        addRenderableWidget(new SSTwoButton(
                panelLeft + PANEL_WIDTH - 118, panelTop + PANEL_HEIGHT - 38, 104, 24,
                Component.literal("Reset all"), () -> {
                    ZoomSettings.reset();
                    refreshOptionLabels();
                }
        ));

        refreshVisibleOptions();
    }

    private void addCategoryButtons() {
        for (Category category : Category.values()) {
            SSTwoButton button = new SSTwoButton(
                    panelLeft + 8,
                    panelTop + 54 + category.ordinal() * 31,
                    SIDEBAR_WIDTH - 16,
                    25,
                    categoryLabel(category),
                    () -> selectCategory(category)
            );
            categoryButtons[category.ordinal()] = addRenderableWidget(button);
        }
    }

    private void addSearchBox() {
        searchBox = new EditBox(minecraft.font, contentLeft, panelTop + 48, contentWidth, 23,
                Component.literal("Search settings"));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.literal("Search all settings..."));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> {
            searchQuery = value;
            refreshVisibleOptions();
        });
        addRenderableWidget(searchBox);
    }

    private void addZoomOptions() {
        int narrowWidth = 38;
        int labelWidth = contentWidth - narrowWidth * 2 - 12;

        addOption(Category.ZOOM, "default zoom fov magnification",
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("−"), () -> changeZoom(-1)),
                new SSTwoButton(0, 0, labelWidth, 22, fovLabel(), () -> {}),
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("+"), () -> changeZoom(1)));
        addOption(Category.ZOOM, "zoom key step amount bracket sensitivity",
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("−"), () -> changeStep(-1)),
                new SSTwoButton(0, 0, labelWidth, 22, stepLabel(), () -> {}),
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("+"), () -> changeStep(1)));
        addToggleOption(Category.ZOOM, "zoom key c mode hold toggle",
                modeLabel(), ZoomSettings::toggleZoomMode, this::modeLabel);
        addToggleOption(Category.ZOOM, "smooth animation transition easing",
                smoothLabel(), ZoomSettings::toggleSmoothZoom, this::smoothLabel);
    }

    private void addHudOptions() {
        addToggleOption(Category.HUD, "coordinates xyz position location",
                coordinatesLabel(), ZoomSettings::toggleCoordinatesHud, this::coordinatesLabel);
        addToggleOption(Category.HUD, "fps frames performance counter",
                fpsLabel(), ZoomSettings::toggleFpsHud, this::fpsLabel);
        addToggleOption(Category.HUD, "direction facing compass",
                directionLabel(), ZoomSettings::toggleDirectionHud, this::directionLabel);
        addToggleOption(Category.HUD, "zoom status indicator",
                zoomHudLabel(), ZoomSettings::toggleZoomHud, this::zoomHudLabel);

        int narrowWidth = 38;
        int labelWidth = contentWidth - narrowWidth * 2 - 12;
        addOption(Category.HUD, "hud opacity transparency background",
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("−"),
                        () -> changeHudOpacity(-10)),
                new SSTwoButton(0, 0, labelWidth, 22, hudOpacityLabel(), () -> {}),
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("+"),
                        () -> changeHudOpacity(10)));
        addToggleOption(Category.HUD, "hud position corner placement location",
                hudPositionLabel(), ZoomSettings::cycleHudPosition, this::hudPositionLabel);
        addToggleOption(Category.HUD, "compact hud spacing layout",
                compactHudLabel(), ZoomSettings::toggleCompactHud, this::compactHudLabel);
    }

    private void addGeneralOptions() {
        addOption(Category.GENERAL, "resource packs textures custom packs folder",
                new SSTwoButton(0, 0, contentWidth, 22, Component.literal("Open Resource Packs..."),
                        this::openResourcePacks));
        addOption(Category.GENERAL, "reset defaults restore settings",
                new SSTwoButton(0, 0, contentWidth, 22, Component.literal("Reset all SSTwo settings"), () -> {
                    ZoomSettings.reset();
                    refreshOptionLabels();
                }));
    }

    private void addCheatOptions() {
        addToggleOption(Category.CHEATS, "xray ore finder mining diamond coal iron gold loaded chunks",
                oreFinderLabel(), ZoomSettings::toggleOreFinder, this::oreFinderLabel);
        addToggleOption(Category.CHEATS, "autoclicker auto click attack cps hold mouse",
                autoClickerLabel(), ZoomSettings::toggleAutoClicker, this::autoClickerLabel);
        addCpsOption("attack autoclicker clicks per second cps", "Attack speed",
                ZoomSettings::getAutoClickerCps, ZoomSettings::setAutoClickerCps);
        addToggleOption(Category.CHEATS, "right click autoclicker bridge block placement hold use",
                autoBridgeClickerLabel(), ZoomSettings::toggleAutoBridgeClicker, this::autoBridgeClickerLabel);
        addCpsOption("bridge autoclicker right click clicks per second cps", "Bridge speed",
                ZoomSettings::getAutoBridgeClickerCps, ZoomSettings::setAutoBridgeClickerCps);
        addToggleOption(Category.CHEATS, "auto totem offhand pop re-equip inventory",
                autoTotemLabel(), ZoomSettings::toggleAutoTotem, this::autoTotemLabel);
        addToggleOption(Category.CHEATS, "auto sprint run forward",
                autoSprintLabel(), ZoomSettings::toggleAutoSprint, this::autoSprintLabel);
        addToggleOption(Category.CHEATS, "extended longer long reach block entity interaction distance",
                extendedReachLabel(), ZoomSettings::toggleExtendedReach, this::extendedReachLabel);
        addToggleOption(Category.CHEATS, "auto tool best pickaxe axe shovel mining switch hotbar",
                autoToolLabel(), ZoomSettings::toggleAutoTool, this::autoToolLabel);
        addToggleOption(Category.CHEATS, "auto eat food hunger automatic",
                autoEatLabel(), ZoomSettings::toggleAutoEat, this::autoEatLabel);
    }

    private void addKeybindOptions() {
        keybindButtons.clear();
        for (KeyMapping keyMapping : SSTwoClient.getCustomKeyMappings()) {
            Component label = Component.translatable(keyMapping.getName());
            SSTwoButton button = new SSTwoButton(0, 0, contentWidth, 22,
                    keybindLabel(keyMapping), () -> {
                selectedKeyMapping = keyMapping;
                buttonMessage(keyMapping, Component.literal("Press a key (Esc to unbind)"));
            });
            keybindButtons.put(keyMapping, button);
            addOption(Category.KEYBINDS, "keybind controls keys reassign keyboard mouse "
                    + label.getString().toLowerCase(Locale.ROOT), button);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (selectedKeyMapping == null) {
            return super.keyPressed(event);
        }

        selectedKeyMapping.setKey(event.isEscape()
                ? InputConstants.UNKNOWN
                : InputConstants.getKey(event));
        finishKeybindSelection();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (selectedKeyMapping == null) {
            return super.mouseClicked(event, doubleClick);
        }

        selectedKeyMapping.setKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
        finishKeybindSelection();
        return true;
    }

    private Component keybindLabel(KeyMapping keyMapping) {
        return Component.translatable(keyMapping.getName())
                .append(Component.literal("  •  "))
                .append(keyMapping.getTranslatedKeyMessage());
    }

    private void buttonMessage(KeyMapping keyMapping, Component message) {
        SSTwoButton button = keybindButtons.get(keyMapping);
        if (button != null) {
            button.setMessage(message);
        }
    }

    private void finishKeybindSelection() {
        KeyMapping keyMapping = selectedKeyMapping;
        selectedKeyMapping = null;
        minecraft.options.save();
        buttonMessage(keyMapping, keybindLabel(keyMapping));
    }

    private void openResourcePacks() {
        var client = minecraft;
        client.setScreenAndShow(new PackSelectionScreen(
                client.getResourcePackRepository(),
                repository -> client.reloadResourcePacks(),
                client.getResourcePackDirectory(),
                Component.literal("Resource Packs")
        ) {
            @Override
            public void onClose() {
                super.onClose();
                client.setScreenAndShow(SSTwoSettingsScreen.this);
            }
        });
    }

    private void addCombatOptions() {
        addToggleOption(Category.COMBAT, "crosshair pvp bridging reticle aim",
                crosshairLabel(), ZoomSettings::cycleCrosshairMode, this::crosshairLabel);
        addToggleOption(Category.COMBAT, "hit particles crit attack effects enable disable",
                hitParticlesLabel(), ZoomSettings::toggleHitParticles, this::hitParticlesLabel);
        addToggleOption(Category.COMBAT, "hit particle style stars purple portal ender pearl end rod enchant",
                hitParticleStyleLabel(), ZoomSettings::cycleHitParticleStyle, this::hitParticleStyleLabel);
    }

    private void addToggleOption(Category category, String searchTerms, Component initialLabel,
                                 Runnable toggle, java.util.function.Supplier<Component> labelFactory) {
        SSTwoButton button = new SSTwoButton(0, 0, contentWidth, 22, initialLabel, () -> {});
        button.setAction(() -> {
            toggle.run();
            button.setMessage(labelFactory.get());
        });
        addOption(category, searchTerms, button);
    }

    private void addCpsOption(String searchTerms, String label, IntSupplier cpsSupplier, IntConsumer cpsSetter) {
        int narrowWidth = 38;
        int labelWidth = contentWidth - narrowWidth * 2 - 12;
        SSTwoButton cpsButton = new SSTwoButton(0, 0, labelWidth, 22, cpsLabel(label, cpsSupplier), () -> {});
        addOption(Category.CHEATS, searchTerms,
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("−"),
                        () -> changeCps(label, cpsSupplier, cpsSetter, cpsButton, -1)),
                cpsButton,
                new SSTwoButton(0, 0, narrowWidth, 22, Component.literal("+"),
                        () -> changeCps(label, cpsSupplier, cpsSetter, cpsButton, 1)));
    }

    private void changeCps(String label, IntSupplier cpsSupplier, IntConsumer cpsSetter,
                           SSTwoButton cpsButton, int direction) {
        int cps = cpsSupplier.getAsInt();
        int step = cps < 20 ? 1 : cps < 100 ? 5 : 50;
        cpsSetter.accept(cps + direction * step);
        cpsButton.setMessage(cpsLabel(label, cpsSupplier));
    }

    private Component cpsLabel(String label, IntSupplier cpsSupplier) {
        return Component.literal(label + "  •  " + cpsSupplier.getAsInt() + " CPS (3–1000)");
    }

    private void addOption(Category category, String searchTerms, SSTwoButton... widgets) {
        List<SSTwoButton> optionWidgets = List.of(widgets);
        for (SSTwoButton widget : optionWidgets) {
            addRenderableWidget(widget);
        }
        options.add(new OptionEntry(category, searchTerms.toLowerCase(Locale.ROOT), optionWidgets));
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        searchQuery = "";
        searchBox.setValue("");
        for (Category item : Category.values()) {
            categoryButtons[item.ordinal()].setMessage(categoryLabel(item));
        }
        refreshVisibleOptions();
    }

    // Search spans every category; an empty search shows only the selected category.
    private void refreshVisibleOptions() {
        if (searchBox != null && !searchQuery.isBlank()) {
            String query = searchQuery.toLowerCase(Locale.ROOT).trim();
            int row = 0;
            for (OptionEntry option : options) {
                boolean matches = option.category.title.toLowerCase(Locale.ROOT).contains(query)
                        || option.searchTerms.contains(query);
                setOptionVisible(option, matches, row);
                if (matches) {
                    row++;
                }
            }
            return;
        }

        int row = 0;
        for (OptionEntry option : options) {
            boolean visible = option.category == selectedCategory;
            setOptionVisible(option, visible, row);
            if (visible) {
                row++;
            }
        }
    }

    private void setOptionVisible(OptionEntry option, boolean visible, int row) {
        int x = contentLeft;
        int y = contentTop + row * ROW_SPACING;
        for (SSTwoButton widget : option.widgets) {
            widget.visible = visible;
            widget.setPosition(x, y);
            x += widget.getWidth() + 6;
        }
    }

    private void refreshOptionLabels() {
        // Recreate widgets to refresh every label after resetting the saved values.
        rebuildWidgets();
    }

    private void changeZoom(int amount) {
        ZoomController.adjust(minecraft, amount);
        refreshOptionLabels();
    }

    private void changeStep(int amount) {
        ZoomSettings.setZoomStep(ZoomSettings.getZoomStep() + amount);
        refreshOptionLabels();
    }

    private void changeHudOpacity(int amount) {
        ZoomSettings.setHudOpacity(ZoomSettings.getHudOpacity() + amount);
        refreshOptionLabels();
    }

    private Component categoryLabel(Category category) {
        return Component.literal((category == selectedCategory ? "•  " : "") + category.title);
    }

    private Component fovLabel() {
        return Component.literal("Default zoom  •  " + ZoomSettings.getZoomFov() + "° FOV");
    }

    private Component stepLabel() {
        return Component.literal("Key step [ / ]  •  " + ZoomSettings.getZoomStep() + "°");
    }

    private Component modeLabel() {
        return Component.literal("Zoom key (C)  •  " + (ZoomSettings.isHoldToZoom() ? "Hold" : "Toggle"));
    }

    private Component smoothLabel() {
        return Component.literal("Smooth animation  •  " + onOff(ZoomSettings.isSmoothZoom()));
    }

    private Component coordinatesLabel() {
        return Component.literal("Coordinates HUD  •  " + onOff(ZoomSettings.hasCoordinatesHud()));
    }

    private Component fpsLabel() {
        return Component.literal("FPS counter  •  " + onOff(ZoomSettings.hasFpsHud()));
    }

    private Component directionLabel() {
        return Component.literal("Direction HUD  •  " + onOff(ZoomSettings.hasDirectionHud()));
    }

    private Component zoomHudLabel() {
        return Component.literal("Zoom status HUD  •  " + onOff(ZoomSettings.hasZoomHud()));
    }

    private Component hudOpacityLabel() {
        return Component.literal("HUD background opacity  •  " + ZoomSettings.getHudOpacity() + "%");
    }

    private Component hudPositionLabel() {
        return Component.literal("HUD screen corner  •  " + ZoomSettings.getHudPositionName());
    }

    private Component compactHudLabel() {
        return Component.literal("Compact HUD layout  •  " + onOff(ZoomSettings.isCompactHud()));
    }

    private Component crosshairLabel() {
        return Component.literal("Crosshair style  •  " + ZoomSettings.getCrosshairModeName());
    }

    private Component hitParticlesLabel() {
        return Component.literal("Hit particles  •  " + onOff(ZoomSettings.hasHitParticles()));
    }

    private Component hitParticleStyleLabel() {
        return Component.literal("Hit particle style  •  " + ZoomSettings.getHitParticleStyleName());
    }

    private Component oreFinderLabel() {
        return Component.literal("Ore finder (X-ray)  •  " + onOff(ZoomSettings.isOreFinderEnabled()));
    }

    private Component autoClickerLabel() {
        return Component.literal("Attack autoclicker (while holding attack)  •  "
                + onOff(ZoomSettings.isAutoClickerEnabled()));
    }

    private Component autoBridgeClickerLabel() {
        return Component.literal("Bridge autoclicker (hold use; block targets only)  •  "
                + onOff(ZoomSettings.isAutoBridgeClickerEnabled()));
    }

    private Component autoTotemLabel() {
        return Component.literal("Auto totem (equip from inventory)  •  "
                + onOff(ZoomSettings.isAutoTotemEnabled()));
    }

    private Component autoSprintLabel() {
        return Component.literal("Auto sprint (while moving forward)  •  "
                + onOff(ZoomSettings.isAutoSprintEnabled()));
    }

    private Component extendedReachLabel() {
        return Component.literal("Extended reach (6 blocks; server permitting)  •  "
                + onOff(ZoomSettings.isExtendedReachEnabled()));
    }

    private Component autoToolLabel() {
        return Component.literal("Auto tool (best hotbar tool while mining)  •  "
                + onOff(ZoomSettings.isAutoToolEnabled()));
    }

    private Component autoEatLabel() {
        return Component.literal("Auto eat (hotbar food when hunger is low)  •  "
                + onOff(ZoomSettings.isAutoEatEnabled()));
    }

    private String onOff(boolean enabled) {
        return enabled ? "On" : "Off";
    }

    private enum Category {
        GENERAL("General"),
        ZOOM("Zoom"),
        HUD("HUD"),
        COMBAT("Combat"),
        CHEATS("Cheats"),
        KEYBINDS("Keybinds");

        private final String title;

        Category(String title) {
            this.title = title;
        }
    }

    private record OptionEntry(Category category, String searchTerms, List<SSTwoButton> widgets) {
    }
}
