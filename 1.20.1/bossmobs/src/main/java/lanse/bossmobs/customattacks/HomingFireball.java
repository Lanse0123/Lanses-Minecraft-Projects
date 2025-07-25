package lanse.bossmobs.customattacks;

import lanse.bossmobs.BossMobs;
import lanse.bossmobs.SpawnListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class HomingFireball extends FireballEntity {

    public float mainPower;
    public boolean shouldSpawnSnowballs;
    public float snowballPower;
    public int snowCount;
    public HomingFireball(World world, double x, double y, double z, float mainPower, boolean shouldSpawnSnowballs, float snowballPower, int snowCount) {
        super(EntityType.FIREBALL, world);

        this.updatePosition(x, y, z);
        this.mainPower = mainPower;
        this.shouldSpawnSnowballs = shouldSpawnSnowballs;
        this.snowballPower = snowballPower;
        this.snowCount = snowCount;
        BossMobs.addId(this.getId());
    }

    @Override //Disable Drag
    public float getDrag(){return 1;}

    @Override
    protected void onCollision(HitResult hitResult){
        super.onCollision(hitResult);
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), mainPower, World.ExplosionSourceType.MOB);

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(this.getWorld());
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(this.getX(), this.getY(), this.getZ());
            this.getWorld().spawnEntity(lightning);
        }

        if (this.shouldSpawnSnowballs){

            World world = this.getWorld();
            Random random = new Random();
            ExplosiveSnowBallEntity snowball;

            for (int i = 0; i < snowCount; i++) {

                snowball = new ExplosiveSnowBallEntity(world, this.getX(), this.getY() + 5, this.getZ(), snowballPower);

                // Set a random velocity for each snowball
                double xVelocity = (random.nextDouble() - 0.5) * 1.2;
                double yVelocity = 0.6;
                double zVelocity = (random.nextDouble() - 0.5) * 1.2;

                snowball.setVelocity(xVelocity, yVelocity, zVelocity);
                world.spawnEntity(snowball);
            }
        }
        BossMobs.removeId(this.getId());
    }
    public static void spawnReactor(Entity entity, ServerWorld world){
        HomingFireball fireball = new HomingFireball(world, entity.getX(), entity.getY(), entity.getZ(), 6.0F, true, 4, 9);
        PlayerEntity nearestPlayer = world.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), 192, true);

        if (nearestPlayer != null) {
            Vec3d direction = new Vec3d(
                    nearestPlayer.getX() - fireball.getX(),
                    nearestPlayer.getEyeY() - fireball.getY(),
                    nearestPlayer.getZ() - fireball.getZ()
            ).normalize(); // Normalize to get the unit vector direction

            fireball.setVelocity(direction.multiply(2.7));
        }
        world.spawnEntity(fireball);
        entity.discard();
        SpawnListener.updated = true;
    }
}
