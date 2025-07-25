package lanse.lobotocraft.ColorMatchers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.world.ServerWorld;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class BiomeMatcher {
    private static final Map<String, Map<String, int[]>> biomeColors;
    public static String matchedBiome;

    static {
        InputStreamReader reader = new InputStreamReader(BiomeMatcher.class.getResourceAsStream("/assets/lobotocraft/biome_colors.json"));
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, int[]>>>() {}.getType();
        biomeColors = gson.fromJson(reader, type);
    }

    //TODO - get the dimension the player is in, and use that for comparing. If the player is in the end,
    // Do nothing. If they are overworld, use overworld list. Nether is Nether list.

    public static void matchBiomeToColor(int[] averageColor) {
        double closestDistance = Double.MAX_VALUE;
        String closestBiome = null;

        // Iterate through all categories (Overworld, Nether, End)
        for (Map.Entry<String, Map<String, int[]>> categoryEntry : biomeColors.entrySet()) {
            String category = categoryEntry.getKey();
            Map<String, int[]> biomes = categoryEntry.getValue();

            // Iterate through each biome in the category
            for (Map.Entry<String, int[]> biomeEntry : biomes.entrySet()) {
                String biomeName = biomeEntry.getKey();
                int[] biomeColor = biomeEntry.getValue();

                // Calculate the Euclidean distance between colors
                double distance = calculateColorDistance(averageColor, biomeColor);

                // Update the closest match if a smaller distance is found
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestBiome = biomeName;
                }
            }
        }

        // Store the matched biome
        matchedBiome = closestBiome;
    }

    private static double calculateColorDistance(int[] color1, int[] color2) {
        return Math.sqrt(
                Math.pow(color1[0] - color2[0], 2) +
                        Math.pow(color1[1] - color2[1], 2) +
                        Math.pow(color1[2] - color2[2], 2)
        );
    }
}