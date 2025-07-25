package lanse.mobrain.custom_mobs;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class SkeletonHorsemen {

    //this is a weird custom skeleton horse
    public static SkeletonHorseEntity createSkeletonHorseman(World world) {
        SkeletonHorseEntity skeletonHorse = EntityType.SKELETON_HORSE.create(world);
        if (skeletonHorse != null) {
            SkeletonEntity skeletonRider = EntityType.SKELETON.create(world);
            if (skeletonRider != null) {
                skeletonRider.startRiding(skeletonHorse);

                // Create and equip the bow with Power 3, Punch 2, and Flame
                ItemStack enchantedBow = new ItemStack(Items.BOW);
                enchantedBow.addEnchantment(Enchantments.POWER, 3);
                enchantedBow.addEnchantment(Enchantments.PUNCH, 2);
                enchantedBow.addEnchantment(Enchantments.FLAME, 1);
                skeletonRider.equipStack(EquipmentSlot.MAINHAND, enchantedBow);

                ItemStack ironHelmet = new ItemStack(Items.IRON_HELMET);
                ironHelmet.addEnchantment(Enchantments.PROTECTION, 3);
                ironHelmet.addEnchantment(Enchantments.THORNS, 3);
                skeletonRider.equipStack(EquipmentSlot.HEAD, ironHelmet);

                world.spawnEntity(skeletonRider);
            }
        }
        return skeletonHorse;
    }
}