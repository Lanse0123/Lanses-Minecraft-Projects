package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.FishTorpedo;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class BoatBoss extends BoatEntity {
    public BoatBoss(World world) {
        super(EntityType.BOAT, world);

        //No armor or stuff. After all, it is just a boat

        BossMobs.addId(this.getId());
    }

    public static void Deconstruct(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (NO LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, Entity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 50)) return;

        Random random = new Random();
        if (random.nextInt(80) == 25) {

            AttackFishTorpedo(world, boss);
        }
    }
    private static void AttackFishTorpedo(ServerWorld world, Entity boss) {

        Vec3d pos = boss.getPos();
        Box searchBox = new Box(pos.subtract(50, 50, 50), pos.add(50, 50, 50));

        List<BoatEntity> nearbyBoats = world.getEntitiesByClass(BoatEntity.class, searchBox,
                boat -> boat.squaredDistanceTo(boss) > 25);

        BoatEntity targetBoat = null;
        for (BoatEntity boat : nearbyBoats) {
            targetBoat = boat;
            break;
        }

        // If no boat was found, search for nearby entities other than players
        Entity targetEntity = targetBoat;
        if (targetEntity == null) {
            List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, searchBox,
                    entity -> !(entity instanceof PlayerEntity) && entity.squaredDistanceTo(boss) > 25);

            if (!nearbyEntities.isEmpty()) {
                targetEntity = nearbyEntities.get(0); // Choose the first visible entity
            }
        }

        // Lastly, search for nearby players if no other target was found
        if (targetEntity == null) {
            List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, searchBox,
                    player -> player.squaredDistanceTo(boss) > 25);

            if (!nearbyPlayers.isEmpty()) {
                targetEntity = nearbyPlayers.get(0); // Choose the first visible player
            }
        }

        // Spawn FishTorpedo targeting the selected entity if one was found
        if (targetEntity != null) {
            new FishTorpedo(world, boss.getX(), boss.getY(), boss.getZ(), targetEntity, 3);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        BoatBoss boat = new BoatBoss(world);
        boat.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(boat);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossBoat")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        BoatBoss boat = new BoatBoss(world);
                        boat.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(boat);
                        context.getSource().sendFeedback(() -> Text.literal("Boat Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Boat Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}