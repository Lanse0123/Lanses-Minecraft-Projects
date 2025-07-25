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
import java.util.Random;

import static lanse.imageworld.WorldEditor.*;

public class SkyblockPreset {

    public static void overworldLogic(ServerWorld world, int x, int z, Color pixelColor){
        if (pixelColor.equals(ImageConverter.ColorCategory.RED.color)){
            replaceSurface(world, x, z, Blocks.RED_SANDSTONE, 10, "OVERWORLD");
            replaceSurface(world, x, z, Blocks.RED_SAND, 1, "OVERWORLD");
            structureFeaturePlacer(world, x, z, Blocks.RED_SAND, 1, WorldEditor.StructureFeatureType.ERODED_MESA);

        } else if (pixelColor.equals(ImageConverter.ColorCategory.ORANGE.color)){
            //TODO - eventually have it place random lava patches like how the random water patches work below
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
            WorldEditor.replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
            WorldEditor.replaceSurface(world, x, z, Blocks.GRASS_BLOCK, 1, "OVERWORLD");
            WorldEditor.structureFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.01, WorldEditor.StructureFeatureType.OAK_TREE, WorldEditor.StructureFeatureType.BIRCH_TREE);
            WorldEditor.surfaceFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.04, "OVERWORLD", Blocks.DANDELION, Blocks.POPPY,
                    Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                    Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                    Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.LILAC);

        } else if (pixelColor.equals(ImageConverter.ColorCategory.BLUE.color) ||
                pixelColor.equals(ImageConverter.ColorCategory.LIGHT_BLUE.color) ||
                pixelColor.equals(ImageConverter.ColorCategory.DARK_BLUE.color)){
            Random random = new Random();
            WorldEditor.replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
            WorldEditor.replaceSurface(world, x, z, Blocks.GRASS_BLOCK, 1, "OVERWORLD");
            WorldEditor.structureFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.01, WorldEditor.StructureFeatureType.OAK_TREE, WorldEditor.StructureFeatureType.BIRCH_TREE);
            WorldEditor.surfaceFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.04, "OVERWORLD", Blocks.BLUE_ORCHID, Blocks.AZURE_BLUET, Blocks.CORNFLOWER);
            if (random.nextInt(100) == 25) WorldEditor.replaceSurface(world, x, z, Blocks.WATER, 1, "OVERWORLD");

        } else if (pixelColor.equals(ImageConverter.ColorCategory.PURPLE.color) ||
                pixelColor.equals(ImageConverter.ColorCategory.DARK_PURPLE.color)){
            replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
            replaceSurface(world, x, z, Blocks.MYCELIUM, 1, "OVERWORLD");
            surfaceFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.05, "OVERWORLD", Blocks.BROWN_MUSHROOM);
            surfaceFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.1, "OVERWORLD", Blocks.RED_MUSHROOM);
            structureFeaturePlacer(world, x, z, Blocks.MYCELIUM, 0.007, WorldEditor.StructureFeatureType.BROWN_MUSHROOM, WorldEditor.StructureFeatureType.RED_MUSHROOM);

        } else if (pixelColor.equals(ImageConverter.ColorCategory.PINK.color)){
            replaceSurface(world, x, z, Blocks.DIRT, 3, "OVERWORLD");
            replaceSurface(world, x, z, Blocks.GRASS_BLOCK, 1, "OVERWORLD");
            structureFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.075, WorldEditor.StructureFeatureType.CHERRY_TREE);
            surfaceFeaturePlacer(world, x, z, Blocks.GRASS_BLOCK, 0.85, "OVERWORLD", Blocks.PINK_PETALS);
            BlockPos pos = new BlockPos(x, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) - 1, z);
            for (int i = 0; i < world.getRandom().nextInt(4); i++) {
                BoneMealItem.useOnFertilizable(Items.BONE_MEAL.getDefaultStack(), world, pos);
            }

            //TODO - add patches of podzol like how the ore works
        } else if (pixelColor.equals(ImageConverter.ColorCategory.BROWN.color)){
            replaceSurface(world, x, z, Blocks.COARSE_DIRT, 3, "OVERWORLD");
            surfaceFeaturePlacer(world, x, z, Blocks.COARSE_DIRT, 0.05, "OVERWORLD", Blocks.DEAD_BUSH);
            surfaceFeaturePlacer(world, x, z, Blocks.COARSE_DIRT, 0.01, "OVERWORLD", Blocks.DANDELION, Blocks.POPPY,
                    Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                    Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                    Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.LILAC
            );
        } else if (pixelColor.equals(ImageConverter.ColorCategory.BLACK.color)){
            replaceSurface(world, x, z, Blocks.MUD, 3, "OVERWORLD");
            structureFeaturePlacer(world, x, z, Blocks.MUD, 0.01, WorldEditor.StructureFeatureType.MANGROVE_TREE);

        } else if (pixelColor.equals(ImageConverter.ColorCategory.WHITE.color)){
            replaceSurface(world, x, z, Blocks.SNOW_BLOCK, 3, "OVERWORLD");
            structureFeaturePlacer(world, x, z, Blocks.SNOW_BLOCK, 0.01, WorldEditor.StructureFeatureType.SNOWY_SPRUCE_TREE);

        } else if (pixelColor.equals(ImageConverter.ColorCategory.GRAY.color)){
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