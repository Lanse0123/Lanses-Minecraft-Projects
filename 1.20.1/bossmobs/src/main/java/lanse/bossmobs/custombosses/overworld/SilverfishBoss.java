package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.SilverfishEntity;
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
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class SilverfishBoss extends SilverfishEntity {

    public SilverfishBoss(World world) {
        super(EntityType.SILVERFISH, world);

        this.setCustomName(Text.literal("SilverfishBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 5);
        helmet.addEnchantment(Enchantments.UNBREAKING, 2);
        helmet.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorColor(helmet, DyeColor.GRAY);
        ArmorHandler.setArmorTrim(helmet, "netherite", "silence");

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 2);
        chestplate.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorColor(chestplate, DyeColor.GRAY);
        ArmorHandler.setArmorTrim(chestplate, "netherite", "silence");

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 5);
        leggings.addEnchantment(Enchantments.UNBREAKING, 2);
        leggings.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorColor(leggings, DyeColor.GRAY);
        ArmorHandler.setArmorTrim(leggings, "netherite", "silence");

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 5);
        boots.addEnchantment(Enchantments.UNBREAKING, 2);
        boots.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorColor(boots, DyeColor.GRAY);
        ArmorHandler.setArmorTrim(boots, "netherite", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 45;
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));

        BossMobs.addId(this.getId());
    }
    public static void Eliminate(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        for (int i = 0; i < 15; i++) {
            SilverfishEntity silverfishMinion = new SilverfishEntity(EntityType.SILVERFISH, world);
            BossMobs.addMinionID(silverfishMinion.getId());
            silverfishMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
            world.spawnEntity(silverfishMinion);
        }
        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 30)) return;

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

            int attackIndex = random.nextInt(3); // 0 - 2

            switch (attackIndex) {
                case 0 -> AttackPoisonLaser(world, boss);
                case 1 -> AttackSummonMinions(world, boss);
                case 2 -> AttackInfestSurroundings(world, boss);
            }
        }
    }
    private static void AttackPoisonLaser(ServerWorld world, LivingEntity boss) {
        if (boss.getAttacking() != null){
            if (boss.canSee(boss.getAttacking())) {
                Laser.PoisonLaser(world, boss, 5);
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
                if (random1.nextInt(100) == 25){
                    SilverfishBoss silverfishBoss = new SilverfishBoss(world);
                    BossMobs.addMinionID(silverfishBoss.getId());
                    silverfishBoss.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                    silverfishBoss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
                    silverfishBoss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
                    world.spawnEntity(silverfishBoss);

                } else if (random1.nextInt(7) == 5){
                    EndermiteEntity endermiteMinion = new EndermiteEntity(EntityType.ENDERMITE, world);
                    BossMobs.addMinionID(endermiteMinion.getId());
                    endermiteMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                    endermiteMinion.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
                    endermiteMinion.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
                    world.spawnEntity(endermiteMinion);

                } else {
                    SilverfishEntity silverfishMinion = new SilverfishEntity(EntityType.SILVERFISH, world);
                    BossMobs.addMinionID(silverfishMinion.getId());
                    silverfishMinion.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                    silverfishMinion.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1));
                    silverfishMinion.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1));
                    world.spawnEntity(silverfishMinion);
                }
            }
        }
    }
    private static void AttackInfestSurroundings(ServerWorld world, LivingEntity boss) {
        Random random = new Random();
        Vec3d pos = boss.getPos();
        BlockPos blockPos = new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
        int radius = 4;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = blockPos.add(x, y, z);

                    if (random.nextBoolean()) {
                        if (world.getBlockState(currentPos).getBlock() == Blocks.STONE) {
                            world.setBlockState(currentPos, Blocks.INFESTED_STONE.getDefaultState());
                        }
                        else if (world.getBlockState(currentPos).getBlock() == Blocks.DEEPSLATE) {
                            world.setBlockState(currentPos, Blocks.INFESTED_DEEPSLATE.getDefaultState());
                        }
                        else if (world.getBlockState(currentPos).getBlock() == Blocks.STONE_BRICKS) {
                            world.setBlockState(currentPos, Blocks.INFESTED_STONE_BRICKS.getDefaultState());
                        }
                        else if (world.getBlockState(currentPos).getBlock() == Blocks.MOSSY_STONE_BRICKS) {
                            world.setBlockState(currentPos, Blocks.INFESTED_MOSSY_STONE_BRICKS.getDefaultState());
                        }
                    }
                }
            }
        }
    }


    public static void spawnReactor(Entity entity, ServerWorld world) {
        SilverfishBoss silverfish = new SilverfishBoss(world);
        silverfish.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(silverfish);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossSilverfish")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        SilverfishBoss silverfish = new SilverfishBoss(world);
                        silverfish.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(silverfish);
                        context.getSource().sendFeedback(() -> Text.literal("Silverfish Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Silverfish Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}
