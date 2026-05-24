package lanse.secretdrunk;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class SecretDrunk implements ModInitializer {

	public static boolean isEnabled = false;
	public static int tickCount = 0;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private void onServerTick(MinecraftServer server) {
		if (!isEnabled) return;
		tickCount++;

		if (tickCount % 24000 == 0 && tickCount > 21){
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
				player.sendMessage(Text.literal("If your not sober, Take a shot :)"), true);
				player.sendMessage(Text.literal("If your not sober, Take a shot :)"), false);
			}
		}
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("SecretSoberPick").executes(context -> {
			MinecraftServer server = context.getSource().getServer();
			Collection<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
			List<ServerPlayerEntity> playerList = List.copyOf(players);

			Random random = new Random();
			ServerPlayerEntity soberPlayer = playerList.get(random.nextInt(playerList.size()));

			for (ServerPlayerEntity player : playerList) {
				if (player.equals(soberPlayer)) {
					player.sendMessage(Text.literal("You are Sober. Good luck understanding the others."), false);
					player.sendMessage(Text.literal("SOBER!"), true);
				} else {
					player.sendMessage(Text.literal("You are drunk."), false);
					player.sendMessage(Text.literal("DRUNK!"), true);
				}
			}

			context.getSource().sendFeedback(() -> Text.literal("A Secret Sober was chosen! Who might it be?"), true);
			isEnabled = true;
			return 1;
		}));

		dispatcher.register(CommandManager.literal("SecretSoberPickExact")
				.then(CommandManager.argument("player", EntityArgumentType.player()).executes(context -> {

					MinecraftServer server = context.getSource().getServer();
					Collection<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
					List<ServerPlayerEntity> playerList = List.copyOf(players);
					ServerPlayerEntity soberPlayer = EntityArgumentType.getPlayer(context, "player");

					if (!playerList.contains(soberPlayer)) {
						context.getSource().sendError(Text.literal("That player is not found."));
						return 0;
					}

					for (ServerPlayerEntity player : playerList) {
						if (player.equals(soberPlayer)) {
							player.sendMessage(Text.literal("You are Sober. Good luck understanding the others."), false);
							player.sendMessage(Text.literal("SOBER!"), true);
						} else {
							player.sendMessage(Text.literal("You are drunk."), false);
							player.sendMessage(Text.literal("DRUNK!"), true);
						}
					}

					context.getSource().sendFeedback(() -> Text.literal("A Secret Sober was chosen! Who might it be?"), true);
					isEnabled = true;
					return 1;
				})));
	}
}