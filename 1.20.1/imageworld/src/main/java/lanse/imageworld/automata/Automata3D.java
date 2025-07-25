package lanse.imageworld.automata;

import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lanse.imageworld.imagecalculator.ImageConverter;
import lanse.imageworld.imagecalculator.worldpresets.SkyblockPreset;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;

public class Automata3D {
    public static final Map<ServerWorld, Set<BlockPos>> worldState = new HashMap<>();
    private static final Map<ServerWorld, AutomatonRule> worldRules = new HashMap<>();
    public static final Map<BlockPos, Integer> decayTimers = new HashMap<>();
    public static final Queue<InitializerTask> automataQueue = new LinkedList<>();
    private static AutomataTask currentTask = null;
    private static int ticksRemaining = 0;

    //for multithreading pain (DAEMON stops memory leaks from unused eternal threads)
    private static final ThreadFactory DAEMON_THREAD_FACTORY = runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    };
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), DAEMON_THREAD_FACTORY);
    private static final ExecutorService postTickExecutor = Executors.newSingleThreadExecutor(DAEMON_THREAD_FACTORY);

    private static final ThreadLocal<BlockPos.Mutable[]> MUTABLE_POOL = ThreadLocal.withInitial(() -> {
        int maxSize = 27; // fits all known neighborhoods
        BlockPos.Mutable[] pool = new BlockPos.Mutable[maxSize];
        for (int i = 0; i < maxSize; i++) pool[i] = new BlockPos.Mutable();
        return pool;
    });

    private static Future<Set<BlockPos>> nextTickFuture = null;
    public static AutomataType currentAutomataType = AutomataType.NORMAL;
    public static ServerPlayerEntity recentContext;
    public static boolean stopSimulation = false;
    public static boolean debug = false;
    public static boolean skyblockMode = false;
    public enum NeighborhoodType {
        MOOSE, VON_NEUMANN
    }
    public enum AutomataType {
        NORMAL, EXPLOSIVE, SKYBLOCK, VANISHING
    }

    public record AutomataTask(ServerWorld world, Set<BlockPos> startAlive, AutomatonRule rule, short maxTicks, BlockPos origin, Boolean forceSymmetry, int tickCounter) { }
    public record InitializerTask(ServerWorld world, Set<BlockPos> startAlive, AutomatonRule rule, short maxTicks, BlockPos origin, Boolean forceSymmetry, int tickCounter, AutomataType automataType) { }
    public record AutomatonRule(Set<Integer> survive, Set<Integer> spawn, int decayTicks, NeighborhoodType neighborhoodType) { }

    private static final Map<NeighborhoodType, int[]> NEIGHBORHOOD_TYPES = Map.of(
            NeighborhoodType.VON_NEUMANN, new int[] {
                    1, 0, 0,  -1, 0, 0,
                    0, 1, 0,   0, -1, 0,
                    0, 0, 1,   0, 0, -1
            },
            NeighborhoodType.MOOSE, new int[] {
                    -1, -1, -1,  0, -1, -1,  1, -1, -1,
                    -1,  0, -1,  0,  0, -1,  1,  0, -1,
                    -1,  1, -1,  0,  1, -1,  1,  1, -1,

                    -1, -1,  0,  0, -1,  0,  1, -1,  0,
                    -1,  0,  0,               1,  0,  0,
                    -1,  1,  0,  0,  1,  0,  1,  1,  0,

                    -1, -1,  1,  0, -1,  1,  1, -1,  1,
                    -1,  0,  1,  0,  0,  1,  1,  0,  1,
                    -1,  1,  1,  0,  1,  1,  1,  1,  1
            }
    );

    //stone palette can be used for making proper terrain
    public static final Block[] stonePalette = {
            Blocks.STONE, Blocks.STONE, Blocks.STONE
    };
    public static final Block[] rockPalette = {
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE,
            Blocks.TUFF, Blocks.STONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE,
            Blocks.TUFF, Blocks.STONE, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE,
            Blocks.TUFF, Blocks.STONE, Blocks.COPPER_ORE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE,
            Blocks.TUFF, Blocks.STONE, Blocks.IRON_ORE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE,
            Blocks.TUFF, Blocks.STONE, Blocks.EMERALD_ORE
    };
    public static final Block[] concretePalette = {
            Blocks.RED_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE,
            Blocks.GREEN_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.PURPLE_CONCRETE,
            Blocks.MAGENTA_CONCRETE, Blocks.PINK_CONCRETE
    };
    public static final Block[] terracottaPalette = {
            Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA,
            Blocks.GREEN_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.PURPLE_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA, Blocks.PINK_TERRACOTTA
    };
    public static final Block[] woolPalette = {
            Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.GREEN_WOOL,
            Blocks.CYAN_WOOL, Blocks.BLUE_WOOL, Blocks.PURPLE_WOOL, Blocks.MAGENTA_WOOL, Blocks.PINK_WOOL
    };
    public static final Block[] smallPalette = {
            Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.GREEN_WOOL,
            Blocks.BLUE_WOOL, Blocks.PURPLE_WOOL, Blocks.PINK_WOOL
    };
    public static final Block[] glassPalette = {
            Blocks.RED_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS
    };
    public static final Block[] combinedPalette = {
            Blocks.RED_CONCRETE, Blocks.RED_TERRACOTTA, Blocks.RED_WOOL,
            Blocks.ORANGE_CONCRETE, Blocks.ORANGE_TERRACOTTA, Blocks.ORANGE_WOOL,
            Blocks.YELLOW_CONCRETE, Blocks.YELLOW_TERRACOTTA, Blocks.YELLOW_WOOL,
            Blocks.LIME_CONCRETE, Blocks.LIME_TERRACOTTA, Blocks.LIME_WOOL,
            Blocks.GREEN_CONCRETE, Blocks.GREEN_TERRACOTTA, Blocks.GREEN_WOOL,
            Blocks.CYAN_CONCRETE, Blocks.CYAN_TERRACOTTA, Blocks.CYAN_WOOL,
            Blocks.BLUE_CONCRETE, Blocks.BLUE_TERRACOTTA, Blocks.BLUE_WOOL,
            Blocks.PURPLE_CONCRETE, Blocks.PURPLE_TERRACOTTA, Blocks.PURPLE_WOOL,
            Blocks.MAGENTA_CONCRETE, Blocks.MAGENTA_TERRACOTTA, Blocks.MAGENTA_WOOL,
            Blocks.PINK_CONCRETE, Blocks.PINK_TERRACOTTA, Blocks.PINK_WOOL
    };
    public static final Block[] woodPalette = {
            Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_WOOD, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.MANGROVE_WOOD,
            Blocks.JUNGLE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_PLANKS, Blocks.OAK_WOOD, Blocks.OAK_PLANKS,
            Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.STRIPPED_JUNGLE_WOOD,
            Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_PLANKS,
            Blocks.MANGROVE_PLANKS, Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_PLANKS,

            Blocks.STRIPPED_CHERRY_WOOD, Blocks.MANGROVE_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.STRIPPED_CRIMSON_HYPHAE,
            Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_PLANKS,
            Blocks.BIRCH_PLANKS, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_PLANKS, Blocks.OAK_WOOD,
            Blocks.SPRUCE_PLANKS, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.JUNGLE_WOOD, Blocks.MANGROVE_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD,
            Blocks.DARK_OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.DARK_OAK_PLANKS
    };

    public static final List<String> COLOR_PALLETS = Arrays.asList(
            "rock", "stone", "concrete", "terracotta", "wool", "full_rainbow", "small_rainbow", "glass", "wood"
    );
    public static String colorPallet = "rock";
    private static Block[] currentColorPalette = rockPalette;

    public static void queueAutomaton(ServerWorld world, Set<BlockPos> startAlive, AutomatonRule rule, short maxTicks, BlockPos origin, Boolean forceSymmetry, AutomataType automataType) {
        automataQueue.add(new InitializerTask(world, startAlive, rule, maxTicks, origin, forceSymmetry, 0, automataType));
    }

    //TODO - line blocker between fields and functions

    public static void tick() {
        if (stopSimulation) {
            if (currentAutomataType != AutomataType.NORMAL && currentTask != null) {
                Set<BlockPos> activeSnapshot = worldState.get(currentTask.world());
                AutomataTask task = currentTask; // avoid lambda weirdness
                postTickExecutor.submit(() -> finishSpecialAutomata(task, activeSnapshot));
            }
            clearAllSimulationState();
            return;
        }

        //I know its weird putting this before everything else but its good to get out of the way
        finishTick();

        // If in the middle of computing next state, wait
        if (nextTickFuture != null && !nextTickFuture.isDone()) return;

        if (currentTask == null) {
            if (!automataQueue.isEmpty()) {
                InitializerTask task = automataQueue.poll();
                currentTask = new AutomataTask(task.world, task.startAlive, task.rule, task.maxTicks, task.origin, task.forceSymmetry, task.tickCounter);
                initCurrentTask(task);
            }
            if (skyblockMode && recentContext != null) {
                Random random = new Random();
                if (random.nextInt(1200) == 25) {
                    if (debug) {
                        for (ServerPlayerEntity player : recentContext.getServerWorld().getPlayers()) {
                            player.sendMessage(Text.of("New Random Island!"));
                        }
                    }
                    ServerWorld world = recentContext.getServerWorld();
                    List<ServerPlayerEntity> players = world.getPlayers();

                    if (!players.isEmpty()) {
                        ServerPlayerEntity randomPlayer = players.get(random.nextInt(players.size()));
                        BlockPos playerPos = randomPlayer.getBlockPos();
                        BlockPos spawnPos = null;

                        for (int i = 0; i < 100; i++) {
                            int dx = random.nextInt(257) - 128;
                            int dz = random.nextInt(257) - 128;
                            int x = playerPos.getX() + dx;
                            int z = playerPos.getZ() + dz;

                            Chunk chunk = world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
                            if (chunk == null) continue;

                            int topY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 15, z & 15);
                            if (topY <= world.getBottomY()) {
                                // nothing in this column, valid void area
                                spawnPos = new BlockPos(x, 0, z);
                                break;
                            }
                        }
                        if (spawnPos != null) {
                            int y = random.nextInt(100);
                            spawnPos = new BlockPos(spawnPos.getX(), y, spawnPos.getZ());
                            clearAllSimulationState();
                            startRandomAutomaton(world, spawnPos, recentContext, (short) (random.nextInt(15) + 10), null, AutomataType.SKYBLOCK, false);
                        }
                    }
                }
            }
        }

        //I SWEAR TO FREAKING GOD IF CURRENT TASK IS NULL AND CRASHES MY GAME ONE MORE TIME IM GOING TO
        //THROW THIS LAPTOP INTO THE LAKE WHERE THE KING OF ENGLAND WAS GIVEN A SWORD BY A MERMAID BACK IN THE DAY
        //ITS CRASHED MY GAME SO MANY TIMES AT THIS POINT ITS SO INFURIATING
        //assert (currentTask != null); <--- it was wrong :sob:
        //AND THIS ASSERTION BETTER BE RIGHT OR I SWEAR I WILL CALL FOR PETER GRIFFIN TO GET IN THE HINDENPETER
        //AND CRASH IT INTO THE EIFFEL TOWER WHERE IT BELONGS WHERE FRENCH PEOPLE CAN GET ANGRY AS ME

        if (currentTask == null) return;

        // Check for death or tick‐limit
        if (ticksRemaining-- <= 0 || worldState.get(currentTask.world()).isEmpty() || worldState.get(currentTask.world()).size() == decayTimers.size()) {
            // If died too early, queue a retry
            if (ticksRemaining > 0) {
                startRandomAutomaton(currentTask.world, currentTask.origin, recentContext, currentTask.maxTicks, currentTask.forceSymmetry, currentAutomataType, true);
                ticksRemaining = currentTask.maxTicks();
            } else {
                if (currentAutomataType != AutomataType.NORMAL && currentTask != null) {
                    Set<BlockPos> activeSnapshot = worldState.get(currentTask.world());
                    AutomataTask taskSnapshot = currentTask; // avoid lambda weirdness
                    postTickExecutor.submit(() -> finishSpecialAutomata(taskSnapshot, activeSnapshot));
                }
                clearAllSimulationState();
            }
            return;
        }

        // Submit the next automaton logic step
        ServerWorld world = currentTask.world();
        nextTickFuture = executor.submit(() -> update(world, worldState.get(world), worldRules.get(world)));
    }

    // Update function has been extremely optimized because cellular automata are very laggy. I dont think I will remember
    // how to read this in a few days, because I went over every single line of code numerous times lol
    // TO ANYONE ELSE VIEWING THIS: GOOD LUCK READING IT!!! IT HAS BEEN OPTIMIZED BEYOND COMPREHENSION
    public static Set<BlockPos> update(ServerWorld world, Set<BlockPos> currentAlive, AutomatonRule rule) {
        if (currentTask != null && debug) {
            for (ServerPlayerEntity player : currentTask.world().getPlayers()) {
                player.sendMessage(Text.of("Tick! " + currentTask.tickCounter()));
            }
        }

        List<Runnable> blockChanges = Collections.synchronizedList(new ArrayList<>());
        Set<BlockPos> nextAlive = ConcurrentHashMap.newKeySet();
        ObjectOpenHashSet<BlockPos> aliveFast = new ObjectOpenHashSet<>(currentAlive);

        // Snapshot decay timers
        Map<BlockPos, Integer> decaySnapshot;
        synchronized (decayTimers) {
            decaySnapshot = Map.copyOf(decayTimers);
        }

        // Thread-safe neighbor counting
        List<BlockPos> aliveList = new ArrayList<>(currentAlive);
        int threads = Runtime.getRuntime().availableProcessors();
        List<Future<Object2IntOpenHashMap<BlockPos>>> futures = new ArrayList<>(threads);

        for (int t = 0; t < threads; t++) {
            final int start = t * aliveList.size() / threads;
            final int end = (t + 1) * aliveList.size() / threads;

            futures.add(executor.submit(() -> {
                Object2IntOpenHashMap<BlockPos> localMap = new Object2IntOpenHashMap<>();
                localMap.defaultReturnValue(0);

                BlockPos.Mutable[] pool = MUTABLE_POOL.get();

                int[] deltas = NEIGHBORHOOD_TYPES.get(rule.neighborhoodType);

                for (int i = start; i < end; i++) {
                    BlockPos pos = aliveList.get(i);
                    if (!decaySnapshot.containsKey(pos)) {
                        for (int j = 0; j < deltas.length; j += 3) {
                            BlockPos.Mutable neighbor = pool[j / 3].set(
                                    pos.getX() + deltas[j],
                                    pos.getY() + deltas[j + 1],
                                    pos.getZ() + deltas[j + 2]
                            );
                            localMap.addTo(neighbor.toImmutable(), 1);
                        }
                    }
                }
                return localMap;
            }));
        }

        // Merge neighbor counts
        Object2IntOpenHashMap<BlockPos> neighborCount = new Object2IntOpenHashMap<>();
        neighborCount.defaultReturnValue(0);

        for (Future<Object2IntOpenHashMap<BlockPos>> future : futures) {
            try {
                Object2IntOpenHashMap<BlockPos> local = future.get();
                for (Object2IntMap.Entry<BlockPos> entry : local.object2IntEntrySet()) {
                    neighborCount.addTo(entry.getKey(), entry.getIntValue());
                }
            } catch (InterruptedException | ExecutionException ignored) {}
        }

        Block spawnBlock = currentColorPalette[0].getDefaultState().getBlock();

        // Apply rules with inlined block updates
        for (Object2IntMap.Entry<BlockPos> entry : neighborCount.object2IntEntrySet()) {
            BlockPos pos = entry.getKey();
            int count = entry.getIntValue();
            boolean isAlive = aliveFast.contains(pos);

            if (isAlive) {
                if (rule.survive.contains(count)) {
                    nextAlive.add(pos);
                    blockChanges.add(new BlockChange(world, pos, spawnBlock));
                } else if (rule.decayTicks > 0) {
                    synchronized (decayTimers) {
                        decayTimers.put(pos, rule.decayTicks);
                    }
                } else {
                    blockChanges.add(new BlockChange(world, pos, Blocks.AIR));
                }
            } else if (rule.spawn.contains(count)) {
                nextAlive.add(pos);
                synchronized (decayTimers) {
                    decayTimers.remove(pos);
                }
                blockChanges.add(new BlockChange(world, pos, spawnBlock));
            }
        }

        // Handle existing decays
        int decayThreads = Runtime.getRuntime().availableProcessors();
        List<BlockPos> decayingPositions = new ArrayList<>(decaySnapshot.keySet());
        List<Future<?>> decayFutures = new ArrayList<>(decayThreads);

        for (int t = 0; t < decayThreads; t++) {
            final int start = t * decayingPositions.size() / decayThreads;
            final int end = (t + 1) * decayingPositions.size() / decayThreads;

            decayFutures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    BlockPos pos = decayingPositions.get(i);
                    int ticksLeft = decaySnapshot.get(pos) - 1;

                    if (ticksLeft <= 0) {
                        synchronized (decayTimers) {
                            decayTimers.remove(pos);
                        }
                        blockChanges.add(new BlockChange(world, pos, Blocks.AIR));
                    } else {
                        synchronized (decayTimers) {
                            decayTimers.put(pos, ticksLeft);
                        }
                        nextAlive.add(pos); // still considered alive
                        int paletteIndex = 1 + (ticksLeft % (currentColorPalette.length - 1));
                        blockChanges.add(new BlockChange(world, pos, currentColorPalette[paletteIndex].getDefaultState().getBlock()));
                    }
                }
            }));
        }

        // Wait for all decay threads to finish
        for (Future<?> future : decayFutures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ignored) {}
        }

        // Group block changes by chunk for safe parallel writes
        Map<ChunkPos, List<Runnable>> chunkToChanges = new ConcurrentHashMap<>();

        for (Runnable change : blockChanges) {
            if (change instanceof BlockChange bc) {
                chunkToChanges.computeIfAbsent(bc.chunkPos, k -> Collections.synchronizedList(new ArrayList<>())).add(change);
            } else {
                change.run(); //fallback
            }
        }

        // Submit one task per chunk
        List<Future<?>> blockFutures = new ArrayList<>();
        for (List<Runnable> chunkChanges : chunkToChanges.values()) {
            blockFutures.add(executor.submit(() -> {
                for (Runnable change : chunkChanges) {
                    change.run();
                }
            }));
        }

        // Wait for all block placements
        for (Future<?> f : blockFutures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException ignored) {}
        }
        return nextAlive;
    }

    public static void finishTick() {
        if (nextTickFuture != null && nextTickFuture.isDone()) {
            try {
                Set<BlockPos> nextAlive = nextTickFuture.get();
                ServerWorld world = currentTask.world();
                worldState.put(world, nextAlive);

                currentTask = new AutomataTask(
                        currentTask.world(), nextAlive, currentTask.rule(),
                        currentTask.maxTicks(), currentTask.origin(), currentTask.forceSymmetry(),
                        currentTask.tickCounter() + 1);

            } catch (Exception ignored) {}
            finally {
                nextTickFuture = null;
            }
        }
    }

    private static void fastUnsafeSetBlock(ServerWorld world, BlockPos pos, Block block) {
        Chunk chunk = world.getChunk(pos);
        if (!chunk.getBlockState(pos).isOf(block)) {
            chunk.setBlockState(pos, block.getDefaultState(), false); // false = no light/neighbor updates
            world.getChunkManager().markForUpdate(pos); // Triggers client resend
        }
    }

    private static void clearAllSimulationState() {
        worldState.clear();
        worldRules.clear();
        automataQueue.clear();
        decayTimers.clear();
        if (nextTickFuture != null) nextTickFuture.cancel(true);
        currentTask = null;
        stopSimulation = false;
    }

    public static void startRandomAutomaton(ServerWorld world, BlockPos origin, ServerPlayerEntity context, short maxTicks, Boolean forceSymmetry, AutomataType automataType, boolean regenerateCurrentTask) {
        Random random = new Random();
        int size = 2 + random.nextInt(10);
        boolean isFull = forceSymmetry != null ? forceSymmetry : random.nextFloat() < 0.2f;

        Set<BlockPos> startAlive = new HashSet<>();
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                for (int dz = 0; dz < size; dz++) {
                    BlockPos pos = origin.add(dx, dy, dz);
                    if (isFull || random.nextBoolean()) {
                        world.setBlockState(pos, currentColorPalette[0].getDefaultState());
                        startAlive.add(pos);
                    }
                }
            }
        }
        int numSpawn = 1 + random.nextInt(6);
        int numSurvive = 1 + random.nextInt(6);
        Set<Integer> spawn = new HashSet<>();
        Set<Integer> survive = new HashSet<>();
        while (spawn.size() < numSpawn) spawn.add(random.nextInt(27));
        while (survive.size() < numSurvive) survive.add(random.nextInt(27));

        int decayTicks = random.nextInt(11);
        if (random.nextInt(100) < 25) decayTicks = random.nextInt(100);

        NeighborhoodType neighborhood = random.nextBoolean() ? NeighborhoodType.MOOSE : NeighborhoodType.VON_NEUMANN;
        AutomatonRule rule = new AutomatonRule(survive, spawn, decayTicks, neighborhood);

        recentContext = context;
        if (debug) context.sendMessage(Text.of(spawn + "/" + survive + "/" + decayTicks + "/" + neighborhood.name().toLowerCase() + "/" + automataType.name().toLowerCase()));

        if (regenerateCurrentTask){
            currentTask = new AutomataTask(world, startAlive, rule, maxTicks, origin, forceSymmetry, 0);
            worldState.clear();
            worldRules.clear();
            decayTimers.clear();
            if (nextTickFuture != null) nextTickFuture.cancel(true);
            worldState.put(currentTask.world(), new HashSet<>(startAlive));
            worldRules.put(currentTask.world(), rule);
            ticksRemaining = currentTask.maxTicks();
        } else {
            queueAutomaton(world, startAlive, rule, maxTicks, origin, forceSymmetry, automataType);
        }
    }

    public static int executeAutomatonCreate(CommandContext<ServerCommandSource> ctx, String sym, AutomataType automataType) {
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.getPlayer();

        // Get origin 20 blocks in front of the player
        assert player != null;
        Vec3d eyePos = player.getEyePos(); // Eye position for accuracy
        float pitch = player.getPitch();
        float yaw = player.getYaw();

        // Convert pitch/yaw to a normalized direction vector
        double pitchRad = Math.toRadians(-pitch);
        double yawRad = Math.toRadians(-yaw);
        double x = Math.cos(pitchRad) * Math.sin(yawRad);
        double y = Math.sin(pitchRad);
        double z = Math.cos(pitchRad) * Math.cos(yawRad);

        Vec3d direction = new Vec3d(x, y, z).normalize().multiply(20);
        Vec3d targetPos = eyePos.add(direction);
        BlockPos origin = BlockPos.ofFloored(targetPos);
        short maxTicks = (short) IntegerArgumentType.getInteger(ctx, "maxTicks");

        //Boolean wrapper non primitive for optional randomization
        Boolean forceSymmetry = switch (sym.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> null; // randomized
        };

        Automata3D.startRandomAutomaton(world, origin, source.getPlayer(), maxTicks, forceSymmetry, automataType, false);
        return 1;
    }

    private static void initCurrentTask(InitializerTask task) {
        worldState.put(currentTask.world(), new HashSet<>(currentTask.startAlive()));
        worldRules.put(currentTask.world(), currentTask.rule());
        ticksRemaining = currentTask.maxTicks();
        currentAutomataType = task.automataType;
    }

    private static void finishSpecialAutomata(AutomataTask currentTask, Set<BlockPos> active) {
        ServerWorld world = currentTask.world();

        if (debug) {
            for (ServerPlayerEntity player : currentTask.world().getPlayers()) {
                player.sendMessage(Text.of("End! " + active.size()));
            }
        }

        switch (currentAutomataType) {
            case EXPLOSIVE -> {
                BlockPos redstoneCenter = null;
                for (BlockPos pos : active) {
                    fastUnsafeSetBlock(world, pos, Blocks.TNT);
                    redstoneCenter = pos; // Last one becomes the center
                }
                if (redstoneCenter != null) {
                    int cx = redstoneCenter.getX();
                    int cy = redstoneCenter.getY();
                    int cz = redstoneCenter.getZ();

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos target = new BlockPos(cx + dx, cy, cz + dz);
                            world.setBlockState(target, Blocks.REDSTONE_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
                        }
                    }
                }
                if (debug) {
                    for (ServerPlayerEntity player : currentTask.world().getPlayers()) {
                        player.sendMessage(Text.of("TNT"));
                    }
                }
            }
            case VANISHING -> { for (BlockPos pos : active) fastUnsafeSetBlock(world, pos, Blocks.AIR); }
            case SKYBLOCK -> {
                // Group blocks by column (x, z) and track the highest y for each
                Map<Long, BlockPos> columnMap = new HashMap<>(); // packed x/z to BlockPos
                Color pixelColor = ImageConverter.ColorCategory.GREEN.color;
                boolean firstColorFound = false;

                for (BlockPos pos : active) {
                    long key = (((long) pos.getX()) << 32) | (pos.getZ() & 0xFFFFFFFFL);
                    columnMap.compute(key, (k, existing) -> (existing == null || pos.getY() > existing.getY()) ? pos : existing);
                    if (!firstColorFound){
                        pixelColor = ImageConverter.getColorAt(pos.getX(), pos.getZ(), true);
                        firstColorFound = true;
                    }
                }

                List<BlockPos> surfaceBlocks = new ArrayList<>(columnMap.values());

                for (BlockPos pos : surfaceBlocks){
                    if (pixelColor == null) pixelColor = ImageConverter.ColorCategory.GREEN.color;
                    SkyblockPreset.overworldLogic(world, pos.getX(), pos.getZ(), pixelColor);
                }

                if (debug) {
                    for (ServerPlayerEntity player : currentTask.world().getPlayers()) {
                        player.sendMessage(Text.of("Skyblock"));
                    }
                }
            }
        }
    }

    public static void setColorPalette(String palette) {

        switch (palette) {
            case "rock" -> {
                currentColorPalette = rockPalette;
                Automata3D.colorPallet = palette;
            }
            case "stone" -> {
                currentColorPalette = stonePalette;
                Automata3D.colorPallet = palette;
            }
            case "concrete" -> {
                currentColorPalette = concretePalette;
                Automata3D.colorPallet = palette;
            }
            case "terracotta" -> {
                currentColorPalette = terracottaPalette;
                Automata3D.colorPallet = palette;
            }
            case "wool" -> {
                currentColorPalette = woolPalette;
                Automata3D.colorPallet = palette;
            }
            case "full_rainbow" -> {
                currentColorPalette = combinedPalette;
                Automata3D.colorPallet = palette;
            }
            case "small_rainbow" -> {
                currentColorPalette = smallPalette;
                Automata3D.colorPallet = palette;
            }
            case "glass" -> {
                currentColorPalette = glassPalette;
                Automata3D.colorPallet = palette;
            }
            case "wood" -> {
                currentColorPalette = woodPalette;
                Automata3D.colorPallet = palette;
            }
        }
    }

    static class BlockChange implements Runnable {
        final ServerWorld world;
        final BlockPos pos;
        final ChunkPos chunkPos;
        final Block block;

        BlockChange(ServerWorld world, BlockPos pos, Block block) {
            this.world = world;
            this.pos = pos;
            this.chunkPos = new ChunkPos(pos);
            this.block = block;
        }

        @Override
        public void run() {
            fastUnsafeSetBlock(world, pos, block);
        }
    }
}