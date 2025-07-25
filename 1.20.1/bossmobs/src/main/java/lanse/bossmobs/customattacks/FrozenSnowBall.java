package lanse.bossmobs.customattacks;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class FrozenSnowBall extends SnowballEntity {

    private static float power = 2;

    public FrozenSnowBall(ServerWorld world, double x, double y, double z, float power) {
        super(EntityType.SNOWBALL, world);
        this.updatePosition(x, y, z);
        FrozenSnowBall.power = power;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        ServerWorld world = (ServerWorld) this.getWorld();
        world.createExplosion(this, this.getX(), this.getY(), this.getZ(), power, World.ExplosionSourceType.NONE);
        Vec3d pos = this.getPos();
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(7, 7, 7), pos.add(7, 7, 7)), e -> true);

        for (int i = 0; i < 200 + (power * 5); i++) {
            double XSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            double YSpeed = world.random.nextDouble() * 2.0;
            double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY(), this.getZ(), 1, XSpeed, YSpeed, ZSpeed, 1.0);
        }

        for (LivingEntity entity : nearbyEntities){
            if (entity.getType() != EntityType.SNOW_GOLEM
            && entity.getType() != EntityType.STRAY) {

                entity.setFrozenTicks(Math.min(entity.getFrozenTicks() + 300, 800));
            }
        }
        this.discard();
    }
}