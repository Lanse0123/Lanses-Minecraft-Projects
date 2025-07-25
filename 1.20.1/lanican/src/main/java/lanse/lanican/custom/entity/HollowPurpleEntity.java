package lanse.lanican.custom.entity;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class HollowPurpleEntity extends ArmorStandEntity {
    private final Vec3d direction;
    private int tickCounter = 0;
    private final double lockedY;
    private final boolean hasGravity;
    private final Vec3d ownerPos;
    private final ServerPlayerEntity owner;
    private final boolean hasWarmup;
    private static final DustParticleEffect RED_DUST = new DustParticleEffect(new Vector3f(1.0F, 0.0F, 0.0F), 2.0F);
    private static final DustParticleEffect BLUE_DUST = new DustParticleEffect(new Vector3f(0.0F, 0.0F, 1.0F), 2.0F);
    private static final DustParticleEffect PURPLE_DUST = new DustParticleEffect(new Vector3f(1.0F, 0.0F, 1.0F), 2.0F);


    public HollowPurpleEntity(ServerWorld world, LivingEntity owner, Vec3d direction, boolean hasGravity, boolean hasWarmup) {
        super(EntityType.ARMOR_STAND, world);
        this.direction = direction.normalize();
        this.lockedY = owner.getEyePos().y;
        this.hasGravity = hasGravity;
        this.ownerPos = owner.getPos();

        if (owner.getType() == EntityType.PLAYER) {
            this.owner = (ServerPlayerEntity) owner;
        } else {
            this.owner = null;
        }

        this.hasWarmup = hasWarmup;
        this.setInvisible(true);
        this.setInvulnerable(true);

        Vec3d lookDirection = owner.getRotationVec(1.0F).normalize(); //broken lines
        Vec3d spawnPos = owner.getEyePos().add(lookDirection.multiply(15));
        this.refreshPositionAndAngles(spawnPos.x, lockedY, spawnPos.z, owner.getYaw(), 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        ServerWorld world = (ServerWorld) this.getWorld();
        Vec3d currentPos;
        if (this.hasGravity){
            currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());
        } else {
            currentPos = new Vec3d(this.getX(), lockedY, this.getZ());
        }

        if (tickCounter < 20 && this.hasWarmup && this.owner != null) {
            world.spawnParticles(BLUE_DUST, currentPos.x + 2, lockedY, currentPos.z, 30, 0.3, 0.3, 0.3, 0.1);
            owner.teleport(world, ownerPos.x, ownerPos.y, ownerPos.z, owner.getYaw(), owner.getPitch());

        } else if (tickCounter < 40 && this.hasWarmup && this.owner != null) {
            world.spawnParticles(RED_DUST, currentPos.x - 2, lockedY, currentPos.z, 30, 0.3, 0.3, 0.3, 0.1);
            owner.teleport(world, ownerPos.x, ownerPos.y, ownerPos.z, owner.getYaw(), owner.getPitch());

        } else if (tickCounter < 60 && this.hasWarmup && this.owner != null) {
            world.spawnParticles(PURPLE_DUST, currentPos.x, lockedY, currentPos.z, 50, 0.35, 0.35, 0.35, 0.15);
            owner.teleport(world, ownerPos.x, ownerPos.y, ownerPos.z, owner.getYaw(), owner.getPitch());

        } else {
            Vec3d newPos = currentPos.add(direction.multiply(0.8)); // Adjust speed as needed
            if (this.hasGravity){
                this.setPosition(newPos.x, newPos.y, newPos.z);
            } else {
                this.setPosition(newPos.x, lockedY, newPos.z);
            }
            world.spawnParticles(PURPLE_DUST, currentPos.x, currentPos.y, currentPos.z, 100, 0.4, 0.4, 0.4, 0.2);
            detonate(world, 4, false);
        }

        // Handle detonation and custom block destruction
        int lifeTime = 160;
        if (tickCounter >= lifeTime) {
            detonate(world, 15, true);
        }
        tickCounter++;
    }
    private void detonate(ServerWorld world, int radius, boolean destroy) {
        BlockPos center = this.getBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();

                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance > radius) {
                        continue; // Skip positions outside the sphere
                    }

                    if (block == Blocks.BEDROCK || block == Blocks.END_PORTAL
                            || block == Blocks.END_PORTAL_FRAME || block == Blocks.END_GATEWAY) {
                        continue;
                    }

                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }
        if (destroy) {
            world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 20.0F, false, World.ExplosionSourceType.BLOCK);

            for (int i = 0; i < 750; i++) {
                double XSpeed = (world.random.nextDouble() - 0.5) * 2.0 * 10.0;
                double YSpeed = (world.random.nextDouble() - 0.5) * 2.0 * 10.0;
                double ZSpeed = (world.random.nextDouble() - 0.5) * 2.0 * 10.0;
                world.spawnParticles(PURPLE_DUST, this.getX(), lockedY, this.getZ(), 1, XSpeed, YSpeed, ZSpeed, 10);
            }
            this.discard();
        }
    }
}