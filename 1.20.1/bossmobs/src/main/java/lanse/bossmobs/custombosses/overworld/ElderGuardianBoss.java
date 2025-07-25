package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.EntityGravity;
import lanse.bossmobs.customattacks.FishTorpedo;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static net.minecraft.server.command.CommandManager.literal;

public class ElderGuardianBoss extends ElderGuardianEntity {

    private static final Logger LOGGER = Logger.getLogger(ElderGuardianBoss.class.getName());
    public ElderGuardianBoss(World world) {
        super(EntityType.ELDER_GUARDIAN, world);

        this.setCustomName(Text.literal("ElderGuardianBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 5);
        helmet.addEnchantment(Enchantments.UNBREAKING, 10);
        helmet.addEnchantment(Enchantments.THORNS, 20);
        helmet.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
        helmet.addEnchantment(Enchantments.RESPIRATION, 50);
        ArmorHandler.setArmorColor(helmet, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(helmet, "netherite", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 10);
        chestplate.addEnchantment(Enchantments.THORNS, 50);
        ArmorHandler.setArmorColor(chestplate, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(chestplate, "netherite", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 5);
        leggings.addEnchantment(Enchantments.UNBREAKING, 10);
        leggings.addEnchantment(Enchantments.THORNS, 50);
        ArmorHandler.setArmorColor(leggings, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(leggings, "netherite", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 5);
        boots.addEnchantment(Enchantments.UNBREAKING, 10);
        boots.addEnchantment(Enchantments.THORNS, 50);
        ArmorHandler.setArmorColor(boots, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(boots, "netherite", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 300;

        BossMobs.addId(this.getId());
    }
    public static void Exterminate(ServerWorld world, LivingEntity boss) {

        for (int i = 0; i < 250; i++) {
            double XSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            double YSpeed = world.random.nextDouble() * 2.0;
            double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY(), boss.getZ(), 1, XSpeed, YSpeed, ZSpeed, 1.2);
        }

        Random random = new Random();
        int numItems = random.nextInt(160) + 70;
        double xVelocity;
        double yVelocity = 0.5;
        double zVelocity;

        for (int i = 0; i < numItems; i++) {
            ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.PRISMARINE_SHARD));
            ItemEntity loot2 = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.PRISMARINE_CRYSTALS));
            ItemEntity loot3 = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.INK_SAC));

            xVelocity = (random.nextDouble() - 0.5); // Random X direction
            zVelocity = (random.nextDouble() - 0.5); // Random Z direction

            loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
            int lootIndex = random.nextInt(3); //0 - 2

            switch (lootIndex) {
                case 0 -> world.spawnEntity(loot);
                case 1 -> world.spawnEntity(loot2);
                case 2 -> world.spawnEntity(loot3);
            }
        }

        ItemEntity loot4 = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.WET_SPONGE));
        int lootIndex2 = random.nextInt(10) + 5;
        for (int i = 0; i < lootIndex2; i++){
            world.spawnEntity(loot4);
        }

        BossMobs.removeId(boss.getId());
    }

    /**
     * Custom Attacks below here. ----------------------------------------------------
     **/

    public static void attack(ServerWorld world, LivingEntity boss, MinecraftServer server) {
        //This is called each tick that the boss is loaded.

        //TODO - Make sure this is safe
        if (!AttackHandler.checkForNearbyPlayers(world, boss, 50)) return;

        Vec3d bossPos = boss.getPos();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            double distance = player.getPos().distanceTo(bossPos);

            if (distance <= 50 && !player.isCreative() && !player.isSpectator()) {
                //Elder Guardians actually give mining fatigue now. >:)
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 5000, 3));
            }
        }
        Random random = new Random();
        if (random.nextInt(42) == 25) {

            int attackIndex = random.nextInt(6); // 0 - 5

            switch (attackIndex) {
                case 0 -> AttackWaterShockwave(world, boss);
                case 1 -> AttackPushawayAttack(world, boss);
                case 2 -> AttackSpecialLazerBeam(world, boss);
                case 3 -> AttackSummonMinions(world, boss);
                case 4 -> AttackFishTorpedo(world, boss, server);
                case 5 -> AttackInkBlast(world, boss);
            }
        }
    }

    private static void AttackWaterShockwave(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);

        if (nearbyPlayers.isEmpty()){
            return;
        }
        world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 1.0F, false, World.ExplosionSourceType.MOB);

        for (int i = 0; i < 300; i++) {
            Random random = new Random();
            double xSpeed = (random.nextDouble() - 0.5) * 2.5;
            double ySpeed = (random.nextDouble() - 0.5) * 2.5;
            double zSpeed = (random.nextDouble() - 0.5) * 2.5;

            world.spawnParticles(ParticleTypes.FALLING_WATER, boss.getX(), boss.getY(), boss.getZ(),
                    1, xSpeed, ySpeed, zSpeed, 0);
        }

        for (PlayerEntity player : nearbyPlayers){
            double distance = boss.distanceTo(player);
            float damage = (float) (20 - distance);
            player.damage(new DamageSource(AttackHandler.bossDamageType), damage);
        }
    }

    private static void AttackPushawayAttack(ServerWorld world, LivingEntity boss) {

        for (int i = 0; i < 300; i++) {
            Random random = new Random();
            double xSpeed = (random.nextDouble() - 0.5) * 2.5;
            double ySpeed = (random.nextDouble() - 0.5) * 2.5;
            double zSpeed = (random.nextDouble() - 0.5) * 2.5;

            world.spawnParticles(ParticleTypes.BUBBLE, boss.getX(), boss.getY(), boss.getZ(),
                    1, xSpeed, ySpeed, zSpeed, 0);
        }

        EntityGravity.pushAway(world, boss.getPos(), 10);
    }
    private static void AttackSpecialLazerBeam(ServerWorld world, LivingEntity boss) {
        Laser.GuardianLaser(world, boss, 15);
    }

    private static void AttackSummonMinions(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(25, 25, 25), pos.add(25, 25, 25)), e -> true);

        if (!nearbyPlayers.isEmpty()){

            Random random1 = new Random();
            int spawnCount = random1.nextInt(3) + 1;

            for (int i = 0; i < spawnCount; i++) {
                GuardianEntity guardianMinion = new GuardianEntity(EntityType.GUARDIAN, world);
                BossMobs.addMinionID(guardianMinion.getId());
                guardianMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                world.spawnEntity(guardianMinion);
            }
        }
    }

    private static void AttackFishTorpedo(ServerWorld world, LivingEntity boss, MinecraftServer server) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(50, 50, 50), pos.add(50, 50, 50)), e -> true);
        PlayerEntity targetPlayer = null;

        for (PlayerEntity player : nearbyPlayers) {
            if (boss.canSee(player)) {
                targetPlayer = player;
                break; // If the boss can see a player, stop looking.
            }
        }

        if (targetPlayer != null) {

            FishTorpedo fishTorpedo = new FishTorpedo(world, boss.getX(), boss.getY(), boss.getZ(), targetPlayer, 7);
            world.spawnEntity(fishTorpedo);

        } else {
            // If no players can be seen, use a different attack
            ElderGuardianBoss.attack(world, boss, server);
        }
    }

    private static void AttackInkBlast(ServerWorld world, LivingEntity boss) {

        for (int i = 0; i < 300; i++) {
            Random random = new Random();
            double xSpeed = (random.nextDouble() - 0.5) * 2.5;
            double ySpeed = (random.nextDouble() - 0.5) * 2.5;
            double zSpeed = (random.nextDouble() - 0.5) * 2.5;

            world.spawnParticles(ParticleTypes.SMOKE, boss.getX(), boss.getY(), boss.getZ(),
                    1, xSpeed, ySpeed, zSpeed, 0);

            Vec3d pos = boss.getPos();
            List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(25, 25, 25), pos.add(25, 25, 25)), e -> true);

            for (PlayerEntity player : nearbyPlayers){
                if (!player.isCreative() && !player.isSpectator()) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 2000, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 2000, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2000, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 2000, 1));
                }
            }
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        ElderGuardianBoss elderGuardian = new ElderGuardianBoss(world);
        elderGuardian.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(elderGuardian);
    }

        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossElderGuardian")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        ElderGuardianBoss elderGuardian = new ElderGuardianBoss(world);
                        elderGuardian.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(elderGuardian);
                        context.getSource().sendFeedback(() -> Text.literal("Elder Guardian Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Elder Guardian Boss. Player not found."));
                        return 0;
                    }}));
    }
}
