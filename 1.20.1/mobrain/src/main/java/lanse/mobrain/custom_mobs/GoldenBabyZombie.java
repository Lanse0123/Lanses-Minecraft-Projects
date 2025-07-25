package lanse.mobrain.custom_mobs;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.entity.EntityType;
import net.minecraft.enchantment.Enchantments;

public class GoldenBabyZombie {

    public static ZombieEntity createGoldenBaby(World world) {
        ZombieEntity babyZombie = new ZombieEntity(EntityType.ZOMBIE, world);
        babyZombie.setBaby(true);

        ItemStack goldHelmet = new ItemStack(Items.GOLDEN_HELMET);
        goldHelmet.addEnchantment(Enchantments.PROTECTION, 4);
        goldHelmet.addEnchantment(Enchantments.THORNS, 3);

        ItemStack goldChestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        goldChestplate.addEnchantment(Enchantments.PROTECTION, 4);
        goldChestplate.addEnchantment(Enchantments.THORNS, 3);

        ItemStack goldLeggings = new ItemStack(Items.GOLDEN_LEGGINGS);
        goldLeggings.addEnchantment(Enchantments.PROTECTION, 4);
        goldLeggings.addEnchantment(Enchantments.THORNS, 3);

        ItemStack goldBoots = new ItemStack(Items.GOLDEN_BOOTS);
        goldBoots.addEnchantment(Enchantments.PROTECTION, 4);
        goldBoots.addEnchantment(Enchantments.THORNS, 3);

        babyZombie.equipStack(EquipmentSlot.HEAD, goldHelmet);
        babyZombie.equipStack(EquipmentSlot.CHEST, goldChestplate);
        babyZombie.equipStack(EquipmentSlot.LEGS, goldLeggings);
        babyZombie.equipStack(EquipmentSlot.FEET, goldBoots);

        return babyZombie;
    }
}