package lanse.imageworld.automata;

import lanse.imageworld.ImageWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class LavaCaster {

    public static Random random = new Random();
    public static boolean isLavaCasterOn = false;
    private static int currentY;
    public static int maxSpeed = 750;
    public static LavaCasterTask lavaCasterTask;
    public static List<LavaCasterTask> lavaQueue = new ArrayList<>();
    public static List<InitializerTask> initializerQueue = new ArrayList<>();
    public static final Set<BlockPos> seen = new HashSet<>();

    public record InitializerTask(ServerWorld world, BlockPos pos, boolean alreadyBeenQueued, LavaCastType type, int rule, int age) {}
    public record LavaCasterTask(ServerWorld world, BlockPos pos, LavaCastType type, int rule, int age) {}

    public enum LavaCastType {
        LAVACAST, MOSSYCAST, MESACAST, SANDCAST, OBSIDICAST
    }

    public static void initializeLavaCast(ServerWorld world, BlockPos pos, LavaCastType type, int rule, int age) {
        if (rule == -40404) {
            rule = random.nextInt(1 << 9); // random rule (0..511)
        }

        if (age == -40404) age = random.nextInt(100);

        if (initializerQueue.isEmpty() && lavaQueue.isEmpty()) {
            // If nothing is running, start immediately
            lavaCasterTask = new LavaCasterTask(world, pos, type, rule, age);
            lavaQueue.add(lavaCasterTask);
            currentY = pos.getY();
            seen.clear();
            seen.add(pos);
            maxSpeed = ImageWorld.maxColumnsPerTick * 100;
        } else {
            // Otherwise queue as an initializer
            initializerQueue.add(new InitializerTask(world, pos, true, type, rule, age));
        }
        isLavaCasterOn = true;
    }

    public static void tick() {
        if (!isLavaCasterOn) return;

        // If we have no lava‐tasks but do have an awaiting initializer, start it now
        if (lavaQueue.isEmpty() && !initializerQueue.isEmpty()) {
            InitializerTask init = initializerQueue.remove(0);
            currentY = init.pos.getY();
            lavaQueue.add(new LavaCasterTask(init.world, init.pos, init.type, init.rule, init.age));
            seen.clear();
            seen.add(init.pos);
            maxSpeed = ImageWorld.maxColumnsPerTick * 100;
        }

        // If theres still nothing to run, we’re done
        if (lavaQueue.isEmpty()) {
            isLavaCasterOn = false;
            seen.clear();
            return;
        }

        // Pull up to maxSpeed tasks at the current Y level
        List<LavaCasterTask> currentLevelTasks = new ArrayList<>();
        Iterator<LavaCasterTask> iter = lavaQueue.iterator();
        while (iter.hasNext()) {
            LavaCasterTask t = iter.next();
            if (t.pos.getY() == currentY) {
                currentLevelTasks.add(t);
                iter.remove();
                if (currentLevelTasks.size() >= maxSpeed) break;
            }
        }

        // If nothing at this Y-level, move down one
        if (currentLevelTasks.isEmpty()) {
            currentY--;
            return;
        }

        // Process each task at this level
        for (LavaCasterTask task : currentLevelTasks) {
            ServerWorld world = task.world;
            BlockPos pos = task.pos;
            LavaCastType type = task.type;
            int rule = task.rule;
            int age = task.age;

            int pattern = getCobblePattern(world, pos);
            boolean placeSolid = ((rule >> pattern) & 1) == 1;
            BlockState toPlace = Blocks.AIR.getDefaultState();

            switch (type) {
                case LAVACAST -> // Regular: place Cobblestone or Air
                        toPlace = placeSolid ? Blocks.COBBLESTONE.getDefaultState() : Blocks.AIR.getDefaultState();

                case MOSSYCAST -> {
                    if (placeSolid) {
                        // Some fraction of cobblestone becomes mossy depending on age
                        // e.g. age=0→0% mossy; age=100→100% mossy
                        double mossChance = Math.min(1.0, age / 100.0);
                        if (world.getRandom().nextDouble() < mossChance) {
                            toPlace = Blocks.MOSSY_COBBLESTONE.getDefaultState();
                        } else {
                            toPlace = Blocks.COBBLESTONE.getDefaultState();
                        }

                        // After placing a cobblestone/mossy block, occasionally grow vines:
                        // Check all four horizontal neighbors; if neighbor is air, put vines facing correctly
                        if (!toPlace.isAir() && world.getRandom().nextInt(100) < 10) {
                            // 10% chance per block to attempt vine growth
                            for (var horizontal : List.of(
                                    BlockPos.ORIGIN.north(), BlockPos.ORIGIN.south(),
                                    BlockPos.ORIGIN.east(), BlockPos.ORIGIN.west())) {

                                BlockPos neighbor = pos.add(horizontal.getX(), 0, horizontal.getZ());
                                if (world.isAir(neighbor)) {
                                    // Place a single vine block facing “towards” our source
                                    // e.g. if neighbor = pos.north(), then the vine needs the “south” property
                                    var vineState = Blocks.VINE.getDefaultState()
                                            .with(Properties.NORTH, horizontal.getZ() == 1)  // if we are north→ place facing south
                                            .with(Properties.SOUTH, horizontal.getZ() == -1)
                                            .with(Properties.EAST, horizontal.getX() == -1)
                                            .with(Properties.WEST, horizontal.getX() == 1);
                                    world.setBlockState(neighbor, vineState);
                                }
                            }
                        }
                    }
                }

                case MESACAST -> // Completely ignore “placeSolid” logic. Instead, pick a mesa‐style block at this Y.
                        toPlace = getMesaBlock(world, pos).getDefaultState();

                case SANDCAST -> // If automaton says “solid”, place sandstone; otherwise air
                        toPlace = placeSolid ? Blocks.SANDSTONE.getDefaultState() : Blocks.AIR.getDefaultState();

                case OBSIDICAST -> {
                    if (placeSolid) {
                        // If age < 20 → pure obsidian; if age > 80 → mostly crying obsidian
                        // ramp linearly in between
                        double cryChance = Math.max(0.0, (age - 20) / 60.0);
                        if (world.getRandom().nextDouble() < cryChance) {
                            toPlace = Blocks.CRYING_OBSIDIAN.getDefaultState();
                        } else {
                            toPlace = Blocks.OBSIDIAN.getDefaultState();
                        }
                    }
                }
            }

            world.setBlockState(pos, toPlace);

            if (toPlace != Blocks.AIR.getDefaultState()) {
                int belowY = pos.getY() - 1;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos below = new BlockPos(pos.getX() + dx, belowY, pos.getZ() + dz);
                        if (world.getBlockState(below).isAir() && seen.add(below)) {
                            lavaQueue.add(new LavaCasterTask(world, below, type, rule, age));
                        }
                    }
                }
            }
            //old lavacasts are more destroyed than young ones (excluding obsidicasts)
            if (age > 10 && type != LavaCastType.OBSIDICAST) {
                if (random.nextInt(200000) < age * 3) {
                    world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), ((float) age / 5) + 3, World.ExplosionSourceType.MOB);
                }
            }
        }

        // If there are no more tasks at this Y, drop down one
        boolean anyLeftAtThisY = lavaQueue.stream().anyMatch(task -> task.pos.getY() == currentY);
        if (!anyLeftAtThisY) currentY--;
    }

    private static int getCobblePattern(ServerWorld world, BlockPos pos) {
        int pattern = 0;
        int bitIndex = 0;
        int yAbove = pos.getY() + 1;

        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                BlockPos p = new BlockPos(pos.getX() + dx, yAbove, pos.getZ() + dz);
                Block b = world.getBlockState(p).getBlock();
                if (b == Blocks.COBBLESTONE) {
                    pattern |= (1 << bitIndex);
                }
                bitIndex++;
            }
        }
        return pattern;
    }

    public static Block getMesaBlock(ServerWorld world, BlockPos pos) {
        Block currentMesaBlock;
        Random random = new Random();

        currentMesaBlock = Blocks.TERRACOTTA;
        if (pos.getY() % 5 == 0) currentMesaBlock = Blocks.ORANGE_TERRACOTTA;
        if (pos.getY() % 6 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
        if (pos.getY() % 8 == 0) currentMesaBlock = Blocks.WHITE_TERRACOTTA;
        if (pos.getY() % 9 == 0) currentMesaBlock = Blocks.CYAN_TERRACOTTA;
        if (pos.getY() % 11 == 0) currentMesaBlock = Blocks.BLACK_TERRACOTTA;
        if (pos.getY() % 12 == 0) currentMesaBlock = Blocks.TERRACOTTA;
        if (pos.getY() % 14 == 0) currentMesaBlock = Blocks.RED_TERRACOTTA;
        if (pos.getY() % 17 == 0) currentMesaBlock = Blocks.ORANGE_TERRACOTTA;
        if (pos.getY() % 23 == 0) currentMesaBlock = Blocks.YELLOW_TERRACOTTA;

        BlockPos above = pos.up();
        if (random.nextInt(100) > 96 && !world.isAir(above)) {
            currentMesaBlock = Blocks.TERRACOTTA;
        }

        return currentMesaBlock;
    }
}