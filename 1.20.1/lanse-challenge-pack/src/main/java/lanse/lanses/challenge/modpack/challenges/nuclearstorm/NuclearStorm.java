package lanse.lanses.challenge.modpack.challenges.nuclearstorm;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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

public class NuclearStorm {

    private static int tickCount = 0;
    private static final int LEAF_DECAY_RADIUS = 64;
    private static final List<BlockPos> leavesToDecay = new ArrayList<>();

    public static void tick(MinecraftServer server) {

        tickCount ++;

        Random random = new Random();
        if (random.nextInt(185) == 25) {
            summonLightning(server);
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

    private static void summonLightning(MinecraftServer server) {
        Random random = new Random();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            World world = player.getEntityWorld();

            // Spawn nuclear lightning in a ring around the player up to 128 blocks away.
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
                world.createExplosion(lightning, x, y, z, power, true, World.ExplosionSourceType.TNT);
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
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20, 100));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 100));
                }
            });
        }
    }

    private static void spawnParticles(Vec3d playerPos, ServerWorld world) {
        int particleRange = 42;
        Random random = new Random();

        ParticleEffect sporeBlossomParticle = ParticleTypes.FALLING_SPORE_BLOSSOM;

        for (int i = 0; i < 75; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0 * particleRange;

            world.spawnParticles(sporeBlossomParticle, playerPos.x + offsetX, playerPos.y + 15, playerPos.z + offsetZ, 1, 0, -0.2, 0, 0);
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

        dispatcher.register(CommandManager.literal("LCP_Preset_NuclearStorm")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.NUCLEARSTORM;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to NuclearStorm!"), true);
                    return 1;
                }));
    }
}