package com.sstwo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class CheatController {
    private static final long AUTOCLICK_INTERVAL_NANOS = 125_000_000L;
    private static final int ORE_SCAN_RADIUS = 8;
    private static final int MAX_ORE_RESULTS = 4;
    private static final List<String> oreLines = new ArrayList<>();

    private static long nextClickTime;
    private static long nextTotemAttemptTick;
    private static int oreScanTicks;
    private static BlockPos autoMiningPosition;

    private CheatController() {
    }

    static void tick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            nextClickTime = 0;
            nextTotemAttemptTick = 0;
            autoMiningPosition = null;
            oreLines.clear();
            return;
        }

        if (client.gui.screen() != null) {
            nextClickTime = 0;
            if (autoMiningPosition != null) {
                client.gameMode.stopDestroyBlock();
                autoMiningPosition = null;
            }
            return;
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
        if (now >= nextClickTime) {
            nextClickTime = now + AUTOCLICK_INTERVAL_NANOS;
            client.gameMode.attack(client.player, client.crosshairPickEntity);
        }
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
