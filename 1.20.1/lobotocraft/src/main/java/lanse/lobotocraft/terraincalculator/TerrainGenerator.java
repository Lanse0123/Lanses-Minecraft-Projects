package lanse.lobotocraft.terraincalculator;

import lanse.lobotocraft.Lobotocraft;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.SimplexNoiseSampler;

import java.util.ArrayList;
import java.util.Random;

public class TerrainGenerator {

    public static ArrayList<Vec3d> centerSmoothingPosList = new ArrayList<>();
    public static boolean randomizer = true;

    public static int getHeight(ServerWorld world, int x, int z, int highestY, String dimension) {
        int height = highestY;

        switch (dimension) {
            case "OVERWORLD", "NETHER", "END" -> {
                int generatedTerrainHeight = TerrainPresets.getHeight(x, z);
                double smoothingFactor = 1.0;  // Full effect by default
                for (Vec3d center : centerSmoothingPosList) {
                    double distance = center.distanceTo(new Vec3d(x + 0.5, center.y, z + 0.5));

                    if (distance <= 32) {
                        smoothingFactor = distance / 32.0;
                        break;
                    }
                }

                // Blend the current height (highestY) and the new generated terrain height
                height = (int) ((1 - smoothingFactor) * highestY + smoothingFactor * generatedTerrainHeight);
            }

            case "nether" -> {

                //TODO - idk how I will make the nether yet. For now, they are the same.

            }

            case "end" -> {

                //TODO - this will be hell you know. I guess I can place end city structures randomly
                // if the player is far enough from 0,0. Therefore I should split the End dimension into
                // the Main end island, the void ring, and the outer end islands.

            }
        }
        return height;
    }

    public static void randomizeSeed(){
        Random random = new Random();
        TerrainPresets.seed = random.nextLong();
        TerrainPresets.noiseSampler = new SimplexNoiseSampler(net.minecraft.util.math.random.Random.create(TerrainPresets.seed));
    }

    public static void getSettings(ServerCommandSource source) {
        String preset = String.valueOf(TerrainPresets.preset);
        String mode = Lobotocraft.currentMode.name();

        String settingsMessage = String.format(
                """     
                        Dementiacraft Settings:
                        
                        - Preset: %s
                        - Mode: %s""",
                preset, mode
        );
        source.sendFeedback(() -> Text.literal(settingsMessage), false);
    }
}