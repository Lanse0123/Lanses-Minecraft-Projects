package lanse.bossmobs.customattacks;

import lanse.bossmobs.BossMobs;
import lanse.bossmobs.SpawnListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
public class SmallFireball extends SmallFireballEntity {

    public float mainPower;

    public SmallFireball(World world, double x, double y, double z, float mainPower) {
        super(EntityType.SMALL_FIREBALL, world);

        this.updatePosition(x, y, z);
        this.mainPower = mainPower;
        BossMobs.addId(this.getId());
    }

    @Override //Disable Drag
    public float getDrag() {
        return 1;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), mainPower, true, World.ExplosionSourceType.NONE);
        BossMobs.removeId(this.getId());
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        SmallFireball fireball = new SmallFireball(world, entity.getX(), entity.getY(), entity.getZ(), 4.0F);
        PlayerEntity nearestPlayer = world.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), 192, true);

        if (nearestPlayer != null) {
            Vec3d direction = new Vec3d(
                    nearestPlayer.getX() - fireball.getX(),
                    nearestPlayer.getEyeY() - fireball.getY(),
                    nearestPlayer.getZ() - fireball.getZ()
            ).normalize(); // Normalize to get the unit vector direction

            fireball.setVelocity(direction.multiply(2.5));
        }
        world.spawnEntity(fireball);
        entity.discard();
        SpawnListener.updated = true;
    }
}
