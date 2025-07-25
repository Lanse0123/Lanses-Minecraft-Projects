package lanse.bossmobs.custombosses.nether;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.ArmorHandler;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MagmaCubeEntity;
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

public class MagmaTitan extends MagmaCubeEntity {
    public MagmaTitan(World world) {
        super(EntityType.MAGMA_CUBE, world);

        this.setCustomName(Text.literal("Titanic Magma").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.addEnchantment(Enchantments.PROTECTION, 2);
        helmet.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(helmet, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(helmet, "redstone", "silence");
        ArmorHandler.setUnbreakable(helmet);

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 2);
        chestplate.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(chestplate, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(chestplate, "redstone", "silence");
        ArmorHandler.setUnbreakable(chestplate);

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantments.PROTECTION, 2);
        leggings.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(leggings, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(leggings, "redstone", "silence");
        ArmorHandler.setUnbreakable(leggings);

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.addEnchantment(Enchantments.PROTECTION, 2);
        boots.addEnchantment(Enchantments.THORNS, 2);
        ArmorHandler.setArmorColor(boots, DyeColor.BLACK);
        ArmorHandler.setArmorTrim(boots, "redstone", "silence");
        ArmorHandler.setUnbreakable(boots);

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);

        this.setSize(8, true);
        this.experiencePoints = 50;
        BossMobs.addId(this.getId());
    }
    public static void terminate(ServerWorld world, LivingEntity boss) {

        MinecraftServer server = world.getServer();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            for (int i = 0; i < 300; i++) {
                Random random = new Random();
                double xSpeed = (random.nextDouble() - 0.5) * 2;
                double ySpeed = (random.nextDouble() - 0.5) * 2;
                double zSpeed = (random.nextDouble() - 0.5) * 2;

                ServerWorld playerWorld = player.getServerWorld();
                playerWorld.spawnParticles(ParticleTypes.LAVA, boss.getX(), boss.getY(), boss.getZ(),
                        1, xSpeed, ySpeed, zSpeed, 1);
            }

            BossMobs.removeId(boss.getId());

            Random random = new Random();
            int numItems = random.nextInt(80) + 10;
            double xVelocity;
            double yVelocity = 0.5;
            double zVelocity;

            for (int i = 0; i < numItems; i++) {
                ItemEntity loot = new ItemEntity(world, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(Items.MAGMA_CREAM));

                xVelocity = (random.nextDouble() - 0.5) / 2; // Random X direction
                zVelocity = (random.nextDouble() - 0.5) / 2; // Random Z direction

                loot.setVelocity(xVelocity, yVelocity, zVelocity); // Set random velocity for the item
                world.spawnEntity(loot);
            }
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        MagmaTitan magmaTitan = new MagmaTitan(world);
        magmaTitan.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(magmaTitan);
    }

        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossMagma")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        MagmaTitan magmaTitan = new MagmaTitan(world);
                        magmaTitan.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(magmaTitan);
                        context.getSource().sendFeedback(() -> Text.literal("Titanic Magma has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Titanic Magma. Player not found."));
                        return 0;
                    }}));
    }
}
