package lanse.lanses.challenge.modpack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.challenges.blizzard.Blizzard;
import lanse.lanses.challenge.modpack.challenges.elytrarace.ElytraRace;
import lanse.lanses.challenge.modpack.challenges.floorislava.FloorIsLava;
import lanse.lanses.challenge.modpack.challenges.lavastorm.LavaStorm;
import lanse.lanses.challenge.modpack.challenges.lightningworld.LightningWorld;
import lanse.lanses.challenge.modpack.challenges.midastouch.MidasTouch;
import lanse.lanses.challenge.modpack.challenges.mobdoubler.MobDoubler;
import lanse.lanses.challenge.modpack.challenges.nuclearstorm.NuclearStorm;
import lanse.lanses.challenge.modpack.challenges.potionrain.PotionRain;
import lanse.lanses.challenge.modpack.challenges.wallspike.WallSpike;
import lanse.lanses.challenge.modpack.challenges.worldcorruptor.WorldCorrupter;
import lanse.lanses.challenge.modpack.storage.Database;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainControl implements ModInitializer {

	public static final String MOD_ID = "lanses-challenge-modpack";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean isModEnabled = false;

	public enum Preset {
		NONE, WALLSPIKE, NUCLEARSTORM, LAVASTORM, BLIZZARD, FLOORISLAVA, LIGHTNINGWORLD, WORLDCORRUPTOR,
		POTIONRAIN, MIDASTOUCH, MOBDOUBLER, MOBEXPLODER, ELYTRARACE, ALL
	}
	public static Preset modPreset = Preset.NONE;
	public static int tickCount = 0;
	public static String[] weatherPresets = {
			"NuclearStorm", "LavaStorm", "Blizzard", "LightningWorld", "WorldCorrupter", "PotionRain"
	};

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
		InitializedListeners.register();

		// Load config on startup
		Path configFolder = getConfigFolder();
		if (!Files.exists(configFolder)) {
			try {
				Files.createDirectories(configFolder);
			} catch (IOException ignored) {}
		} else {
			try {
				loadConfigFromJson(configFolder);
			} catch (IOException ignored) {}
		}
	}

	private void onServerTick(MinecraftServer server) {
		tickCount++;
		if (tickCount > 2147483641) tickCount = 0;

		if (tickCount % 300 == 0 && Database.changed) {
			Database dataSaver = Database.getOrCreate(server);
			Path configFolder = getConfigFolder();
			dataSaver.saveToJson(configFolder);
		}

		if (isModEnabled) {

			switch (modPreset){
				case WALLSPIKE -> WallSpike.tick(server);
				case NUCLEARSTORM -> NuclearStorm.tick(server);
				case LAVASTORM -> LavaStorm.tick(server);
				case BLIZZARD -> Blizzard.tick(server);
				case FLOORISLAVA -> FloorIsLava.tick(server);
				case LIGHTNINGWORLD -> LightningWorld.tick(server);
				case WORLDCORRUPTOR -> WorldCorrupter.tick(server);
				case POTIONRAIN -> PotionRain.tick(server);
				case MIDASTOUCH -> MidasTouch.tick(server);
				case MOBDOUBLER -> MobDoubler.tick(server);
				case ELYTRARACE -> ElytraRace.tick(server);
				case ALL -> TickAll(server);
			}
		}
	}

	private void TickAll(MinecraftServer server) {
		WallSpike.tick(server);
		NuclearStorm.tick(server);
		LavaStorm.tick(server);
		Blizzard.tick(server);
		FloorIsLava.tick(server);
		LightningWorld.tick(server);
		WorldCorrupter.tick(server);
		PotionRain.tick(server);
		MidasTouch.tick(server);
		MobDoubler.tick(server);
	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		Commands.registerCommands(dispatcher);
	}

	public static Path getConfigFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("config/lcp_challenge_pack");
	}

	private void loadConfigFromJson(Path configFolder) throws IOException {
		Path configFile = configFolder.resolve("config.json");
		if (Files.exists(configFile) && Files.size(configFile) > 0) {
			try (Reader reader = Files.newBufferedReader(configFile)) {
				Gson gson = new Gson();
				JsonObject jsonConfig = gson.fromJson(reader, JsonObject.class);
				Database.fromJson(jsonConfig);
			} catch (IOException ignored) {}
		}
	}
}