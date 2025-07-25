package lanse.copperworld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lanse.copperworld.storage.Database;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CopperWorld implements ModInitializer {
	public static boolean isModEnabled = false;
	public static MinecraftServer originalServer;
	public static final Queue<ChunkTaskResult> editQueue = new ConcurrentLinkedQueue<>();
	public static Queue<ChunkTask> processingQueue = new ConcurrentLinkedQueue<>();
	public static int maxColumnsPerTick = 100;
	public static int tickCount = 0;
	public static ServerPlayerEntity originalPlayer;

	private static final ExecutorService workerPool = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors()
	);


	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));

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
			if (server != originalServer) isModEnabled = false;

			if (ChunkProcessor.processedChunkCount > 25000) {
				ChunkProcessor.clearProcessedChunks();
			}

			processQueuedChunks();

			if (processingQueue.size() < maxColumnsPerTick * 20 && (!ChunkProcessor.complete || tickCount % 120 == 0)) {
				ChunkProcessor.tryNewChunks(server, true);
			}
		}
	}

	public void processQueuedChunks() {
		int processedCount = 0;
		List<ChunkTask> tasks = new ArrayList<>();

		while (!processingQueue.isEmpty() && processedCount < maxColumnsPerTick) {
			ChunkTask task = processingQueue.poll();
			if (task != null) {
				tasks.add(task);
				processedCount++;
			}
		}

		for (ChunkTask task : tasks) {
			workerPool.submit(() -> {
				List<BlockEdit> edits = new ArrayList<>();
				RegistryKey<World> dimensionKey = task.world.getRegistryKey();
				boolean shouldProcess = switch (dimensionKey.getValue().getPath()) {
					case "overworld" -> processOverworldColumn(task, edits);
					case "the_nether" -> processNetherColumn(task, edits);
					case "the_end" -> processEndColumn(task, edits);
					default -> false;
				};
				if (shouldProcess && !edits.isEmpty()) {
					editQueue.add(new ChunkTaskResult(task.world, task.x, task.z, edits));
				}
			});
		}

		//TODO - fix bottleneck somehow...
		int applied = 0;
		while (!editQueue.isEmpty() && applied < maxColumnsPerTick * 100) {
			ChunkTaskResult result = editQueue.poll();
			if (result != null) {
				for (BlockEdit edit : result.edits()) {
					Chunk chunk = result.world.getChunk(edit.pos());
					chunk.setBlockState(edit.pos(), edit.state(), 0);
					result.world.getChunkManager().markForUpdate(edit.pos());
				}
				applied++;
			}
		}
	}

	private boolean processOverworldColumn(ChunkTask task, List<BlockEdit> edits) {
		if (ChunkProcessor.overworldIsDisabled) return false;
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, edits);
		return true;
	}
	private boolean processNetherColumn(ChunkTask task, List<BlockEdit> edits) {
		if (ChunkProcessor.netherIsDisabled) return false;
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, edits);
		return true;
	}
	private boolean processEndColumn(ChunkTask task, List<BlockEdit> edits) {
		if (ChunkProcessor.endIsDisabled) return false;
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, edits);
		return true;
	}

	public record ChunkTask(ServerWorld world, int x, int z) { }
	public record BlockEdit(BlockPos pos, BlockState state) {}
	public record ChunkTaskResult(ServerWorld world, int x, int z, List<BlockEdit> edits) {}


	public static Path getConfigFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("config/copperworld");
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