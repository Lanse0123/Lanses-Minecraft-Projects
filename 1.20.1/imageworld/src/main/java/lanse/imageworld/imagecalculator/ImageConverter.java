package lanse.imageworld.imagecalculator;

import lanse.imageworld.ImageWorld;
import lanse.imageworld.WorldEditor;
import net.minecraft.text.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class ImageConverter {
    public static String inputFile;
    public static String inputFolder;
    public static int targetWidth;
    public static int targetHeight;
    public static BufferedImage currentBufferedImage = null;
    public static boolean isUsingLargeImage = false;

    public enum ColorCategory {
        RED(new Color(183, 28, 28)),
        ORANGE(new Color(255, 87, 34)),
        YELLOW(new Color(255, 235, 59)),
        GREEN(new Color(76, 175, 80)),
        DARK_GREEN(new Color(0, 100, 0)),
        DARK_BLUE(new Color(20, 20, 180)),
        BLUE(new Color(33, 150, 243)),
        LIGHT_BLUE(new Color(80, 200, 255)),
        PINK(new Color(255, 105, 180)),
        PURPLE(new Color(156, 39, 176)),
        DARK_PURPLE(new Color(110, 0, 110)),
        BROWN(new Color(101, 65, 52)),
        BLACK(new Color(0, 0, 0)),
        WHITE(new Color(255, 255, 255)),
        GRAY(new Color(158, 158, 158));

        public final Color color;
        ColorCategory(Color color) {
            this.color = color;
        }
    }

    public void processCurrentFrame() {
        if (ImageWorld.originalPlayer == null) return;

        ImageCalculator.currentFrameIndex++;
        File frameFile = new File(inputFile);

        try {
            if (Objects.equals(inputFile, "UNKNOWN")) {
                frameFile = getFrameFile(ImageCalculator.currentFrameIndex);
                if (frameFile == null || !frameFile.exists()) {
                    ImageWorld.originalPlayer.sendMessage(Text.of("Frame not found: index " + ImageCalculator.currentFrameIndex + ". Stopping video creation."), false);
                    ImageWorld.isModEnabled = false;
                    return;
                }
            }

            BufferedImage image = ImageIO.read(frameFile);
            if (image == null) {
                ImageWorld.originalPlayer.sendMessage(Text.of("Failed to read frame image."), false);
                return;
            }

            currentBufferedImage = image;

            int[][] pixelData;
            if (!isUsingLargeImage) {
                BufferedImage resizedImage = resizeImage(image, targetWidth, targetHeight);
                pixelData = convertToPixelData(resizedImage);
                ImageCalculator.setCurrentFrameData(pixelData); // Only needed in small image mode
            }

            ImageWorld.originalPlayer.sendMessage(Text.of("Processed frame " + ImageCalculator.currentFrameIndex), false);

        } catch (Exception e) {
            e.printStackTrace();
            ImageWorld.originalPlayer.sendMessage(Text.of("Exception during frame processing."), false);
        }
    }

    private int[][] convertToPixelData(BufferedImage resizedImage) {
        int[][] pixelData = new int[targetHeight][targetWidth];

        if (WorldEditor.colorPalette == WorldEditor.ColorPalette.BLACK_AND_WHITE) {
            BufferedImage bwImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g2 = bwImage.createGraphics();
            g2.drawImage(resizedImage, 0, 0, null);
            g2.dispose();

            //somehow magically works
            for (int y = 0; y < targetHeight; y++) {
                for (int x = 0; x < targetWidth; x++) {
                    int color = bwImage.getRGB(x, y) & 0xFFFFFF;
                    pixelData[y][x] = (color == 0xFFFFFF) ? 1 : 0;
                }
            }
        } else {
            for (int y = 0; y < targetHeight; y++) {
                for (int x = 0; x < targetWidth; x++) {
                    int rgb = resizedImage.getRGB(x, y) & 0xFFFFFF;
                    Color actual = new Color(rgb);
                    int colorIndex = getClosestColorIndex(actual);
                    pixelData[y][x] = colorIndex;
                }
            }
        }
        return pixelData;
    }

    public static int getClosestColorIndex(Color actual) {
        Color[] comparisonColors;

        if (WorldEditor.colorPalette == WorldEditor.ColorPalette.BLACK_AND_WHITE) {
            comparisonColors = new Color[]{Color.BLACK, Color.WHITE};
        } else {
            ColorCategory[] palette = ColorCategory.values();
            comparisonColors = new Color[palette.length];
            for (int i = 0; i < palette.length; i++) {
                comparisonColors[i] = palette[i].color;
            }
        }

        double minDist = Double.MAX_VALUE;
        int bestIndex = 0;

        for (int i = 0; i < comparisonColors.length; i++) {
            double dist = colorDistance(actual, comparisonColors[i]);
            if (dist < minDist) {
                minDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public static Color getColorByIndex(int index) {
        ColorCategory[] categories = ColorCategory.values();
        if (index < 0 || index >= categories.length) {
            return Color.BLACK;
        }
        return categories[index].color;
    }

    private static double colorDistance(Color c1, Color c2) {
        int rDiff = c1.getRed() - c2.getRed();
        int gDiff = c1.getGreen() - c2.getGreen();
        int bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    private File getFrameFile(int index) {
        File folder = new File(inputFolder);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (files == null) return null;

        for (File file : files) {
            String digits = file.getName().replaceAll("\\D+", "");
            if (!digits.isEmpty() && Integer.parseInt(digits) == index) {
                return file;
            }
        }
        return null;
    }

    public static Color getColorAt(int worldX, int worldZ, boolean overrideColorMap) {

        if (ColorMapGenerator.isUsingColorMap || overrideColorMap){
            Color color = ColorMapGenerator.getRandomMapColor(worldX, worldZ);
            int index = getClosestColorIndex(color);
            return getColorByIndex(index);
        }

        //This is only used for large images
        int x = worldX - ImageCalculator.currentFrameStartX;
        int z = worldZ - ImageCalculator.currentFrameStartZ;

        //These are separate due to the history of testing and the roman empire. Segregation :D
        //Don't worry though. They are separate but equal :D
        if (currentBufferedImage == null) return null;

        if (x < 0 || z < 0 || x >= targetWidth || z >= targetHeight) return null;

        int imageWidth = currentBufferedImage.getWidth();
        int imageHeight = currentBufferedImage.getHeight();

        // There's a lot of scary math here because this function ate another function that used up 10 GB of Ram for a picture...
        int originalX = (int) ((x / (double) targetWidth) * imageWidth);
        int originalZ = (int) ((z / (double) targetHeight) * imageHeight);
        originalX = Math.min(originalX, imageWidth - 1);
        originalZ = Math.min(originalZ, imageHeight - 1);
        int rgb = currentBufferedImage.getRGB(originalX, originalZ) & 0xFFFFFF;
        Color actualColor = new Color(rgb);

        if (WorldEditor.colorPalette == WorldEditor.ColorPalette.BLACK_AND_WHITE) {
            return (rgb == 0xFFFFFF) ? Color.WHITE : Color.BLACK;
        } else {
            int index = getClosestColorIndex(actualColor);
            return getColorByIndex(index);
        }
    }
}
