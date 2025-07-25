package lanse.imageworld.imagecalculator;

import lanse.imageworld.ImageWorld;
import lanse.imageworld.WorldEditor;
import net.minecraft.text.Text;

import java.awt.*;

public class ImageCalculator {
    public static int currentFrameStartX;
    public static int currentFrameStartZ;
    public static int[][] currentFrameData;
    public static int currentFramePixelWidth = 240;
    public static int currentFramePixelHeight = 184;
    public static int currentFrameIndex = 1;
    public static boolean currentFrameComplete = true;
    public static String videoName = "Untitled";

    public static void setCurrentFrameData(int[][] pixelData) {
        if (ImageConverter.isUsingLargeImage) return;

        currentFrameData = pixelData;
        if (pixelData == null) {
            return;
        }

        currentFramePixelHeight = pixelData.length;
        currentFramePixelWidth = pixelData[0].length;

        // Add ChunkTasks to queue
        for (int y = 0; y < currentFramePixelHeight; y++) {
            for (int x = 0; x < currentFramePixelWidth; x++) {
                int pixel = pixelData[y][x];

                int worldX = currentFrameStartX + x;
                int worldZ = currentFrameStartZ + y;

                if (WorldEditor.colorPalette == WorldEditor.ColorPalette.BLACK_AND_WHITE) {
                    boolean isWhite = (pixel == 1);
                    Color color = isWhite ? Color.WHITE : Color.BLACK;

                    ImageWorld.processingQueue.add(new ImageWorld.ChunkTask(
                            ImageWorld.originalPlayer.getServerWorld(), worldX, worldZ, color, (short) 0
                    ));
                } else {
                    Color color = ImageConverter.getColorByIndex(pixel);
                    // Pass 0 = terrain base, Pass 1 = decoration. Add more later if needed.
                    ImageWorld.processingQueue.add(new ImageWorld.ChunkTask(
                            ImageWorld.originalPlayer.getServerWorld(), worldX, worldZ, color, (short) 0
                    ));
                    ImageWorld.processingQueue.add(new ImageWorld.ChunkTask(
                            ImageWorld.originalPlayer.getServerWorld(), worldX, worldZ, color, (short) 1
                    ));
                }
            }
        }
        ImageWorld.processingQueue.add(new ImageWorld.ChunkTask(ImageWorld.originalPlayer.getServerWorld(), 0, 0, Color.RED, (short) 999));
        ImageWorld.originalPlayer.sendMessage(Text.of("Loaded frame " + currentFrameIndex), false);
    }
}
