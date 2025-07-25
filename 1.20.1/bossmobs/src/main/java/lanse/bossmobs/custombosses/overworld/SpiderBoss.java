package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class SpiderBoss extends SpiderEntity {

    public SpiderBoss(World world) {
        super(EntityType.SPIDER, world);

        this.setCustomName(Text.literal("SpiderBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 8);
        helmet.addEnchantment(Enchantments.UNBREAKING, 2);
        helmet.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(helmet, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(helmet, "redstone", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 8);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 2);
        chestplate.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(chestplate, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(chestplate, "redstone", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 8);
        leggings.addEnchantment(Enchantments.UNBREAKING, 2);
        leggings.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(leggings, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(leggings, "redstone", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 8);
        boots.addEnchantment(Enchantments.UNBREAKING, 2);
        boots.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(boots, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(boots, "redstone", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 55;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(25.0);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
        this.setHealth(25.0F);

        BossMobs.addId(this.getId());
    }
    public static void Decimate(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        for (int i = 0; i < 25; i++) {
            SilverfishEntity silverfishMinion = new SilverfishEntity(EntityType.SILVERFISH, world);
            BossMobs.addMinionID(silverfishMinion.getId());
            silverfishMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
            world.spawnEntity(silverfishMinion);
        }
        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 25)) return;

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(15, 15, 15), pos.add(15, 15, 15)), e -> true);

        if  (!nearbyPlayers.isEmpty()){
            for (PlayerEntity player : nearbyPlayers){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
            }
        }

        Random random = new Random();
        if (random.nextInt(85) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            switch (attackIndex) {
                case 0 -> AttackWebTrap(world, boss);
                case 1 -> AttackSummonMinions(world, boss);
            }
        }
    }

    private static void AttackWebTrap(ServerWorld world, LivingEntity boss) {
        Random random = new Random();
        Entity targetEntity = boss.getAttacking();
        if (targetEntity != null) {
            Vec3d pos = targetEntity.getPos();
            BlockPos blockPos = new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
            int radius = 1;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos currentPos = blockPos.add(x, y, z);

                        if (world.getBlockState(currentPos).isAir()) {
                            if (random.nextBoolean()) {
                                world.setBlockState(currentPos, Blocks.COBWEB.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void AttackSummonMinions(ServerWorld world, LivingEntity boss) {
        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(25, 25, 25), pos.add(25, 25, 25)), e -> true);

        if (!nearbyPlayers.isEmpty()){

            Random random1 = new Random();
            int spawnCount = random1.nextInt(4) + 2;

            for (int i = 0; i < spawnCount; i++) {
                SilverfishEntity silverfishMinion = new SilverfishEntity(EntityType.SILVERFISH, world);
                BossMobs.addMinionID(silverfishMinion.getId());
                silverfishMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                world.spawnEntity(silverfishMinion);
            }
        }
    }


    public static void spawnReactor(Entity entity, ServerWorld world) {
        SpiderBoss spider = new SpiderBoss(world);
        spider.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(spider);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossSpider")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        SpiderBoss spider = new SpiderBoss(world);
                        spider.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(spider);
                        context.getSource().sendFeedback(() -> Text.literal("Spider Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Spider Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}
