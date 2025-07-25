package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class PufferfishBoss extends PufferfishEntity {
    public PufferfishBoss(World world) {
        super(EntityType.PUFFERFISH, world);

        this.setCustomName(Text.literal("PufferfishBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 2);
        helmet.addEnchantment(Enchantments.UNBREAKING, 4);
        helmet.addEnchantment(Enchantments.THORNS, 30);
        helmet.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
        helmet.addEnchantment(Enchantments.RESPIRATION, 5);
        ArmorHandler.setArmorColor(helmet, DyeColor.YELLOW);
        ArmorHandler.setArmorTrim(helmet, "lapis", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 2);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 4);
        chestplate.addEnchantment(Enchantments.THORNS, 30);
        ArmorHandler.setArmorColor(chestplate, DyeColor.YELLOW);
        ArmorHandler.setArmorTrim(chestplate, "lapis", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 2);
        leggings.addEnchantment(Enchantments.UNBREAKING, 4);
        leggings.addEnchantment(Enchantments.THORNS, 30);
        ArmorHandler.setArmorColor(leggings, DyeColor.YELLOW);
        ArmorHandler.setArmorTrim(leggings, "lapis", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 2);
        boots.addEnchantment(Enchantments.UNBREAKING, 4);
        boots.addEnchantment(Enchantments.THORNS, 30);
        ArmorHandler.setArmorColor(boots, DyeColor.YELLOW);
        ArmorHandler.setArmorTrim(boots, "lapis", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
        this.setHealth(20.0F);
        this.experiencePoints = 35;

        BossMobs.addId(this.getId());
    }
    public static void Deoxygenate(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 15)) return;

        Random random = new Random();
        if (random.nextInt(45) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            switch (attackIndex) {
                case 0 -> AttackPoisonLaser(world, boss);
                case 1 -> AttackBubbleBeam(world, boss);
            }
        }
    }
    private static void AttackPoisonLaser(ServerWorld world, LivingEntity boss) {
        Laser.PoisonLaser(world, boss, 6);
    }
    private static void AttackBubbleBeam(ServerWorld world, LivingEntity boss) {
        Laser.BubbleBeam(world, boss, 6);
    }


    public static void spawnReactor(Entity entity, ServerWorld world) {
        PufferfishBoss pufferfish = new PufferfishBoss(world);
        pufferfish.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(pufferfish);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossPufferfish")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        PufferfishBoss pufferfishBoss = new PufferfishBoss(world);
                        pufferfishBoss.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(pufferfishBoss);
                        context.getSource().sendFeedback(() -> Text.literal("Pufferfish Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Pufferfish Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}