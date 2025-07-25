package lanse.imageworld.imagecalculator;

import lanse.imageworld.ImageWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreenshotHelper {
    private static boolean capturing = false;
    private static boolean captured = false;

    public static void captureScreenshot() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || capturing) return;

        capturing = true;
        captured = false;

        // Get video name from ImageCalculator (or default to "Untitled")
        String videoName = (ImageCalculator.videoName == null || ImageCalculator.videoName.isEmpty())
                ? "Untitled" : ImageCalculator.videoName;

        // Get system Minecraft directory
        String userHome = System.getProperty("user.home");
        Path basePath;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows: AppData/.minecraft
            basePath = Paths.get(System.getenv("APPDATA"), ".minecraft");
        } else {
            // macOS/Linux
            basePath = Paths.get(userHome, ".minecraft");
        }

        Path screenshotDir = basePath.resolve("ImageWorld/FrameVideos").resolve(videoName);

        // Ensure directories exist
        try {
            if (!ImageConverter.isUsingLargeImage) Files.createDirectories(screenshotDir);
        } catch (Exception e) {
            e.printStackTrace();
            ImageWorld.originalPlayer.sendMessage(Text.of("Failed to create screenshot directory!"), false);
            capturing = false;
            return;
        }

        // Name file by current frame index
        Path screenshotPath = screenshotDir.resolve("frame_" + ImageCalculator.currentFrameIndex + ".png");

        if (!ImageConverter.isUsingLargeImage){
            ScreenshotRecorder.saveScreenshot(
                    screenshotDir.toFile(),
                    screenshotPath.getFileName().toString(),
                    client.getFramebuffer(),
                    result -> {
                        capturing = false;
                        captured = true;
                    }
            );
        } else {
           capturing = false;
           captured = true;
        }
    }
    public static boolean isCapturing() {
        return capturing;
    }
    public static boolean hasScreenshotBeenTaken() { return captured; }
    public static void resetScreenshotFlag() { captured = false; }
}
