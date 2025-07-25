package lanse.lanican;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanican.item.ModItemGroups;
import lanse.lanican.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Lanican implements ModInitializer {
	public static final String MOD_ID = "lanican";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
	}

	private void onServerTick(MinecraftServer server) {

	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		Commands.registerCommands(dispatcher);
	}
}