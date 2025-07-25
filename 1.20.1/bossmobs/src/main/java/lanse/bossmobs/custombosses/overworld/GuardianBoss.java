package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.FishTorpedo;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class GuardianBoss extends GuardianEntity {

    public GuardianBoss(World world) {
        super(EntityType.GUARDIAN, world);

        this.setCustomName(Text.literal("GuardianBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 3);
        helmet.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        helmet.addEnchantment(Enchantments.UNBREAKING, 4);
        helmet.addEnchantment(Enchantments.THORNS, 30);
        helmet.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
        helmet.addEnchantment(Enchantments.RESPIRATION, 5);
        ArmorHandler.setArmorColor(helmet, DyeColor.CYAN);
        ArmorHandler.setArmorTrim(helmet, "lapis", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 3);
        chestplate.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 4);
        chestplate.addEnchantment(Enchantments.THORNS, 30);
        ArmorHandler.setArmorColor(chestplate, DyeColor.CYAN);
        ArmorHandler.setArmorTrim(chestplate, "lapis", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 3);
        leggings.addEnchantment(Enchantments.BLAST_PROTECTION, 3);
        leggings.addEnchantment(Enchantments.UNBREAKING, 4);
        leggings.addEnchantment(Enchantments.THORNS, 30);
        ArmorHandler.setArmorColor(leggings, DyeColor.CYAN);
        ArmorHandler.setArmorTrim(leggings, "lapis", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 3);
        boots.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        boots.addEnchantment(Enchantments.UNBREAKING, 4);
        boots.addEnchantment(Enchantments.THORNS, 30);
        ArmorHandler.setArmorColor(boots, DyeColor.CYAN);
        ArmorHandler.setArmorTrim(boots, "lapis", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 65;

        BossMobs.addId(this.getId());
    }
    public static void Eradicate(ServerWorld world, LivingEntity boss) {

        for (int i = 0; i < 100; i++) {
            double XSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            double YSpeed = world.random.nextDouble() * 2.0;
            double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY(), boss.getZ(), 1, XSpeed, YSpeed, ZSpeed, 1.0);
        }

        Random random = new Random();
        int numItems = random.nextInt(60) + 20;
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

        BossMobs.removeId(boss.getId());

    }

    public static void attack(ServerWorld world, LivingEntity boss, MinecraftServer server) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 25)) return;

        Random random = new Random();
        if (random.nextInt(75) == 25) {

            int attackIndex = random.nextInt(3); // 0 - 2

            switch (attackIndex) {
                case 0 -> AttackWaterShockwave(world, boss);
                case 1 -> AttackSpecialLazerBeam(world, boss);
                case 2 -> AttackFishTorpedo(world, boss, server);
            }
        }
    }

    private static void AttackWaterShockwave(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(10, 10, 10), pos.add(10, 10, 10)), e -> true);

        if (nearbyPlayers.isEmpty()){
            return;
        }
        world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 1.0F, false, World.ExplosionSourceType.MOB);

        for (int i = 0; i < 200; i++) {
            Random random = new Random();
            double xSpeed = (random.nextDouble() - 0.5) * 2.5;
            double ySpeed = (random.nextDouble() - 0.5) * 2.5;
            double zSpeed = (random.nextDouble() - 0.5) * 2.5;

            world.spawnParticles(ParticleTypes.FALLING_WATER, boss.getX(), boss.getY(), boss.getZ(),
                    1, xSpeed, ySpeed, zSpeed, 0);
        }

        for (PlayerEntity player : nearbyPlayers){
            double distance = boss.distanceTo(player);
            float damage = (float) Math.max(1, (6 - distance));
            player.damage(new DamageSource(AttackHandler.bossDamageType), damage);
        }
    }

    private static void AttackSpecialLazerBeam(ServerWorld world, LivingEntity boss) {
        Laser.GuardianLaser(world, boss, 5);
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

            FishTorpedo fishTorpedo = new FishTorpedo(world, boss.getX(), boss.getY(), boss.getZ(), targetPlayer, 2);
            world.spawnEntity(fishTorpedo);

        } else {
            // If no players can be seen, use a different attack
            ElderGuardianBoss.attack(world, boss, server);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        GuardianBoss guardianBoss = new GuardianBoss(world);
        guardianBoss.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(guardianBoss);
    }

        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossGuardian")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        GuardianBoss guardianBoss = new GuardianBoss(world);
                        guardianBoss.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(guardianBoss);
                        context.getSource().sendFeedback(() -> Text.literal("Guardian Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Guardian Boss. Player not found."));
                        return 0;
                    }}));
    }
}
