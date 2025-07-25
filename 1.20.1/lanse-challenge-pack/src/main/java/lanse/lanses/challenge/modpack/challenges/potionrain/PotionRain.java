package lanse.lanses.challenge.modpack.challenges.potionrain;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PotionRain {
    public static double potionRange = 42;
    public static int potionCount = 3;

    public static void tick(MinecraftServer server) {

        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                spawnRandomPotions(player.getPos(), world);
            }
        }
    }

    private static void spawnRandomPotions(Vec3d playerPos, ServerWorld world) {
        Random random = new Random();

        for (int i = 0; i < potionCount; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0 * potionRange;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0 * potionRange;

            PotionEntity potionEntity = new PotionEntity(world, playerPos.x + offsetX, playerPos.y + 20, playerPos.z + offsetZ);
            potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), getRandomPotion(random)));
            world.spawnEntity(potionEntity);
        }
    }

    private static Potion getRandomPotion(Random random) {
        List<Potion> potions = Registries.POTION.stream().toList();
        Potion selectedPotion = potions.get(random.nextInt(potions.size()));

        //Turn good potions into bad potions lul
        if (selectedPotion == Potions.HEALING || selectedPotion == Potions.STRONG_HEALING) {
            return Potions.STRONG_HARMING;
        }

        if (selectedPotion == Potions.REGENERATION
                || selectedPotion == Potions.STRONG_REGENERATION
                || selectedPotion == Potions.LONG_REGENERATION) {
            return Potions.STRONG_POISON;
        }
        return selectedPotion;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("LCP_PotionRainOn")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    potionRange = 42;
                    potionCount = 3;
                    MainControl.modPreset = MainControl.Preset.POTIONRAIN;
                    MainControl.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.of("Potion Rain has begun. (Range: 42. Count: 3.)"), true);
                    return 1;
                }));

        dispatcher.register(literal("LCP_PotionHeavyRainOn")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    potionRange = 35;
                    potionCount = 8;
                    MainControl.modPreset = MainControl.Preset.POTIONRAIN;
                    MainControl.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.of("Heavy Potion Rain has begun. (Range: 35. Count: 8.)"), true);
                    return 1;
                }));

        dispatcher.register(literal("LCP_PotionDeathRayOn")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    potionRange = 0.1;
                    potionCount = 3;
                    MainControl.modPreset = MainControl.Preset.POTIONRAIN;
                    MainControl.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.of("Potion Death Ray has begun. (Range: 0.1. Count: 3.)"), true);
                    return 1;
                }));

        dispatcher.register(literal("LCP_PotionRainOff")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    MainControl.isModEnabled = false;
                    context.getSource().sendFeedback(() -> Text.of("Potion Rain has stopped."), true);
                    return 1;
                }));

        dispatcher.register(literal("LCP_PotionRainCustom")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("potionRange", IntegerArgumentType.integer())
                        .then(argument("potionCount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    MainControl.modPreset = MainControl.Preset.POTIONRAIN;
                                    MainControl.isModEnabled = true;
                                    potionRange = Math.max(IntegerArgumentType.getInteger(context, "potionRange"), 0.1); //lowest below is 1, max is 25
                                    potionCount = Math.min(Math.max(IntegerArgumentType.getInteger(context, "potionCount"), 1), 25);
                                    context.getSource().sendFeedback(() -> Text.of("Custom Potion Rain has begun with range " + potionRange + " and count " + potionCount + "."), true);
                                    return 1;
                                }))));
    }
}