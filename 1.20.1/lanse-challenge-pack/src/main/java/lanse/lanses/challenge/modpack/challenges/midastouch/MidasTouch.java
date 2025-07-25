package lanse.lanses.challenge.modpack.challenges.midastouch;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class MidasTouch {
    public static void tick(MinecraftServer server) {
        server.getWorlds().forEach(world -> world.getPlayers().forEach(player -> {
            if (!player.isSpectator() && !world.isClient()) {
                BlockPos pos = player.getBlockPos().down();
                Block blockBelow = world.getBlockState(pos).getBlock();

                if (blockBelow != Blocks.AIR
                        && blockBelow != Blocks.NETHER_PORTAL
                        && blockBelow != Blocks.END_PORTAL
                        && blockBelow != Blocks.END_GATEWAY) {

                    world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
                }
            }
        }));
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("LCP_Preset_MidasTouch")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.MIDASTOUCH;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to MidasTouch!"), true);
                    return 1;
                }));
    }
}