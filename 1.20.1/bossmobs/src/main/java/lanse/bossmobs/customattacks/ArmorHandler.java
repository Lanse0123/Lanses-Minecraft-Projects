package lanse.bossmobs.customattacks;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Vec3d;

public class ArmorHandler {

    public static void setArmorColor(ItemStack itemStack, DyeColor color) {
        NbtCompound display = itemStack.getOrCreateSubNbt("display");
        display.putInt("color", color.getFireworkColor());
    }

    public static void setArmorTrim(ItemStack itemStack, String material, String pattern) {
        NbtCompound trim = itemStack.getOrCreateSubNbt("Trim");
        trim.putString("material", "minecraft:" + material);
        trim.putString("pattern", "minecraft:" + pattern);
    }

    public static void setUnbreakable(ItemStack itemStack) {
        NbtCompound tag = itemStack.getOrCreateNbt();
        tag.putBoolean("Unbreakable", true);
    }

    public static void summonGodSword(Vec3d position, ServerWorld world) {

        ItemStack godSwordStack = new ItemStack(Items.GOLDEN_SWORD);
        godSwordStack.addEnchantment(Enchantments.SHARPNESS, 120);
        godSwordStack.addEnchantment(Enchantments.LOOTING, 120);
        godSwordStack.addEnchantment(Enchantments.UNBREAKING, 100);
        godSwordStack.addEnchantment(Enchantments.FIRE_ASPECT, 50);
        godSwordStack.addEnchantment(Enchantments.SWEEPING, 25);
        godSwordStack.addEnchantment(Enchantments.MENDING, 1);

        Vec3d spawnPosition = position.add(0, 3, 0);

        ItemEntity godSwordEntity = new ItemEntity(world, spawnPosition.getX(),
                spawnPosition.getY(), spawnPosition.getZ(), godSwordStack);

        godSwordEntity.setInvulnerable(true);
        godSwordEntity.setPickupDelay(0);
        world.spawnEntity(godSwordEntity);
    }

    public static void summonMaxNetheriteSet(Vec3d pos, ServerWorld world) {

        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 4);
        helmet.addEnchantment(Enchantments.UNBREAKING, 3);
        helmet.addEnchantment(Enchantments.MENDING, 1);

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 4);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 3);
        chestplate.addEnchantment(Enchantments.MENDING, 1);

        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 4);
        leggings.addEnchantment(Enchantments.UNBREAKING, 10);
        leggings.addEnchantment(Enchantments.MENDING, 1);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 4);
        boots.addEnchantment(Enchantments.UNBREAKING, 3);
        boots.addEnchantment(Enchantments.MENDING, 1);
        helmet.addEnchantment(Enchantments.FEATHER_FALLING, 3);

        Vec3d spawnPos = pos.add(0, 3, 0);

        ItemEntity helmetEntity = new ItemEntity(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), helmet);
        helmetEntity.setPickupDelay(0);
        world.spawnEntity(helmetEntity);
        ItemEntity chestplateEntity = new ItemEntity(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), chestplate);
        chestplateEntity.setPickupDelay(0);
        world.spawnEntity(chestplateEntity);
        ItemEntity leggingsEntity = new ItemEntity(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), leggings);
        leggingsEntity.setPickupDelay(0);
        world.spawnEntity(leggingsEntity);
        ItemEntity bootsEntity = new ItemEntity(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), boots);
        bootsEntity.setPickupDelay(0);
        world.spawnEntity(bootsEntity);
    }
}
