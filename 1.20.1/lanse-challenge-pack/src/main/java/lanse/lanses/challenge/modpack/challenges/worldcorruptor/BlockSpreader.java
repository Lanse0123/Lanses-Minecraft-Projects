package lanse.lanses.challenge.modpack.challenges.worldcorruptor;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class BlockSpreader {
    private static final Random random = Random.create();
    private static final java.util.Random javaRandom = new java.util.Random();
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;
    private static final int MIN_DIM_Y = 0;
    private static final int MAX_DIM_Y = 256;

    public static void spreadBlocks(ServerWorld world) {
        world.getPlayers().forEach(player -> {

            BlockPos pos1 = getRandomPositionAroundPlayer(player.getBlockPos(), world, 64);

            //1 in 50 chance to swap 2 random blocks.
            if (javaRandom.nextInt(50) == 25){
                BlockPos pos2 = getRandomPositionAroundPlayer(player.getBlockPos(), world, 128);

                Block block1 = world.getBlockState(pos1).getBlock();
                Block block2 = world.getBlockState(pos2).getBlock();

                world.setBlockState(pos1, block2.getDefaultState());
                world.setBlockState(pos2, block1.getDefaultState());
                return;
            }

            BlockPos pos2 = getRandomNearbyPosition(pos1, world);

            //If both blocks are the same, do nothing. If its both Stone or Deepslate, make it infested.
            if (world.getBlockState(pos1).getBlock() == world.getBlockState(pos2).getBlock()) {
                if (world.getBlockState(pos1).getBlock() == Blocks.STONE){
                    world.setBlockState(pos1, Blocks.INFESTED_STONE.getDefaultState());
                    world.setBlockState(pos2, Blocks.INFESTED_STONE.getDefaultState());
                }
                if (world.getBlockState(pos1).getBlock() == Blocks.DEEPSLATE){
                    world.setBlockState(pos1, Blocks.INFESTED_DEEPSLATE.getDefaultState());
                    world.setBlockState(pos2, Blocks.INFESTED_DEEPSLATE.getDefaultState());
                }
                return;
            }

            if (isAllowedBlock(pos1, world) && isAllowedBlock(pos2, world)) {
                world.setBlockState(pos2, world.getBlockState(pos1));

                if (javaRandom.nextInt(25) == 0) {
                    brokenSpreadInCardinalDirection(pos2, world);
                }

                if (javaRandom.nextInt(25) == 5) {
                    createStraightPillar(pos2, world, 0);
                }

                if (javaRandom.nextInt(30) == 25) {
                    world.setBlockState(pos1, Blocks.AIR.getDefaultState());
                }
            }
        });
    }

    private static void brokenSpreadInCardinalDirection(BlockPos startPos, ServerWorld world) {
        // Randomly pick a cardinal direction (0 = North, 1 = East, 2 = South, 3 = West, 4 = Up, 5 = Down)
        int direction = javaRandom.nextInt(6);
        int spreadDistance = MathHelper.nextInt(random, 1, WorldCorrupter.stormPower);

        for (int i = 0; i < spreadDistance; i++) {
            BlockPos nextPos = getNextPositionInDirection(startPos, direction, i + 1);
            if (isAllowedBlock(startPos, world) && isAllowedBlock(nextPos, world)) {
                world.setBlockState(nextPos, world.getBlockState(startPos));
                startPos = nextPos; // Move to the next position for further spreading
            } else {
                break; // Stop spreading if the next block is not allowed
            }
        }
    }

    public static void createStraightPillar(BlockPos startPos, World world, int spreadDistance) {
        // Randomly pick a cardinal direction (0 = North, 1 = East, 2 = South, 3 = West, 4 = Up, 5 = Down)
        int direction = javaRandom.nextInt(6);
        spreadDistance = MathHelper.nextInt(random, 1 + spreadDistance, 15 + spreadDistance);
        BlockPos currentPos = startPos;

        for (int i = 0; i < spreadDistance; i++) {
            BlockPos nextPos = getNextPositionInDirection(currentPos, direction, 1);
            if (isAllowedBlock(currentPos, (ServerWorld) world) && isAllowedBlock(nextPos, (ServerWorld) world)) {
                world.setBlockState(nextPos, world.getBlockState(currentPos));
                currentPos = nextPos; // Move to the next position for further spreading
            } else {
                break; // Stop spreading if the next block is not allowed
            }
        }
    }

    private static BlockPos getNextPositionInDirection(BlockPos origin, int direction, int distance) {
        return switch (direction) {
            case 0 -> origin.north(distance);
            case 1 -> origin.east(distance);
            case 2 -> origin.south(distance);
            case 3 -> origin.west(distance);
            case 4 -> origin.up(distance);
            case 5 -> origin.down(distance);
            default -> origin;
        };
    }

    public static void createDiagonalPillar(BlockPos startPos, ServerWorld world) {
        // Randomly pick a diagonal direction (0 = Northwest, 1 = Northeast, 2 = Southwest, 3 = Southeast)
        int diagonalDirection = javaRandom.nextInt(4);
        // Randomly decide if the pillar should move up, down, or stay flat
        int verticalMovement = javaRandom.nextInt(3); // 0 = flat, 1 = up, 2 = down
        int height = MathHelper.nextInt(random, 3, 20); // Random height for the pillar
        int spreadDistance = MathHelper.nextInt(random, 1, 10); // Random spread distance for diagonals

        BlockPos currentPos = startPos;

        for (int i = 0; i < height; i++) {
            currentPos = getNextDiagonalPosition(currentPos, diagonalDirection, spreadDistance, verticalMovement);
            if (isAllowedBlock(startPos, world) && isAllowedBlock(currentPos, world)) {
                world.setBlockState(currentPos, world.getBlockState(startPos));
            } else {
                break; // Stop if the next position is not allowed
            }
        }
    }

    private static BlockPos getNextDiagonalPosition(BlockPos origin, int diagonalDirection, int distance, int verticalMovement) {
        // Move in a diagonal direction and handle vertical movement (flat, up, or down)
        return switch (diagonalDirection) {
            case 0 -> origin.north(distance).west(distance).up(verticalStepDiag(verticalMovement));  // Northwest
            case 1 -> origin.north(distance).east(distance).up(verticalStepDiag(verticalMovement));  // Northeast
            case 2 -> origin.south(distance).west(distance).up(verticalStepDiag(verticalMovement));  // Southwest
            case 3 -> origin.south(distance).east(distance).up(verticalStepDiag(verticalMovement));  // Southeast
            default -> origin;
        };
    }

    private static int verticalStepDiag(int verticalMovement) {
        return switch (verticalMovement) {
            case 1 -> 1;  // Move up
            case 2 -> -1; // Move down
            default -> 0; // Stay flat
        };
    }

    private static BlockPos getRandomPositionAroundPlayer(BlockPos playerPos, ServerWorld world, int distance) {
        int x = playerPos.getX() + MathHelper.nextInt(random, -distance, distance);
        int y = clampYLevel(playerPos.getY() + MathHelper.nextInt(random, -distance, distance), world, x, playerPos.getZ());
        int z = playerPos.getZ() + MathHelper.nextInt(random, -distance, distance);
        return new BlockPos(x, y, z);
    }

    private static BlockPos getRandomNearbyPosition(BlockPos origin, ServerWorld world) {
        int xOffset = MathHelper.nextInt(random, -1, 1);
        int yOffset = MathHelper.nextInt(random, -1, 1);
        int zOffset = MathHelper.nextInt(random, -1, 1);

        int newY = clampYLevel(origin.getY() + yOffset, world, origin.getX(), origin.getZ());
        return new BlockPos(origin.getX() + xOffset, newY, origin.getZ() + zOffset);
    }

    private static int clampYLevel(int y, ServerWorld world, int x, int z) {

        // Check if the dimension is Nether or End
        if (!(world.getDimension().bedWorks())) {
            return MathHelper.clamp(y, MIN_DIM_Y, MAX_DIM_Y);
        } else {
            // Overworld: Use the world surface to clamp Y
            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            return MathHelper.clamp(y, MIN_Y, Math.min(MAX_Y, surfaceY));
        }
    }

    private static boolean isAllowedBlock(BlockPos pos, ServerWorld world) {
        return world.getBlockState(pos).getBlock() != Blocks.NETHER_PORTAL
                && world.getBlockState(pos).getBlock() != Blocks.END_PORTAL
                && world.getBlockState(pos).getBlock() != Blocks.END_PORTAL_FRAME
                && world.getBlockState(pos).getBlock() != Blocks.END_GATEWAY
                && world.getBlockState(pos).getBlock() != Blocks.SPAWNER
                && world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN;
    }

    public static void createParticles(ServerWorld world) {
        int particleRange = 40;
        ParticleEffect corruptedParticle = ParticleTypes.DRAGON_BREATH;

        world.getPlayers().forEach(player -> {
            Vec3d playerPos = player.getPos();

            for (int i = 0; i < 100; i++) {
                double offsetX = (javaRandom.nextDouble() - 0.5) * 2.0 * particleRange;
                double offsetY = (javaRandom.nextDouble() - 0.5) * 2.0 * particleRange;
                double offsetZ = (javaRandom.nextDouble() - 0.5) * 2.0 * particleRange;

                world.spawnParticles(corruptedParticle, playerPos.x + offsetX,
                        playerPos.y + offsetY, playerPos.z + offsetZ,
                        1, 0, 0, 0, 0.15);
            }
        });
    }
}