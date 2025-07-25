package lanse.imageworld.imagecalculator.worldpresets;

import lanse.imageworld.imagecalculator.ImageConverter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

import java.awt.*;
import java.util.Random;

import static lanse.imageworld.WorldEditor.*;

public class FullColorMesaPreset {

    public static boolean naturalMesaTop = false;

    public static void overworldLogic(ServerWorld world, int x, int z, Color pixelColor, int pass){
        if (pass == 0) {
            clearSurface(world, x, z);
            moveColumn(world, x, z);
        } else if (pass == 1){
            if (pixelColor.equals(ImageConverter.ColorCategory.RED.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.NORMAL_MESA);
                if (!naturalMesaTop) replaceSurface(world, x, z, Blocks.RED_TERRACOTTA, 3, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.ORANGE.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                surfaceFeaturePlacer(world, x, z, Blocks.RED_SAND, 0.03, "OVERWORLD", Blocks.DEAD_BUSH);
                surfaceFeaturePlacer(world, x, z, Blocks.RED_SAND, 0.005, "OVERWORLD", Blocks.CACTUS);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.YELLOW.color)){
                replaceSurface(world, x, z, Blocks.SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.SAND, 1, "OVERWORLD");
                surfaceFeaturePlacer(world, x, z, Blocks.SAND, 0.05, "OVERWORLD", Blocks.DEAD_BUSH);
                surfaceFeaturePlacer(world, x, z, Blocks.SAND, 0.01, "OVERWORLD", Blocks.CACTUS);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GREEN.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_GREEN.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.NORMAL_MESA);
                replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.GRASS_BLOCK, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.GRASS_BLOCK, 0.01, MesaFeatureType.OAK_TREE);
                surfaceFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.04, "OVERWORLD", Blocks.GRASS, Blocks.DEAD_BUSH);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.LIGHT_BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_BLUE.color)) {
                replaceSurface(world, x, z, Blocks.SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.SAND, 1, MesaFeatureType.BLUE_MESA);
                if (!naturalMesaTop) replaceSurface(world, x, z, Blocks.BLUE_TERRACOTTA, 3, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLUE.color)){
                oceanLevelCheck(world, x, z, Blocks.WATER, 7);
                replaceSurface(world, x, z, Blocks.WATER, 10, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PURPLE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_PURPLE.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.PURPLE_MESA);
                replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.MYCELIUM, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.MYCELIUM, 0.01, MesaFeatureType.RED_MUSHROOM, MesaFeatureType.BROWN_MUSHROOM);
                surfaceFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.03, "OVERWORLD", Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PINK.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.PURPLE_MESA);
                if (!naturalMesaTop) replaceSurface(world, x, z, Blocks.PINK_TERRACOTTA, 3, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BROWN.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.WHITE_MESA);
                if (!naturalMesaTop) replaceSurface(world, x, z, Blocks.BROWN_TERRACOTTA, 3, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLACK.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.WHITE_MESA);
                if (!naturalMesaTop) replaceSurface(world, x, z, Blocks.BLACK_TERRACOTTA, 3, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.WHITE.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacerMesa(world, x, z, Blocks.RED_SAND, 1, MesaFeatureType.WHITE_MESA);
                if (!naturalMesaTop) replaceSurface(world, x, z, Blocks.WHITE_TERRACOTTA, 3, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GRAY.color)){
                replaceSurface(world, x, z, Blocks.STONE, 10, "OVERWORLD");
                //TODO - make ore veins slightly rarer. Maybe add gravel patches or something too, and andesite
                if (world.getRandom().nextFloat() < 0.1f) {
                    Block[] ores = new Block[]{Blocks.COAL_ORE, Blocks.GOLD_ORE, Blocks.COPPER_ORE};
                    Block ore = ores[world.getRandom().nextInt(ores.length)];
                    int baseY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) - 1;
                    for (int i = 0; i < 4 + world.getRandom().nextInt(6); i++) {
                        int dx = x + world.getRandom().nextInt(3) - 1;
                        int dy = baseY - world.getRandom().nextInt(3);
                        int dz = z + world.getRandom().nextInt(3) - 1;
                        BlockPos veinPos = new BlockPos(dx, dy, dz);
                        if (world.getBlockState(veinPos).isOf(Blocks.STONE)) {
                            world.setBlockState(veinPos, ore.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }

    public static void netherLogic(ServerWorld world, int x, int z, Color pixelColor, int pass){
        if (pass == 0) {
            clearSurface(world, x, z);
            moveColumn(world, x, z);
        } else if (pass == 1){
            if (pixelColor.equals(ImageConverter.ColorCategory.RED.color)){
                replaceSurface(world, x, z, Blocks.NETHERRACK, 10, "NETHER");
                replaceSurface(world, x, z, Blocks.CRIMSON_NYLIUM, 1, "NETHER");
                //1% chance of nether tree
                //5% chance of random nether plant, but most likely a red one

            } else if (pixelColor.equals(ImageConverter.ColorCategory.ORANGE.color)){
                oceanLevelCheck(world, x, z, Blocks.LAVA, 3);
                replaceSurface(world, x, z, Blocks.LAVA, 10, "NETHER");
            } else if (pixelColor.equals(ImageConverter.ColorCategory.YELLOW.color)){
                replaceSurface(world, x, z, Blocks.MAGMA_BLOCK, 10, "NETHER");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GREEN.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_GREEN.color)){
                replaceSurface(world, x, z, Blocks.NETHERRACK, 10, "NETHER");
                replaceSurface(world, x, z, Blocks.WARPED_NYLIUM, 1, "NETHER");
                //1% chance of nether tree
                //5% chance of random nether plant, but most likely a blue / green one

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.LIGHT_BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_BLUE.color)){
                replaceSurface(world, x, z, Blocks.NETHERRACK, 10, "NETHER");
                replaceSurface(world, x, z, Blocks.WARPED_NYLIUM, 1, "NETHER");
                //4% chance of nether tree
                //5% chance of random nether plant, but most likely a blue / green one

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PURPLE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_PURPLE.color)){
                replaceSurface(world, x, z, Blocks.NETHERRACK, 10, "NETHER");
                //2% chance of fire

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PINK.color)){
                replaceSurface(world, x, z, Blocks.NETHERRACK, 10, "NETHER");
                //2% chance of fire

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BROWN.color)){
                // Soul soil appears in patches (~30% of area) ((overcomplicated but cool))
                int hash = (x * 734287 + z * 912271) ^ 0x5f3759df;
                int value = Math.abs(hash % 100); // value from 0 to 99
                Block soulBlock = value < 30 ? Blocks.SOUL_SOIL : Blocks.SOUL_SAND;
                replaceSurface(world, x, z, soulBlock, 10, "NETHER");
                //0.2% chance of nether fossil structure
                //1% chance of crimson root plant if its soul SAND
                //0.3% chance of basalt pillar thing
                //2% chance of fire

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLACK.color)){
                replaceSurface(world, x, z, Blocks.BLACKSTONE, 10, "NETHER");
                //0.5% chance of ATTEMPTING to place a small lava pool. (EXTRA LOGIC NEEDED)
                //0.3% chance of basalt pillar thing

            } else if (pixelColor.equals(ImageConverter.ColorCategory.WHITE.color)){
                replaceSurface(world, x, z, Blocks.NETHERRACK, 10, "NETHER");
                replaceSurface(world, x, z, Blocks.NETHER_QUARTZ_ORE, 1, "NETHER");
                //2% chance of fire

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GRAY.color)){
                replaceSurface(world, x, z, Blocks.BASALT, 10, "NETHER");
                //1% chance of basalt pillar thing
            }
        }
    }

//    public static void endLogic(ServerWorld world, int x, int z, Color pixelColor, int pass){
//      //TODO - move this eventually
//    }

    public enum MesaFeatureType {
        OAK_TREE, NORMAL_MESA, WHITE_MESA, PURPLE_MESA, BLUE_MESA, RED_MUSHROOM, BROWN_MUSHROOM
    }

    public static void structureFeaturePlacerMesa(ServerWorld world, int x, int z, Block surfaceBlock, double chance, MesaFeatureType... structures) {
        if (structures.length == 0 || Math.random() >= chance) return;

        int topY = world.getTopY() - 1;
        int bottomY = world.getBottomY();

        for (int y = topY; y >= bottomY; y--) {
            BlockPos surfacePos = new BlockPos(x, y, z);
            if (world.getBlockState(surfacePos).getBlock() == surfaceBlock) {
                BlockPos placePos = surfacePos.up();
                MesaFeatureType chosen = structures[(int) (Math.random() * structures.length)];
                placeStructureMesa(world, placePos, chosen);
                break;
            }
        }
    }

    private static void placeStructureMesa(ServerWorld world, BlockPos pos, MesaFeatureType type) {
        switch (type) {
            case OAK_TREE -> placeTree(world, pos, TreeConfiguredFeatures.OAK);
            case RED_MUSHROOM -> placeTree(world, pos, TreeConfiguredFeatures.HUGE_RED_MUSHROOM);
            case BROWN_MUSHROOM -> placeTree(world, pos, TreeConfiguredFeatures.HUGE_BROWN_MUSHROOM);
            case NORMAL_MESA -> placeNormalMesa(world, pos);
            case WHITE_MESA -> placeWhiteMesa(world, pos);
            case PURPLE_MESA -> placePurpleMesa(world, pos);
            case BLUE_MESA -> placeBlueMesa(world, pos);
        }
    }

    private static void placeNormalMesa(ServerWorld world, BlockPos pos) {
        Block currentMesaBlock;
        Random random = new Random();

        for (int i = 0; i < 36; i++){
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

    private static void placeWhiteMesa(ServerWorld world, BlockPos pos) {
        Block currentMesaBlock;
        Random random = new Random();

        for (int i = 0; i < 36; i++){
            currentMesaBlock = Blocks.WHITE_TERRACOTTA;
            if (pos.getY() % 5 == 0) currentMesaBlock = Blocks.LIGHT_GRAY_TERRACOTTA;
            if (pos.getY() % 6 == 0) currentMesaBlock = Blocks.LIGHT_GRAY_TERRACOTTA;
            if (pos.getY() % 8 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
            if (pos.getY() % 9 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
            if (pos.getY() % 11 == 0) currentMesaBlock = Blocks.CYAN_TERRACOTTA;
            if (pos.getY() % 12 == 0) currentMesaBlock = Blocks.GRAY_TERRACOTTA;
            if (pos.getY() % 14 == 0) currentMesaBlock = Blocks.WHITE_TERRACOTTA;
            if (pos.getY() % 17 == 0) currentMesaBlock = Blocks.GRAY_TERRACOTTA;
            if (pos.getY() % 23 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;

            BlockPos above = pos.up();
            if (random.nextInt(100) > 96 && !world.isAir(above)) {
                currentMesaBlock = Blocks.WHITE_TERRACOTTA;
            }

            world.setBlockState(pos, currentMesaBlock.getDefaultState(), 3);
            pos = pos.up();
        }
    }

    private static void placePurpleMesa(ServerWorld world, BlockPos pos) {
        Block currentMesaBlock;
        Random random = new Random();

        for (int i = 0; i < 36; i++){
            currentMesaBlock = Blocks.WHITE_TERRACOTTA;
            if (pos.getY() % 5 == 0) currentMesaBlock = Blocks.PURPLE_TERRACOTTA;
            if (pos.getY() % 6 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
            if (pos.getY() % 8 == 0) currentMesaBlock = Blocks.PURPLE_TERRACOTTA;
            if (pos.getY() % 9 == 0) currentMesaBlock = Blocks.LIGHT_GRAY_TERRACOTTA;
            if (pos.getY() % 11 == 0) currentMesaBlock = Blocks.CYAN_TERRACOTTA;
            if (pos.getY() % 12 == 0) currentMesaBlock = Blocks.PURPLE_TERRACOTTA;
            if (pos.getY() % 14 == 0) currentMesaBlock = Blocks.PURPLE_TERRACOTTA;
            if (pos.getY() % 17 == 0) currentMesaBlock = Blocks.GRAY_TERRACOTTA;
            if (pos.getY() % 23 == 0) currentMesaBlock = Blocks.PURPLE_TERRACOTTA;

            BlockPos above = pos.up();
            if (random.nextInt(100) > 96 && !world.isAir(above)) {
                currentMesaBlock = Blocks.WHITE_TERRACOTTA;
            }

            world.setBlockState(pos, currentMesaBlock.getDefaultState(), 3);
            pos = pos.up();
        }
    }

    private static void placeBlueMesa(ServerWorld world, BlockPos pos) {
        Block currentMesaBlock;
        Random random = new Random();

        for (int i = 0; i < 36; i++){
            currentMesaBlock = Blocks.BLUE_TERRACOTTA;
            if (pos.getY() % 5 == 0) currentMesaBlock = Blocks.LIGHT_BLUE_TERRACOTTA;
            if (pos.getY() % 6 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
            if (pos.getY() % 8 == 0) currentMesaBlock = Blocks.PURPLE_TERRACOTTA;
            if (pos.getY() % 9 == 0) currentMesaBlock = Blocks.LIGHT_BLUE_TERRACOTTA;
            if (pos.getY() % 11 == 0) currentMesaBlock = Blocks.CYAN_TERRACOTTA;
            if (pos.getY() % 12 == 0) currentMesaBlock = Blocks.GRAY_TERRACOTTA;
            if (pos.getY() % 14 == 0) currentMesaBlock = Blocks.BLUE_TERRACOTTA;
            if (pos.getY() % 17 == 0) currentMesaBlock = Blocks.PINK_TERRACOTTA;
            if (pos.getY() % 23 == 0) currentMesaBlock = Blocks.BLUE_TERRACOTTA;

            BlockPos above = pos.up();
            if (random.nextInt(100) > 96 && !world.isAir(above)) {
                currentMesaBlock = Blocks.BLUE_TERRACOTTA;
            }

            world.setBlockState(pos, currentMesaBlock.getDefaultState(), 3);
            pos = pos.up();
        }
    }
}