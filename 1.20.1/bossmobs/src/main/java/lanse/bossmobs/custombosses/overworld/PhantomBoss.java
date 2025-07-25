package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class PhantomBoss extends PhantomEntity {

    public PhantomBoss(World world) {
        super(EntityType.PHANTOM, world);

        this.setCustomName(Text.literal("PhantomBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.CHAINMAIL_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 5);
        helmet.addEnchantment(Enchantments.UNBREAKING, 5);
        helmet.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorTrim(helmet, "lapis", "silence");

        ItemStack chestplate = new ItemStack(Items.CHAINMAIL_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 5);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 5);
        chestplate.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorTrim(chestplate, "lapis", "silence");

        ItemStack leggings = new ItemStack(Items.CHAINMAIL_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 5);
        leggings.addEnchantment(Enchantments.UNBREAKING, 5);
        leggings.addEnchantment(Enchantments.THORNS, 5);
        ArmorHandler.setArmorTrim(leggings, "lapis", "silence");

        ItemStack boots = new ItemStack(Items.CHAINMAIL_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 5);
        boots.addEnchantment(Enchantments.UNBREAKING, 5);
        boots.addEnchantment(Enchantments.THORNS, 5);
        boots.addEnchantment(Enchantments.FEATHER_FALLING, 5);
        ArmorHandler.setArmorTrim(boots, "lapis", "silence");

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 55;
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 2));

        BossMobs.addId(this.getId());
    }
    public static void Realitify(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 25)) return;

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(20, 20, 20), pos.add(20, 20, 20)), e -> true);

        if  (!nearbyPlayers.isEmpty()){
            for (PlayerEntity player : nearbyPlayers){
                if (!player.isCreative() && !player.isSpectator()) {

                    //Phantoms are literally sleep deprivation nightmares. They should act like that, not just a bird
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 1));
                }
            }
        }
        Random random = new Random();
        if (random.nextInt(120) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            switch (attackIndex) {
                case 0 -> Laser.WitherLaser(world, boss, 10);
                case 1 -> AttackSummonMinions(world, boss);
            }
        }
    }
    private static void AttackSummonMinions(ServerWorld world, LivingEntity boss) {
        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(25, 25, 25), pos.add(25, 25, 25)), e -> true);

        if (!nearbyPlayers.isEmpty()){

            Random random1 = new Random();
            int spawnCount = random1.nextInt(3) + 1;

            for (int i = 0; i < spawnCount; i++) {
                PhantomEntity phantom = new PhantomEntity(EntityType.PHANTOM, world);
                BossMobs.addMinionID(phantom.getId());
                phantom.refreshPositionAndAngles(BlockPos.ofFloored(boss.getPos()), 0, 0);
                world.spawnEntity(phantom);
            }
        }
    }


    public static void spawnReactor(Entity entity, ServerWorld world) {
        PhantomBoss phantom = new PhantomBoss(world);
        phantom.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(phantom);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossPhantom")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        PhantomBoss phantom = new PhantomBoss(world);
                        phantom.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(phantom);
                        context.getSource().sendFeedback(() -> Text.literal("Phantom Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Phantom Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}
