package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.EntityGravity;
import lanse.bossmobs.customattacks.ExplosiveSnowBallEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class Bomby extends CreeperEntity {

    public static boolean isCharged = false;
    private static final ArrayList<Integer> GravityBombIdList = new ArrayList<>();
    private static final ArrayList<Integer> ExplosiveStompIdList = new ArrayList<>();
    private static final ArrayList<Integer> PushAwayStompIdList = new ArrayList<>();
    private static final ArrayList<Integer> LightningCircleIdList = new ArrayList<>();

    public Bomby(World world) {
        super(EntityType.CREEPER, world);

        this.setCustomName(Text.literal("Bomby").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 7);
        chestplate.addEnchantment(Enchantments.BLAST_PROTECTION, 120);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 10);
        chestplate.addEnchantment(Enchantments.THORNS, 2);
        setArmorColor(chestplate);
        setArmorTrim(chestplate);

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 7);
        leggings.addEnchantment(Enchantments.BLAST_PROTECTION, 120);
        leggings.addEnchantment(Enchantments.UNBREAKING, 10);
        leggings.addEnchantment(Enchantments.THORNS, 2);
        setArmorColor(leggings);
        setArmorTrim(leggings);

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 7);
        boots.addEnchantment(Enchantments.BLAST_PROTECTION, 120);
        boots.addEnchantment(Enchantments.UNBREAKING, 10);
        boots.addEnchantment(Enchantments.THORNS, 2);
        setArmorColor(boots);
        setArmorTrim(boots);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 7);
        helmet.addEnchantment(Enchantments.BLAST_PROTECTION, 120);
        helmet.addEnchantment(Enchantments.UNBREAKING, 10);
        helmet.addEnchantment(Enchantments.THORNS, 2);
        helmet.addEnchantment(Enchantments.FEATHER_FALLING, 10);
        setArmorColor(helmet);
        setArmorTrim(helmet);

        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.MAINHAND, Items.TOTEM_OF_UNDYING.getDefaultStack());

        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(35.0);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
        this.setHealth(35.0F);
        this.experiencePoints = 45;

        BossMobs.addId(this.getId());
    }

    private void setArmorColor(ItemStack itemStack) {
        NbtCompound display = itemStack.getOrCreateSubNbt("display");
        display.putInt("color", DyeColor.LIME.getFireworkColor());
    }

    private void setArmorTrim(ItemStack itemStack) {
        NbtCompound trim = itemStack.getOrCreateSubNbt("Trim");
        trim.putString("material", "minecraft:" + "lapis");
        trim.putString("pattern", "minecraft:" + "silence");
    }

    @Override //I don't want bomby to suicide bomb
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Vec3d bossPos = this.getPos();

            // Find all players within 5 blocks of the boss
            List<PlayerEntity> nearbyPlayers = serverWorld.getEntitiesByClass(PlayerEntity.class, new Box(bossPos.subtract(5, 5, 5), bossPos.add(5, 5, 5)), player -> true);

            if (!nearbyPlayers.isEmpty()) {
                // Get the nearest player
                PlayerEntity nearestPlayer = nearbyPlayers.get(0);
                double nearestDistance = this.distanceTo(nearestPlayer);

                for (PlayerEntity player : nearbyPlayers) {
                    double distance = this.distanceTo(player);
                    if (distance < nearestDistance) {
                        nearestPlayer = player;
                        nearestDistance = distance;
                    }
                }

                // Create an explosive snowball 200 blocks above the nearest player and set it as the target
                ExplosiveSnowBallEntity snowball = new ExplosiveSnowBallEntity(this.getWorld(), nearestPlayer.getX(), nearestPlayer.getY() + 200, nearestPlayer.getZ(), 3);
                snowball.refreshPositionAndAngles(nearestPlayer.getX(), nearestPlayer.getY() + 200, nearestPlayer.getZ(), 0.0F, 0.0F);
                serverWorld.spawnEntity(snowball);
                super.setTarget(snowball.getControllingPassenger());

            } else if (!(super.getTarget() instanceof PlayerEntity)) {
                // If the current target is not a player, find the nearest player and set them as the target
                List<PlayerEntity> allPlayers = serverWorld.getEntitiesByClass(PlayerEntity.class, new Box(bossPos.subtract(30, 30, 30), bossPos.add(30, 30, 30)), player -> true);

                if (!allPlayers.isEmpty()) {
                    PlayerEntity nearestPlayer = allPlayers.get(0);
                    double nearestDistance = this.distanceTo(nearestPlayer);

                    for (PlayerEntity player : allPlayers) {
                        double distance = this.distanceTo(player);
                        if (distance < nearestDistance) {
                            nearestPlayer = player;
                            nearestDistance = distance;
                        }
                    }
                    super.setTarget(nearestPlayer);
                }
            }
        }
    }

    public static void attack(ServerWorld world, LivingEntity entity, MinecraftServer server) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, entity, 30)) return;

        int bossId = entity.getId();
        if (entity instanceof Bomby bomby) {
            bomby.setTarget(bomby.getAttacking());
        }

        //Check if creeper is charged. The attacks will be stronger if it is. (Strength applied at charge)
        isCharged = entity.hasStatusEffect(StatusEffects.STRENGTH);

        if (GravityBombIdList.contains(bossId)) {
            AttackGravityBomb(world, entity, server);
            return;
        }
        if (ExplosiveStompIdList.contains(bossId)) {
            AttackExplosiveStomp(world, entity);
            return;
        }
        if (PushAwayStompIdList.contains(bossId)) {
            AttackPushAwayStomp(world, entity);
            return;
        }
        if (LightningCircleIdList.contains(bossId)) {
            AttackLightningCircle(entity.getPos(), entity);
            return;
        }

        Random random = new Random();
        if (random.nextInt(65) == 25) {

            int attackIndex = random.nextInt(6); // 0 - 5
            Vec3d bossPos = entity.getPos();

            switch (attackIndex) {
                case 0 -> AttackGravityBomb(world, entity, server);
                case 1 -> AttackExplosiveProjectiles(entity);
                case 2 -> AttackExplosiveStomp(world, entity);
                case 3 -> AttackParticleExplosion(bossPos, entity, server);
                case 4 -> AttackPushAwayStomp(world, entity);
                case 5 -> {
                    if (isCharged) {
                        AttackLightningCircle(bossPos, entity);
                    } else {
                        AttackSelfLightning(bossPos, entity);
                    }
                }
            }
        }
    }

    public static void obliterate(ServerWorld world, LivingEntity boss) {

        //Strength is applied at Charge, so I can check that to see if it is charged.
        isCharged = boss.hasStatusEffect(StatusEffects.STRENGTH);
        boss.clearStatusEffects();
        MinecraftServer server = boss.getServer();
        DefaultParticleType particleType;

        if (isCharged) {
            particleType = ParticleTypes.ELECTRIC_SPARK;
        } else {
            particleType = ParticleTypes.WHITE_ASH;
        }

        assert server != null;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            for (int i = 0; i < 300; i++) {
                Random random = new Random();
                double xSpeed = random.nextDouble() - 0.5 * 2;
                double ySpeed = random.nextDouble() - 0.5 * 2;
                double zSpeed = random.nextDouble() - 0.5 * 2;

                ServerWorld playerWorld = player.getServerWorld();
                playerWorld.spawnParticles(particleType, boss.getX(), boss.getY(), boss.getZ(),
                        1, xSpeed, ySpeed, zSpeed, 1);
            }

            if (isCharged) {
                world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 13.0F, true, World.ExplosionSourceType.MOB);
            } else {
                world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 9.0F, false, World.ExplosionSourceType.MOB);
            }
            BossMobs.removeId(boss.getId());

            Random random = new Random();
            int numItems = random.nextInt(45) + 1;
            double xVelocity;
            double yVelocity = 0.5;
            double zVelocity;

            for (int i = 0; i < numItems; i++) {
                ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.GUNPOWDER));

                xVelocity = (random.nextDouble() - 0.5) / 2; // Random X direction
                zVelocity = (random.nextDouble() - 0.5) / 2; // Random Z direction

                loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
                world.spawnEntity(loot);
            }
        }
    }

    /**
     * Custom attacks are below here -----------------------------------------------------------
     **/

    private static void AttackGravityBomb(ServerWorld world, LivingEntity boss, MinecraftServer server) {

        int bossID = boss.getId();

        if (!GravityBombIdList.contains(bossID)) {
            GravityBombIdList.add(bossID);
            BossMobs.addBossCounter(bossID);
        }

        int currentTicks = BossMobs.getBossTickCount(bossID);
        EntityGravity.applyGravity(world, boss);

        Random random = new Random();
        double distance = random.nextDouble() * 30.0; // Random distance within 30 blocks
        double angle = random.nextDouble() * 2 * Math.PI; // Random direction
        double xOffset = distance * Math.cos(angle);
        double zOffset = distance * Math.sin(angle);

        Vec3d particlePos = boss.getPos().add(xOffset, 0, zOffset); // Position of the particle
        Vec3d velocity = boss.getPos().subtract(particlePos).normalize().multiply(0.1); // Velocity towards the creeper

        // Spawn particles for each player on the server
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld playerWorld = player.getServerWorld();

            if (playerWorld == world) {
                // Send the particle effect to each player's client
                playerWorld.spawnParticles(ParticleTypes.CLOUD, particlePos.x, particlePos.y + random.nextDouble() * 2, particlePos.z,
                        1, velocity.x, velocity.y, velocity.z, 0.1);
            }
        }

        if (currentTicks > 140) {
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 2));
            boss.setHealth(boss.getHealth() + 10);

            if (isCharged) {
                world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 8.0F, true, World.ExplosionSourceType.MOB);
            } else {
                world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 5.0F, false, World.ExplosionSourceType.MOB);
            }

            BossMobs.removeBossCounter(bossID);
            for (int i = 0; i < GravityBombIdList.size(); i++) {
                if (GravityBombIdList.get(i) == bossID) {
                    GravityBombIdList.remove(i);
                    return;
                }
            }
        }
    }

    private static void AttackExplosiveStomp(ServerWorld world, LivingEntity boss) {

        int bossID = boss.getId();

        if (!ExplosiveStompIdList.contains(bossID)) {
            ExplosiveStompIdList.add(bossID);
            BossMobs.addBossCounter(bossID);
            boss.addVelocity(0, 1.5, 0);
        }
        int currentTicks = BossMobs.getBossTickCount(bossID);

        if (currentTicks > 5) {
            BlockPos topPosition = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, BlockPos.ofFloored(boss.getPos()));
            double topY = topPosition.getY();

            if (Math.abs(boss.getY() - topY) < 0.05) {
                boss.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 3));
                boss.setHealth(boss.getHealth() + 10);

                if (isCharged) {
                    world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 7.0F, true, World.ExplosionSourceType.MOB);
                } else {
                    world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 5.0F, false, World.ExplosionSourceType.MOB);
                }

                BossMobs.removeBossCounter(bossID);
                for (int i = 0; i < ExplosiveStompIdList.size(); i++) {
                    if (ExplosiveStompIdList.get(i) == bossID) {
                        ExplosiveStompIdList.remove(i);
                        return;
                    }
                }
            }
        }
    }

    private static void AttackPushAwayStomp(ServerWorld world, LivingEntity boss) {

        int bossID = boss.getId();

        if (!PushAwayStompIdList.contains(bossID)) {
            PushAwayStompIdList.add(bossID);
            BossMobs.addBossCounter(bossID);
            boss.addVelocity(0, 1.5, 0);
        }
        int currentTicks = BossMobs.getBossTickCount(bossID);

        if (currentTicks > 5) {
            BlockPos topPosition = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, BlockPos.ofFloored(boss.getPos()));
            double topY = topPosition.getY();

            if (Math.abs(boss.getY() - topY) < 0.05) {
                EntityGravity.pushAway(world, boss.getPos(), 7);
                BossMobs.removeBossCounter(bossID);

                for (int i = 0; i < PushAwayStompIdList.size(); i++) {
                    if (PushAwayStompIdList.get(i) == bossID) {
                        PushAwayStompIdList.remove(i);
                        return;
                    }
                }
            }
        }
    }

    private static void AttackParticleExplosion(Vec3d bossPos, LivingEntity boss, MinecraftServer server) {
        ServerWorld world = (ServerWorld) boss.getWorld();

        // Apply effects to players within a certain radius
        double effectRadius = isCharged ? 20.0 : 10.0;

        if (isCharged) {
            world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 5.0F, false, World.ExplosionSourceType.MOB);
        } else {
            world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 3.0F, false, World.ExplosionSourceType.MOB);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            for (int i = 0; i < 200; i++) {
                Random random = new Random();
                double xSpeed = random.nextDouble() - 0.5 * 2;
                double ySpeed = random.nextDouble() - 0.5 * 2;
                double zSpeed = random.nextDouble() - 0.5 * 2;

                ServerWorld playerWorld = player.getServerWorld();
                playerWorld.spawnParticles(ParticleTypes.SMOKE, boss.getX(), boss.getY(), boss.getZ(),
                        1, xSpeed, ySpeed, zSpeed, 1);
            }

            double distance = player.getPos().distanceTo(bossPos);

            if (distance <= effectRadius) {
                // Apply blindness and damage to players within the radius
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 160, 1));
                float damage = (float) (effectRadius - distance);
                player.damage(new DamageSource(AttackHandler.bossDamageType), damage);
            }
        }
    }

    private static void AttackLightningCircle(Vec3d bossPos, LivingEntity boss) {

        int bossID = boss.getId();

        if (!LightningCircleIdList.contains(bossID)) {
            LightningCircleIdList.add(bossID);
            BossMobs.addBossCounter(bossID);
        }

        int currentTick = BossMobs.getBossTickCount(bossID);

        if (!(currentTick % 4 == 0)) {
            return;
        }

        World world = boss.getWorld();
        int distanceBetweenStrikes = 3;

        Vec3d[] directions = new Vec3d[]{
                new Vec3d(0, 0, -1),  // North
                new Vec3d(1, 0, -1),  // NorthEast
                new Vec3d(1, 0, 0),   // East
                new Vec3d(1, 0, 1),   // SouthEast
                new Vec3d(0, 0, 1),   // South
                new Vec3d(-1, 0, 1),  // SouthWest
                new Vec3d(-1, 0, 0),  // West
                new Vec3d(-1, 0, -1)  // NorthWest
        };

        // Calculate which circle we are striking based on the tick count
        int currentRadius = currentTick * distanceBetweenStrikes;

        if (currentRadius > 0 && currentRadius <= 40) {
            for (Vec3d direction : directions) {
                BlockPos strikePos = BlockPos.ofFloored(bossPos.add(direction.normalize().multiply(currentRadius)));
                strikePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, strikePos);

                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(strikePos.toCenterPos());
                    world.spawnEntity(lightning);
                }
            }
        }
        if (currentRadius >= 40) {
            BossMobs.removeBossCounter(bossID);
            for (int i = 0; i < LightningCircleIdList.size(); i++) {
                if (LightningCircleIdList.get(i) == bossID) {
                    LightningCircleIdList.remove(i);
                    return;
                }
            }
        }
    }

    public static void AttackSelfLightning(Vec3d bossPos, LivingEntity boss) {

        World world = boss.getWorld();

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(bossPos);
            world.spawnEntity(lightning);

            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 100, 1));
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
            boss.setCustomName(Text.literal("UltraBomby").formatted(Formatting.AQUA));
        }
    }

    private static void AttackExplosiveProjectiles(LivingEntity boss) {

        ServerWorld world = (ServerWorld) boss.getWorld();
        Random random = new Random();
        ExplosiveSnowBallEntity snowball;

        for (int i = 0; i < 12; i++) {

            if (isCharged) {
                snowball = new ExplosiveSnowBallEntity(world, boss.getX(), boss.getY() + 5, boss.getZ(), 7.0F);
            } else {
                snowball = new ExplosiveSnowBallEntity(world, boss.getX(), boss.getY() + 5, boss.getZ(), 4.0F);
            }

            // Set a random velocity for each snowball
            double xVelocity = (random.nextDouble() - 0.5) * 2; // Random X direction
            double yVelocity = 0.5;
            double zVelocity = (random.nextDouble() - 0.5) * 2; // Random Z direction

            snowball.setVelocity(xVelocity, yVelocity, zVelocity);
            world.spawnEntity(snowball);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world){
        Bomby bomby = new Bomby(world);
        bomby.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(bomby);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("SummonBossCreeper")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        Bomby bomby = new Bomby(world);
                        bomby.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(bomby);
                        context.getSource().sendFeedback(() -> Text.literal("Bomby has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Bomby. Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("SummonBossChargedCreeper")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        Bomby Ultrabomby = new Bomby(world);
                        Ultrabomby.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(Ultrabomby);
                        Bomby.AttackSelfLightning(Ultrabomby.getPos(), Ultrabomby);
                        context.getSource().sendFeedback(() -> Text.literal("UltraBomby has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon UltraBomby. Player not found."));
                        return 0;
                    }}));
    }
}