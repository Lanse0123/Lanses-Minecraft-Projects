package lanse.imageworld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lanse.imageworld.automata.Automata3D;
import lanse.imageworld.automata.LavaCaster;
import lanse.imageworld.imagecalculator.*;
import lanse.imageworld.storage.Database;
import lanse.imageworld.storage.RegionStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class ImageWorld implements ModInitializer {
	public static boolean isModEnabled = false;
	public static MinecraftServer originalServer;
	public static List<ChunkTask> processingQueue = new ArrayList<>();
	public static int maxColumnsPerTick = 50;
	public static int tickCount = 0;
	public static boolean permaSave = false;
	public static ServerPlayerEntity originalPlayer;
	public static Queue<PermaTask> permaQueue = new LinkedList<>();
	public static Queue<PermaTask2> permaQueue2 = new LinkedList<>();
	public static final Map<String, RegionStorage> regionCache = new LinkedHashMap<>(50, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, RegionStorage> eldest) {
			return size() > 50; // Keep only 50 most recently accessed regions in memory
		}
	};
	public static boolean permaSaveNeedsWrite = false;
	public static ImageConverter imageConverter = new ImageConverter();
	public static int pauseTimer = 0;
	public static int pauseTimerLength = 200;
	public static int currentPass = 0;
	public static boolean singleStopper = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));

		//ImageIO cache could've been being problematic
		ImageIO.setUseCache(false);

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

		if (LavaCaster.isLavaCasterOn && WorldEditor.colorPalette == WorldEditor.ColorPalette.ANARCHY) LavaCaster.tick();

		Automata3D.tick();

		if (isModEnabled) {
			if (server != originalServer){
				isModEnabled = false;
				return;
			}

			if (tickCount - pauseTimer < pauseTimerLength && CoordinateCalculator.coordinateMode != CoordinateCalculator.CoordinateMode.SINGLE
			&& CoordinateCalculator.coordinateMode != CoordinateCalculator.CoordinateMode.SINGLE_AT_LOCATION){
				return;
			}

			if (ChunkProcessor.processedChunkCount > 25000) ChunkProcessor.clearProcessedChunks();

			// This is a mess, but it is the logic to capture a screenshot since it is asynchronous.
			if (ImageCalculator.currentFrameComplete && !ColorMapGenerator.isUsingColorMap) {
				if (!ScreenshotHelper.isCapturing() && !ScreenshotHelper.hasScreenshotBeenTaken()) {
					ScreenshotHelper.captureScreenshot();

				} else if (ScreenshotHelper.hasScreenshotBeenTaken()) {
					ScreenshotHelper.resetScreenshotFlag();
					CoordinateCalculator.teleportToNextFrame();
					ImageCalculator.currentFrameComplete = false;

					if (!singleStopper){
						ImageWorld.imageConverter.processCurrentFrame();
					} else {
						isModEnabled = false;
					}
				}
			} else {
				// only happens after processCurrentFrame is done
				processQueuedChunks();
			}

			if (processingQueue.isEmpty() && (!ChunkProcessor.complete || tickCount % 120 == 0) &&
					(ImageConverter.isUsingLargeImage || ColorMapGenerator.isUsingColorMap)) {
				ChunkProcessor.tryNewChunks(server);
				currentPass = 0;
			}

			if (permaSave && tickCount % 100 == 0) {
				regionCache.values().forEach(RegionStorage::saveToFile);
				regionCache.clear(); // Prevent excessive memory usage
				convertToNewPermaSave();
			}
		}
	}

	public void processQueuedChunks() {
		int processedCount = 0;
		while (processedCount < maxColumnsPerTick * 1000) {
			// Find the next task for the current pass
			ChunkTask task = null;
			for (ChunkTask t : processingQueue) {
				if (t.pass == currentPass) {
					task = t;
					break;
				}
			}
			if (task == null) {
				// No tasks for current pass, check if there are any for the next pass
				int nextPass = Integer.MAX_VALUE;
				for (ChunkTask t : processingQueue) {
					if (t.pass < nextPass) {
						nextPass = t.pass;
					}
				}
				if (nextPass == Integer.MAX_VALUE) {
					// No more tasks at all, done with the frame
					if (!ImageConverter.isUsingLargeImage) ImageCalculator.currentFrameComplete = true;

					if (CoordinateCalculator.coordinateMode == CoordinateCalculator.CoordinateMode.SINGLE ||
							CoordinateCalculator.coordinateMode == CoordinateCalculator.CoordinateMode.SINGLE_AT_LOCATION &&
							!ImageConverter.isUsingLargeImage){
						singleStopper = true;
					}
					return;
				} else {
					currentPass = nextPass;
					continue;
				}
			}
			processingQueue.remove(task);
			boolean full = true;

			if (task.pass != 999) {
				BlockPos blockPos;
				RegistryKey<World> dimensionKey = task.world.getRegistryKey();
				if (dimensionKey.equals(World.OVERWORLD)) {
					blockPos = new BlockPos(task.x, 319, task.z);
					full = processOverworldColumn(task, blockPos);
				} else if (dimensionKey.equals(World.NETHER)) {
					blockPos = new BlockPos(task.x, 255, task.z);
					full = processNetherColumn(task, blockPos);
				} else if (dimensionKey.equals(World.END)) {
					blockPos = new BlockPos(task.x, 255, task.z);
					full = processEndColumn(task, blockPos);
				}
			}
			processedCount += full ? 1000 : 1;

			if (task.pass == 999){
				ImageCalculator.currentFrameComplete = true;
				pauseTimer = tickCount;

				//RESETTING THIS IS SUPER IMPORTANT, I SPENT A WHOLE DAY DEBUGGING JUST TO REALIZE I NEED IT
				//This is because the logic checks if there are any other 999s available, which it should only do for
				// other stuff.
				currentPass = Integer.MAX_VALUE;

				if (CoordinateCalculator.coordinateMode == CoordinateCalculator.CoordinateMode.SINGLE ||
						CoordinateCalculator.coordinateMode == CoordinateCalculator.CoordinateMode.SINGLE_AT_LOCATION &&
						!ImageConverter.isUsingLargeImage){
					singleStopper = true;
				}
			}
		}
	}

	private boolean processOverworldColumn(ChunkTask task, BlockPos blockPos) {
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;
		if (hasPermaSave(task.world, blockPos)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, "OVERWORLD", task.pixelColor, task.pass);

		processPermaSave(task.world, blockPos);
		return true;
	}
	private boolean processNetherColumn(ChunkTask task, BlockPos blockPos) {
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;
		if (hasPermaSave(task.world, blockPos)) return false;

		// Destroy Nether Roof
		for (int y = 128; y >= 85; y--) {
			BlockPos pos = new BlockPos(task.x, y, task.z);
			if (!task.world.getBlockState(pos).isAir()) {
				task.world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
		WorldEditor.adjustColumn(task.world, task.x, task.z, "NETHER", task.pixelColor, task.pass);
		processPermaSave(task.world, blockPos);

		return true;
	}
	private boolean processEndColumn(ChunkTask task, BlockPos blockPos) {
		if (task.world.getPlayers().stream().noneMatch(player -> player.getWorld() == task.world)) return false;
		if (hasPermaSave(task.world, blockPos)) return false;

		WorldEditor.adjustColumn(task.world, task.x, task.z, "END", task.pixelColor, task.pass);
		processPermaSave(task.world, blockPos);
		return true;
	}

	public record ChunkTask(ServerWorld world, int x, int z, Color pixelColor, short pass) {
		//IDK why this works, but it autocorrected to record from class
		//I don't even know what a record is or how it works, but it somehow just works
		//It sounds kinda funny though "public record"
		//I HAVE YOU ON MY PUBLIC RECORD!!! WE CAUGHT YOU STEALING THE SECRET FORMULA!!
		//Its only 12:40 AM...

		//Oh hey I learned what records are
		//Future Lanse - I need to add a dimension holder for this
		//Nvm no I dont, it is already storing the world which can get the registry key
	}

	public record PermaTask(ServerWorld world, BlockPos pos) {}
	public record PermaTask2(ServerWorld world, BlockPos pos) {}

	//TODO - make this use structure voids too just to be safe.
	public static void processPermaSave(ServerWorld world, BlockPos blockPos) {
		if (!permaSave || !isModEnabled) return;

		// Only save if this block is the final one in the chunk.
		if ((blockPos.getX() & 15) != 15 || (blockPos.getZ() & 15) != 15) return;

		int chunkX = blockPos.getX() >> 4;
		int chunkZ = blockPos.getZ() >> 4;
		int regionX = chunkX >> 10;
		int regionZ = chunkZ >> 10;
		int localX = chunkX & 1023;
		int localZ = chunkZ & 1023;

		String dimensionId = getDimensionId(world);
		String regionKey = dimensionId + ":" + regionX + "," + regionZ;

		RegionStorage region = regionCache.computeIfAbsent(regionKey, key -> {
			Path regionFile = RegionStorage.getRegionFile(world.getServer().getSavePath(WorldSavePath.ROOT), dimensionId, regionX, regionZ);
			return new RegionStorage(regionFile);
		});
		region.saveChunk(localX, localZ);
	}

	private void convertToNewPermaSave() {
		while (!permaQueue.isEmpty()) {
			PermaTask task = permaQueue.poll();
			if (task == null) continue;

			ServerWorld world = task.world();
			RegistryKey<World> dimensionKey = world.getRegistryKey();
			int y = (dimensionKey.equals(World.OVERWORLD)) ? 319 : 255;
			BlockPos chunkPos = task.pos();
			int chunkX = chunkPos.getX() >> 4;
			int chunkZ = chunkPos.getZ() >> 4;

			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int i = 10; i >= 0; i--) { // Scan downwards from `topY`
						BlockPos currentPos = new BlockPos((chunkX << 4) + x, y, (chunkZ << 4) + z);

						if (world.getBlockState(currentPos).getBlock() == Blocks.STRUCTURE_VOID) {
							// Only remove if it's NOT in the (x mod 15, z mod 15) position
							if ((x & 15) != 15 || (z & 15) != 15) {
								world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 18);
							}
						}
					}
				}
			}
		}
		while (!permaQueue2.isEmpty()){
			PermaTask2 task = permaQueue2.poll();
			if (task == null) continue;

			ServerWorld world = task.world();
			RegistryKey<World> dimensionKey = world.getRegistryKey();
			int y = (dimensionKey.equals(World.OVERWORLD)) ? 319 : 255;
			BlockPos currentPos = new BlockPos(task.pos.getX(), y, task.pos.getZ());

			world.setBlockState(currentPos, Blocks.STRUCTURE_VOID.getDefaultState(), 18);
		}
	}

	public static boolean hasPermaSave(ServerWorld world, BlockPos blockPos) {
		int chunkX = blockPos.getX() >> 4;
		int chunkZ = blockPos.getZ() >> 4;
		int regionX = chunkX >> 10;
		int regionZ = chunkZ >> 10;
		int localX = chunkX & 1023;
		int localZ = chunkZ & 1023;

		String dimensionId = getDimensionId(world);
		String regionKey = dimensionId + ":" + regionX + "," + regionZ;

		RegionStorage region = regionCache.computeIfAbsent(regionKey, key -> {
			Path regionFile = RegionStorage.getRegionFile(world.getServer().getSavePath(WorldSavePath.ROOT), dimensionId, regionX, regionZ);
			return new RegionStorage(regionFile);
		});

		if (region.isChunkSaved(localX, localZ)) return true;

		if (world.getBlockState(blockPos).getBlock() == Blocks.STRUCTURE_VOID){
			if ((blockPos.getX() & 15) != 15 || (blockPos.getZ() & 15) != 15) return true;
			permaQueue.add(new PermaTask(world, blockPos));
			return true;
		}

		BlockPos lastBlockInChunk = new BlockPos((chunkX << 4) + 15, blockPos.getY(), (chunkZ << 4) + 15);
		if (world.getBlockState(lastBlockInChunk).getBlock() == Blocks.STRUCTURE_VOID) {
			return true;
		}

		// If blockPos = lastBlockInChunk and no structure void, place one and add to permaQueue
		if (permaSave) {
			if ((blockPos.getX() & 15) == 15 && (blockPos.getZ() & 15) == 15) {
				permaQueue.add(new PermaTask(world, blockPos));
				permaQueue2.add(new PermaTask2(world, blockPos));
				return false;
			}
		}
		return false;
	}

	private static String getDimensionId(ServerWorld world) {
		if (world.getRegistryKey().equals(World.OVERWORLD)) {
			return "0";
		} else if (world.getRegistryKey().equals(World.NETHER)) {
			return "-1";
		} else if (world.getRegistryKey().equals(World.END)) {
			return "1";
		}
		// Fallback for other dimensions:
		return "0";
	}

	public static Path getConfigFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("config/imageworld");
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