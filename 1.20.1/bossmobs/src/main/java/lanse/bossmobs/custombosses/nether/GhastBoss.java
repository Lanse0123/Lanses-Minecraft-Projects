package lanse.bossmobs.custombosses.nether;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class GhastBoss extends GhastEntity {
    public GhastBoss(World world) {
        super(EntityType.GHAST, world);

        this.setCustomName(Text.literal("GhastBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.BLAST_PROTECTION, 212);
        helmet.addEnchantment(Enchantments.PROTECTION, 3);
        helmet.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 1);
        ArmorHandler.setArmorColor(helmet, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(helmet, "redstone", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.BLAST_PROTECTION, 212);
        chestplate.addEnchantment(Enchantments.PROTECTION, 3);
        chestplate.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 1);
        ArmorHandler.setArmorColor(chestplate, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(chestplate, "redstone", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.BLAST_PROTECTION, 212);
        leggings.addEnchantment(Enchantments.PROTECTION, 3);
        leggings.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 1);
        ArmorHandler.setArmorColor(leggings, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(leggings, "redstone", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.BLAST_PROTECTION, 212);
        boots.addEnchantment(Enchantments.FEATHER_FALLING, 25);
        boots.addEnchantment(Enchantments.PROTECTION, 3);
        boots.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 1);
        ArmorHandler.setArmorColor(leggings, DyeColor.WHITE);
        ArmorHandler.setArmorTrim(leggings, "redstone", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(50.0);
        this.setHealth(50.0f);
        this.experiencePoints = 50;

        BossMobs.addId(this.getId());
    }
    public static void annihilate(ServerWorld world, LivingEntity boss) {

        world.createExplosion(boss, boss.getX(), boss.getY(), boss.getZ(), 16.0F, true, World.ExplosionSourceType.MOB);

        BossMobs.removeId(boss.getId());

        Random random = new Random();
        int numItems = random.nextInt(45) + 10;
        double xVelocity;
        double yVelocity = 0.5;
        double zVelocity;

        for (int i = 0; i < numItems; i++) {
            ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.GUNPOWDER));
            ItemEntity loot2 = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.GHAST_TEAR));

            xVelocity = (random.nextDouble() - 0.5); // Random X direction
            zVelocity = (random.nextDouble() - 0.5); // Random Z direction

            loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
            world.spawnEntity(random.nextBoolean() ? loot : loot2);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world){
        GhastBoss ghastBoss = new GhastBoss(world);
        ghastBoss.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(ghastBoss);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossGhast")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        GhastBoss ghastBoss = new GhastBoss(world);
                        ghastBoss.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(ghastBoss);
                        context.getSource().sendFeedback(() -> Text.literal("Ghast Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Ghast Boss. Player not found."));
                        return 0;
                    }}));
    }
}