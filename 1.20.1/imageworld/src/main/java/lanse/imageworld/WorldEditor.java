package lanse.imageworld;

import lanse.imageworld.imagecalculator.worldpresets.AnarchyPreset;
import lanse.imageworld.imagecalculator.worldpresets.BlackAndWhitePreset;
import lanse.imageworld.imagecalculator.worldpresets.FullColorMesaPreset;
import lanse.imageworld.imagecalculator.worldpresets.FullColorPreset;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.Optional;

public class WorldEditor {

    //List of valid surface blocks making up the ground. This does not include stuff like
    //structures, since those will be placed on top of this ground level.
    public static final Set<Block> validOverworldBlocks = Set.of(
            Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.ROOTED_DIRT,
            Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS, Blocks.MYCELIUM, Blocks.DIRT_PATH,
            Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.TUFF, Blocks.CALCITE,
            Blocks.DRIPSTONE_BLOCK, Blocks.DEEPSLATE, Blocks.BASALT, Blocks.SANDSTONE, Blocks.RED_SANDSTONE,
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.CLAY,
            Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW, Blocks.MOSS_BLOCK
    );

    public static final Set<Block> terrocataBlocks = Set.of(
            Blocks.TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
            Blocks.BLUE_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.WHITE_TERRACOTTA,
            Blocks.BLACK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.BROWN_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA, Blocks.PINK_TERRACOTTA
    );

    public static final Set<Block> excludedNetherBlocks = Set.of(
            Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_WALL,
            Blocks.NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_SLAB,
            Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.BLACKSTONE_SLAB,
            Blocks.BLACKSTONE_STAIRS, Blocks.BLACKSTONE_WALL, Blocks.GILDED_BLACKSTONE, Blocks.CHAIN, Blocks.LANTERN,
            Blocks.CHISELED_NETHER_BRICKS, Blocks.LODESTONE, Blocks.CRYING_OBSIDIAN, Blocks.OBSIDIAN
    );

    public static final Set<Block> excludedBlocks = Set.of(
            Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.NETHER_PORTAL, Blocks.OBSIDIAN
    );

    public enum StructureFeatureType {
        OAK_TREE, BIRCH_TREE, CHERRY_TREE, MANGROVE_TREE, SNOWY_SPRUCE_TREE, DESERT_WELL, ERODED_MESA, RED_MUSHROOM, BROWN_MUSHROOM
    }

    public enum ColorPalette {
        FULL_COLOR, BLACK_AND_WHITE, FULL_COLOR_MESA, ANARCHY
    }
    public static ColorPalette colorPalette = ColorPalette.BLACK_AND_WHITE;

    public static boolean clampHeight = true;

    public static void adjustColumn(ServerWorld world, int x, int z, String dimensionType, Color pixelColor, int pass) {
        if (dimensionType.equals("OVERWORLD"))overworldLogic(world, x, z, pixelColor, pass);
        if (dimensionType.equals("NETHER"))netherLogic(world, x, z, pixelColor, pass);
        if (dimensionType.equals("END"))endLogic(world, x, z, pixelColor /*, pass*/);
    }

    public static void overworldLogic(ServerWorld world, int x, int z, Color pixelColor, int pass) {

        switch (colorPalette){
            case BLACK_AND_WHITE -> BlackAndWhitePreset.overworldLogic(world, x, z, pixelColor);
            case FULL_COLOR -> FullColorPreset.overworldLogic(world, x, z, pixelColor, pass);
            case FULL_COLOR_MESA -> FullColorMesaPreset.overworldLogic(world, x, z, pixelColor, pass);
            case ANARCHY -> AnarchyPreset.overworldLogic(world, x, z, pixelColor, pass);
        }
    }

    public static void netherLogic(ServerWorld world, int x, int z, Color pixelColor, int pass) {

        switch (colorPalette){
            case BLACK_AND_WHITE -> BlackAndWhitePreset.netherLogic(world, x, z, pixelColor);
            case FULL_COLOR -> FullColorPreset.netherLogic(world, x, z, pixelColor, pass);
            case FULL_COLOR_MESA -> FullColorMesaPreset.netherLogic(world, x, z, pixelColor, pass);
            case ANARCHY -> AnarchyPreset.netherLogic(world, x, z, pixelColor, pass);
        }
    }

    public static void endLogic(ServerWorld world, int x, int z, Color pixelColor /*, int pass**/) {

        // Black and white logic
        //TODO - if I find out what to do with FULL_COLOR, Move it out of this if statement.
        if (colorPalette.equals(ColorPalette.BLACK_AND_WHITE) || colorPalette.equals(ColorPalette.FULL_COLOR)
        || colorPalette.equals(ColorPalette.FULL_COLOR_MESA)) {
            BlackAndWhitePreset.endLogic(world, x, z, pixelColor);

            //full color logic
        } //else if (colorPalette.equals(ColorPalette.FULL_COLOR)) {
//            if (pass == 0){
//                clearSurface(world, x, z);
//            } else if (pass == 1){
//                //TODO - figure out what to do with the end. I might just keep it black and white for the end.
//            }
//        }
    }

    //Clear all water and lava blocks within int radius around the given position
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

    public static void clearSurface(ServerWorld world, int x, int z) {
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        BlockPos.Mutable pos = new BlockPos.Mutable(x, world.getTopY() - 1, z); // Start one below top Y
        int surfaceY = -40404;

        // find the valid surface
        for (int y = world.getTopY() - 1; y >= world.getBottomY(); y--) {
            pos.setY(y);
            Block block = world.getBlockState(pos).getBlock();

            if (dimensionKey.equals(World.OVERWORLD)) {
                if (validOverworldBlocks.contains(block)) {
                    surfaceY = y;
                    break;
                }
            } else if (dimensionKey.equals(World.NETHER)) {
                if (!excludedNetherBlocks.contains(block) && block != Blocks.AIR) {
                    surfaceY = y;
                    break;
                }
            }
        }
        // clear everything above valid surface
        if (surfaceY != -40404) {
            for (int y = world.getTopY() - 1; y > surfaceY; y--) {
                pos.setY(y);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    public static void replaceSurface(ServerWorld world, int x, int z, Block block, int depth, String dimension) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, world.getTopY() - 1, z);
        int topY = world.getTopY() - 1;

        BlockPos surfacePos = null;
        for (int y = topY; y >= world.getBottomY(); y--) {
            pos.setY(y);
            Block blockAtPos = world.getBlockState(pos).getBlock();

            if (dimension.equals("OVERWORLD") && (validOverworldBlocks.contains(blockAtPos) || terrocataBlocks.contains(blockAtPos))) {
                surfacePos = pos.toImmutable(); // immutable locks mutable
                break;
            } else if (dimension.equals("NETHER") && !excludedNetherBlocks.contains(blockAtPos)) {
                surfacePos = pos.toImmutable();
                break;
            } else if (dimension.equals("END")) {
                // do later
                break;
            }
        }

        if (surfacePos != null) {
            int replaced = 0;
            for (int y = surfacePos.getY(); y >= world.getBottomY() && replaced < depth; y--) {
                BlockPos target = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(target);
                if (!state.isAir()) {
                    world.setBlockState(target, block.getDefaultState());
                    replaced++;
                }
            }
        }
    }

    public static void moveColumn(ServerWorld world, int x, int z) {
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        BlockPos.Mutable pos = new BlockPos.Mutable(x, world.getTopY() - 1, z); // Start at the top
        int surfaceY = -40404;

        // get valid surface
        for (int y = world.getTopY() - 1; y >= world.getBottomY(); y--) {
            pos.setY(y);
            Block block = world.getBlockState(pos).getBlock();

            boolean isValid = false;

            if (dimensionKey.equals(World.OVERWORLD)) {
                isValid = validOverworldBlocks.contains(block);
            } else if (dimensionKey.equals(World.NETHER)) {
                isValid = !excludedNetherBlocks.contains(block) && block != Blocks.AIR;
            } else if (dimensionKey.equals(World.END)) {
                isValid = block != Blocks.AIR;
            }

            if (isValid) {
                surfaceY = y;
                break;
            } else {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
            }
        }

        // Skip if no valid surface found
        if (surfaceY <= -64 || surfaceY >= 320) return;

        int targetY = getTargetHeightFromInput(surfaceY);
        int heightDifference = targetY - surfaceY;

        if (heightDifference == 0 || (surfaceY > 64 && surfaceY < 86)) return;

        if (heightDifference > 0) {

            // Move blocks up
            for (int y = surfaceY; y >= 0; y--) {
                BlockPos oldPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(oldPos);

                if (state.isOf(Blocks.END_PORTAL_FRAME)) {
                    world.createExplosion(null, x, y + 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    world.createExplosion(null, x, y - 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    continue;
                }

                if (excludedBlocks.contains(state.getBlock())) continue;

                BlockPos newPos = new BlockPos(x, y + heightDifference, z);
                if (newPos.getY() <= 319 && newPos.getY() >= -64) {
                    if (!excludedBlocks.contains(world.getBlockState(newPos).getBlock())) {
                        moveBlockWithNbt(world, oldPos, newPos);
                    }
                }

                if (!excludedBlocks.contains(world.getBlockState(oldPos).getBlock())) {
                    world.setBlockState(oldPos, Blocks.AIR.getDefaultState(), 18);
                }
            }
        } else {
            // Move blocks down
            for (int y = -64; y <= surfaceY + 1; y++) {
                BlockPos oldPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(oldPos);

                if (state.isOf(Blocks.END_PORTAL_FRAME)) {
                    world.createExplosion(null, x, y + 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    world.createExplosion(null, x, y - 5, z, 35.0F, World.ExplosionSourceType.MOB);
                    continue;
                }

                if (excludedBlocks.contains(state.getBlock())) continue;

                BlockPos newPos = new BlockPos(x, y + heightDifference, z);
                if (newPos.getY() <= 319 && newPos.getY() >= -64) {
                    if (!excludedBlocks.contains(world.getBlockState(newPos).getBlock())) {
                        moveBlockWithNbt(world, oldPos, newPos);
                    }
                }

                if (!excludedBlocks.contains(world.getBlockState(oldPos).getBlock())) {
                    world.setBlockState(oldPos, Blocks.AIR.getDefaultState(), 18);
                }
            }
        }
    }

    // Safely move block with NBT because NBT blocks are cringe
    private static void moveBlockWithNbt(ServerWorld world, BlockPos oldPos, BlockPos newPos) {
        BlockState state = world.getBlockState(oldPos);

        // Copy tile entity data if applicable
        BlockEntity blockEntity = world.getBlockEntity(oldPos);
        NbtCompound nbt = null;

        if (blockEntity != null) {
            // No arguments needed in 1.20.1
            nbt = blockEntity.createNbtWithId();
            world.removeBlockEntity(oldPos);
        }

        // Set the block state at the new position
        world.setBlockState(newPos, state, 18);

        // Restore NBT data if needed
        if (nbt != null) {
            BlockEntity newBlockEntity = world.getBlockEntity(newPos);
            if (newBlockEntity != null) {
                try {
                    // Corrected method signature for 1.20.1
                    Method readNbtMethod = BlockEntity.class.getDeclaredMethod("readNbt", NbtCompound.class);
                    readNbtMethod.setAccessible(true);
                    readNbtMethod.invoke(newBlockEntity, nbt);
                } catch (Exception ignored) {}
            }
        }
    }

    public static int getTargetHeightFromInput(int v) {
        if (!clampHeight) return v;

        final int min = 65;
        final int max = 85;
        if (v >= min && v <= max) return v;
        //mod math wasn't working so this just bounces back and forth
        while (v < min || v > max) {
            if (v > max) {
                v = max - (v - max);
            } else {
                v = min + (min - v);
            }
        }
        return v;
    }

    public static void oceanLevelCheck(ServerWorld world, int x, int z, Block block, int radius) {
        int oceanY = 64;

        // Clear a vertical column at the center (from oceanY to the top of the world)
        BlockPos.Mutable clearPos = new BlockPos.Mutable(x, 0, z);
        int topY = world.getTopY() - 1;
        for (int y = oceanY + 1; y <= topY; y++) {
            clearPos.setY(y);
            world.setBlockState(clearPos, Blocks.AIR.getDefaultState());
        }

        // Fill a square radius around the center at oceanY with the specified block
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos fillPos = new BlockPos(x + dx, oceanY, z + dz);
                world.setBlockState(fillPos, block.getDefaultState());
            }
        }
    }

    public static void surfaceFeaturePlacer(ServerWorld world, int x, int z, Block surfaceBlock, double chance, String dimension, Block... featureBlocks) {
        if (featureBlocks.length == 0 || Math.random() >= chance) return;
        BlockPos.Mutable pos = new BlockPos.Mutable(x, world.getTopY() - 1, z);
        int topY = world.getTopY() - 1;

        BlockPos surfacePos = new BlockPos(x, topY, z);
        for (int y = topY; y >= world.getBottomY(); y--) {
            pos.setY(y);
            Block blockAtPos = world.getBlockState(pos).getBlock();

            if (dimension.equals("OVERWORLD") && validOverworldBlocks.contains(blockAtPos)) {
                surfacePos = pos.toImmutable(); // immutable locks mutable
                break;
            } else if (dimension.equals("NETHER") && !excludedNetherBlocks.contains(blockAtPos) && blockAtPos != Blocks.AIR) {
                surfacePos = pos.toImmutable();
                break;
            } else if (dimension.equals("END")) {
                // do later
                break;
            }
        }
        for (int y = topY; y >= world.getBottomY(); y--) {
            if (world.getBlockState(surfacePos).getBlock() == surfaceBlock) {
                Block feature = featureBlocks[(int) (Math.random() * featureBlocks.length)];
                BlockPos placePos = surfacePos.up();
                world.setBlockState(placePos, feature.getDefaultState());
                break;
            }
        }
    }

    public static void structureFeaturePlacer(ServerWorld world, int x, int z, Block surfaceBlock, double chance, StructureFeatureType... structures) {
        if (structures.length == 0 || Math.random() >= chance) return;

        int topY = world.getTopY() - 1;
        int bottomY = world.getBottomY();

        for (int y = topY; y >= bottomY; y--) {
            BlockPos surfacePos = new BlockPos(x, y, z);
            if (world.getBlockState(surfacePos).getBlock() == surfaceBlock) {
                BlockPos placePos = surfacePos.up();
                StructureFeatureType chosen = structures[(int) (Math.random() * structures.length)];
                placeStructure(world, placePos, chosen);
                break;
            }
        }
    }

    private static void placeStructure(ServerWorld world, BlockPos pos, StructureFeatureType type) {
        switch (type) {
            case OAK_TREE -> placeTree(world, pos, TreeConfiguredFeatures.OAK);
            case BIRCH_TREE -> placeTree(world, pos, TreeConfiguredFeatures.BIRCH);
            case CHERRY_TREE -> placeTree(world, pos, TreeConfiguredFeatures.CHERRY);
            case MANGROVE_TREE -> placeTree(world, pos, TreeConfiguredFeatures.MANGROVE);
            case SNOWY_SPRUCE_TREE -> placeTree(world, pos, TreeConfiguredFeatures.SPRUCE);
            case RED_MUSHROOM -> placeTree(world, pos, TreeConfiguredFeatures.HUGE_RED_MUSHROOM);
            case BROWN_MUSHROOM -> placeTree(world, pos, TreeConfiguredFeatures.HUGE_BROWN_MUSHROOM);
            case DESERT_WELL -> placeDesertWell(world, pos);
            case ERODED_MESA -> placeErodedMesa(world, pos);
        }
    }

    public static void placeTree(ServerWorld world, BlockPos pos, RegistryKey<ConfiguredFeature<?, ?>> featureKey) {
        ConfiguredFeature<?, ?> feature = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(featureKey);
        if (feature != null) feature.generate(world, world.getChunkManager().getChunkGenerator(), world.getRandom(), pos);
    }

    private static void placeDesertWell(ServerWorld world, BlockPos pos) {
        StructureTemplateManager manager = world.getStructureTemplateManager();
        Optional<StructureTemplate> optional = manager.getTemplate(new Identifier("minecraft", "desert_well"));

        if (optional.isPresent()) {
            StructureTemplate template = optional.get();
            StructurePlacementData settings = new StructurePlacementData();
            template.place(world, pos, pos, settings, world.getRandom(), Block.NOTIFY_ALL);
        }
    }

    private static void placeErodedMesa(ServerWorld world, BlockPos pos) {
        Block currentMesaBlock;
        Random random = new Random();

        for (int i = 0; i < 25; i++){
            currentMesaBlock = Blocks.TERRACOTTA;
            if (pos.getY() % 5 == 0) currentMesaBlock = Blocks.ORANGE_TERRACOTTA;
            if (pos.getY() % 6 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
            if (pos.getY() % 8 == 0) currentMesaBlock = Blocks.WHITE_TERRACOTTA;
            if (pos.getY() % 9 == 0) currentMesaBlock = Blocks.CYAN_TERRACOTTA;
            if (pos.getY() % 11 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
            if (pos.getY() % 12 == 0) currentMesaBlock = Blocks.TERRACOTTA;
            if (pos.getY() % 14 == 0) currentMesaBlock = Blocks.RED_TERRACOTTA;
            if (pos.getY() % 17 == 0) currentMesaBlock = Blocks.ORANGE_TERRACOTTA;
            if (pos.getY() % 23 == 0) currentMesaBlock = Blocks.YELLOW_TERRACOTTA;

            BlockPos above = pos.up();
            if (random.nextInt(100) > 96 && !world.isAir(above)) {
                currentMesaBlock = Blocks.TERRACOTTA;
            }

            world.setBlockState(pos, currentMesaBlock.getDefaultState(), 3);
            pos = pos.up();
        }
    }
}