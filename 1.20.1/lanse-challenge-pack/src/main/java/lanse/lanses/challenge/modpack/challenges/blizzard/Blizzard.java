package lanse.lanses.challenge.modpack.challenges.blizzard;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.block.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.world.World;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.GameMode;
import net.minecraft.world.LightType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.*;

public class Blizzard {
    private static int tickCount = 0;
    private static final int LEAF_DECAY_RADIUS = 64;
    private static final List<BlockPos> leavesToDecay = new ArrayList<>();
    private static final ArrayList<Object> mobIdList = new ArrayList<>();
    private static RegistryEntry<DamageType> freezeDamageType;

    private static void initializeFreezeDamageType(ServerWorld world) {
        freezeDamageType = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(DamageTypes.FREEZE).orElseThrow();
    }
    public static void tick(MinecraftServer server) {

        tickCount ++;

        if (tickCount % 10000 == 0){
            leavesToDecay.clear();
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            if (world.getRegistryKey() != World.NETHER){
                spawnParticles(player.getPos(), world);
                placeSnow(world, player.getPos());

                //Do this once every once in a while idk lol
                if (tickCount % 500 == 0) {
                    scheduleLeafDecay(world, player.getBlockPos());
                }
            }
            RegulateTemperature(world);
        }
        processLeafDecay(server);
        mobIdList.clear();
    }

    public static void onEntitySpawn(Entity entity, ServerWorld world) {

        if (world.getRegistryKey() == World.NETHER) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        EntityType<?> entityType = entity.getType();
        if (entityType != EntityType.STRAY
                && entityType != EntityType.ENDER_DRAGON
                && entityType != EntityType.PLAYER) {

            int maxStrays = 80 * world.getPlayers().size();
            int currentStrays = 0;
            entity.discard();

            for (Entity ignored : world.getEntitiesByType(EntityType.STRAY, Objects::nonNull)) {
                currentStrays++;
                if (currentStrays >= maxStrays) {
                    return; // Don't spawn more strays if we've reached the cap
                }
            }
            StrayEntity stray = new StrayEntity(EntityType.STRAY, world);
            stray.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());

            if (new Random().nextBoolean()) {
                stray.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            } else {
                stray.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            stray.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            stray.setCurrentHand(Hand.MAIN_HAND);
            world.spawnEntity(stray);
        }
    }

    private static void RegulateTemperature(ServerWorld world) {
        initializeFreezeDamageType(world);

        for (ServerPlayerEntity player : world.getPlayers()) {
            int radius = 64;
            Box boundingBox = new Box(
                    player.getX() - radius, world.getBottomY(), player.getZ() - radius,
                    player.getX() + radius, world.getTopY(), player.getZ() + radius);

            world.getEntitiesByClass(LivingEntity.class, boundingBox, entity -> true).forEach(entity -> {

                //This removes mobs from getting frozen multiple times per tick
                if (mobIdList.contains(entity.getId())) {
                    return;
                }
                mobIdList.add(entity.getId());

                if (world.getRegistryKey() == World.NETHER){
                    entity.setFrozenTicks(entity.getFrozenTicks() - 5);
                    return;
                }

                if (entity.getType() != EntityType.STRAY) {

                    if (entity instanceof ServerPlayerEntity serverPlayer) {
                        if (serverPlayer.interactionManager.getGameMode() == GameMode.CREATIVE ||
                                serverPlayer.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                            return;
                        }
                    }

                    BlockPos entityPos = entity.getBlockPos();
                    boolean isInDanger = true;
                    boolean leatherDamage = false;
                    boolean extraFreeze = false;
                    int dangerLevel = 7;
                    Random random = new Random();

                    // Check for leather armor protection
                    int leatherArmorCount = 0;
                    for (ItemStack armorItem : entity.getArmorItems()) {
                        if (armorItem.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.LEATHER) {
                            leatherArmorCount++;
                        }
                    }
                    if (random.nextInt(leatherArmorCount + 1) <= 1){
                        leatherDamage = true;
                    }

                    for (int i = 1; i <= 15; i++) {
                        BlockPos checkPos = entityPos.up(i);
                        if (!world.isAir(checkPos)) {
                            isInDanger = false;
                            break;
                        }
                    }
                    if (isInDanger) {
                        dangerLevel = 10;
                        extraFreeze = true;
                    }

                    entity.setFrozenTicks(Math.max(0, entity.getFrozenTicks() + 2));

                    if (tickCount % 25 == 0) {
                        if (world.getLightLevel(LightType.BLOCK, entity.getBlockPos()) < dangerLevel) {
                            entity.setInPowderSnow(true);
                            double freezeIncrement = Math.max(0, 6 - leatherArmorCount);
                            if (extraFreeze){
                                freezeIncrement = freezeIncrement * 1.4;
                            }
                            entity.setFrozenTicks((int) Math.min(entity.getMinFreezeDamageTicks(), entity.getFrozenTicks() + freezeIncrement));

                            if (entity.getFrozenTicks() >= entity.getMinFreezeDamageTicks() && leatherDamage) {
                                entity.damage(new DamageSource(freezeDamageType), 1.0F);
                            }
                        } else {
                            entity.setFrozenTicks(Math.max(0, entity.getFrozenTicks() - world.getLightLevel(entity.getBlockPos())));
                        }
                    }
                    boolean shouldApplyFOVEffect = (player.getFrozenTicks() > 25);
                    int power = player.getFrozenTicks() / 25;
                    maintainFOV(player, shouldApplyFOVEffect, power);
                }
            });
        }
    }

    private static void maintainFOV(ServerPlayerEntity player, boolean applyEffect, int power) {
        if (applyEffect) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, power, false, false, false));
        } else {
            player.removeStatusEffect(StatusEffects.SLOWNESS);
        }
    }

    private static void spawnParticles(Vec3d playerPos, ServerWorld world) {
        int particleRange = 20;
        Random random = new Random();

        ParticleEffect snowflake = ParticleTypes.WHITE_ASH;

        for (int i = 0; i < 300; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            double offsetY = (random.nextDouble() - 0.5) * 2.0 * particleRange;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0 * particleRange;

            world.spawnParticles(snowflake, playerPos.x + offsetX, playerPos.y + offsetY, playerPos.z + offsetZ, 1, 0, 0, 0, 0.15);
        }
    }

    private static void scheduleLeafDecay(ServerWorld world, BlockPos playerPos) {

        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }
        int startX = playerPos.getX() - LEAF_DECAY_RADIUS;
        int startY = playerPos.getY() - LEAF_DECAY_RADIUS;
        int startZ = playerPos.getZ() - LEAF_DECAY_RADIUS;
        int endX = playerPos.getX() + LEAF_DECAY_RADIUS;
        int endY = playerPos.getY() + LEAF_DECAY_RADIUS;
        int endZ = playerPos.getZ() + LEAF_DECAY_RADIUS;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getBlock() instanceof LeavesBlock) {
                        leavesToDecay.add(pos);
                    }
                }
            }
        }
    }

    private static void processLeafDecay(MinecraftServer server) {
        if (!leavesToDecay.isEmpty()) {
            int clearAmount = (leavesToDecay.size() / 400) + 1;
            ServerWorld world = server.getWorld(World.OVERWORLD);

            for (int i = 0; i < clearAmount; i++){
                int randomIndex = new Random().nextInt(leavesToDecay.size());
                BlockPos pos = leavesToDecay.get(randomIndex);

                if (pos != null) {
                    if (world != null && world.getBlockState(pos).getBlock() instanceof LeavesBlock) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                        leavesToDecay.remove(randomIndex);
                    }
                }
            }
        }
    }

    private static void checkAndFreezeWater(World world, BlockPos targetPos) {

        BlockPos[] surroundingPositions = new BlockPos[]{
                targetPos.north(),
                targetPos.south(),
                targetPos.east(),
                targetPos.west(),
                targetPos.north().east(),
                targetPos.north().west(),
                targetPos.south().east(),
                targetPos.south().west() };

        for (BlockPos pos : surroundingPositions) {
            BlockState surroundingBlockState = world.getBlockState(pos);
            if (surroundingBlockState.isOpaqueFullCube(world, pos) || surroundingBlockState.getBlock() == Blocks.ICE) {
                world.setBlockState(targetPos, Blocks.ICE.getDefaultState());
                return;
            }
        }
    }

    private static void placeSnow(ServerWorld world, Vec3d playerPos) {

        // tbh I dont remember how i made this 💀💀💀💀💀💀💀💀💀💀💀💀💀💀 good luck reading it lol
        final int SEARCH_RADIUS = 128;
        final double WEIGHT_EXPONENT = 1.2;
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = Math.pow(random.nextDouble(), WEIGHT_EXPONENT) * SEARCH_RADIUS;
        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;
        int targetX = (int) Math.round(playerPos.x + offsetX);
        int targetZ = (int) Math.round(playerPos.z + offsetZ);
        BlockPos targetPos = new BlockPos(targetX, world.getTopY(), targetZ);
        targetPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetPos);
        if (targetPos.getY() < 5 && world.getRegistryKey() == World.END) { return; }
        if (world.getLightLevel(LightType.BLOCK, targetPos) >= 9) { return; } if (world
                .getBlockState(targetPos.down()).getBlock() == Blocks.WATER && world.getBlockState
                (targetPos.down()).get(FluidBlock.LEVEL) == 0){ checkAndFreezeWater(world,
                targetPos.down()); return; } if (world.getBlockState(targetPos).getBlock() instanceof
                FluidBlock || world.getBlockState(targetPos.down()).getBlock()
                instanceof FluidBlock) { return; } if (world.getBlockState(targetPos.down())
                .getBlock() instanceof IceBlock) { return; } if (world.getBlockState
                (targetPos).getBlock() instanceof PlantBlock && !world.getBlockState(targetPos)
                .isOpaque()) { world.setBlockState (targetPos, Blocks.SNOW.getDefaultState().with
                (SnowBlock.LAYERS, 1), 3); return; } while (targetPos.getY() <
                318) { if (world.getBlockState(targetPos).getBlock() ==
                Blocks.AIR) { world.setBlockState(targetPos, Blocks.SNOW.getDefaultState(), 3);
            return; } else if (world.getBlockState(targetPos).getBlock() instanceof SnowBlock) {
            int currentLayers = world.getBlockState(targetPos).get(SnowBlock.LAYERS);
            if (currentLayers < 8) { world.setBlockState(targetPos,
                    Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, currentLayers + 1),
                    3); return; } else { targetPos
                    = targetPos.up(); }} else { targetPos = targetPos.up();} }}



    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("LCP_Preset_Blizzard")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.BLIZZARD;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to Blizzard!"), true);
                    return 1;
                }));
    }
}