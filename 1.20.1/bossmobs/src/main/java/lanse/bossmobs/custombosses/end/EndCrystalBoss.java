package lanse.bossmobs.custombosses.end;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class EndCrystalBoss extends EndCrystalEntity {

    public EndCrystalBoss(World world) {
        super(EntityType.END_CRYSTAL, world);

        BossMobs.addId(this.getId());
    }

    public static void Kaboom(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, Entity boss) {
        //This is called each tick that the boss is loaded.

        //Every 10 ticks, this will attack.
        if (BossMobs.tickCount % 10 != 0){
            return;
        }

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 30)) return;

        Random random = new Random();
        int attackIndex = random.nextInt(9); // 0 - 8

        Laser.DragonHealingLaser(world, boss, 5);

        switch (attackIndex) {
            case 0 -> Laser.BlindingLaser(world, boss, 8);
            case 1 -> Laser.BubbleBeam(world, boss, 7);
            case 2 -> Laser.IceLaser(world, boss, 8);
            case 3 -> Laser.FireLaser(world, boss, 7);
            case 4 -> Laser.GuardianLaser(world, boss, 8);
            case 5 -> Laser.HolyInkLaser(world, boss, 7);
            case 6 -> Laser.InkLaser(world, boss, 8);
            case 7 -> Laser.PoisonLaser(world, boss, 7);
            case 8 -> Laser.WitherLaser(world, boss, 8);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        EndCrystalBoss endCrystal = new EndCrystalBoss(world);
        endCrystal.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(endCrystal);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossEndCrystal")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        EndCrystalBoss endCrystal = new EndCrystalBoss(world);
                        endCrystal.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(endCrystal);
                        context.getSource().sendFeedback(() -> Text.literal("EndCrystal Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon EndCrystal Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}