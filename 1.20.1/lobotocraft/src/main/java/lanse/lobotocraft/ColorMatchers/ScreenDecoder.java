package lanse.lobotocraft.ColorMatchers;

import lanse.lobotocraft.ColorMatchers.ColorPicker;
import lanse.lobotocraft.Lobotocraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ScreenDecoder {
    private static final int GRID_SIZE = 20; // Number of rays per row/column
    private static final int MAX_DISTANCE = 64;
    private static final float FOV = 120.0F;

    public static void analyzeScreen(ServerWorld world, PlayerEntity player) {

        if (Lobotocraft.currentMode != Lobotocraft.Mode.LOBOTOMY) return;

        // Player's position and view angles
        Vec3d eyePosition = player.getEyePos();
        Vec3d lookDirection = player.getRotationVec(1.0F);

        // Calculate grid step size in radians
        double horizontalStep = Math.toRadians(FOV) / GRID_SIZE;
        double verticalStep = Math.toRadians(FOV) / GRID_SIZE;

        // Iterate through the grid
        for (int x = -GRID_SIZE / 2; x < GRID_SIZE / 2; x++) {
            for (int y = -GRID_SIZE / 2; y < GRID_SIZE / 2; y++) {

                double yawOffset = x * horizontalStep;
                double pitchOffset = y * verticalStep;
                Vec3d rayDirection = lookDirection.rotateY((float) yawOffset).rotateX((float) pitchOffset);

                // do raycasting
                Vec3d target = eyePosition.add(rayDirection.multiply(MAX_DISTANCE));
                BlockHitResult hitResult = world.raycast(new RaycastContext(
                        eyePosition, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player
                ));

                // get the block hit by the ray
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockPos = hitResult.getBlockPos();
                    ColorPicker.getColor(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
                }
            }
        }
    }

    public static void clearScreenSeletion(){
        ColorPicker.visibleBlocks.clear();
        ColorPicker.colorAverage = new double[]{0.0, 0.0, 0.0};
        ColorPicker.colorAverageDisplay = new int[]{0, 0, 0};
        ColorPicker.colorCount = 0;
    }
}