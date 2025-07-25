package lanse.fractalworld.FractalCalculator;

import lanse.fractalworld.ChunkProcessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class AutoZoomScroller {

    public static boolean AutoZoomScrollerIsEnabled = false;
    public static ServerPlayerEntity targetZoomPlayer = null;

    public static void AutoZoomCheck() {
        if (targetZoomPlayer == null || !AutoZoomScrollerIsEnabled) {
            return;
        }

        ServerWorld world = targetZoomPlayer.getServerWorld();
        BlockPos playerPos = targetZoomPlayer.getBlockPos();

        // Find the surface height at the player's X,Z position
        int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, playerPos.getX(), playerPos.getZ());
        int playerY = playerPos.getY();
        int heightAboveSurface = playerY - surfaceY;

        if (heightAboveSurface < 20) {
            ZoomIn(surfaceY);
        } else if (heightAboveSurface > 300) {
            ZoomOut(surfaceY);
        }
    }

    public static void ZoomIn(int surfaceY) {
        if (targetZoomPlayer != null) {
            ChunkProcessor.clearProcessedChunks();
            double[] complexCoords = FractalGenerator.findComplexCoordinates(targetZoomPlayer.getX(), targetZoomPlayer.getZ());
            FractalGenerator.xOffset = -complexCoords[0];
            FractalGenerator.zOffset = -complexCoords[1];
            FractalGenerator.setScale( FractalGenerator.playerScale * 5);

            targetZoomPlayer.teleport(targetZoomPlayer.getServerWorld(), 0, surfaceY + 250, 0, targetZoomPlayer.getYaw(), targetZoomPlayer.getPitch());
        }
    }

    public static void ZoomOut(int surfaceY) {
        if (targetZoomPlayer != null) {
            ChunkProcessor.clearProcessedChunks();
            double[] complexCoords = FractalGenerator.findComplexCoordinates(targetZoomPlayer.getX(), targetZoomPlayer.getZ());
            FractalGenerator.xOffset = -complexCoords[0];
            FractalGenerator.zOffset = -complexCoords[1];
            FractalGenerator.setScale((double) FractalGenerator.playerScale / 5);

            targetZoomPlayer.teleport(targetZoomPlayer.getServerWorld(), 0, surfaceY + 250, 0, targetZoomPlayer.getYaw(), targetZoomPlayer.getPitch());
        }
    }
}
