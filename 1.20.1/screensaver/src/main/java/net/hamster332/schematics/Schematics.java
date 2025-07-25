package net.hamster332.schematics;

import net.fabricmc.api.ModInitializer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class Schematics implements ModInitializer {
	public static final String MOD_ID = "schematics";
	public static int TickCounter = 0;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
	}


	private void onServerTick(MinecraftServer server) {
		if (TickCounter % 200 == 0) {
			TickCounter = 0;
			ServerWorld world = server.getOverworld();
			ServerPlayerEntity player = world.getRandomAlivePlayer();
			if (player != null) {
				BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
				SchematicsLoader.placeStructure(world, pos, new Identifier("schematics", "test_structure"), BlockMirror.NONE, BlockRotation.NONE);
			}
		}
		TickCounter += 1;
	}
}
