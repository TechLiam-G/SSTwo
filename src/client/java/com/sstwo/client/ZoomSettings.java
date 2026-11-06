package com.sstwo.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class ZoomSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger("SSTwo");
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("sstwo.properties");
    private static final int MIN_ZOOM_FOV = 30;
    private static final int MAX_ZOOM_FOV = 70;
    private static final int MIN_ZOOM_STEP = 1;
    private static final int MAX_ZOOM_STEP = 10;
    private static final int MIN_HUD_OPACITY = 20;
    private static final int MAX_HUD_OPACITY = 100;
    private static final int MIN_AUTOCLICK_CPS = 3;
    private static final int MAX_AUTOCLICK_CPS = 1000;
    private static final String[] HUD_POSITIONS = {"Top left", "Top right", "Bottom left", "Bottom right"};
    private static final String[] CROSSHAIR_MODES = {"Vanilla", "PvP", "Bridging"};
    private static final String[] HIT_PARTICLE_STYLES = {"Stars", "Purple portal", "End rod", "Enchant"};

    private static int zoomFov = 30;
    private static int zoomStep = 5;
    private static int hudOpacity = 75;
    private static int hudPosition;
    private static int crosshairMode;
    private static int hitParticleStyle;
    private static boolean holdToZoom;
    private static boolean smoothZoom = true;
    private static boolean coordinatesHud = true;
    private static boolean fpsHud = true;
    private static boolean directionHud = true;
    private static boolean zoomHud = true;
    private static boolean compactHud;
    private static boolean hitParticles = true;
    private static boolean oreFinder;
    private static boolean autoClicker;
    private static boolean autoBridgeClicker;
    private static int autoClickerCps = 8;
    private static int autoBridgeClickerCps = 20;
    private static boolean autoTotem;
    private static boolean autoSprint;
    private static boolean extendedReach;
    private static boolean autoTool;
    private static boolean autoEat;

    private ZoomSettings() {
    }

    static int getZoomFov() {
        return zoomFov;
    }

    static int getZoomStep() {
        return zoomStep;
    }

    static boolean isHoldToZoom() {
        return holdToZoom;
    }

    static boolean isSmoothZoom() {
        return smoothZoom;
    }

    static boolean hasCoordinatesHud() {
        return coordinatesHud;
    }

    static boolean hasFpsHud() {
        return fpsHud;
    }

    static boolean hasDirectionHud() {
        return directionHud;
    }

    static boolean hasZoomHud() {
        return zoomHud;
    }

    static int getHudOpacity() {
        return hudOpacity;
    }

    static int getHudPosition() {
        return hudPosition;
    }

    static String getHudPositionName() {
        return HUD_POSITIONS[hudPosition];
    }

    static boolean isCompactHud() {
        return compactHud;
    }

    static int getCrosshairMode() {
        return crosshairMode;
    }

    static String getCrosshairModeName() {
        return CROSSHAIR_MODES[crosshairMode];
    }

    static boolean hasHitParticles() {
        return hitParticles;
    }

    static int getHitParticleStyle() {
        return hitParticleStyle;
    }

    static String getHitParticleStyleName() {
        return HIT_PARTICLE_STYLES[hitParticleStyle];
    }

    static boolean isOreFinderEnabled() {
        return oreFinder;
    }

    static boolean isAutoClickerEnabled() {
        return autoClicker;
    }

    static boolean isAutoBridgeClickerEnabled() {
        return autoBridgeClicker;
    }

    static int getAutoClickerCps() {
        return autoClickerCps;
    }

    static int getAutoBridgeClickerCps() {
        return autoBridgeClickerCps;
    }

    static boolean isAutoTotemEnabled() {
        return autoTotem;
    }

    static boolean isAutoSprintEnabled() {
        return autoSprint;
    }

    static boolean isExtendedReachEnabled() {
        return extendedReach;
    }

    static boolean isAutoToolEnabled() {
        return autoTool;
    }

    static boolean isAutoEatEnabled() {
        return autoEat;
    }

    static void setZoomFov(int value) {
        zoomFov = Math.clamp(value, MIN_ZOOM_FOV, MAX_ZOOM_FOV);
        save();
    }

    static void setZoomStep(int value) {
        zoomStep = Math.clamp(value, MIN_ZOOM_STEP, MAX_ZOOM_STEP);
        save();
    }

    static void setHudOpacity(int value) {
        hudOpacity = Math.clamp(value, MIN_HUD_OPACITY, MAX_HUD_OPACITY);
        save();
    }

    static void cycleHudPosition() {
        hudPosition = (hudPosition + 1) % HUD_POSITIONS.length;
        save();
    }

    static void cycleCrosshairMode() {
        crosshairMode = (crosshairMode + 1) % CROSSHAIR_MODES.length;
        save();
    }

    static void toggleZoomMode() {
        holdToZoom = !holdToZoom;
        save();
    }

    static void toggleSmoothZoom() {
        smoothZoom = !smoothZoom;
        save();
    }

    static void toggleCoordinatesHud() {
        coordinatesHud = !coordinatesHud;
        save();
    }

    static void toggleFpsHud() {
        fpsHud = !fpsHud;
        save();
    }

    static void toggleDirectionHud() {
        directionHud = !directionHud;
        save();
    }

    static void toggleZoomHud() {
        zoomHud = !zoomHud;
        save();
    }

    static void toggleCompactHud() {
        compactHud = !compactHud;
        save();
    }

    static void toggleHitParticles() {
        hitParticles = !hitParticles;
        save();
    }

    static void cycleHitParticleStyle() {
        hitParticleStyle = (hitParticleStyle + 1) % HIT_PARTICLE_STYLES.length;
        save();
    }

    static void toggleOreFinder() {
        oreFinder = !oreFinder;
        save();
    }

    static void toggleAutoClicker() {
        autoClicker = !autoClicker;
        save();
    }

    static void toggleAutoBridgeClicker() {
        autoBridgeClicker = !autoBridgeClicker;
        save();
    }

    static void setAutoClickerCps(int value) {
        autoClickerCps = Math.clamp(value, MIN_AUTOCLICK_CPS, MAX_AUTOCLICK_CPS);
        save();
    }

    static void setAutoBridgeClickerCps(int value) {
        autoBridgeClickerCps = Math.clamp(value, MIN_AUTOCLICK_CPS, MAX_AUTOCLICK_CPS);
        save();
    }

    static void toggleAutoTotem() {
        autoTotem = !autoTotem;
        save();
    }

    static void toggleAutoSprint() {
        autoSprint = !autoSprint;
        save();
    }

    static void toggleExtendedReach() {
        extendedReach = !extendedReach;
        save();
    }

    static void toggleAutoTool() {
        autoTool = !autoTool;
        save();
    }

    static void toggleAutoEat() {
        autoEat = !autoEat;
        save();
    }

    static void reset() {
        zoomFov = 30;
        zoomStep = 5;
        hudOpacity = 75;
        hudPosition = 0;
        crosshairMode = 0;
        hitParticleStyle = 0;
        holdToZoom = false;
        smoothZoom = true;
        coordinatesHud = true;
        fpsHud = true;
        directionHud = true;
        zoomHud = true;
        compactHud = false;
        hitParticles = true;
        oreFinder = false;
        autoClicker = false;
        autoBridgeClicker = false;
        autoClickerCps = 8;
        autoBridgeClickerCps = 20;
        autoTotem = false;
        autoSprint = false;
        extendedReach = false;
        autoTool = false;
        autoEat = false;
        save();
    }

    static void load() {
        if (!Files.exists(CONFIG_FILE)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_FILE)) {
            properties.load(input);
            int loadedFov = Integer.parseInt(properties.getProperty("zoomFov", "30"));
            int loadedStep = Integer.parseInt(properties.getProperty("zoomStep", "5"));
            int loadedHudOpacity = Integer.parseInt(properties.getProperty("hudOpacity", "75"));
            int loadedHudPosition = Integer.parseInt(properties.getProperty("hudPosition", "0"));
            int loadedCrosshairMode = Integer.parseInt(properties.getProperty("crosshairMode", "0"));
            int loadedHitParticleStyle = Integer.parseInt(properties.getProperty("hitParticleStyle", "0"));
            boolean loadedHoldToZoom = Boolean.parseBoolean(properties.getProperty("holdToZoom", "false"));
            boolean loadedSmoothZoom = Boolean.parseBoolean(properties.getProperty("smoothZoom", "true"));
            boolean loadedCoordinatesHud = Boolean.parseBoolean(properties.getProperty("coordinatesHud", "true"));
            boolean loadedFpsHud = Boolean.parseBoolean(properties.getProperty("fpsHud", "true"));
            boolean loadedDirectionHud = Boolean.parseBoolean(properties.getProperty("directionHud", "true"));
            boolean loadedZoomHud = Boolean.parseBoolean(properties.getProperty("zoomHud", "true"));
            boolean loadedCompactHud = Boolean.parseBoolean(properties.getProperty("compactHud", "false"));
            boolean loadedHitParticles = Boolean.parseBoolean(properties.getProperty("hitParticles", "true"));
            boolean loadedOreFinder = Boolean.parseBoolean(properties.getProperty("oreFinder", "false"));
            boolean loadedAutoClicker = Boolean.parseBoolean(properties.getProperty("autoClicker", "false"));
            boolean loadedAutoBridgeClicker = Boolean.parseBoolean(
                    properties.getProperty("autoBridgeClicker", "false"));
            int loadedAutoClickerCps = Integer.parseInt(properties.getProperty("autoClickerCps", "8"));
            int loadedAutoBridgeClickerCps = Integer.parseInt(
                    properties.getProperty("autoBridgeClickerCps", "20"));
            boolean loadedAutoTotem = Boolean.parseBoolean(properties.getProperty("autoTotem", "false"));
            boolean loadedAutoSprint = Boolean.parseBoolean(properties.getProperty("autoSprint", "false"));
            boolean loadedExtendedReach = Boolean.parseBoolean(properties.getProperty("extendedReach", "false"));
            boolean loadedAutoTool = Boolean.parseBoolean(properties.getProperty("autoTool", "false"));
            boolean loadedAutoEat = Boolean.parseBoolean(properties.getProperty("autoEat", "false"));
            if (loadedFov < MIN_ZOOM_FOV || loadedFov > MAX_ZOOM_FOV) {
                throw new IllegalArgumentException("zoomFov must be between 30 and 70");
            }
            if (loadedStep < MIN_ZOOM_STEP || loadedStep > MAX_ZOOM_STEP) {
                throw new IllegalArgumentException("zoomStep must be between 1 and 10");
            }
            if (loadedHudOpacity < MIN_HUD_OPACITY || loadedHudOpacity > MAX_HUD_OPACITY) {
                throw new IllegalArgumentException("hudOpacity must be between 20 and 100");
            }
            if (loadedHudPosition < 0 || loadedHudPosition >= HUD_POSITIONS.length) {
                throw new IllegalArgumentException("hudPosition is not a valid screen corner");
            }
            if (loadedCrosshairMode < 0 || loadedCrosshairMode >= CROSSHAIR_MODES.length) {
                throw new IllegalArgumentException("crosshairMode is not valid");
            }
            if (loadedHitParticleStyle < 0 || loadedHitParticleStyle >= HIT_PARTICLE_STYLES.length) {
                throw new IllegalArgumentException("hitParticleStyle is not valid");
            }
            if (loadedAutoClickerCps < MIN_AUTOCLICK_CPS || loadedAutoClickerCps > MAX_AUTOCLICK_CPS) {
                throw new IllegalArgumentException("autoClickerCps must be between 3 and 1000");
            }
            if (loadedAutoBridgeClickerCps < MIN_AUTOCLICK_CPS
                    || loadedAutoBridgeClickerCps > MAX_AUTOCLICK_CPS) {
                throw new IllegalArgumentException("autoBridgeClickerCps must be between 3 and 1000");
            }
            zoomFov = loadedFov;
            zoomStep = loadedStep;
            hudOpacity = loadedHudOpacity;
            hudPosition = loadedHudPosition;
            crosshairMode = loadedCrosshairMode;
            hitParticleStyle = loadedHitParticleStyle;
            holdToZoom = loadedHoldToZoom;
            smoothZoom = loadedSmoothZoom;
            coordinatesHud = loadedCoordinatesHud;
            fpsHud = loadedFpsHud;
            directionHud = loadedDirectionHud;
            zoomHud = loadedZoomHud;
            compactHud = loadedCompactHud;
            hitParticles = loadedHitParticles;
            oreFinder = loadedOreFinder;
            autoClicker = loadedAutoClicker;
            autoBridgeClicker = loadedAutoBridgeClicker;
            autoClickerCps = loadedAutoClickerCps;
            autoBridgeClickerCps = loadedAutoBridgeClickerCps;
            autoTotem = loadedAutoTotem;
            autoSprint = loadedAutoSprint;
            extendedReach = loadedExtendedReach;
            autoTool = loadedAutoTool;
            autoEat = loadedAutoEat;
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.error("Could not load SSTwo settings from {}", CONFIG_FILE, exception);
        }
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty("zoomFov", Integer.toString(zoomFov));
        properties.setProperty("zoomStep", Integer.toString(zoomStep));
        properties.setProperty("hudOpacity", Integer.toString(hudOpacity));
        properties.setProperty("hudPosition", Integer.toString(hudPosition));
        properties.setProperty("crosshairMode", Integer.toString(crosshairMode));
        properties.setProperty("hitParticleStyle", Integer.toString(hitParticleStyle));
        properties.setProperty("holdToZoom", Boolean.toString(holdToZoom));
        properties.setProperty("smoothZoom", Boolean.toString(smoothZoom));
        properties.setProperty("coordinatesHud", Boolean.toString(coordinatesHud));
        properties.setProperty("fpsHud", Boolean.toString(fpsHud));
        properties.setProperty("directionHud", Boolean.toString(directionHud));
        properties.setProperty("zoomHud", Boolean.toString(zoomHud));
        properties.setProperty("compactHud", Boolean.toString(compactHud));
        properties.setProperty("hitParticles", Boolean.toString(hitParticles));
        properties.setProperty("oreFinder", Boolean.toString(oreFinder));
        properties.setProperty("autoClicker", Boolean.toString(autoClicker));
        properties.setProperty("autoBridgeClicker", Boolean.toString(autoBridgeClicker));
        properties.setProperty("autoClickerCps", Integer.toString(autoClickerCps));
        properties.setProperty("autoBridgeClickerCps", Integer.toString(autoBridgeClickerCps));
        properties.setProperty("autoTotem", Boolean.toString(autoTotem));
        properties.setProperty("autoSprint", Boolean.toString(autoSprint));
        properties.setProperty("extendedReach", Boolean.toString(extendedReach));
        properties.setProperty("autoTool", Boolean.toString(autoTool));
        properties.setProperty("autoEat", Boolean.toString(autoEat));
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_FILE)) {
                properties.store(output, "SSTwo client settings");
            }
        } catch (IOException exception) {
            LOGGER.error("Could not save SSTwo settings to {}", CONFIG_FILE, exception);
        }
    }
}
