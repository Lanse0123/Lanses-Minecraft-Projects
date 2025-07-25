package lanse.lobotocraft.ColorMatchers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lanse.lobotocraft.Lobotocraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ColorPicker {

    public static double[] colorAverage = new double[]{0.0, 0.0, 0.0};
    public static int[] colorAverageDisplay = new int[]{0, 0, 0};
    public static int colorCount = 0;
    private static final Map<String, int[]> blockColors;
    public static final Set<Block> visibleBlocks = new HashSet<>(); // set for visible blocks
    public static final Set<Block> closeBlocks = new HashSet<>(); // Blocks close in color
    public static int colorThreshold = 30;

    static {
        InputStreamReader reader = new InputStreamReader((ColorPicker.class.getResourceAsStream("/assets/lobotocraft/block_colors.json")));
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, int[]>>() {}.getType();
        blockColors = gson.fromJson(reader, type);
    }

    public static void getColor(ServerWorld world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.AIR) return;

        String blockName = block.getTranslationKey().replace("block.minecraft.", "");
        int[] rgb = blockColors.get(blockName);

        if (rgb != null) {
            colorCount++;
            colorAverage[0] = ((colorAverage[0] * (colorCount - 1)) + rgb[0]) / colorCount;
            colorAverage[1] = ((colorAverage[1] * (colorCount - 1)) + rgb[1]) / colorCount;
            colorAverage[2] = ((colorAverage[2] * (colorCount - 1)) + rgb[2]) / colorCount;

            colorAverageDisplay = new int[]{
                    (int) Math.round(colorAverage[0]),
                    (int) Math.round(colorAverage[1]),
                    (int) Math.round(colorAverage[2])
            };
            visibleBlocks.add(block);
        }
    }

    public static void findCloseBlocks() {

        if (Lobotocraft.currentMode == Lobotocraft.Mode.DEMENTIA){
            //BiomeMatcher.matchBiomeToColor(colorAverageDisplay);
            return;
        }

        closeBlocks.clear(); // Clear any previous results
        for (Map.Entry<String, int[]> entry : blockColors.entrySet()) {
            String blockName = entry.getKey();
            int[] rgb = entry.getValue();
            double distance = calculateColorDistance(rgb, colorAverageDisplay);

            // If within threshold, add the block to the closeBlocks set
            if (distance <= colorThreshold) {
                // Convert block name back to Block instance
                Block block = Registries.BLOCK.get(new Identifier("minecraft", blockName));
                closeBlocks.add(block);
            }
        }
    }

    private static double calculateColorDistance(int[] color1, int[] color2) {
        return Math.sqrt(Math.pow(color1[0] - color2[0], 2) +
                        Math.pow(color1[1] - color2[1], 2) +
                        Math.pow(color1[2] - color2[2], 2));
    }
}