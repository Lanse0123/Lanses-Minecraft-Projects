package lanse.lobotocraft.lobotocalculator;

import lanse.lobotocraft.ColorMatchers.ColorPicker;
import lanse.lobotocraft.WorldEditor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class LobotoCalculator {

    public static final Set<Block> EXCLUDED_BLOCKS = Set.of(
            Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.NETHER_PORTAL
    );

    private static final Random random = new Random();

    public static void moveColumn(ServerWorld world, int x, int z, int currentY, int targetY) {

        int heightDifference = targetY - currentY;

        if (heightDifference > 0) {
            // Move the column up
            for (int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z); y >= -64; y--) {
                BlockPos oldPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(oldPos);

                // Trigger explosion for End Portal Frames
                if (state.isOf(Blocks.END_PORTAL_FRAME)) {
                    world.createExplosion(null, x, y + 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    world.createExplosion(null, x, y - 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    continue;
                }

                if (EXCLUDED_BLOCKS.contains(state.getBlock())) continue;

                BlockPos newPos = new BlockPos(x, y + heightDifference, z);

                // Move block state and preserve tile entity data
                if (!EXCLUDED_BLOCKS.contains(world.getBlockState(newPos).getBlock())) {
                    WorldEditor.moveBlockWithNbt(world, oldPos, newPos);
                }

                // Clear the old block
                if (!EXCLUDED_BLOCKS.contains(world.getBlockState(oldPos).getBlock())) {
                    world.setBlockState(oldPos, Blocks.AIR.getDefaultState(), 18);
                }
            }
        } else if (heightDifference < 0) {
            // Move the column down
            for (int y = -64; y <= world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) + 1; y++) {
                BlockPos oldPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(oldPos);

                // Trigger explosion for End Portal Frames
                if (state.isOf(Blocks.END_PORTAL_FRAME)) {
                    world.createExplosion(null, x, y + 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    world.createExplosion(null, x, y - 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    continue;
                }

                if (EXCLUDED_BLOCKS.contains(state.getBlock())) continue;

                BlockPos newPos = new BlockPos(x, y + heightDifference, z);

                // Ensure new position is within world bounds
                if (newPos.getY() <= 319 && newPos.getY() >= -64) {
                    if (!EXCLUDED_BLOCKS.contains(world.getBlockState(newPos).getBlock())) {
                        WorldEditor.moveBlockWithNbt(world, oldPos, newPos);
                    }
                }

                // Clear the old block
                if (!EXCLUDED_BLOCKS.contains(world.getBlockState(oldPos).getBlock())) {
                    world.setBlockState(oldPos, Blocks.AIR.getDefaultState(), 18);
                }
            }
        }
    }

    public static void lobotomize(ServerWorld world, int x, int z, int highestY, Vec3d playerPos){
        replaceBlocksAbove(world, x, z, highestY);
        replaceBlocksBelow(world, x, z, highestY);

        if (playerPos.getY() > highestY) return;

        // If the player is underground, extend to top and bottom limits
        int minY = (int) Math.max(playerPos.getY(), world.getBottomY());
        int maxY = (int) Math.min(playerPos.getY(), world.getTopY());
        replaceBlocksInRangeWithLimit(world, x, z, minY, maxY);
    }

    private static void replaceBlocksInRangeWithLimit(ServerWorld world, int x, int z, int minY, int maxY) {
        int replacedAbove = 0;
        int replacedBelow = 0;

        // Replace blocks above the player
        for (int y = Math.min(maxY, world.getTopY()); y >= minY && replacedAbove < 5; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block != Blocks.AIR && !EXCLUDED_BLOCKS.contains(block)) {
                Block replacementBlock = getRandomReplacementBlock();
                // Flag 18 disables block updates
                world.setBlockState(pos, replacementBlock.getDefaultState(), 18);
                replacedAbove++;
            }
        }

        // Replace blocks below the player
        for (int y = minY; y >= world.getBottomY() && replacedBelow < 5; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block != Blocks.AIR && !EXCLUDED_BLOCKS.contains(block)) {
                Block replacementBlock = getRandomReplacementBlock();
                // Flag 18 disables block updates
                world.setBlockState(pos, replacementBlock.getDefaultState(), 18);
                replacedBelow++;
            }
        }
    }

    private static void replaceBlocksAbove(ServerWorld world, int x, int z, int highestY) {
        for (int y = highestY + 1; y <= world.getTopY(); y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block != Blocks.AIR && !EXCLUDED_BLOCKS.contains(block)) {
                Block replacementBlock = getRandomReplacementBlock();
                // flag 18 disables block updates
                world.setBlockState(pos, replacementBlock.getDefaultState(), 18);
            }
        }
    }

    private static void replaceBlocksBelow(ServerWorld world, int x, int z, int highestY) {
        int replaced = 0;

        for (int y = highestY; y >= -64 && replaced < 5; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block != Blocks.AIR && !EXCLUDED_BLOCKS.contains(block)) {
                Block replacementBlock = getRandomReplacementBlock();
                // flag 18 disables block updates
                world.setBlockState(pos, replacementBlock.getDefaultState(), 18);
                replaced++;
            }
        }
    }

    private static Block getRandomReplacementBlock() {
        boolean useVisibleBlocks = random.nextInt(100) < 97;
        Set<Block> sourceBlocks = useVisibleBlocks ? ColorPicker.visibleBlocks : ColorPicker.closeBlocks;

        // Filter the source blocks to include only 1-block-tall blocks
        Set<Block> filteredBlocks = sourceBlocks.stream().filter(block -> {
                    try {
                        // Only consider blocks that have a valid bounding box and a YLength of 1 or less
                        return block.getDefaultState().getOutlineShape(null, null).getBoundingBox().getYLength() <= 1.0;
                    } catch (Exception e) {
                        // Handle blocks that don't have a valid bounding box or outline shape
                        return false;
                    }
                })
                .filter(block -> !EXCLUDED_BLOCKS.contains(block)).collect(Collectors.toSet());

        if (!filteredBlocks.isEmpty()) {
            // Pick a random block from the filtered set
            int index = random.nextInt(filteredBlocks.size());
            return filteredBlocks.stream().skip(index).findFirst().orElse(Blocks.STONE);
        }
        return Blocks.STONE; // Default to stone if no valid blocks are found
    }
}