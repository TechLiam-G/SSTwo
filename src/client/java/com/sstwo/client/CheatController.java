package com.sstwo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class CheatController {
    private static final int MAX_CATCH_UP_CLICKS_PER_TICK = 100;
    private static final int ORE_SCAN_RADIUS = 8;
    private static final int MAX_ORE_RESULTS = 4;
    private static final List<String> oreLines = new ArrayList<>();

    private static long nextClickTime;
    private static long nextUseClickTime;
    private static long nextTotemAttemptTick;
    private static int oreScanTicks;
    private static BlockPos autoMiningPosition;
    private static int originalHotbarSlot = -1;
    private static int autoEatOriginalSlot = -1;
    private static LocalPlayer autoEatPlayer;
    private static LocalPlayer reachPlayer;
    private static double originalBlockReach;
    private static double originalEntityReach;
    private static boolean reachApplied;

    private CheatController() {
    }

    static void tick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            nextClickTime = 0;
            nextUseClickTime = 0;
            nextTotemAttemptTick = 0;
            autoMiningPosition = null;
            restoreReach();
            originalHotbarSlot = -1;
            restoreAutoEat();
            oreLines.clear();
            return;
        }

        if (client.gui.screen() != null) {
            nextClickTime = 0;
            nextUseClickTime = 0;
            restoreAutoTool(client);
            restoreReach();
            if (autoEatPlayer != null && !autoEatPlayer.isUsingItem()) {
                restoreAutoEat();
            }
            if (autoMiningPosition != null) {
                client.gameMode.stopDestroyBlock();
                autoMiningPosition = null;
            }
            return;
        }

        if (ZoomSettings.isExtendedReachEnabled()) {
            applyReach(client.player);
        } else {
            restoreReach();
        }
        if (ZoomSettings.isAutoToolEnabled()) {
            autoTool(client);
        } else {
            restoreAutoTool(client);
        }
        if (ZoomSettings.isAutoEatEnabled()) {
            autoEat(client);
        } else {
            restoreAutoEat();
        }
        if (ZoomSettings.isAutoTotemEnabled()) {
            equipTotem(client);
        }
        if (ZoomSettings.isAutoClickerEnabled()) {
            autoClick(client);
        } else {
            nextClickTime = 0;
            autoMiningPosition = null;
        }
        if (ZoomSettings.isAutoBridgeClickerEnabled()) {
            autoBridgeClick(client);
        } else {
            nextUseClickTime = 0;
        }
        if (ZoomSettings.isAutoSprintEnabled()
                && client.options.keyUp.isDown()
                && !client.options.keyShift.isDown()
                && !client.player.isInWater()) {
            client.player.setSprinting(true);
        }

        if (ZoomSettings.isOreFinderEnabled() && ++oreScanTicks >= 20) {
            oreScanTicks = 0;
            scanForOres(client);
        } else if (!ZoomSettings.isOreFinderEnabled()) {
            oreScanTicks = 0;
            oreLines.clear();
        }
    }

    private static void applyReach(LocalPlayer player) {
        if (reachPlayer != player) {
            restoreReach();
            reachPlayer = player;
            AttributeInstance blockReach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
            AttributeInstance entityReach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (blockReach == null || entityReach == null) {
                reachPlayer = null;
                return;
            }
            originalBlockReach = blockReach.getBaseValue();
            originalEntityReach = entityReach.getBaseValue();
            reachApplied = true;
        }

        if (!reachApplied) {
            return;
        }
        player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(6.0);
        player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(6.0);
    }

    private static void restoreReach() {
        if (reachApplied && reachPlayer != null) {
            AttributeInstance blockReach = reachPlayer.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
            AttributeInstance entityReach = reachPlayer.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (blockReach != null) {
                blockReach.setBaseValue(originalBlockReach);
            }
            if (entityReach != null) {
                entityReach.setBaseValue(originalEntityReach);
            }
        }
        reachPlayer = null;
        reachApplied = false;
    }

    private static void autoTool(Minecraft client) {
        if (!client.options.keyAttack.isDown() || !(client.hitResult instanceof BlockHitResult blockHit)) {
            restoreAutoTool(client);
            return;
        }

        Inventory inventory = client.player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        if (originalHotbarSlot == -1) {
            originalHotbarSlot = selectedSlot;
        }

        var state = client.level.getBlockState(blockHit.getBlockPos());
        int bestSlot = selectedSlot;
        float bestSpeed = inventory.getItem(selectedSlot).getDestroySpeed(state);
        for (int slot = 0; slot < 9; slot++) {
            float speed = inventory.getItem(slot).getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSlot = slot;
                bestSpeed = speed;
            }
        }
        inventory.setSelectedSlot(bestSlot);
    }

    private static void restoreAutoTool(Minecraft client) {
        if (originalHotbarSlot != -1 && client.player != null) {
            client.player.getInventory().setSelectedSlot(originalHotbarSlot);
        }
        originalHotbarSlot = -1;
    }

    private static void autoEat(Minecraft client) {
        var player = client.player;
        if (autoEatOriginalSlot != -1) {
            if (player.isUsingItem()) {
                return;
            }
            restoreAutoEat();
            return;
        }

        if (!player.getFoodData().needsFood()
                || player.getFoodData().getFoodLevel() > 14
                || player.isUsingItem()
                || client.options.keyAttack.isDown()
                || client.options.keyUse.isDown()) {
            return;
        }

        Inventory inventory = player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        int foodSlot = -1;
        int bestNutrition = 0;
        for (int slot = 0; slot < 9; slot++) {
            var food = inventory.getItem(slot).get(DataComponents.FOOD);
            if (food != null && food.nutrition() > bestNutrition) {
                foodSlot = slot;
                bestNutrition = food.nutrition();
            }
        }
        if (foodSlot == -1) {
            return;
        }

        inventory.setSelectedSlot(foodSlot);
        autoEatOriginalSlot = selectedSlot;
        autoEatPlayer = player;
        client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        if (!player.isUsingItem()) {
            restoreAutoEat();
        }
    }

    private static void restoreAutoEat() {
        if (autoEatOriginalSlot != -1 && autoEatPlayer != null) {
            autoEatPlayer.getInventory().setSelectedSlot(autoEatOriginalSlot);
        }
        autoEatOriginalSlot = -1;
        autoEatPlayer = null;
    }

    static List<String> getOreLines() {
        return List.copyOf(oreLines);
    }

    private static void autoClick(Minecraft client) {
        if (!client.options.keyAttack.isDown()) {
            if (autoMiningPosition != null) {
                client.gameMode.stopDestroyBlock();
                autoMiningPosition = null;
            }
            nextClickTime = 0;
            return;
        }

        if (client.hitResult instanceof BlockHitResult blockHit) {
            BlockPos target = blockHit.getBlockPos();
            if (!target.equals(autoMiningPosition)) {
                if (autoMiningPosition != null) {
                    client.gameMode.stopDestroyBlock();
                }
                client.gameMode.startDestroyBlock(target, blockHit.getDirection());
                autoMiningPosition = target.immutable();
            } else {
                client.gameMode.continueDestroyBlock(target, blockHit.getDirection());
            }
            return;
        }

        if (autoMiningPosition != null) {
            client.gameMode.stopDestroyBlock();
            autoMiningPosition = null;
        }
        if (client.crosshairPickEntity == null) {
            return;
        }

        long now = System.nanoTime();
        long interval = clickIntervalNanos(ZoomSettings.getAutoClickerCps());
        if (nextClickTime == 0) {
            nextClickTime = now;
        }
        int clicks = 0;
        while (now >= nextClickTime && clicks++ < MAX_CATCH_UP_CLICKS_PER_TICK) {
            nextClickTime += interval;
            client.gameMode.attack(client.player, client.crosshairPickEntity);
        }
        if (clicks > MAX_CATCH_UP_CLICKS_PER_TICK) {
            nextClickTime = now + interval;
        }
    }

    private static void autoBridgeClick(Minecraft client) {
        if (!client.options.keyUse.isDown() || !(client.hitResult instanceof BlockHitResult blockHit)) {
            nextUseClickTime = 0;
            return;
        }

        long now = System.nanoTime();
        long interval = clickIntervalNanos(ZoomSettings.getAutoBridgeClickerCps());
        if (nextUseClickTime == 0) {
            nextUseClickTime = now;
        }
        int clicks = 0;
        while (now >= nextUseClickTime && clicks++ < MAX_CATCH_UP_CLICKS_PER_TICK) {
            nextUseClickTime += interval;
            client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, blockHit);
        }
        if (clicks > MAX_CATCH_UP_CLICKS_PER_TICK) {
            nextUseClickTime = now + interval;
        }
    }

    private static long clickIntervalNanos(int cps) {
        return 1_000_000_000L / cps;
    }

    private static void equipTotem(Minecraft client) {
        var player = client.player;
        long gameTime = client.level.getGameTime();
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || gameTime < nextTotemAttemptTick) {
            return;
        }

        Inventory inventory = player.getInventory();
        for (int inventorySlot = 0; inventorySlot < Inventory.INVENTORY_SIZE; inventorySlot++) {
            if (!inventory.getItem(inventorySlot).is(Items.TOTEM_OF_UNDYING)) {
                continue;
            }

            int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
            nextTotemAttemptTick = gameTime + 20;
            client.gameMode.handleContainerInput(
                    player.inventoryMenu.containerId,
                    menuSlot,
                    Inventory.SLOT_OFFHAND,
                    ContainerInput.SWAP,
                    player
            );
            return;
        }
    }

    private static void scanForOres(Minecraft client) {
        oreLines.clear();
        BlockPos center = client.player.blockPosition();
        List<OreLocation> ores = new ArrayList<>();
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

        for (int x = -ORE_SCAN_RADIUS; x <= ORE_SCAN_RADIUS; x++) {
            for (int y = -ORE_SCAN_RADIUS; y <= ORE_SCAN_RADIUS; y++) {
                for (int z = -ORE_SCAN_RADIUS; z <= ORE_SCAN_RADIUS; z++) {
                    position.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!client.level.hasChunkAt(position)) {
                        continue;
                    }

                    String blockName = BuiltInRegistries.BLOCK
                            .getKey(client.level.getBlockState(position).getBlock())
                            .getPath();
                    if (blockName.endsWith("_ore") || blockName.equals("ancient_debris")) {
                        BlockPos orePosition = position.immutable();
                        ores.add(new OreLocation(
                                blockName.replace('_', ' '),
                                orePosition,
                                center.distSqr(orePosition)
                        ));
                    }
                }
            }
        }

        ores.stream()
                .sorted(Comparator.comparingDouble(OreLocation::distanceSquared))
                .limit(MAX_ORE_RESULTS)
                .forEach(ore -> oreLines.add(String.format(
                        "Xray: %s  %d %d %d",
                        ore.name(),
                        ore.position().getX(),
                        ore.position().getY(),
                        ore.position().getZ()
                )));

        if (oreLines.isEmpty()) {
            oreLines.add("Xray: No nearby ores in loaded chunks");
        }
    }

    private record OreLocation(String name, BlockPos position, double distanceSquared) {
    }
}
