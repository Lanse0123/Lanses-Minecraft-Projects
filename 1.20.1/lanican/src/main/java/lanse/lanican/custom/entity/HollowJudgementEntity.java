package lanse.lanican.custom.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class HollowJudgementEntity extends ArmorStandEntity {
    private int tickCounter = 0;
    private final ServerPlayerEntity owner;
    private static final DustParticleEffect GOLD_DUST = new DustParticleEffect(new Vector3f(1.0F, 0.84F, 0.0F), 2.0F);

    public HollowJudgementEntity(ServerWorld world, LivingEntity owner) {
        super(EntityType.ARMOR_STAND, world);
        this.setInvisible(true);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.owner = (ServerPlayerEntity) owner;

        Vec3d spawnPos = owner.getEyePos();
        this.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, owner.getYaw(), 0.0F);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 32767, 1));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        ServerWorld world = (ServerWorld) this.getWorld();
        Vec3d currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());

        // Move upwards
        Vec3d newPos = currentPos.add(0, 2, 0); // Adjust speed as needed
        this.setPosition(newPos.x, newPos.y, newPos.z);
        world.spawnParticles(GOLD_DUST, currentPos.x, currentPos.y, currentPos.z, 30, 0.5, 0.5, 0.5, 0.1);

        //TODO - get a box around this position, search for all living entities, and damage them by 15.

        // Detonate after a certain height or time
        if (tickCounter >= 100) {
            detonate(world);
        }
        tickCounter++;
    }

    private void detonate(ServerWorld world) {

        world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 10.0F, World.ExplosionSourceType.BLOCK);

        for (int i = 0; i < 20; i++) {
            double x = (world.random.nextDouble() - 0.5) * 2.0;
            double y = (world.random.nextDouble() - 0.5) * 2.0;
            double z = (world.random.nextDouble() - 0.5) * 2.0;
            Vec3d randomDirection = new Vec3d(x, y, z).normalize();

            HollowPurpleEntity hollowPurple = new HollowPurpleEntity(world, this, randomDirection, true, false);
            world.spawnEntity(hollowPurple);
        }
        this.discard();
    }
}