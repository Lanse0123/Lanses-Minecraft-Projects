package lanse.lanses.challenge.modpack.challenges.floorislava;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class FloorIsLava {

    private static final Map<RegistryKey<World>, Map<BlockPos, Long>> lavaMaps = new HashMap<>();
    private static final Random random = new Random();

    public static void tick(MinecraftServer server) {
        server.getWorlds().forEach(world -> {
            RegistryKey<World> dimension = world.getRegistryKey();
            lavaMaps.putIfAbsent(dimension, new HashMap<>());
            Map<BlockPos, Long> lavaMap = lavaMaps.get(dimension);

            long currentTick = world.getTime();
            world.getPlayers().forEach(player -> {
                if (!player.isSpectator() && !world.isClient) {
                    BlockPos pos = player.getBlockPos().down();
                    if (isValidBlockForLava(world, pos)) {
                        lavaMap.computeIfAbsent(pos, p -> currentTick + (100 + random.nextInt(60))); // 5-8 seconds
                    }
                }
            });

            Iterator<Map.Entry<BlockPos, Long>> iterator = lavaMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Long> entry = iterator.next();
                if (currentTick >= entry.getValue()) {
                    world.setBlockState(entry.getKey(), Blocks.LAVA.getDefaultState());
                    iterator.remove();
                }
            }
        });
    }

    private static boolean isValidBlockForLava(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() != Blocks.AIR
                && world.getBlockState(pos).getBlock() != Blocks.NETHER_PORTAL
                && world.getBlockState(pos).getBlock() != Blocks.END_PORTAL
                && world.getBlockState(pos).getBlock() != Blocks.END_GATEWAY
                && world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN
                && world.getBlockState(pos).getBlock() != Blocks.END_PORTAL_FRAME;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("LCP_Preset_FloorIsLava")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    MainControl.modPreset = MainControl.Preset.FLOORISLAVA;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to FloorIsLava!"), true);
                    return 1;
                }));
    }
}