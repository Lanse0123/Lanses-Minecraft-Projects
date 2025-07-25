package lanse.bossmobs.custombosses.nether;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.EntityGravity;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class BlazeBoss extends BlazeEntity {
    public BlazeBoss(World world) {
        super(EntityType.BLAZE, world);

        this.setCustomName(Text.literal("BlazeBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.BLAST_PROTECTION, 100);
        helmet.addEnchantment(Enchantments.FIRE_PROTECTION, 150);
        helmet.addEnchantment(Enchantments.PROTECTION, 10);
        helmet.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 2);
        ArmorHandler.setArmorColor(helmet, DyeColor.RED);
        ArmorHandler.setArmorTrim(helmet, "copper", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.BLAST_PROTECTION, 100);
        chestplate.addEnchantment(Enchantments.FIRE_PROTECTION, 150);
        chestplate.addEnchantment(Enchantments.PROTECTION, 10);
        chestplate.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 2);
        ArmorHandler.setArmorColor(chestplate, DyeColor.RED);
        ArmorHandler.setArmorTrim(chestplate, "copper", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.BLAST_PROTECTION, 100);
        leggings.addEnchantment(Enchantments.FIRE_PROTECTION, 150);
        leggings.addEnchantment(Enchantments.PROTECTION, 10);
        leggings.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 2);
        ArmorHandler.setArmorColor(leggings, DyeColor.RED);
        ArmorHandler.setArmorTrim(leggings, "copper", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.BLAST_PROTECTION, 100);
        boots.addEnchantment(Enchantments.FIRE_PROTECTION, 150);
        boots.addEnchantment(Enchantments.FEATHER_FALLING, 25);
        boots.addEnchantment(Enchantments.PROTECTION, 10);
        boots.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 2);
        ArmorHandler.setArmorColor(boots, DyeColor.RED);
        ArmorHandler.setArmorTrim(boots, "copper", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 50;

        BossMobs.addId(this.getId());
    }
    public static void Extinguish(ServerWorld world, LivingEntity boss) {

        world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 4.0F, true, World.ExplosionSourceType.MOB);

        BossMobs.removeId(boss.getId());

        Random random = new Random();
        int numItems = random.nextInt(30) + 15;
        double xVelocity;
        double yVelocity = 0.5;
        double zVelocity;

        for (int i = 0; i < numItems; i++) {
            ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.BLAZE_ROD));
            ItemEntity loot2 = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.BLAZE_POWDER));

            xVelocity = (random.nextDouble() - 0.5); // Random X direction
            zVelocity = (random.nextDouble() - 0.5); // Random Z direction

            loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
            world.spawnEntity(random.nextBoolean() ? loot : loot2);
        }
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 30)) return;

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(5, 5, 5), pos.add(5, 5, 5)), e -> true);

        if (!nearbyPlayers.isEmpty()){
            for (PlayerEntity player : nearbyPlayers){
                player.setOnFireFor(5);
            }
        }

        Random random = new Random();
        if (random.nextInt(100) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            switch (attackIndex) {
                case 0 -> AttackHeatWave(world, boss);
                case 1 -> AttackFireLaser(world, boss);
            }
        }
    }

    private static void AttackHeatWave(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);
        List<BlazeBoss> nearbyBlazes = world.getEntitiesByClass(BlazeBoss.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);
        List<BlazeEntity> nearbyBlazes2 = world.getEntitiesByClass(BlazeEntity.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);

        for (BlazeBoss blaze : nearbyBlazes){
            blaze.setHealth(blaze.getHealth() + 3);
        }
        for (BlazeEntity blaze : nearbyBlazes2){
            blaze.setHealth(blaze.getHealth() + 3);
        }

        for (int i = 0; i < 280; i++) {
            double XSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            double YSpeed = world.random.nextDouble() * 2.0;
            double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.FLAME, boss.getX(), boss.getY(), boss.getZ(), 1, XSpeed, YSpeed, ZSpeed, 1.0);
        }

        for (PlayerEntity player : nearbyPlayers){
            player.setOnFireFor(5);
        }
        boss.setHealth(boss.getHealth() + 5);
        EntityGravity.pushAway(world, boss.getPos(), 3);
        world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 1.0F, true, World.ExplosionSourceType.MOB);
    }

    private static void AttackFireLaser(ServerWorld world, LivingEntity boss) {
        Laser.FireLaser(world, boss, 7);
    }


    public static void spawnReactor(Entity entity, ServerWorld world){
        BlazeBoss blazeBoss = new BlazeBoss(world);
        blazeBoss.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(blazeBoss);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossBlaze")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        BlazeBoss blazeBoss = new BlazeBoss(world);
                        blazeBoss.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(blazeBoss);
                        context.getSource().sendFeedback(() -> Text.literal("Blaze Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Blaze Boss. Player not found."));
                        return 0;
                    }}));
    }
}