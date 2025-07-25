package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class SlimeTitan extends SlimeEntity {
    public SlimeTitan(World world) {
        super(EntityType.SLIME, world);

        this.setCustomName(Text.literal("Titanic Slime").formatted(Formatting.GREEN));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 1);
        helmet.addEnchantment(Enchantments.THORNS, 1);
        ArmorHandler.setArmorColor(helmet, DyeColor.GREEN);
        ArmorHandler.setArmorTrim(helmet, "emerald", "silence");
        ArmorHandler.setUnbreakable(helmet);

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 1);
        chestplate.addEnchantment(Enchantments.THORNS, 1);
        ArmorHandler.setArmorColor(chestplate, DyeColor.GREEN);
        ArmorHandler.setArmorTrim(chestplate, "emerald", "silence");
        ArmorHandler.setUnbreakable(chestplate);

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 1);
        leggings.addEnchantment(Enchantments.THORNS, 1);
        ArmorHandler.setArmorColor(leggings, DyeColor.GREEN);
        ArmorHandler.setArmorTrim(leggings, "emerald", "silence");
        ArmorHandler.setUnbreakable(leggings);

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 1);
        boots.addEnchantment(Enchantments.THORNS, 1);
        ArmorHandler.setArmorColor(boots, DyeColor.GREEN);
        ArmorHandler.setArmorTrim(boots, "emerald", "silence");
        ArmorHandler.setUnbreakable(boots);

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);

        this.experiencePoints = 50;
        this.setSize(8, true);
        BossMobs.addId(this.getId());
    }
    public static void systematicallyDestroy(ServerWorld world, LivingEntity boss) {

        MinecraftServer server = world.getServer();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            for (int i = 0; i < 300; i++) {
                Random random = new Random();
                double xSpeed = (random.nextDouble() - 0.5) * 2;
                double ySpeed = (random.nextDouble() - 0.5) * 2;
                double zSpeed = (random.nextDouble() - 0.5) * 2;

                ServerWorld playerWorld = player.getServerWorld();
                playerWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, boss.getX(), boss.getY(), boss.getZ(),
                        1, xSpeed, ySpeed, zSpeed, 1);
            }

            BossMobs.removeId(boss.getId());

            Random random = new Random();
            int numItems = random.nextInt(90) + 15;
            double xVelocity;
            double yVelocity = 0.5;
            double zVelocity;

            for (int i = 0; i < numItems; i++) {
                ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.SLIME_BALL));

                xVelocity = (random.nextDouble() - 0.5) / 2; // Random X direction
                zVelocity = (random.nextDouble() - 0.5) / 2; // Random Z direction

                loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
                world.spawnEntity(loot);
            }
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        SlimeTitan slimeTitan = new SlimeTitan(world);
        slimeTitan.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(slimeTitan);
    }

        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossSlime")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        SlimeTitan slimeTitan = new SlimeTitan(world);
                        slimeTitan.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(slimeTitan);
                        context.getSource().sendFeedback(() -> Text.literal("Titanic Slime has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Titanic Slime. Player not found."));
                        return 0;
                    }}));
    }
}
