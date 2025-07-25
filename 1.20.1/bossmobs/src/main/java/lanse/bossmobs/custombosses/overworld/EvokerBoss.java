package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.Laser;
import lanse.bossmobs.customattacks.PotionRain;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class EvokerBoss extends EvokerEntity {
    private static final ArrayList<Integer> PotionRainIdList = new ArrayList<>();
    private static final ArrayList<Integer> PotionDeathRayIdList = new ArrayList<>();

    public EvokerBoss(World world) {
        super(EntityType.EVOKER, world);

        this.setCustomName(Text.literal("EvokerBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 5);
        helmet.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        helmet.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5);
        helmet.addEnchantment(Enchantments.FIRE_PROTECTION, 5);
        helmet.addEnchantment(Enchantments.UNBREAKING, 5);
        helmet.addEnchantment(Enchantments.THORNS, 3);
        ArmorHandler.setArmorColor(helmet, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(helmet, "emerald", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.FIRE_PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 5);
        chestplate.addEnchantment(Enchantments.THORNS, 3);
        ArmorHandler.setArmorColor(chestplate, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(chestplate, "emerald", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 5);
        leggings.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        leggings.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5);
        leggings.addEnchantment(Enchantments.FIRE_PROTECTION, 5);
        leggings.addEnchantment(Enchantments.UNBREAKING, 5);
        leggings.addEnchantment(Enchantments.THORNS, 3);
        ArmorHandler.setArmorColor(leggings, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(leggings, "emerald", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 5);
        boots.addEnchantment(Enchantments.BLAST_PROTECTION, 5);
        boots.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5);
        boots.addEnchantment(Enchantments.FIRE_PROTECTION, 5);
        boots.addEnchantment(Enchantments.UNBREAKING, 5);
        boots.addEnchantment(Enchantments.THORNS, 3);
        ArmorHandler.setArmorColor(boots, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(boots, "emerald", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 75;
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, Integer.MAX_VALUE, 1));

        BossMobs.addId(this.getId());
    }
    public static void TotemOfDying(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 45)) return;

        int bossId = boss.getId();

        if (PotionRainIdList.contains(bossId)) {
            AttackPotionRain(world, boss);
            return;
        }

        if (PotionDeathRayIdList.contains(bossId)) {
            AttackDeathRay(world, boss);
            return;
        }

        Random random = new Random();
        if (random.nextInt(40) == 25) {

            int attackIndex = random.nextInt(9); // 0 - 8

            switch (attackIndex) {
                case 0 -> AttackDeathRay(world, boss);
                case 1 -> AttackPotionRain(world, boss);
                case 2 -> AttackCursePlayer(world, boss);
                case 3 -> AttackSplashPotionFountain(world, boss);
                case 4 -> AttackLingeringPotionFountain(world, boss);
                case 5 -> AttackLightningStrike(world, boss);
                case 6 -> AttackPoisonLaser(world, boss);
                case 7 -> AttackSummonVexes(world, boss);
                case 8 -> AttackHealPrayer(world, boss);
            }
        }
    }
    private static void AttackDeathRay(ServerWorld world, LivingEntity boss) {
        int bossID = boss.getId();

        if (!PotionDeathRayIdList.contains(bossID)) {
            PotionDeathRayIdList.add(bossID);
            BossMobs.addBossCounter(bossID);
        }

        PotionRain.PotionDeathRayAttack(world, boss, 2);

        if (BossMobs.getBossTickCount(bossID) > 90){

            BossMobs.removeBossCounter(bossID);
            for (int i = 0; i < PotionDeathRayIdList.size(); i++) {
                if (PotionDeathRayIdList.get(i) == bossID) {
                    PotionDeathRayIdList.remove(i);
                    return;
                }
            }
        }
    }

    private static void AttackPotionRain(ServerWorld world, LivingEntity boss) {
        int bossID = boss.getId();

        if (!PotionRainIdList.contains(bossID)) {
            PotionRainIdList.add(bossID);
            BossMobs.addBossCounter(bossID);
        }

        PotionRain.PotionRainAttack(world, boss, 2);

        if (BossMobs.getBossTickCount(bossID) > 140){

            BossMobs.removeBossCounter(bossID);
            for (int i = 0; i < PotionRainIdList.size(); i++) {
                if (PotionRainIdList.get(i) == bossID) {
                    PotionRainIdList.remove(i);
                    return;
                }
            }
        }
    }

    private static void AttackCursePlayer(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);

        for (PlayerEntity player : nearbyPlayers){
            player.addStatusEffect(new StatusEffectInstance(PotionRain.getRandomPotionEffect()));
        }
    }

    private static void AttackSplashPotionFountain(ServerWorld world, LivingEntity boss) {
        PotionRain.potionFountain(world, boss, false);
    }

    private static void AttackLingeringPotionFountain(ServerWorld world, LivingEntity boss) {
        PotionRain.potionFountain(world, boss, true);
    }

    private static void AttackLightningStrike(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);

        for (PlayerEntity player : nearbyPlayers){

            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(player.getPos());
                world.spawnEntity(lightning);
            }
        }
    }
    private static void AttackPoisonLaser(ServerWorld world, LivingEntity boss) {
        Laser.PoisonLaser(world, boss, 6);
    }

    private static void AttackSummonVexes(ServerWorld world, LivingEntity boss) {
        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(25, 25, 25), pos.add(25, 25, 25)), e -> true);

        if (!nearbyPlayers.isEmpty()) {

            Random random1 = new Random();
            int spawnCount = random1.nextInt(4) + 2;

            for (int i = 0; i < spawnCount; i++) {
                VexEntity vexMinion = new VexEntity(EntityType.VEX, world);
                BossMobs.addMinionID(vexMinion.getId());
                vexMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                vexMinion.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
                vexMinion.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
                world.spawnEntity(vexMinion);
            }
        }
    }
    private static void AttackHealPrayer(ServerWorld world, LivingEntity boss) {

        for (int i = 0; i < 100; i++) {
            double XSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            double YSpeed = world.random.nextDouble() * 2.0;
            double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, boss.getX(), boss.getY(), boss.getZ(), 1, XSpeed, YSpeed, ZSpeed, 1.0);
        }
        boss.setHealth(boss.getHealth() + 5);
        boss.clearStatusEffects();
    }


    public static void spawnReactor(Entity entity, ServerWorld world) {
        EvokerBoss evoker = new EvokerBoss(world);
        evoker.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(evoker);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossEvoker")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        EvokerBoss evoker = new EvokerBoss(world);
                        evoker.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(evoker);
                        context.getSource().sendFeedback(() -> Text.literal("Evoker Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Evoker Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}