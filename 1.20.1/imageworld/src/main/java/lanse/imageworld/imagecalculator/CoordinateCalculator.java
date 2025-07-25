package lanse.imageworld.imagecalculator;

import lanse.imageworld.ImageWorld;

public class CoordinateCalculator {

    public enum CoordinateMode {
        LINE, BOX_SPIRAL, SINGLE, SINGLE_AT_LOCATION
    }

    public static CoordinateMode coordinateMode = CoordinateMode.LINE;

    public static void teleportToNextFrame(){
        switch (coordinateMode){
            case LINE -> {
                int frameWidthInBlocks = ImageCalculator.currentFramePixelWidth;
                int frameHeightInBlocks = ImageCalculator.currentFramePixelHeight;

                ImageCalculator.currentFrameStartX = ImageCalculator.currentFrameIndex * frameWidthInBlocks;
                ImageCalculator.currentFrameStartZ = 0;

                double playerX = ImageCalculator.currentFrameStartX + (frameWidthInBlocks / 2.0);
                double playerY = ImageWorld.originalPlayer.getY();
                double playerZ = ImageCalculator.currentFrameStartZ + (frameHeightInBlocks / 2.0);
                ImageWorld.originalPlayer.teleport(ImageWorld.originalPlayer.getServerWorld(), playerX, playerY,
                        playerZ, ImageWorld.originalPlayer.getYaw(), ImageWorld.originalPlayer.getPitch());
            }

            case BOX_SPIRAL -> {
                int frameWidthInBlocks = ImageCalculator.currentFramePixelWidth;
                int frameHeightInBlocks = ImageCalculator.currentFramePixelHeight;
                int n = ImageCalculator.currentFrameIndex;
                int layer = (int) Math.ceil((Math.sqrt(n) - 1) / 2.0); // Which layer of the spiral we're in
                int legLength = layer * 2; // How long each side of the square is
                int minInLayer = (2 * layer - 1) * (2 * layer - 1) + 1; // The minimum index in this layer
                int offset = n - minInLayer;

                int side = offset / legLength;
                int pos = offset % legLength;

                int x = 0;
                int z = 0;

                switch (side) {
                    case 0 -> { // Right side
                        x = layer;
                        z = -layer + 1 + pos;
                    }
                    case 1 -> { // Top side
                        x = layer - 1 - pos;
                        z = layer;
                    }
                    case 2 -> { // Left side
                        x = -layer;
                        z = layer - 1 - pos;
                    }
                    case 3 -> { // Bottom side
                        x = -layer + 1 + pos;
                        z = -layer;
                    }
                }
                ImageCalculator.currentFrameStartX = x * frameWidthInBlocks;
                ImageCalculator.currentFrameStartZ = z * frameHeightInBlocks;

                double playerX = ImageCalculator.currentFrameStartX + (frameWidthInBlocks / 2.0);
                double playerY = ImageWorld.originalPlayer.getY();
                double playerZ = ImageCalculator.currentFrameStartZ + (frameHeightInBlocks / 2.0);
                ImageWorld.originalPlayer.teleport(ImageWorld.originalPlayer.getServerWorld(), playerX, playerY,
                        playerZ, ImageWorld.originalPlayer.getYaw(), ImageWorld.originalPlayer.getPitch());
            }

            //TODO - allow one before disabling mod (IGNORE THIS)
            case SINGLE -> {/*find the nearest empty area (dont do this yet)*/
                int frameWidthInBlocks = ImageCalculator.currentFramePixelWidth;
                int frameHeightInBlocks = ImageCalculator.currentFramePixelHeight;

                int EditThisOneEventuallyIJustMadeThisVariableToRemoveTheErrorButItShouldBeTheTODOAbove = 0;
                double playerX = ImageWorld.originalPlayer.getX();
                double playerZ = ImageWorld.originalPlayer.getZ();

                ImageCalculator.currentFrameStartX = (int)(playerX - frameWidthInBlocks / 2.0);
                ImageCalculator.currentFrameStartZ = (int)(playerZ - frameHeightInBlocks / 2.0);
            }

            case SINGLE_AT_LOCATION -> {
                int frameWidthInBlocks = ImageCalculator.currentFramePixelWidth;
                int frameHeightInBlocks = ImageCalculator.currentFramePixelHeight;

                double playerX = ImageWorld.originalPlayer.getX();
                double playerZ = ImageWorld.originalPlayer.getZ();

                ImageCalculator.currentFrameStartX = (int)(playerX - frameWidthInBlocks / 2.0);
                ImageCalculator.currentFrameStartZ = (int)(playerZ - frameHeightInBlocks / 2.0);
            }
        }
    }
}
