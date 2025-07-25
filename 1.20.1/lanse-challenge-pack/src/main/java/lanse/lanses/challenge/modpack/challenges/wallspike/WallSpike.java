package lanse.lanses.challenge.modpack.challenges.wallspike;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WallSpike {

    public static void tick(MinecraftServer server) {

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            ServerWorld world = (ServerWorld) player.getWorld();
            BlockPos playerPos = player.getBlockPos();

            if (isTouchingWall(world, playerPos, player)) {
                player.damage(player.getDamageSources().flyIntoWall(), 250.0F);
            }
        }
    }
    private static boolean isTouchingWall(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {

        double proximityThreshold = 0.5;
        double playerX = player.getX();
        double playerZ = player.getZ();

        return (world.getBlockState(pos.north()).isSolidBlock(world, pos.north()) && Math.abs(playerZ - pos.getZ()) <= proximityThreshold) ||
                (world.getBlockState(pos.south()).isSolidBlock(world, pos.south()) && Math.abs(playerZ - (pos.getZ() + 1)) <= proximityThreshold) ||
                (world.getBlockState(pos.east()).isSolidBlock(world, pos.east()) && Math.abs(playerX - (pos.getX() + 1)) <= proximityThreshold) ||
                (world.getBlockState(pos.west()).isSolidBlock(world, pos.west()) && Math.abs(playerX - pos.getX()) <= proximityThreshold);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("LCP_Preset_WallSpike")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.WALLSPIKE;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to WallSpike!"), true);
                    return 1;
                }));
    }
}