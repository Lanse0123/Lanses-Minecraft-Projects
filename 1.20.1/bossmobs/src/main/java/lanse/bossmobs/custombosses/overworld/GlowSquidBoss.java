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
import net.minecraft.entity.passive.GlowSquidEntity;
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

public class GlowSquidBoss extends GlowSquidEntity {

    public GlowSquidBoss(World world) {
        super(EntityType.GLOW_SQUID, world);

        this.setCustomName(Text.literal("Holy Squidward").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 10);
        helmet.addEnchantment(Enchantments.UNBREAKING, 10);
        helmet.addEnchantment(Enchantments.THORNS, 10);
        helmet.addEnchantment(Enchantments.AQUA_AFFINITY, 10);
        helmet.addEnchantment(Enchantments.RESPIRATION, 10);
        ArmorHandler.setArmorColor(helmet, DyeColor.LIGHT_BLUE);
        ArmorHandler.setArmorTrim(helmet, "emerald", "silence"); //TODO - Change to nether quartz

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 10);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 10);
        chestplate.addEnchantment(Enchantments.THORNS, 10);
        ArmorHandler.setArmorColor(chestplate, DyeColor.LIGHT_BLUE);
        ArmorHandler.setArmorTrim(chestplate, "emerald", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 10);
        leggings.addEnchantment(Enchantments.UNBREAKING, 10);
        leggings.addEnchantment(Enchantments.THORNS, 10);
        ArmorHandler.setArmorColor(leggings, DyeColor.LIGHT_BLUE);
        ArmorHandler.setArmorTrim(leggings, "emerald", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 10);
        boots.addEnchantment(Enchantments.UNBREAKING, 10);
        boots.addEnchantment(Enchantments.THORNS, 10);
        ArmorHandler.setArmorColor(boots, DyeColor.LIGHT_BLUE);
        ArmorHandler.setArmorTrim(boots, "emerald", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 35;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(40.0);
        this.setHealth(40.F);

        BossMobs.addId(this.getId());
    }

    public static void Decapitate(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 20)) return;

        Random random = new Random();
        if (random.nextInt(60) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            //TODO - Add more custom attacks for holy squidward.

            switch (attackIndex) {
                case 0 -> AttackInkBeam(world, boss);
                case 1 -> AttackPushDown(world, boss);
            }
        }
    }
    private static void AttackInkBeam(ServerWorld world, LivingEntity boss) {
        Laser.HolyInkLaser(world, boss, 10);
    }

    private static void AttackPushDown(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(35, 35, 35), pos.add(35, 35, 35)), e -> true);

        if (!nearbyEntities.isEmpty()) {
            EntityGravity.pushDown(world, boss.getPos(), 5);
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        GlowSquidBoss holySquidward = new GlowSquidBoss(world);
        holySquidward.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(holySquidward);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossGlowSquid")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        GlowSquidBoss holySquidward = new GlowSquidBoss(world);
                        holySquidward.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(holySquidward);
                        context.getSource().sendFeedback(() -> Text.literal("Holy Squidward has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Holy Squidward. Player not found."));
                        return 0;
                    }
                }));
    }
}
