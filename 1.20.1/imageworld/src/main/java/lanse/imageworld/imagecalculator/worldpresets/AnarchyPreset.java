package lanse.imageworld.imagecalculator.worldpresets;

import lanse.imageworld.automata.LavaCaster;
import lanse.imageworld.imagecalculator.ImageConverter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Random;

import static lanse.imageworld.WorldEditor.*;

public class AnarchyPreset {

    public static Random random = new Random();

    public static void overworldLogic(ServerWorld world, int x, int z, Color pixelColor, int pass){
        if (pass == 0) {
            clearSurface(world, x, z);

            int[][] yRanges = {
                    {-63, -20},
                    {15, 30},
                    {45, 55}
            };

            for (int[] range : yRanges) {
                int minY = Math.min(range[0], range[1]);
                int maxY = Math.max(range[0], range[1]);

                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    // Skip if block is liquid, bedrock, or obsidian
                    if (block == Blocks.WATER || block == Blocks.LAVA || block == Blocks.BEDROCK || block == Blocks.OBSIDIAN) {
                        continue;
                    }
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }

            //destroy top 5 surface for good measure lul
            int i = 0;
            for (int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z); y >= 50 && i < 5; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                i++;
            }

            // 1 in 350 chance to spawn an ender chest on the surface
            if (random.nextInt(350) == 25) {
                int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

                // 10% chance to place it at a completely random Y level (between 50 and 319)
                if (random.nextInt(10) == 2) {
                    y = 50 + random.nextInt(270);
                }

                BlockPos pos = new BlockPos(x, y, z);
                world.setBlockState(pos, Blocks.ENDER_CHEST.getDefaultState());
            }

            // Fake highway system
            if (Math.abs(x) <= 3 || Math.abs(z) <= 3) {
                // Place base obsidian at Y 70
                BlockPos base = new BlockPos(x, 70, z);
                world.setBlockState(base, Blocks.OBSIDIAN.getDefaultState());

                // Clear space above base from Y 71 to Y 75
                for (int y = 71; y <= 75; y++) {
                    world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
                }

                // If exactly 3 blocks from 0 on X or Z axis, place guardrail at Y 70 and 71
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    world.setBlockState(new BlockPos(x, 70, z), Blocks.OBSIDIAN.getDefaultState());
                    world.setBlockState(new BlockPos(x, 71, z), Blocks.OBSIDIAN.getDefaultState());
                }
            }

            //big wall grid and random pillars (idk i see them alot on anarchy)
            if (Math.abs(x) % 3500 == 0 || Math.abs(z) % 3500 == 0 && x != 0 && z != 0){
                for (int y = 319; y >= 40; y--){
                    world.setBlockState(new BlockPos(x, y, z), Blocks.OBSIDIAN.getDefaultState());
                }
            } else if (random.nextInt(2500) == 25) {
                // 1 in 2500 chance for a big obsidian pillar
                int radius = 2 + random.nextInt(3);
                int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
                int maxY = surfaceY + 10 + random.nextInt(141);

                //this is so much work to make what people create by accident
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dz * dz <= radius * radius) {
                            for (int y = 40; y <= maxY; y++) {
                                world.setBlockState(new BlockPos(x + dx, y, z + dz), Blocks.OBSIDIAN.getDefaultState());
                            }
                        }
                    }
                }
            } else if (random.nextInt(2000) == 25) {
                // Small random pillar (cobble or netherrack)
                Block block = random.nextBoolean() ? Blocks.COBBLESTONE : Blocks.NETHERRACK;
                for (int y = 318; y >= 40; y--) {
                    world.setBlockState(new BlockPos(x, y, z), block.getDefaultState());
                }
            }

            //random explosions :D
            if (random.nextInt(969) == 25){
                world.createExplosion(null, x, random.nextInt(220) - 55, z, random.nextInt(35) + 10, World.ExplosionSourceType.MOB);
            }

            //random withers :DDDDDD
            if (random.nextInt(2300) == 25){
                WitherEntity wither = new WitherEntity(EntityType.WITHER, world);
                world.spawnEntity(wither);
                wither.teleport(x, random.nextInt(110) - 50, z);
            }

            //obsidiroof
            if (!(random.nextInt(1000) > 995)) world.setBlockState(new BlockPos(x, 319, z), Blocks.OBSIDIAN.getDefaultState());

        } else if (pass == 1){

            // 1 in 800 chance to spawn a nether portal at a random Y level
            if (random.nextInt(800) == 25) {
                int baseY = -50 + random.nextInt(360);
                int age = random.nextInt(101); // Age 0 to 100

                for (int dy = 0; dy < 4; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        BlockPos pos = new BlockPos(x + dx, baseY + dy, z);

                        boolean isFrame = (dx == -1 || dx == 1 || dy == 0 || dy == 3);
                        boolean isIntact = age <= 10 || random.nextInt(100) > age; // Higher age = more likely to break

                        if (isIntact) {
                            if (isFrame) {
                                world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                            } else {
                                world.setBlockState(pos, Blocks.NETHER_PORTAL.getDefaultState());
                            }
                        } else {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState()); // Block is missing due to decay
                        }
                    }
                }
                //ancient portals have netherrack and lava around them lul
                if (age > 80) {
                    for (int i = 0; i < 3 + random.nextInt(5); i++) {
                        int dx = x + random.nextInt(5) - 2;
                        int dz = z + random.nextInt(5) - 2;
                        int dy = baseY + random.nextInt(5);
                        BlockPos extra = new BlockPos(dx, dy, dz);

                        Block debris = random.nextBoolean() ? Blocks.NETHERRACK : Blocks.LAVA;
                        world.setBlockState(extra, debris.getDefaultState());
                    }
                }
            }
            //global lavacast chance
            boolean lavacastWillHappen = false;
            BlockPos pos = null;
            if (random.nextInt(3000) == 25){
                lavacastWillHappen = true;
                int y = random.nextInt(200);
                pos = new BlockPos(x, y, z);
            }

            if (pixelColor.equals(ImageConverter.ColorCategory.RED.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.MESACAST, -40404, -40404);
                }
                makeCaveOcean(world, x, z, Blocks.LAVA);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.ORANGE.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.MESACAST, -40404, -40404);
                }
                makeCaveOcean(world, x, z, Blocks.LAVA);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.YELLOW.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.SANDCAST, -40404, -40404);
                }
                makeCaveOcean(world, x, z, Blocks.LAVA);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GREEN.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_GREEN.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.MOSSYCAST, -40404, -40404);
                }

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.LIGHT_BLUE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_BLUE.color)){

                if (lavacastWillHappen) {
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.LAVACAST, -40404, -40404);
                }
                makeCaveOcean(world, x, z, Blocks.WATER);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PURPLE.color) ||
                    pixelColor.equals(ImageConverter.ColorCategory.DARK_PURPLE.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.OBSIDICAST, -40404, -40404);
                }

                makeCaveOcean(world, x, z, Blocks.LAVA);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.PINK.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.OBSIDICAST, -40404, -40404);
                }

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BROWN.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.LAVACAST, -40404, -40404);
                }
                makeCaveOcean(world, x, z, Blocks.WATER);

            } else if (pixelColor.equals(ImageConverter.ColorCategory.BLACK.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.LAVACAST, -40404, -40404);
                }

            } else if (pixelColor.equals(ImageConverter.ColorCategory.WHITE.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.LAVACAST, -40404, -40404);
                }

            } else if (pixelColor.equals(ImageConverter.ColorCategory.GRAY.color)){
                if (lavacastWillHappen){
                    LavaCaster.initializeLavaCast(world, pos, LavaCaster.LavaCastType.LAVACAST, -40404, -40404);
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

    public static void makeCaveOcean(ServerWorld world, int x, int z, Block oceanType){
        for (int y = 50; y > 45; y--){
            if (world.getBlockState(new BlockPos(x, y, z)) == Blocks.AIR.getDefaultState()){
                world.setBlockState(new BlockPos(x, y, z), oceanType.getDefaultState());
            }
        }
    }
}