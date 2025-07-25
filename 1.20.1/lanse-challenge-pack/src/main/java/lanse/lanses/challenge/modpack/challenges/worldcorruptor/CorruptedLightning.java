package lanse.lanses.challenge.modpack.challenges.worldcorruptor;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;


import net.minecraft.util.math.MathHelper;


public class CorruptedLightning {

    public static void strike(MinecraftServer server) {

        Random random = Random.create();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            World world = player.getEntityWorld();

            // Spawn corrupted lightning in a ring around the player up to 128 blocks away.
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * 128;
            double x = player.getX() + radius * Math.cos(angle);
            double z = player.getZ() + radius * Math.sin(angle);

            // Find the highest non-air block at the X and Z coordinates
            BlockPos pos = new BlockPos((int) x, world.getTopY(), (int) z);
            pos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos);
            double y = pos.getY();

            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(x, y, z);
                world.spawnEntity(lightning);

                int power = random.nextInt(6) + 6;
                world.createExplosion(lightning, x, y, z, power, true, World.ExplosionSourceType.NONE);

                // Create 150 pillars
                createPillarsAroundLightning(world, pos);
            }
        }
    }

    private static void createPillarsAroundLightning(World world, BlockPos lightningPos) {
        Random random = Random.create();

        for (int i = 0; i < 150; i++) {
            // Pick a random starting point within 7 blocks of the lightning strike
            int offsetX = MathHelper.nextInt(random, -7, 7);
            int offsetY = MathHelper.nextInt(random, -7, 1);
            int offsetZ = MathHelper.nextInt(random, -7, 7);
            BlockPos startPos = lightningPos.add(offsetX, offsetY, offsetZ);

            // 50% chance to create a diagonal pillar, 50% chance for a straight pillar
            if (random.nextBoolean()) {
                BlockSpreader.createDiagonalPillar(startPos, (ServerWorld) world);
            } else {
                BlockSpreader.createStraightPillar(startPos, world, 10);
            }
        }
    }
}
