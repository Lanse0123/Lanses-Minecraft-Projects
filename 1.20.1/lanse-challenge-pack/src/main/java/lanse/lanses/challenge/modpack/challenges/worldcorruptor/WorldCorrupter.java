package lanse.lanses.challenge.modpack.challenges.worldcorruptor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class WorldCorrupter {
    public static boolean blockSpreaderIsOn = false;
    public static int stormPower = 1;

    public static void tick(MinecraftServer server) {

        if (blockSpreaderIsOn) {
            Random random = new Random();
            if (random.nextInt(250) == 25) {
                CorruptedLightning.strike(server);
            }

            // Iterate through each world on the server, if you want this to run in all dimensions
            for (ServerWorld world : server.getWorlds()) {
                for (int i = 0; i < stormPower; i++) {
                    BlockSpreader.spreadBlocks(world);
                }
                BlockSpreader.createParticles(world);
            }
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("LSP_CorruptedStormON")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("value", IntegerArgumentType.integer(1, 35))
                        .executes(context -> {
                            stormPower = IntegerArgumentType.getInteger(context, "power");
                            blockSpreaderIsOn = true;
                            MainControl.modPreset = MainControl.Preset.WORLDCORRUPTOR;
                            context.getSource().sendFeedback(() -> Text.of("The Corrupted Storm has begun with power level: " + stormPower), true);
                            return 1;
                        }))
                .executes(context -> {
                    context.getSource().sendError(Text.of("Please specify a value between 1 and 100."));
                    return 0;
                }));

        dispatcher.register(literal("LSP_CorruptedStormOFF")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    stormPower = 0;
                    blockSpreaderIsOn = false;
                    context.getSource().sendFeedback(() -> Text.of("The Corrupted Storm has stopped."), true);
                    return 1;
                }));
    }
}