package lanse.lanses.challenge.modpack.challenges.lightningworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LightningWorld {

    private static final int TICKS_PER_MINUTE = 1200;
    private static int tickCounter = 0;
    private static final Set<UUID> hitEntities = new HashSet<>();

    public static void tick(MinecraftServer server) {

        for (ServerWorld world : server.getWorlds()) {
            world.getPlayers().forEach(player -> {
                // Spawn particles within 42 blocks of the player
                spawnLightningRodParticles(player.getPos(), world);
            });
        }

        tickCounter++;
        if (tickCounter >= TICKS_PER_MINUTE) {
            tickCounter = 0;
            hitEntities.clear(); // Clear the set at the start of each cycle

            for (ServerWorld world : server.getWorlds()) {
                world.getPlayers().forEach(player -> {
                    // Define the bounding box around the player with a radius of 128 blocks
                    int radius = 128;
                    Box boundingBox = new Box(
                            player.getX() - radius, world.getBottomY(), player.getZ() - radius,
                            player.getX() + radius, world.getTopY(), player.getZ() + radius);

                    world.getEntitiesByClass(LivingEntity.class, boundingBox, entity -> true).forEach(entity -> {
                        if (!hitEntities.contains(entity.getUuid())) {
                            strikeEntity(entity, world);
                        }
                    });
                });
            }
        }
    }

    private static void strikeEntity(LivingEntity entity, ServerWorld world) {

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
            world.spawnEntity(lightning);

            hitEntities.add(entity.getUuid());
            hitEntities.add(lightning.getUuid());
        }
    }

    private static void spawnLightningRodParticles(Vec3d playerPos, ServerWorld world) {
        int particleRange = 42;
        Random random = world.getRandom();

        ParticleEffect[] commonParticles = {
                ParticleTypes.ELECTRIC_SPARK,
                ParticleTypes.WAX_OFF,
                ParticleTypes.END_ROD };

        ParticleEffect[] rareParticles = {
                ParticleTypes.FIREWORK,
                ParticleTypes.POOF,
                ParticleTypes.SPIT,
                ParticleTypes.CLOUD,
                ParticleTypes.CRIMSON_SPORE };

        for (int i = 0; i < 25; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            double offsetY = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0 * particleRange;

            ParticleEffect particle;
            if (random.nextInt(100) < 95) {
                // 95% chance to spawn a common particle
                particle = commonParticles[random.nextInt(commonParticles.length)];
            } else {
                // 5% chance to spawn a rare particle
                particle = rareParticles[random.nextInt(rareParticles.length)];
            }

            if (particle == ParticleTypes.CLOUD) {
                world.spawnParticles(particle, playerPos.x + offsetX, playerPos.y + offsetY, playerPos.z + offsetZ, 1, 0, 0, 0, 1.0);
            } else {
                world.spawnParticles(particle, playerPos.x + offsetX, playerPos.y + offsetY, playerPos.z + offsetZ, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("LCP_Preset_LightningWorld")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.LIGHTNINGWORLD;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to LightningWorld!"), true);
                    return 1;
                }));
    }
}