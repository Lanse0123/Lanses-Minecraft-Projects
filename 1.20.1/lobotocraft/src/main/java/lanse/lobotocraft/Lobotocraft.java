package lanse.lobotocraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lanse.lobotocraft.ColorMatchers.ColorPicker;
import lanse.lobotocraft.ColorMatchers.ScreenDecoder;
import lanse.lobotocraft.terraincalculator.TerrainGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Lobotocraft implements ModInitializer {

	public enum Mode {
		LOBOTOMY, DEMENTIA
	}

	public static boolean isModEnabled = false;
	public static MinecraftServer originalServer;
	public static Queue<ChunkTask> processingQueue = new LinkedList<>();
	public static int maxColumnsPerTick = 1000;
	public static int tickCount = 0;
	public static int refreshRate = 180;
	public static Set<Vec3d> playerPositions = new HashSet<>();
	public static Random random = new Random();
	public static Mode currentMode = Mode.DEMENTIA;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));

		// Check or create config file during initialization
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

			if (server != originalServer) isModEnabled = false;

			playerPositions.clear();
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				playerPositions.add(player.getPos());
			}

			if (tickCount % 20 == 0) {

				//If the player is not in their menu, swap items.
				if (currentMode == Mode.DEMENTIA) {
					for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
						if (player.getVelocity().lengthSquared() > 0.01 && !player.isFallFlying()) {

							// Get the player's upper inventory slots (9-35)
							DefaultedList<ItemStack> mainInventory = player.getInventory().main;

							// Choose two random slots to swap between 9 and 35 (upper inventory)
							int slot1 = 9 + player.getRandom().nextInt(27);
							int slot2 = 9 + player.getRandom().nextInt(27);

							ItemStack temp = mainInventory.get(slot1);
							mainInventory.set(slot1, mainInventory.get(slot2));
							mainInventory.set(slot2, temp);
						}
					}
				}
				//kill@e[type=item] except those within a 5 block radius of any player
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					int searchRadius = 256;
					int spareRadius = 5;

					Box searchBox = new Box(player.getX() - searchRadius, player.getServerWorld().getBottomY(), player.getZ() - searchRadius, player.getX() + searchRadius, player.getServerWorld().getTopY(), player.getZ() + searchRadius);
					List<ItemEntity> items = player.getServerWorld().getEntitiesByClass(ItemEntity.class, searchBox, entity -> true);

					for (ItemEntity item : items) {
						boolean isSpared = server.getPlayerManager().getPlayerList().stream().anyMatch(otherPlayer -> item.getPos().isInRange(otherPlayer.getPos(), spareRadius));
						if (!isSpared) item.kill();
					}
				}
			}

			//These all have their enum mode logic inside each function. Sorry for writing "clean" code.
			if (tickCount % refreshRate == 0) {
				ChunkGenerationListener.clearProcessedChunks();
				ScreenDecoder.clearScreenSeletion();

				// 1 in 5 chance it shuffles the terrain generation
				if (random.nextInt(5) == 3) {
					TerrainGenerator.randomizeSeed();
					TerrainGenerator.centerSmoothingPosList.clear();
				}

				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					ScreenDecoder.analyzeScreen(player.getServerWorld(), player);
					TerrainGenerator.centerSmoothingPosList.add(player.getPos());
				}

				ColorPicker.findCloseBlocks();
			}

			processQueuedChunks();

			if (processingQueue.size() < maxColumnsPerTick * 20 && (!ChunkGenerationListener.complete || tickCount % 120 == 0)) {
				ChunkGenerationListener.tryNewChunks(server, true);
			}
		}
	}

	// Process queued chunks in batches, since the game was crashing when it was unlimited speed
	public void processQueuedChunks() {
		int processedCount = 0;

		while (!processingQueue.isEmpty() && processedCount < maxColumnsPerTick) {
			ChunkTask task = processingQueue.poll();
			if (task != null) {

				if (!isColumnVisibleToAnyPlayer(task.world, task.x, task.z)) {

					RegistryKey<World> dimensionKey = task.world.getRegistryKey();
					if (dimensionKey.equals(World.OVERWORLD)) {
						processOverworldColumn(task);
					} else if (dimensionKey.equals(World.NETHER)) {
						processNetherColumn(task);
					} else if (dimensionKey.equals(World.END)) {
						processEndColumn(task);
					}
					processedCount++;
				}
			}
		}
	}

	private boolean isColumnVisibleToAnyPlayer(ServerWorld world, int x, int z) {
		ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);

		//UGLY
		for (PlayerEntity player : world.getPlayers()) {
			Vec3d eyePosition = player.getEyePos();
			Vec3d lookDirection = player.getRotationVec(1.0F).normalize();
			Vec3d chunkCenter = new Vec3d(chunkPos.getStartX() + 8, eyePosition.y, chunkPos.getStartZ() + 8);
			Vec3d toChunk = chunkCenter.subtract(eyePosition).normalize();
			double halfFOV = Math.toRadians(170.0 / 2.0);

			if (lookDirection.dotProduct(toChunk) >= Math.cos(halfFOV)) {
				return true;
			}
		}
		return false;
	}

	private void processOverworldColumn(ChunkTask task) {
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
		WorldEditor.adjustColumn(task.world, task.x, task.z, "OVERWORLD");
	}

	private void processNetherColumn(ChunkTask task) {
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
		WorldEditor.adjustColumn(task.world, task.x, task.z, "NETHER");
	}

	private void processEndColumn(ChunkTask task) {
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) {
			return; // Skip processing if no players are present in that dimension
		}
		WorldEditor.adjustColumn(task.world, task.x, task.z, "END");
	}

	public record ChunkTask(ServerWorld world, int x, int z) {}

	private Path getConfigFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("config/lobotocraft");
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