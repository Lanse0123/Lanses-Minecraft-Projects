package lanse.mobrain;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import lanse.mobrain.custom_mobs.BOB;
import lanse.mobrain.custom_mobs.KingBOB;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class MobRainCommands {

    //Commands: MobRainStart, MobRainStop, MobRainGetWave, MobRainSetWave, MobRainSummonBOB,
    //BossRainStart, BossRainStop, BossRainGetWave, BossRainSetWave
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("MobRainStart")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    MobRain.startMobRain();
                    context.getSource().sendFeedback(() -> Text.of("Mob Rain started at Wave 1!"), true);
                    return 1;
                }));

        dispatcher.register(literal("MobRainStop")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    MobRain.stopMobRain();
                    context.getSource().sendFeedback(() -> Text.of("Mob Rain stopped and reset!"), true);
                    return 1;
                }));

        dispatcher.register(literal("MobRainSetWave")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("wave", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int wave = IntegerArgumentType.getInteger(context, "wave");
                            MobRain.setWave(wave);
                            context.getSource().sendFeedback(() -> Text.of("Mob Rain wave set to " + wave), true);
                            return 1;
                        })));

        dispatcher.register(literal("MobRainGetWave")
                .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.of("Mob Rain wave is " + (MobRain.getWave())), true);
                            return 1;
                        }));

        dispatcher.register(literal("MobRainSummonBOB")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        ZombieEntity bob = BOB.createBob(world);
                        bob.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(bob);
                        context.getSource().sendFeedback(() -> Text.literal("BOB has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon BOB. Player not found."));
                        return 0;
                    }
                }));

        dispatcher.register(literal("MobRainSummonKingBOB")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        ZombieEntity Kingbob = KingBOB.createKingBob(world);
                        Kingbob.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(Kingbob);
                        context.getSource().sendFeedback(() -> Text.literal("King BOB has been summoned!!!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon King BOB. Player not found."));
                        return 0;
                    }
                }));

        //MOB RAIN (AND BOB) IS ABOVE THIS
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //BOSS RAIN IS BELOW THIS

        dispatcher.register(literal("BossRainStart")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    BossRain.startBossRain();
                    context.getSource().sendFeedback(() -> Text.of("Boss Rain started at Wave 1!"), true);
                    return 1;
                }));

        dispatcher.register(literal("BossRainStop")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    BossRain.stopBossRain();
                    context.getSource().sendFeedback(() -> Text.of("Boss Rain stopped and reset!"), true);
                    return 1;
                }));

        dispatcher.register(literal("BossRainSetWave")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("wave", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int wave = IntegerArgumentType.getInteger(context, "wave");
                            BossRain.setWave(wave);
                            context.getSource().sendFeedback(() -> Text.of("Boss Rain wave set to " + wave), true);
                            return 1;
                        })));

        dispatcher.register(literal("BossRainGetWave")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.of("Boss Rain wave is " + (BossRain.getWave())), true);
                    return 1;
                }));
    }
}