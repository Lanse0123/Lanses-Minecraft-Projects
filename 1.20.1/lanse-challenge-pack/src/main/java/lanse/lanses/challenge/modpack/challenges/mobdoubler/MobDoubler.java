package lanse.lanses.challenge.modpack.challenges.mobdoubler;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MobDoubler {

    private static final int TICKS_PER_MINUTE = 1200;
    private static int tickCounter = 0;
    private static final Set<UUID> duplicatedEntities = new HashSet<>();

    public static int MobCount = 0;

    public static void tick(MinecraftServer server) {

        tickCounter++;
        if (tickCounter >= TICKS_PER_MINUTE) {
            tickCounter = 0;
            MobCount = 0;
            duplicatedEntities.clear(); // Clear the set at the start of each cycle

            for (ServerWorld world : server.getWorlds()) {
                world.getPlayers().forEach(player -> {
                    // Define the bounding box around the player with a radius of 128 blocks
                    int radius = 128;
                    Box boundingBox = new Box(
                            player.getX() - radius, world.getBottomY(), player.getZ() - radius,
                            player.getX() + radius, world.getTopY(), player.getZ() + radius);

                    world.getEntitiesByClass(LivingEntity.class, boundingBox, entity -> true).forEach(entity -> {
                        if ((entity.getType() != EntityType.ENDER_DRAGON)
                                && (entity.getType() != EntityType.PLAYER)
                                && !duplicatedEntities.contains(entity.getUuid())) {
                            duplicateEntity(entity, world);
                            MobCount++;
                        }
                    });
                });
            }
            // Send message to all players about mob counts
            Text message = Text.literal(String.format("THE MOB COUNT HAS RISEN FROM %d TO %d", MobCount, (MobCount * 2)));
            server.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(message, false));
        }
    }

    private static void duplicateEntity(LivingEntity entity, ServerWorld world) {

        EntityType<?> type = entity.getType();
        Entity newEntity = type.create(world);

        if (newEntity != null) {
            newEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
            world.spawnEntity(newEntity);

            duplicatedEntities.add(entity.getUuid());
            duplicatedEntities.add(newEntity.getUuid());
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("LCP_Preset_MobDoubler")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.MOBDOUBLER;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to MobDOubler!"), true);
                    return 1;
                }));
    }
}