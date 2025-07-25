package lanse.lanican.custom.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.List;
import java.util.Random;

public class KirbyWandItem extends Item {

    private static final Random RANDOM = new Random();

    public KirbyWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand); // Makes the player hold right-click
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        // Stop dragging entities or handle falling blocks if needed
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            if (player.isUsingItem() && player.getActiveItem() == stack) {
                applyKirbyEffect(world, player);
            }
        }
    }

    private void applyKirbyEffect(World world, PlayerEntity player) {
        int redstoneCount = countRedstoneBlocks(player);
        double power = 1.0 + 0.1 * redstoneCount; // Base power is 1.0, with 0.1x per redstone block

        // Spawn particles moving toward the player
        Vec3d playerPos = player.getPos();
        for (int i = 0; i < 10; i++) {
            double offsetX = RANDOM.nextGaussian() * 0.5;
            double offsetY = RANDOM.nextGaussian() * 0.5;
            double offsetZ = RANDOM.nextGaussian() * 0.5;
            ((ServerWorld) world).spawnParticles(ParticleTypes.PORTAL,
                    playerPos.x + offsetX, playerPos.y + 1 + offsetY, playerPos.z + offsetZ,
                    1, 0, 0, 0, 0);
        }

        // Drag nearby entities toward the player
        Box pullBox = new Box(playerPos.add(-10, -10, -10), playerPos.add(10, 10, 10));
        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, pullBox, e -> e != player);
        for (Entity entity : nearbyEntities) {
            Vec3d direction = playerPos.subtract(entity.getPos()).normalize().multiply(power * 0.1);
            entity.addVelocity(direction.x, direction.y, direction.z);
            entity.velocityModified = true;

            // Kill entities that get too close
            if (entity.getPos().isInRange(playerPos, 2.0)) {
                entity.kill();
            }
        }

        // Occasionally summon falling block entities
        if (RANDOM.nextInt(20) < (int) power) {
            summonFallingBlock(world, player);
        }
    }

    private int countRedstoneBlocks(PlayerEntity player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == Items.REDSTONE_BLOCK) {
                count += stack.getCount();
            }
        }
        return count;
    }

    //TODO - fix this
    private void summonFallingBlock(World world, PlayerEntity player) {
//        BlockPos pos = player.getBlockPos().down(RANDOM.nextInt(5) + 1);
//        if (world.getBlockState(pos).isAir()) return;
//
//        BlockState blockState = world.getBlockState(pos);
//        world.setBlockState(pos, Blocks.AIR.getDefaultState()); // Remove the block
//
//        // Create the FallingBlockEntity manually
//        FallingBlockEntity fallingBlock = new FallingBlockEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, blockState);
//        fallingBlock.setHurtEntities(true); // Optional: Makes the block hurt entities it hits
//        fallingBlock.setVelocity(0, 0.5, 0); // Adjust velocity toward the player
//
//        world.spawnEntity(fallingBlock); // Spawn the entity in the world
    }
}