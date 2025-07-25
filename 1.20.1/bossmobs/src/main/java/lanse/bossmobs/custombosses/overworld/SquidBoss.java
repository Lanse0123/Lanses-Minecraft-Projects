package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.EntityGravity;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class SquidBoss extends SquidEntity {

    public SquidBoss(World world) {
        super(EntityType.SQUID, world);

        this.setCustomName(Text.literal("Squidward").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 2);
        helmet.addEnchantment(Enchantments.UNBREAKING, 2);
        helmet.addEnchantment(Enchantments.THORNS, 2);
        helmet.addEnchantment(Enchantments.AQUA_AFFINITY, 2);
        helmet.addEnchantment(Enchantments.RESPIRATION, 2);
        ArmorHandler.setArmorColor(helmet, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(helmet, "netherite", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 2);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 2);
        chestplate.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(chestplate, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(chestplate, "netherite", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 2);
        leggings.addEnchantment(Enchantments.UNBREAKING, 2);
        leggings.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(leggings, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(leggings, "netherite", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 2);
        boots.addEnchantment(Enchantments.UNBREAKING, 2);
        boots.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(boots, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(boots, "netherite", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 25;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(35.0);
        this.setHealth(35.0F);

        BossMobs.addId(this.getId());
    }
    public static void Vaporize(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 15)) return;

        Random random = new Random();
        if (random.nextInt(85) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            switch (attackIndex) {
                case 0 -> AttackInkBeam(world, boss);
                case 1 -> AttackPushDown(world, boss);
            }
        }
    }
    private static void AttackInkBeam(ServerWorld world, LivingEntity boss) {
        Laser.InkLaser(world, boss, 4);
    }

    private static void AttackPushDown(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(25, 25, 25), pos.add(25, 25, 25)), e -> true);

        if (!nearbyEntities.isEmpty()) {
            EntityGravity.pushDown(world, boss.getPos(), 2);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        SquidBoss squidward = new SquidBoss(world);
        squidward.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(squidward);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossSquid")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        SquidBoss squidward = new SquidBoss(world);
                        squidward.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(squidward);
                        context.getSource().sendFeedback(() -> Text.literal("Squidward has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Squidward. Player not found."));
                        return 0;
                    }
                }));
    }
}
