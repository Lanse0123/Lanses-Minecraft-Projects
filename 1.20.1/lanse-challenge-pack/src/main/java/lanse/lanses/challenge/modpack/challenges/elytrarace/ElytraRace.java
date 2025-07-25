package lanse.lanses.challenge.modpack.challenges.elytrarace;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ElytraRace {

    private static int tickCount = 0;
    public static boolean isRacing = false;
    public static boolean raceBegun = false;
    private static boolean canPlaceLanterns = true;
    private static float originalDirection = 0.0f;
    private static TurnState currentTurn = TurnState.STRAIGHT;
    private static int turnTicksRemaining = 0;
    private static ArmorStandEntity raceBeacon;
    private static Map<UUID, Double> playerScores;
    private static Set<UUID> eliminatedPlayers;
    private static ServerWorld raceWorld;
    private static final int COUNTDOWN_START_TICKS = 100;
    private static final int COUNTDOWN_INTERVAL = 20;
    private static final int RACE_DISTANCE_LIMIT = 1500;
    private static int raceLength = 0;
    private enum TurnState {
        STRAIGHT, LEFT, RIGHT, UP, DOWN, RESURFACE
    }

    public static void tick(MinecraftServer server) {
        if (!isRacing) return;

        tickCount++;

        if (!raceBegun) {
            if (tickCount % COUNTDOWN_INTERVAL == 0 && tickCount <= COUNTDOWN_START_TICKS) {
                int secondsLeft = (COUNTDOWN_START_TICKS - tickCount) / COUNTDOWN_INTERVAL + 1;
                if (secondsLeft >= 1 && secondsLeft <= 5) {
                    broadcastChat(server, Text.literal(String.valueOf(secondsLeft)));
                }
            }
            if (tickCount == COUNTDOWN_START_TICKS + 1) {
                raceBegun = true;
                initializePlayerScores(server);
                broadcastChat(server, Text.literal("GO!"));
            }
            return;
        }

        //actual race logic
        canPlaceLanterns = true;
        updatePlayerScores();
        advanceRaceBeacon();
        if (allPlayersEliminatedOrFinished()) endRace(server);
    }

    private static void advanceRaceBeacon() {
        if (raceBeacon == null) return;

        Vec3d beaconPos = raceBeacon.getPos();

        ServerPlayerEntity nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (UUID uuid : playerScores.keySet()) {
            if (eliminatedPlayers.contains(uuid)) continue;
            ServerPlayerEntity player = raceWorld.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            double distance = player.getPos().distanceTo(beaconPos);
            if (distance < bestDist) {
                bestDist = distance;
                nearest = player;
            }
        }
        if (nearest == null) return;

        double moveDelta;
        if (bestDist <= 5) moveDelta = 3.0;
        else if (bestDist <= 20) moveDelta = 2.0;
        else if (bestDist <= 85) moveDelta = 1.0;
        else return;

        if (turnTicksRemaining <= 0) {
            if (currentTurn == TurnState.UP || currentTurn == TurnState.DOWN || raceBeacon.getY() > 200 || raceBeacon.getY() < 0) {
                currentTurn = TurnState.RESURFACE;
            } else {
                currentTurn = TurnState.values()[new Random().nextInt(TurnState.values().length)];
            }
            turnTicksRemaining = 25 + new Random().nextInt(76);
        }
        turnTicksRemaining--;

        float yaw = raceBeacon.getYaw();
        float illegalDirection = (originalDirection + 180f) % 360f;
        Vec3d pos = raceBeacon.getPos();

        for (int i = 0; i < (int) moveDelta; i++) {
            switch (currentTurn) {
                case LEFT -> {
                    yaw = (yaw - 1f + 360f) % 360f;
                    float angleDiff = Math.abs(MathHelper.wrapDegrees(yaw - illegalDirection));
                    if (angleDiff < 90f) {
                        yaw = (illegalDirection + (yaw > illegalDirection ? 90f : -90f) + 360f) % 360f;
                    }
                }
                case RIGHT -> {
                    yaw = (yaw + 1f + 360f) % 360f;
                    float angleDiff = Math.abs(MathHelper.wrapDegrees(yaw - illegalDirection));
                    if (angleDiff < 90f) {
                        yaw = (illegalDirection + (yaw > illegalDirection ? 90f : -90f) + 360f) % 360f;
                    }
                }
            }

            double rad = Math.toRadians(yaw);
            Vec3d forward = new Vec3d(-Math.sin(rad), 0, Math.cos(rad)).normalize();

            switch (currentTurn) {
                case UP -> pos = pos.add(forward.x, 1, forward.z);
                case DOWN -> pos = pos.add(forward.x, -1, forward.z);
                case STRAIGHT, LEFT, RIGHT -> pos = pos.add(forward);
                case RESURFACE -> {
                    BlockPos checkPos = BlockPos.ofFloored(pos);

                    // Scan downward to find highest non-road block
                    BlockPos top = raceWorld.getTopPosition(Heightmap.Type.WORLD_SURFACE, checkPos);
                    while (top.getY() > raceWorld.getBottomY()) {
                        Block block = raceWorld.getBlockState(top).getBlock();
                        if (block != Blocks.GRAY_CONCRETE && block != Blocks.IRON_BLOCK && block != Blocks.LANTERN && block != Blocks.AIR) {
                            break; // Found the real surface
                        }
                        top = top.down();
                    }
                    int targetY = top.getY() + 3;
                    double dy = pos.y < targetY ? 1 : (pos.y > targetY ? -1 : 0);
                    pos = pos.add(forward.x, dy, forward.z);
                }
            }
            layTrackAt(BlockPos.ofFloored(pos), yaw);
        }
        raceBeacon.teleport(pos.x, pos.y, pos.z);
        raceBeacon.setYaw(yaw);
    }

    private static void layTrackAt(BlockPos center, float direction) {
        double radians = Math.toRadians(direction);
        Vec3d forward = new Vec3d(Math.cos(radians), 0, Math.sin(radians));
        boolean isEastWest = Math.abs(forward.x) > Math.abs(forward.z);

        //WhAt ThE SiGmUh
        forward = isEastWest
                ? new Vec3d(Math.signum(forward.x), 0, 0)
                : new Vec3d(0, 0, Math.signum(forward.z));

        Vec3d right = new Vec3d(-forward.z, 0, forward.x);

        int halfWidth = 0;   // 1 block wide
        int halfLength = 4;  // 8 blocks long (−4 to +4)
        int cx = center.getX();
        int y = center.getY();
        int cz = center.getZ();

        // Clear vertical space (air volume)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -halfLength; dz <= halfLength; dz++) {
                Vec3d offset = right.multiply(dx).add(forward.multiply(dz));
                for (int dy = 0; dy <= 5; dy++) {
                    BlockPos clearPos = new BlockPos(cx + (int) offset.x, y + dy + 1, cz + (int) offset.z);
                    if (raceWorld.getBlockState(clearPos).getBlock() != Blocks.GRAY_CONCRETE) {
                        raceWorld.setBlockState(clearPos, Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }

        // Gray concrete road
        for (int dx = -halfWidth; dx <= halfWidth; dx++) {
            for (int dz = -halfLength; dz <= halfLength; dz++) {
                Vec3d offset = right.multiply(dx).add(forward.multiply(dz));
                BlockPos roadPos = new BlockPos(cx + (int) offset.x, y, cz + (int) offset.z);
                raceWorld.setBlockState(roadPos, Blocks.GRAY_CONCRETE.getDefaultState(), 3);
            }
        }

        // Iron block borders
        int[] ends = {-halfLength, +halfLength};
        for (int dz : ends) {
            Vec3d leftEdge = right.multiply(-1).add(forward.multiply(dz));
            Vec3d rightEdge = right.multiply(+1).add(forward.multiply(dz));

            BlockPos leftPos = new BlockPos(cx + (int) leftEdge.x, y, cz + (int) leftEdge.z);
            BlockPos rightPos = new BlockPos(cx + (int) rightEdge.x, y, cz + (int) rightEdge.z);

            raceWorld.setBlockState(leftPos, Blocks.IRON_BLOCK.getDefaultState(), 3);
            raceWorld.setBlockState(rightPos, Blocks.IRON_BLOCK.getDefaultState(), 3);
        }

        // Decorative lanterns on border every 4 ticks
        if (tickCount % 4 == 0 && canPlaceLanterns) {
            for (int dz : ends) {
                Vec3d left = right.multiply(-1).add(forward.multiply(dz));
                Vec3d rightV = right.multiply(+1).add(forward.multiply(dz));

                BlockPos l1Pos = new BlockPos(cx + (int) left.x, y + 1, cz + (int) left.z);
                BlockPos l2Pos = new BlockPos(cx + (int) rightV.x, y + 1, cz + (int) rightV.z);

                raceWorld.setBlockState(l1Pos, Blocks.LANTERN.getDefaultState(), 3);
                raceWorld.setBlockState(l2Pos, Blocks.LANTERN.getDefaultState(), 3);
            }
            canPlaceLanterns = false;
        }
    }


    private static void broadcastChat(MinecraftServer server, Text message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message, false); // false = not a system message (it shows with their chat settings)
        }
    }

    private static void initRaceBeacon(ServerWorld world, BlockPos startPos) {
        raceWorld = world;

        if (raceWorld == null) return;

        raceBeacon = new ArmorStandEntity(raceWorld, startPos.getX(), startPos.getY(), startPos.getZ());
        raceBeacon.setInvulnerable(true);
        raceBeacon.setNoGravity(true);
        raceBeacon.setGlowing(true);
        raceBeacon.setCustomName(Text.literal("RaceBeacon"));
        raceBeacon.setCustomNameVisible(false);
        raceBeacon.equipStack(EquipmentSlot.CHEST, Items.ELYTRA.getDefaultStack());
        raceWorld.spawnEntity(raceBeacon);
    }

    private static void initializePlayerScores(MinecraftServer server) {
        // Add every online player into playerScores with 0.0
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            playerScores.put(player.getUuid(), 0.0);
        }
    }

    private static void updatePlayerScores() {
        if (raceBeacon == null) return;

        List<ServerPlayerEntity> online = raceWorld.getServer().getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player : online) {
            UUID uuid = player.getUuid();
            if (eliminatedPlayers.contains(uuid)) continue;

            double dist = player.getPos().distanceTo(raceBeacon.getPos());
            if (dist > RACE_DISTANCE_LIMIT) {
                eliminatedPlayers.add(uuid);
                player.sendMessage(Text.literal("You were too far (>" + RACE_DISTANCE_LIMIT + "). Eliminated!"), false);
                continue;
            }

            double delta = dist / 10.0;
            playerScores.put(uuid, playerScores.get(uuid) + delta);

            //give the player a rocket each second because why not lol
            if (tickCount % 20 == 0){
                ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
                NbtCompound nbt = new NbtCompound();
                NbtCompound fireworks = new NbtCompound();
                fireworks.putByte("Flight", (byte) 3);
                nbt.put("Fireworks", fireworks);
                rocket.setNbt(nbt);
                player.getInventory().insertStack(rocket);
            }
        }
    }

    private static boolean allPlayersEliminatedOrFinished() {
        if (tickCount > raceLength) return true;

        for (UUID uuid : playerScores.keySet()) {
            if (!eliminatedPlayers.contains(uuid)) {
                ServerPlayerEntity p = raceWorld.getServer().getPlayerManager().getPlayer(uuid);
                if (p != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void endRace(MinecraftServer server) {
        List<Map.Entry<UUID, Double>> ranking = playerScores.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue() / tickCount))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .toList();

        //this happens after ranking the scores so I dont accidentally divide by 0 lol
        isRacing = false;
        raceBegun = false;
        tickCount = 0;

        server.getPlayerManager().getPlayerList().forEach(p -> {
            p.sendMessage(Text.literal("=== Race Results ==="), false);
            for (int i = 0; i < ranking.size(); i++) {
                UUID u = ranking.get(i).getKey();
                double score = ranking.get(i).getValue();
                String name = Optional.ofNullable(server.getPlayerManager().getPlayer(u))
                        .map(PlayerEntity::getEntityName)
                        .orElse("Unknown");
                p.sendMessage(Text.literal((i+1) + ". " + name + ": " + String.format("%.1f", score)), false);
            }
        });

        if (raceBeacon != null && !raceBeacon.isRemoved()) {
            raceBeacon.kill();
            raceBeacon.remove(Entity.RemovalReason.DISCARDED);
        }
        playerScores.clear();
        eliminatedPlayers.clear();
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("LCP_ElytraRace_START").requires(src -> src.hasPermissionLevel(2))
                .then(argument("seconds", IntegerArgumentType.integer(1)).executes(ctx -> {
                    int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    assert player != null;
                    originalDirection = player.getYaw();
                    raceLength = seconds * 20;
                    tickCount = 0;
                    isRacing = true;
                    raceBegun = false;
                    currentTurn = TurnState.STRAIGHT;
                    turnTicksRemaining = 160;
                    playerScores = new LinkedHashMap<>();
                    eliminatedPlayers = new HashSet<>();
                    MainControl.modPreset = MainControl.Preset.ELYTRARACE;
                    MainControl.isModEnabled = true;
                    initRaceBeacon(ctx.getSource().getWorld().toServerWorld(), Objects.requireNonNull(ctx.getSource().getPlayer()).getBlockPos());
                    ctx.getSource().sendFeedback(() -> Text.of("Elytra Race will begin in 5 seconds! Race Duration: " + seconds + "s"), true);
                    return 1;
                })));

        dispatcher.register(literal("LCP_ElytraRace_END").requires(src -> src.hasPermissionLevel(2)).executes(context -> {
            endRace(context.getSource().getServer());
            return 0;
        }));
    }
}
