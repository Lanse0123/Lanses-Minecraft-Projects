package lanse.bossmobs.customattacks;

import lanse.bossmobs.AttackHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Laser {

    public static void InkLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 15 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.SQUID_INK, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            (target).damage(new DamageSource(AttackHandler.bossDamageType), power);
            (target).addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 1));
        }
    }
    public static void HolyInkLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 20 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.GLOW_SQUID_INK, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 400, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 400, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 400, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 400, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 400, 2));
        }
    }
    public static void GuardianLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 20 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }
    public static void DragonHealingLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        List<EnderDragonEntity> enderDragon = world.getEntitiesByClass(EnderDragonEntity.class, new Box(bossPos.subtract(30, 30, 30), bossPos.add(30, 30, 30)), e -> true);

        if (!enderDragon.isEmpty()) {
            EnderDragonEntity targetDragon = enderDragon.get(0);
            double nearestDistance = boss.distanceTo(targetDragon);

            for (EnderDragonEntity dragon : enderDragon) {
                double distance = boss.distanceTo(dragon);
                if (distance < nearestDistance) {
                    targetDragon = dragon;
                    nearestDistance = distance;
                }
            }
            Vec3d targetPos = targetDragon.getPos();
            double particleCount = 15 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.DRAGON_BREATH, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            targetDragon.setHealth(Math.min((targetDragon.getHealth() + power), targetDragon.getMaxHealth()));
        }
    }
    public static void FireLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 15 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.setOnFireFor(7);
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }
    public static void IceLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null){
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 20 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.WHITE_ASH, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0.2);
            }

            if (target.getType() != EntityType.SNOW_GOLEM
                    && target.getType() != EntityType.STRAY) {
                target.setFrozenTicks(Math.max((target.getFrozenTicks() + 200), 500));
            }
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }
    public static void WitherLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 25 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.ASH, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 1));
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }
    public static void PoisonLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 20 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 1));
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }
    public static void BlindingLaser(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 15 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }
    public static void BubbleBeam(ServerWorld world, Entity boss, int power) {
        Vec3d bossPos = boss.getPos();
        LivingEntity target = getTargetEntity(world, boss, bossPos);

        if (target != null) {
            double nearestDistance = boss.distanceTo(target);
            Vec3d targetPos = target.getPos();
            double particleCount = 20 * nearestDistance;
            Vec3d direction = targetPos.subtract(bossPos).normalize();

            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d particlePos = bossPos.add(direction.multiply(nearestDistance * progress));
                world.spawnParticles(ParticleTypes.BUBBLE, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            target.setAir(0);
            target.damage(new DamageSource(AttackHandler.bossDamageType), power);
        }
    }


    private static LivingEntity getTargetEntity(ServerWorld world, Entity boss, Vec3d bossPos) {
        if (boss instanceof LivingEntity livingBoss) {
            LivingEntity target = livingBoss.getAttacking();
            if (target != null) {
                return target;
            }
        }

        // If not a LivingEntity or no target, find the nearest player
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class,
                new Box(bossPos.subtract(30, 30, 30), bossPos.add(30, 30, 30)), e -> true);

        if (!nearbyPlayers.isEmpty()) {
            PlayerEntity targetPlayer = nearbyPlayers.get(0);
            double nearestDistance = boss.distanceTo(targetPlayer);

            for (PlayerEntity player : nearbyPlayers) {
                double distance = boss.distanceTo(player);
                if (distance < nearestDistance) {
                    targetPlayer = player;
                    nearestDistance = distance;
                }
            }
            return targetPlayer;
        }
        return null;
    }
}
