package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.FrozenSnowBall;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class SnowGolemBoss extends SnowGolemEntity {

    private static final ArrayList<Integer> SnowRayList = new ArrayList<>();

    public SnowGolemBoss(World world) {
        super(EntityType.SNOW_GOLEM, world);

        this.setCustomName(Text.literal("SnowGolemBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helmet.addEnchantment(Enchantments.BLAST_PROTECTION, 25);
        helmet.addEnchantment(Enchantments.PROTECTION, 2);
        helmet.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 10);
        ArmorHandler.setArmorTrim(helmet, "netherite", "silence");

        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.BLAST_PROTECTION, 25);
        chestplate.addEnchantment(Enchantments.PROTECTION, 2);
        chestplate.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 10);
        ArmorHandler.setArmorTrim(chestplate, "netherite", "silence");

        ItemStack leggings = new ItemStack(Items.IRON_LEGGINGS);
        leggings.addEnchantment(Enchantments.BLAST_PROTECTION, 25);
        leggings.addEnchantment(Enchantments.PROTECTION, 2);
        leggings.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 10);
        ArmorHandler.setArmorTrim(leggings, "netherite", "silence");

        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        boots.addEnchantment(Enchantments.BLAST_PROTECTION, 25);
        boots.addEnchantment(Enchantments.PROTECTION, 2);
        boots.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 10);
        ArmorHandler.setArmorTrim(boots, "netherite", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 50;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(25.0);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
        this.setHealth(30.0F);
        BossMobs.addId(this.getId());
    }
    public static void Melt(ServerWorld world, LivingEntity boss) {

        BossMobs.removeId(boss.getId());
        Random random = new Random();

        for (int i = 0; i < 30; i++) {

            FrozenSnowBall snowball = new FrozenSnowBall(world, boss.getX(), boss.getY() + 5, boss.getZ(), 4.0F);
            double xVelocity = (random.nextDouble() - 0.5) * 2.3;
            double yVelocity = (random.nextDouble() - 0.5) * 2.3;
            double zVelocity = (random.nextDouble() - 0.5) * 2.3;
            snowball.setVelocity(xVelocity, yVelocity, zVelocity);
            world.spawnEntity(snowball);
        }

        int numItems = random.nextInt(30) + 15;
        double xVelocity;
        double yVelocity = 0.5;
        double zVelocity;

        for (int i = 0; i < numItems; i++) {
            ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.SNOWBALL));
            ItemEntity loot2 = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.SNOW_BLOCK));

            xVelocity = (random.nextDouble() - 0.5) * 3; // Random X direction
            zVelocity = (random.nextDouble() - 0.5) * 3; // Random Z direction

            loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
            world.spawnEntity(random.nextBoolean() ? loot : loot2);
        }
    }

    @Override //Copied and edited directly from SnowGolemEntity.class
    public void attack(LivingEntity target, float pullProgress) {
        ServerWorld world = (ServerWorld) this.getWorld();
        FrozenSnowBall snowballEntity = new FrozenSnowBall(world, this.getX(), this.getY(), this.getZ(), 4);
        double d = target.getEyeY() - 1.100000023841858;
        double e = target.getX() - this.getX();
        double f = d - snowballEntity.getY();
        double g = target.getZ() - this.getZ();
        double h = Math.sqrt(e * e + g * g) * 0.20000000298023224;
        snowballEntity.setVelocity(e, f + h, g, 1.6F, 12.0F);
        this.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getWorld().spawnEntity(snowballEntity);
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (boss.getAttacker() instanceof PlayerEntity){
            boss.setAttacking((PlayerEntity) boss.getAttacker());
        }

        if (SnowRayList.contains(boss.getId())){
            AttackSnowRay(world, boss);
        }

        Random random = new Random();
        if (random.nextInt(60) == 25) {

            int attackIndex = random.nextInt(3); // 0 - 2

            switch (attackIndex) {
                case 0 -> AttackSnowFountain(world, boss);
                case 1 -> AttackIceLaser(world, boss);
                case 2 -> AttackSnowRay(world, boss);
            }
        }
    }
    private static void AttackSnowFountain(ServerWorld world, LivingEntity boss) {

        if (boss.getAttacking() != null){
            Random random = new Random();

            for (int i = 0; i < 14; i++) {

                FrozenSnowBall snowball = new FrozenSnowBall(world, boss.getX(), boss.getY() + 5, boss.getZ(), 4.0F);
                double xVelocity = (random.nextDouble() - 0.5) * 2;
                double yVelocity = 0.5;
                double zVelocity = (random.nextDouble() - 0.5) * 2;
                snowball.setVelocity(xVelocity, yVelocity, zVelocity);
                world.spawnEntity(snowball);
            }
        }
    }
    private static void AttackIceLaser(ServerWorld world, LivingEntity boss) {
        Laser.IceLaser(world, boss, 14);
    }

    private static void AttackSnowRay(ServerWorld world, LivingEntity boss) {
        int bossID = boss.getId();

        if (!SnowRayList.contains(bossID)) {
            SnowRayList.add(bossID);
            BossMobs.addBossCounter(bossID);
        }

        int currentTicks = BossMobs.getBossTickCount(bossID);
        LivingEntity target = boss.getAttacking();

        if (target != null) {
            FrozenSnowBall snowball = new FrozenSnowBall(world, target.getX(), target.getY() + 75, target.getZ(), 2);
            world.spawnEntity(snowball);
        }

        if (currentTicks > 20){
            for (int i = 0; i < SnowRayList.size(); i++) {
                if (SnowRayList.get(i) == bossID) {
                    SnowRayList.remove(i);
                    return;
                }
            }
        }
    }


    public static void spawnReactor(Entity entity, ServerWorld world){
        SnowGolemBoss snowGolem = new SnowGolemBoss(world);
        snowGolem.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(snowGolem);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossSnowGolem")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        ServerWorld world = (ServerWorld) player.getEntityWorld();
                        SnowGolemBoss snowGolem = new SnowGolemBoss(world);
                        snowGolem.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(snowGolem);
                        context.getSource().sendFeedback(() -> Text.literal("Snow Golem Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Snow Golem Boss. Player not found."));
                        return 0;
                    }}));
    }
}