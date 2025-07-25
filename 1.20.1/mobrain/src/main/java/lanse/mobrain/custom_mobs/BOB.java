package lanse.mobrain.custom_mobs;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.enchantment.Enchantments;

public class BOB {

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

        return bob;
    }
}