package lanse.mobrain.custom_mobs;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PETER {
    public static SkeletonEntity createPeter(ServerWorld world) {
        SkeletonEntity peter = new SkeletonEntity(EntityType.SKELETON, world);

        // Set custom name and make it visible
        peter.setCustomName(Text.literal("PETER").formatted(Formatting.YELLOW));
        peter.setCustomNameVisible(true);

        // Create and equip armor with Protection V and Thorns V
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

        // Create and equip a bow with Power V, Punch V, and Flame V
        ItemStack bow = new ItemStack(Items.BOW);
        bow.addEnchantment(Enchantments.POWER, 5);
        bow.addEnchantment(Enchantments.PUNCH, 5);
        bow.addEnchantment(Enchantments.FLAME, 5);
        peter.equipStack(EquipmentSlot.MAINHAND, bow);

        return peter;
    }
}