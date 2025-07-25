package lanse.bossmobs.customattacks;

import lanse.bossmobs.AttackHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class EntityGravity {

    private static final double GRAVITATIONAL_CONSTANT = 0.65;
    private static final double GRAVITATIONAL_SPEED_LIMIT = 0.25;

    public static void applyGravity(ServerWorld world, Entity boss) {

        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, boss.getBoundingBox().expand(30), e -> !e.equals(boss));

        for (Entity nearbyEntity : nearbyEntities) {

            if (nearbyEntity != null) {

                Vec3d direction = new Vec3d(boss.getX() - nearbyEntity.getX(), boss.getY() - nearbyEntity.getY(), boss.getZ() - nearbyEntity.getZ());
                double distance = direction.length();

                // Simplify calculation if entities are too close
                if (distance < 3.5) {
                    Vec3d velocityChange = direction.normalize().multiply(GRAVITATIONAL_SPEED_LIMIT);
                    nearbyEntity.setVelocity(nearbyEntity.getVelocity().add(velocityChange));
                } else {
                    direction = direction.normalize();
                    double forceMagnitude = Math.min(GRAVITATIONAL_CONSTANT / (distance * (distance / 2)), GRAVITATIONAL_SPEED_LIMIT);
                    Vec3d velocityChange = direction.multiply(forceMagnitude);
                    nearbyEntity.setVelocity(nearbyEntity.getVelocity().add(velocityChange));
                }
                nearbyEntity.velocityModified = true;
            }
        }
    }

    public static void pushAway(ServerWorld world, Vec3d pos, int power) {

        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, new Box(pos.subtract(50, 50, 50), pos.add(50, 50, 50)), e -> true);

        for (Entity nearbyEntity : nearbyEntities) {

            if (nearbyEntity != null) {

                Vec3d entityPos = new Vec3d(nearbyEntity.getX(), nearbyEntity.getY(), nearbyEntity.getZ());
                Vec3d direction = entityPos.subtract(pos);
                double distance = direction.length();
                direction = direction.normalize();

                // Simplify calculation if entities are too close
                Vec3d velocityChange;
                if (distance < 40) {
                    velocityChange = direction.multiply(GRAVITATIONAL_SPEED_LIMIT * power);
                } else {
                    double forceMagnitude = Math.min(GRAVITATIONAL_CONSTANT / distance, GRAVITATIONAL_SPEED_LIMIT);
                    velocityChange = direction.multiply(forceMagnitude);
                }

                nearbyEntity.setVelocity(nearbyEntity.getVelocity().add(velocityChange));
                float maxDamage = 20.0f + power;
                float minDamage = 2.0f;
                float damage = (float) Math.max(minDamage, maxDamage - (distance / 2.0));

                if (nearbyEntity instanceof LivingEntity && nearbyEntity.isAlive()) {
                    nearbyEntity.damage(new DamageSource(AttackHandler.bossDamageType), damage);
                }
                nearbyEntity.velocityModified = true;
            }
        }
    }

    public static void pushDown(ServerWorld world, Vec3d pos, int power) {

        // Kill all boats within 50 blocks of the pos
        List<BoatEntity> nearbyBoats = world.getEntitiesByClass(BoatEntity.class, new Box(pos.subtract(50, 50, 50), pos.add(50, 50, 50)), e -> true);
        for (BoatEntity boat : nearbyBoats) {
            boat.kill();
        }

        // Find and push down entities
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(35, 35, 35), pos.add(35, 35, 35)), e -> true);

        for (Entity nearbyEntity : nearbyEntities) {

            if (nearbyEntity != null) {

                nearbyEntity.setVelocity(nearbyEntity.getVelocity().add(0, -power, 0));
                nearbyEntity.velocityModified = true;
            }
        }
    }
}