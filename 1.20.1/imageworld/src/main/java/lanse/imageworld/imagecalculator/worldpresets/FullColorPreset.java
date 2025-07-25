package lanse.imageworld.imagecalculator.worldpresets;

import lanse.imageworld.WorldEditor;
import lanse.imageworld.imagecalculator.ImageConverter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.awt.*;

import static lanse.imageworld.WorldEditor.*;

public class FullColorPreset {

    public static void overworldLogic(ServerWorld world, int x, int z, Color pixelColor, int pass){
        if (pass == 0) {
            clearSurface(world, x, z);
            moveColumn(world, x, z);
        } else if (pass == 1){
            if (pixelColor.equals(ImageConverter.ColorCategory.RED.color)){
                replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
                structureFeaturePlacer(world, x, z, Blocks.RED_SAND, 1, WorldEditor.StructureFeatureType.ERODED_MESA);
                replaceSurface(world, x, z, Blocks.RED_TERRACOTTA, 3, "OVERWORLD");

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
                structureFeaturePlacer(world, x, z, Blocks.SAND, 0.002, WorldEditor.StructureFeatureType.DESERT_WELL);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GREEN.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_GREEN.color)){
                replaceSurface(world, x, z, Blocks.STONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.GRASS_BLOCK, 1, "OVERWORLD");
                structureFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.01, WorldEditor.StructureFeatureType.OAK_TREE, WorldEditor.StructureFeatureType.BIRCH_TREE);
                surfaceFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.04, "OVERWORLD", Blocks.DANDELION, Blocks.POPPY,
                        Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                        Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                        Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.LILAC
                );

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.LIGHT_BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_BLUE.color)){
                oceanLevelCheck(world, x, z, Blocks.WATER, 7);
                replaceSurface(world, x, z, Blocks.WATER, 10, "OVERWORLD");

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PURPLE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_PURPLE.color)){
                replaceSurface(world, x, z, Blocks.STONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.MYCELIUM, 1, "OVERWORLD");
                surfaceFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.05, "OVERWORLD", Blocks.BROWN_MUSHROOM);
                surfaceFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.1, "OVERWORLD", Blocks.RED_MUSHROOM);
                structureFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.007, WorldEditor.StructureFeatureType.BROWN_MUSHROOM, WorldEditor.StructureFeatureType.RED_MUSHROOM);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PINK.color)){
                replaceSurface(world, x, z, Blocks.STONE, 10, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
                replaceSurface(world, x, z, Blocks.GRASS_BLOCK, 1, "OVERWORLD");
                structureFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.075, WorldEditor.StructureFeatureType.CHERRY_TREE);
                surfaceFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.85, "OVERWORLD", Blocks.PINK_PETALS);
                //TODO - make the pink petal face a random direction: North, east, south, or west
                BlockPos pos = new BlockPos(x, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) - 1, z);
                for (int i = 0; i < world.getRandom().nextInt(4); i++) {
                    BoneMealItem.useOnFertilizable(Items.BONE_MEAL.getDefaultStack(), world, pos);
                }

                //TODO - add patches of podzol like how the ore works
            } else if (pixelColor.equals(ImageConverter.ColorCategory.BROWN.color)){
                replaceSurface(world, x, z, Blocks.COARSE_DIRT, 10, "OVERWORLD");
                surfaceFeaturePlacer(world, x, z, Blocks.COARSE_DIRT, 0.05, "OVERWORLD", Blocks.DEAD_BUSH);
                surfaceFeaturePlacer(world, x, z, Blocks.COARSE_DIRT, 0.01, "OVERWORLD", Blocks.DANDELION, Blocks.POPPY,
                        Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                        Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                        Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.LILAC
                );
            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLACK.color)){
                replaceSurface(world, x, z, Blocks.MUD, 10, "OVERWORLD");
                structureFeaturePlacer(world, x, z, Blocks.MUD, 0.01, WorldEditor.StructureFeatureType.MANGROVE_TREE);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.WHITE.color)){
                replaceSurface(world, x, z, Blocks.SNOW_BLOCK, 10, "OVERWORLD");
                structureFeaturePlacer(world, x, z, Blocks.SNOW_BLOCK, 0.01, WorldEditor.StructureFeatureType.SNOWY_SPRUCE_TREE);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GRAY.color)){
                replaceSurface(world, x, z, Blocks.STONE, 10, "OVERWORLD");
                //TODO - make ore veins slightly rarer. Maybe add gravel patches or something too, and andesite
                if (world.getRandom().nextFloat() < 0.1f) {
                    Block[] ores = new Block[]{Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE, Blocks.LAPIS_ORE};
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
                //if {if{ if if if{ if if if if if if }}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
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
}