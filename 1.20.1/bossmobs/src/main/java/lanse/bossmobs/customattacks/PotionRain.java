package lanse.bossmobs.customattacks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public class PotionRain {
    public static void PotionDeathRayAttack(ServerWorld world, Entity boss, int power) {

        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(30, 30, 30), pos.add(30, 30, 30)), e -> true);
        if (nearbyPlayers.isEmpty()) {
            return;
        }
        for (int i = 0; i < power; i++){
            for (PlayerEntity player : nearbyPlayers) {
                spawnRandomPotions(player.getPos(), world);
            }
        }
    }

    public static void PotionRainAttack(ServerWorld world, Entity boss, int power) {

        Random random = new Random();
        int xOffset;
        int y;
        int zOffset;
        Vec3d finalPos;
        Vec3d bossPos = boss.getPos();

        // Find nearby players within 30 blocks
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(bossPos.subtract(30, 30, 30), bossPos.add(30, 30, 30)), e -> true);
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        for (int i = 0; i < power; i++){
            for (PlayerEntity player : nearbyPlayers) {

                xOffset = random.nextInt(60) - 30;
                y = (int) (player.getY() + 20);
                zOffset = random.nextInt(60) - 30;

                finalPos = new Vec3d(boss.getX() + xOffset, y, boss.getZ() + zOffset);
                spawnRandomPotions(finalPos, world);
            }
        }
    }

    private static void spawnRandomPotions(Vec3d pos, ServerWorld world) {
        Random random = new Random();

        PotionEntity potionEntity = new PotionEntity(world, pos.x, pos.y + 20, pos.z);
        potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), getRandomPotion(random)));
        world.spawnEntity(potionEntity);
    }

    public static void potionFountain(ServerWorld world, Entity boss, boolean potionType){
        //Potiontype true = lingering. False = splash.
        Random random = new Random();

        for (int i = 0; i < 15; i++) {

            // Set a random velocity for each potion
            double xVelocity = (random.nextDouble() - 0.5) * 2; // Random X direction
            double yVelocity = 0.5;
            double zVelocity = (random.nextDouble() - 0.5) * 2; // Random Z direction

            PotionEntity potionEntity = new PotionEntity(world, boss.getX(), boss.getY() + 5, boss.getZ());
            if (potionType){
                potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), getRandomPotion(random)));
            } else {
                potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), getRandomPotion(random)));
            }
            world.spawnEntity(potionEntity);
            potionEntity.setVelocity(xVelocity, yVelocity, zVelocity);
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

    public static StatusEffectInstance getRandomPotionEffect() {

        Random random = new Random();
        List<StatusEffect> effects = Registries.STATUS_EFFECT.stream().toList();
        StatusEffect selectedEffect = effects.get(random.nextInt(effects.size()));

        int duration = 200;
        int amplifier = 1;

        if (selectedEffect == StatusEffects.INSTANT_HEALTH) {
            selectedEffect = StatusEffects.INSTANT_DAMAGE;
        }
        if (selectedEffect == StatusEffects.REGENERATION) {
            selectedEffect = StatusEffects.POISON;
            duration = 400;
        }
        return new StatusEffectInstance(selectedEffect, duration, amplifier);
    }
}