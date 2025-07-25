package lanse.mobrain.custom_mobs;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Objects;

public class KingBOB {

    public static ZombieEntity createKingBob(World world) {

        ZombieEntity kingBob = new ZombieEntity(world);
        kingBob.setCustomName(Text.literal("King Bob").formatted(Formatting.YELLOW));
        kingBob.setCustomNameVisible(true);

        // Unbreakable White Leather Armor with Gold Silenced Armor Trim
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 25);
        chestplate.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(chestplate);
        setArmorColor(chestplate, DyeColor.WHITE);
        setArmorTrim(chestplate, "gold", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 25);
        leggings.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(leggings);
        setArmorColor(leggings, DyeColor.WHITE);
        setArmorTrim(leggings, "gold", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 25);
        boots.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(boots);
        setArmorColor(boots, DyeColor.WHITE);
        setArmorTrim(boots, "gold", "silence");

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 25);
        helmet.addEnchantment(Enchantments.THORNS, 74);
        setUnbreakable(helmet);
        setArmorColor(helmet, DyeColor.WHITE);
        setArmorTrim(helmet, "gold", "silence");

        // Gold Sword with Sharpness 100, Knockback 20, and Fire Aspect 10
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

        // Set King Bob's health and attributes
        Objects.requireNonNull(kingBob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(500.0);
        kingBob.setHealth(500.0F);
        Objects.requireNonNull(kingBob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(50.0);
        kingBob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 32767, 3));

        return kingBob;
    }

    private static void setUnbreakable(ItemStack itemStack) {
        NbtCompound tag = itemStack.getOrCreateNbt();
        tag.putBoolean("Unbreakable", true);
    }

    private static void setArmorColor(ItemStack itemStack, DyeColor color) {
        NbtCompound display = itemStack.getOrCreateSubNbt("display");
        display.putInt("color", color.getFireworkColor());
    }

    private static void setArmorTrim(ItemStack itemStack, String trimMaterial, String trimPattern) {
        NbtCompound trim = itemStack.getOrCreateSubNbt("Trim");
        trim.putString("material", "minecraft:" + trimMaterial);
        trim.putString("pattern", "minecraft:" + trimPattern);
    }
}