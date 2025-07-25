package lanse.bossmobs.customattacks;

import lanse.bossmobs.BossMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HomingArrow extends ArrowEntity {
    private final Entity target;
    public HomingArrow(World world, double x, double y, double z, Entity target) {
        super(EntityType.ARROW, world);
        this.setVelocity(0, 0.1, 0);
        this.updatePosition(x, y, z);
        this.target = target;
        this.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1));

        BossMobs.addMinionID(this.getId());
        world.spawnEntity(this);
    }

    public static void tick(ServerWorld world, Entity entity) {

        if (entity instanceof HomingArrow homingArrow) {
            Entity target = homingArrow.target;

            if (target == null || target.isRemoved()) {
                entity.kill();
                return;
            }

            world.spawnParticles(ParticleTypes.ENCHANT, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0.1);

            //TODO - Make sure this is actually tracking the target it was given, and make sure the speed isn't too fast.

            // Calculate direction and add velocity towards the target player
            Vec3d direction = target.getPos().subtract(homingArrow.getPos()).normalize();
            Vec3d velocity = direction.multiply(0.05);
            homingArrow.setVelocity(homingArrow.getVelocity().add(velocity));
            homingArrow.velocityModified = true;
        }
    }
    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1));
        ServerWorld world = (ServerWorld) target.getWorld();

        for (int i = 0; i < 150; i++) {
            double XSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            double YSpeed = world.random.nextDouble() * 2.0;
            double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY(), target.getZ(), 1, XSpeed, YSpeed, ZSpeed, 1.2);
        }
        this.kill();
    }
}