package lanse.bossmobs.customattacks;

import lanse.bossmobs.BossMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FishTorpedo extends CodEntity {
    private final Entity target;
    private final int power;

    public FishTorpedo(World world, double x, double y, double z, Entity target, int power) {
        super(EntityType.COD, world);
        this.updatePosition(x, y, z);
        this.target = target;
        this.power = power;

        BossMobs.addMinionID(this.getId());
        world.spawnEntity(this);
    }

    public static void tick(ServerWorld world, LivingEntity entity) {
        if (entity instanceof FishTorpedo fishTorpedo) {
            Entity target = fishTorpedo.target;
            int power = fishTorpedo.power;

            if (target != null && !target.isRemoved()) {
                // Check if the target is within detonation range
                if (target.getPos().distanceTo(entity.getPos()) <= 2) {
                    detonate(world, fishTorpedo, power);
                    return;
                }

                // Calculate direction and add velocity towards the target
                Vec3d direction = target.getPos().subtract(fishTorpedo.getPos()).normalize();
                Vec3d velocity = fishTorpedo.getVelocity().add(direction.multiply(0.05));
                fishTorpedo.setVelocity(velocity);
                fishTorpedo.velocityModified = true;
            } else {
                // Detonate if the target is null or removed
                detonate(world, fishTorpedo, power);
            }
        }
    }
    public static void detonate(ServerWorld world, LivingEntity fish, int power) {

        world.createExplosion(fish, fish.getX(), fish.getY(), fish.getZ(), power, false, World.ExplosionSourceType.MOB);
        BossMobs.removeMinionID(fish.getId());
        fish.discard();
    }
}
