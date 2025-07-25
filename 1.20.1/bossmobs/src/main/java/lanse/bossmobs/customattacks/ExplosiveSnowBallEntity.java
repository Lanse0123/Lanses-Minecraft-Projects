package lanse.bossmobs.customattacks;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class ExplosiveSnowBallEntity extends SnowballEntity {

    private static float power = 2;

    public ExplosiveSnowBallEntity(World world, double x, double y, double z, float power) {
        super(EntityType.SNOWBALL, world);
        this.updatePosition(x, y, z);
        ExplosiveSnowBallEntity.power = power;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        World world = this.getWorld();
        world.createExplosion(this, this.getX(), this.getY(), this.getZ(), power, World.ExplosionSourceType.NONE);

        // Remove the snowball after the explosion
        this.discard();
    }
}