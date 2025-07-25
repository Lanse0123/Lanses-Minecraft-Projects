package lanse.lanses.challenge.modpack.challenges.lavastorm;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.*;

public class LavaStorm {

    private static int tickCount = 0;
    private static final int LEAF_DECAY_RADIUS = 64;
    private static final List<BlockPos> leavesToDecay = new ArrayList<>();

    public static void tick(MinecraftServer server) {

        tickCount ++;

        Random random = new Random();
        if (random.nextInt(30) == 25) {
            startFires(server);
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            spawnParticles(player.getPos(), world);
            skyCheck(world);

            //Do this once every once in a while idk lol
            if (tickCount == 500) {
                scheduleLeafDecay(world, player.getBlockPos());
                tickCount = 0;
            }
        }
        processLeafDecay(server);
    }

    private static void startFires(MinecraftServer server) {
        Random random = new Random();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            BlockPos playerPos = player.getBlockPos();

            for (int i = 5 + random.nextInt(10); i > 0; i--) {
                // Generate random offsets within 50 blocks
                int dx = random.nextInt(101) - 50;
                int dz = random.nextInt(101) - 50;

                BlockPos targetPos = playerPos.add(dx, 0, dz);
                BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, targetPos);

                // Move up to find an AIR block
                if (world.getBlockState(surfacePos).isAir()) {
                    world.setBlockState(surfacePos, Blocks.FIRE.getDefaultState(), 3);
                }
            }
        }
    }

    private static void skyCheck(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            int radius = 64;
            Box boundingBox = new Box(
                    player.getX() - radius, world.getBottomY(), player.getZ() - radius,
                    player.getX() + radius, world.getTopY(), player.getZ() + radius);

            world.getEntitiesByClass(LivingEntity.class, boundingBox, entity -> true).forEach(entity -> {
                BlockPos entityPos = entity.getBlockPos();
                boolean isInDanger = true;

                for (int i = 1; i <= 15; i++) {
                    BlockPos checkPos = entityPos.up(i);
                    if (!world.isAir(checkPos)) {
                        isInDanger = false;
                        break;
                    }
                }
                if (isInDanger) {
                    entity.setOnFireFor(1);
                }
            });
        }
    }

    private static void spawnParticles(Vec3d playerPos, ServerWorld world) {
        int particleRange = 42;
        Random random = new Random();

        ParticleEffect[] lavaParticles = {
                ParticleTypes.DRIPPING_LAVA,
                ParticleTypes.LAVA,
                ParticleTypes.DRIPPING_DRIPSTONE_LAVA,
                ParticleTypes.FALLING_DRIPSTONE_LAVA,
                ParticleTypes.FALLING_LAVA
        };

        for (int i = 0; i < 350; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            ParticleEffect chosenParticle = lavaParticles[random.nextInt(lavaParticles.length)];
            world.spawnParticles(chosenParticle, playerPos.x + offsetX, playerPos.y + 15, playerPos.z + offsetZ, 1, 0, -0.5, 0, 0);
        }
    }

    private static void scheduleLeafDecay(ServerWorld world, BlockPos playerPos) {

        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        int startX = playerPos.getX() - LEAF_DECAY_RADIUS;
        int startY = playerPos.getY() - LEAF_DECAY_RADIUS;
        int startZ = playerPos.getZ() - LEAF_DECAY_RADIUS;
        int endX = playerPos.getX() + LEAF_DECAY_RADIUS;
        int endY = playerPos.getY() + LEAF_DECAY_RADIUS;
        int endZ = playerPos.getZ() + LEAF_DECAY_RADIUS;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getBlock() instanceof LeavesBlock) {
                        leavesToDecay.add(pos);
                    }
                }
            }
        }
    }

    private static void processLeafDecay(MinecraftServer server) {
        if (!leavesToDecay.isEmpty()) {
            int clearAmount = (leavesToDecay.size() / 250) + 1;
            ServerWorld world = server.getWorld(World.OVERWORLD);

            for (int i = 0; i < clearAmount; i++){
                int randomIndex = new Random().nextInt(leavesToDecay.size());
                BlockPos pos = leavesToDecay.get(randomIndex);

                if (pos != null) {
                    if (world != null && world.getBlockState(pos).getBlock() instanceof LeavesBlock) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                        leavesToDecay.remove(randomIndex);
                    }
                }
            }
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("LCP_Preset_LavaStorm")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.LAVASTORM;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to LavaStorm!"), true);
                    return 1;
                }));
    }
}