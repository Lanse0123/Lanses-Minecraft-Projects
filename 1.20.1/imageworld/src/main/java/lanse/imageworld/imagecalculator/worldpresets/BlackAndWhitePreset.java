package lanse.imageworld.imagecalculator.worldpresets;

import lanse.imageworld.WorldEditor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class BlackAndWhitePreset {

    public static void overworldLogic(ServerWorld world, int x, int z, Color pixelColor) {

        int seaLevel = world.getSeaLevel() - 1;

        if (pixelColor == Color.WHITE) {
            // White pixel: water from 0 to sea level, air above only if block is not valid
            for (int y = 0; y <= seaLevel; y++) {
                world.setBlockState(new BlockPos(x, y, z), Blocks.WATER.getDefaultState(), 3);
            }
            for (int y = seaLevel + 1; y < world.getTopY(); y++) {
                BlockPos pos = new BlockPos(x, y, z);
                Block block = world.getBlockState(pos).getBlock();
                if (!WorldEditor.validOverworldBlocks.contains(block)) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
            }
        } else {
            // Always remove any water from Y=seaLevel + 50 down to Y=0
            for (int y = seaLevel + 50; y >= 0; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
            }

            // Make sure terrain exists at sea level
            BlockPos.Mutable pos = new BlockPos.Mutable(x, world.getTopY() - 1, z);
            while (pos.getY() > 0 && world.getBlockState(pos).isAir()) {
                pos.move(0, -1, 0);
            }

            BlockState topBlock = world.getBlockState(pos);
            int y = pos.getY();
            boolean isValid = WorldEditor.validOverworldBlocks.contains(topBlock.getBlock());

            if (!isValid && y <= seaLevel) {
                WorldEditor.clearNearbyFluids(world, new BlockPos(x, seaLevel, z), 1);
                world.setBlockState(new BlockPos(x, seaLevel, z), Blocks.GRASS_BLOCK.getDefaultState(), 3);
            }
        }
    }

    public static void netherLogic(ServerWorld world, int x, int z, Color pixelColor) {
        int lavaLevel = 31;

        if (pixelColor == Color.WHITE) {
            // Place lava from Y=10 to lavaLevel
            for (int y = 10; y <= lavaLevel; y++) {
                world.setBlockState(new BlockPos(x, y, z), Blocks.LAVA.getDefaultState(), 3);
            }
            // Remove unwanted blocks above lavaLevel
            for (int y = lavaLevel + 1; y < world.getTopY(); y++) {
                BlockPos pos = new BlockPos(x, y, z);
                Block block = world.getBlockState(pos).getBlock();
                if (!WorldEditor.excludedNetherBlocks.contains(block)) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
            }
        } else {
            for (int y = 128; y >= 10; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).getBlock() == Blocks.LAVA) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
            }

            // Find topmost non-air block
            BlockPos.Mutable pos = new BlockPos.Mutable(x, world.getTopY() - 1, z);
            while (pos.getY() > 0 && world.getBlockState(pos).isAir()) {
                pos.move(0, -1, 0);
            }

            BlockState topBlock = world.getBlockState(pos);
            int y = pos.getY();
            boolean isExcluded = WorldEditor.excludedNetherBlocks.contains(topBlock.getBlock());

            if (!isExcluded && (y <= lavaLevel || topBlock.isOf(Blocks.LAVA))) {
                WorldEditor.clearNearbyFluids(world, new BlockPos(x, lavaLevel, z), 1);
                world.setBlockState(new BlockPos(x, lavaLevel, z), Blocks.NETHERRACK.getDefaultState(), 3);
            }
        }
    }

    public static void endLogic(ServerWorld world, int x, int z, Color pixelColor) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, 0, z);

        //TODO - if I find out what to do with FULL_COLOR, Move it out of this if statement.
        if (WorldEditor.colorPalette.equals(WorldEditor.ColorPalette.BLACK_AND_WHITE) || WorldEditor.colorPalette.equals(WorldEditor.ColorPalette.FULL_COLOR)) {

            //TODO - organize this to be like the nether, but keep the logic the same as it is rn
            if (!(pixelColor == Color.WHITE)) {
                // Scan the column from top down to check for any End Stone
                boolean hasEndStone = false;
                pos.setY(world.getTopY() - 1);
                while (pos.getY() >= 0) {
                    if (world.getBlockState(pos).isOf(Blocks.END_STONE)) {
                        hasEndStone = true;
                        break;
                    }
                    pos.move(0, -1, 0);
                }

                // If no End Stone found, place one at Y = 56
                if (!hasEndStone) {
                    pos.setY(56);
                    world.setBlockState(pos, Blocks.END_STONE.getDefaultState(), 3);
                }

            } else {
                // Remove all End Stone from top to bottom
                pos.setY(world.getTopY() - 1);
                while (pos.getY() >= 0) {
                    if (world.getBlockState(pos).isOf(Blocks.END_STONE)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    }
                    pos.move(0, -1, 0);
                }
            }
        }
    }

}
