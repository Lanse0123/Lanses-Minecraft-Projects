package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.BossMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.enchantment.Enchantments;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class NostalgiaBosses {

    public static ZombieEntity createBob(World world) {
        ZombieEntity bob = new ZombieEntity(world);

        // Equip BOB with the classic BOB gear. He is a menace.
        ItemStack diamondHelmet = new ItemStack(Items.DIAMOND_HELMET);
        diamondHelmet.addEnchantment(Enchantments.PROTECTION, 5);
        diamondHelmet.addEnchantment(Enchantments.THORNS, 5);
        bob.equipStack(EquipmentSlot.HEAD, diamondHelmet);

        ItemStack diamondChestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        diamondChestplate.addEnchantment(Enchantments.PROTECTION, 5);
        diamondChestplate.addEnchantment(Enchantments.THORNS, 4);
        bob.equipStack(EquipmentSlot.CHEST, diamondChestplate);

        ItemStack diamondLeggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        diamondLeggings.addEnchantment(Enchantments.PROTECTION, 5);
        diamondLeggings.addEnchantment(Enchantments.THORNS, 5);
        bob.equipStack(EquipmentSlot.LEGS, diamondLeggings);

        ItemStack diamondBoots = new ItemStack(Items.DIAMOND_BOOTS);
        diamondBoots.addEnchantment(Enchantments.PROTECTION, 5);
        diamondBoots.addEnchantment(Enchantments.THORNS, 4);
        bob.equipStack(EquipmentSlot.FEET, diamondBoots);

        // Equip BOB with his ultimate sword
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
        diamondSword.addEnchantment(Enchantments.SHARPNESS, 5);
        diamondSword.addEnchantment(Enchantments.KNOCKBACK, 10);
        diamondSword.addEnchantment(Enchantments.FIRE_ASPECT, 1);
        bob.equipStack(EquipmentSlot.MAINHAND, diamondSword);

        // Set BOB's custom name to yellow
        bob.setCustomName(Text.literal("BOB").formatted(Formatting.YELLOW));

        BossMobs.addId(bob.getId());
        return bob;
    }

    public static SkeletonEntity createPeter(World world) {
        SkeletonEntity peter = new SkeletonEntity(EntityType.SKELETON, world);

        peter.setCustomName(Text.literal("PETER").formatted(Formatting.YELLOW));
        peter.setCustomNameVisible(true);

        ItemStack diamondHelmet = new ItemStack(Items.DIAMOND_HELMET);
        diamondHelmet.addEnchantment(Enchantments.PROTECTION, 6);
        diamondHelmet.addEnchantment(Enchantments.THORNS, 5);

        ItemStack diamondChestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        diamondChestplate.addEnchantment(Enchantments.PROTECTION, 6);
        diamondChestplate.addEnchantment(Enchantments.THORNS, 5);

        ItemStack diamondLeggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        diamondLeggings.addEnchantment(Enchantments.PROTECTION, 6);
        diamondLeggings.addEnchantment(Enchantments.THORNS, 5);

        ItemStack diamondBoots = new ItemStack(Items.DIAMOND_BOOTS);
        diamondBoots.addEnchantment(Enchantments.PROTECTION, 6);
        diamondBoots.addEnchantment(Enchantments.THORNS, 5);

        peter.equipStack(EquipmentSlot.HEAD, diamondHelmet);
        peter.equipStack(EquipmentSlot.CHEST, diamondChestplate);
        peter.equipStack(EquipmentSlot.LEGS, diamondLeggings);
        peter.equipStack(EquipmentSlot.FEET, diamondBoots);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.addEnchantment(Enchantments.POWER, 5);
        bow.addEnchantment(Enchantments.PUNCH, 5);
        bow.addEnchantment(Enchantments.FLAME, 5);
        peter.equipStack(EquipmentSlot.MAINHAND, bow);

        BossMobs.addId(peter.getId());
        return peter;
    }

    public static ZombieEntity createKingBob(World world) {

        ZombieEntity kingBob = new ZombieEntity(world);
        kingBob.setCustomName(Text.literal("King Bob").formatted(Formatting.YELLOW));
        kingBob.setCustomNameVisible(true);

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 25);
        chestplate.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(chestplate);
        setArmorColor(chestplate);
        setArmorTrim(chestplate);

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 25);
        leggings.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(leggings);
        setArmorColor(leggings);
        setArmorTrim(leggings);

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 25);
        boots.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(boots);
        setArmorColor(boots);
        setArmorTrim(boots);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 25);
        helmet.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(helmet);
        setArmorColor(helmet);
        setArmorTrim(helmet);

        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.addEnchantment(Enchantments.SHARPNESS, 100);
        sword.addEnchantment(Enchantments.KNOCKBACK, 20);
        sword.addEnchantment(Enchantments.FIRE_ASPECT, 10);
        setUnbreakable(sword);

        kingBob.equipStack(EquipmentSlot.CHEST, chestplate);
        kingBob.equipStack(EquipmentSlot.LEGS, leggings);
        kingBob.equipStack(EquipmentSlot.FEET, boots);
        kingBob.equipStack(EquipmentSlot.HEAD, helmet);
        kingBob.equipStack(EquipmentSlot.MAINHAND, sword);

        Objects.requireNonNull(kingBob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(500.0);
        kingBob.setHealth(500.0F);
        Objects.requireNonNull(kingBob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(50.0);
        kingBob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 0xF9ABC3DF, 3));

        BossMobs.addId(kingBob.getId());
        return kingBob;
    }

    private static void setUnbreakable(ItemStack itemStack) {
        NbtCompound tag = itemStack.getOrCreateNbt();
        tag.putBoolean("Unbreakable", true);
    }

    private static void setArmorColor(ItemStack itemStack) {
        NbtCompound display = itemStack.getOrCreateSubNbt("display");
        display.putInt("color", DyeColor.WHITE.getFireworkColor());
    }

    private static void setArmorTrim(ItemStack itemStack) {
        NbtCompound trim = itemStack.getOrCreateSubNbt("Trim");
        trim.putString("material", "minecraft:" + "gold");
        trim.putString("pattern", "minecraft:" + "silence");
    }

    public static void spawnReactorKINGBOB(Entity entity, ServerWorld world){
        ZombieEntity kingBob = NostalgiaBosses.createKingBob(world);
        kingBob.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(kingBob);
    }

    public static void spawnReactorBOB(Entity entity, ServerWorld world){
        ZombieEntity bob = NostalgiaBosses.createBob(world);
        bob.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(bob);
    }

    public static void spawnReactorPETER(Entity entity, ServerWorld world){
        SkeletonEntity peter = NostalgiaBosses.createPeter(world);
        peter.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(peter);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossZombie")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        ZombieEntity bob = NostalgiaBosses.createBob(world);
                        bob.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(bob);
                        context.getSource().sendFeedback(() -> Text.literal("BOB has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon BOB. Player not found."));
                        return 0;
                    }
                }));

        dispatcher.register(literal("SummonBossKingBOB")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        ZombieEntity Kingbob = NostalgiaBosses.createKingBob(world);
                        Kingbob.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(Kingbob);
                        context.getSource().sendFeedback(() -> Text.literal("King BOB has been summoned!!!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon King BOB. Player not found."));
                        return 0;
                    }
                }));

        dispatcher.register(literal("SummonBossPETER")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        SkeletonEntity peter = NostalgiaBosses.createPeter(world);
                        peter.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(peter);
                        context.getSource().sendFeedback(() -> Text.literal("PETER has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon PETER. Player not found."));
                        return 0;
                    }
                }));
    }
}