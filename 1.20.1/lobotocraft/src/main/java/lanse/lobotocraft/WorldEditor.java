package lanse.lobotocraft;

import lanse.lobotocraft.lobotocalculator.LobotoCalculator;
import lanse.lobotocraft.terraincalculator.TerrainGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorldEditor {

    public static final Set<Block> VALID_BLOCKS = Set.of(
            Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE,
            Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.PODZOL,
            Blocks.MYCELIUM, Blocks.MUD, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW, Blocks.DRIPSTONE_BLOCK, Blocks.CLAY, Blocks.DIRT_PATH, Blocks.COARSE_DIRT,
            Blocks.NETHERRACK, Blocks.END_STONE, Blocks.OBSIDIAN, Blocks.SANDSTONE, Blocks.BUBBLE_COLUMN,
            Blocks.DEEPSLATE
    );

    public static final Set<Block> EXCLUDED_BLOCKS = Set.of(
            Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.NETHER_PORTAL, Blocks.OBSIDIAN
    );

    public static void adjustColumn(ServerWorld world, int x, int z, String dimension) {

        // Find the highest valid block in the column
        int highestY = -2500;
        int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

        for (int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z); y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);

            if (isValidBlock(state.getBlock())) {
                highestY = y;
                break;
            }

            // Check for water or lava above Y level 50, and clear it if it is liquid.
            if (state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA)) {
                if (y > 50 || y >= surface + 1) {
                    clearNearbyFluids(world, pos, 25);
                }
            }
        }
        // Check if the chunk is too close to any player before modifying blocks, and find the closest player position.
        Vec3d closestPlayerPos = null;
        for (Vec3d playerPos : Lobotocraft.playerPositions) {
            if (Math.abs(playerPos.getX() - x) <= 16 && Math.abs(playerPos.getZ() - z) <= 16) return;

            if (closestPlayerPos == null || playerPos.squaredDistanceTo(x + 0.5, playerPos.y, z + 0.5) < closestPlayerPos.squaredDistanceTo(x + 0.5, closestPlayerPos.y, z + 0.5)) {
                closestPlayerPos = playerPos;
            }
        }

        if (Lobotocraft.currentMode == Lobotocraft.Mode.LOBOTOMY){
            if (!(closestPlayerPos == null)) LobotoCalculator.lobotomize(world, x, z, highestY, closestPlayerPos);
        }

        if (Lobotocraft.currentMode == Lobotocraft.Mode.DEMENTIA){
            int targetY = TerrainGenerator.getHeight(world, x, z, highestY, dimension);

            if (targetY == -40404) return; //TODO - replace this with an error in chat

            moveColumn(world, x, z, highestY, targetY);
        }
    }

    public static void moveColumn(ServerWorld world, int x, int z, int currentY, int targetY) {
        int heightDifference = targetY - currentY;

        if (heightDifference == 0) return;  // No change needed

        // Limit the range to surface-level adjustments (10 blocks below and 20 above currentY)
        int startY = Math.max(currentY - 10, -64);
        int endY = Math.min(currentY + 20, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z));

        List<ColumnStateChange> changes = new ArrayList<>();

        if (heightDifference > 0) {
            // Move blocks up
            for (int y = endY; y >= startY; y--) {
                processMoveColumn(world, x, y, z, heightDifference, changes);
            }
        } else {
            // Move blocks down
            for (int y = startY; y <= endY; y++) {
                processMoveColumn(world, x, y, z, heightDifference, changes);
            }
        }

        // Apply all changes in bulk for efficiency
        for (ColumnStateChange change : changes) {
            world.setBlockState(change.newPos, change.state, 18);
            world.setBlockState(change.oldPos, Blocks.AIR.getDefaultState(), 18);
        }
    }

    private static void processMoveColumn(ServerWorld world, int x, int y, int z, int heightDifference, List<ColumnStateChange> changes) {
        BlockPos oldPos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(oldPos);

        if (state.isAir() || EXCLUDED_BLOCKS.contains(state.getBlock())) return;

        BlockPos newPos = new BlockPos(x, y + heightDifference, z);
        BlockState newState = world.getBlockState(newPos);

        // Ensure new position is within world bounds and not in the excluded blocks list
        if (newPos.getY() <= 319 && newPos.getY() >= -64 && !EXCLUDED_BLOCKS.contains(newState.getBlock())) {
            changes.add(new ColumnStateChange(oldPos, newPos, state));
        }
    }
    private record ColumnStateChange(BlockPos oldPos, BlockPos newPos, BlockState state) {}

    //safely move block with NBT because nbt blocks are cringe
    public static void moveBlockWithNbt(ServerWorld world, BlockPos oldPos, BlockPos newPos) {
        BlockState state = world.getBlockState(oldPos);

        // Copy tile entity data if applicable
        BlockEntity blockEntity = world.getBlockEntity(oldPos);
        NbtCompound nbt = null;
        if (blockEntity != null) {
            nbt = blockEntity.createNbtWithId();
            world.removeBlockEntity(oldPos);
        }

        // Set the block state at the new position
        world.setBlockState(newPos, state, 18);

        // Restore NBT data if needed
        if (nbt != null) {
            BlockEntity newBlockEntity = world.getBlockEntity(newPos);
            if (newBlockEntity != null) {
                newBlockEntity.readNbt(nbt);
            }
        }
    }

    private static boolean isValidBlock(Block block) {
        return VALID_BLOCKS.contains(block) || block instanceof IceBlock;
    }

    public static void clearNearbyFluids(ServerWorld world, BlockPos origin, int radius) {

        List<BlockPos> fluidPositions = new ArrayList<>();

        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int y = origin.getY() - radius; y <= origin.getY() + radius; y++) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Check for water, lava, or any waterlogged blocks
                    if (state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA) || state.isOf(Blocks.KELP)
                            || state.isOf(Blocks.KELP_PLANT) || state.isOf(Blocks.SEAGRASS)
                            || state.isOf(Blocks.SEA_PICKLE) || state.isOf(Blocks.TALL_SEAGRASS)
                            || (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))) {

                        fluidPositions.add(pos);
                    }
                }
            }
        }
        // Batch replace all fluid blocks with air
        for (BlockPos pos : fluidPositions) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
        }
    }
}