package lanse.lobotocraft.terraincalculator;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;

public class TerrainPresets {

    public static long seed = 0;
    public static int preset = -1;
    public static double x = 0;
    public static double z = 0;
    public static int terrainCount = 18;
    public static SimplexNoiseSampler noiseSampler = new SimplexNoiseSampler(Random.create(seed));

    public static int getHeight(int x, int z) {
        int choice = (preset == -1) ? (int) (seed % terrainCount) + 1 : preset;
        TerrainPresets.x = x;
        TerrainPresets.z = z;

        return switch (choice) {
            case 1 -> ExtremeHills();
            case 2 -> BrokenSkyworld();
            case 3 -> RoundedBigExtremeHills();
            case 4 -> SpikeWorld();
            case 5 -> BrokenSkywave();
            case 6 -> AlmostAmplified();
            case 7 -> SomehowAWorldInverter();
            case 8 -> CursedRegular();
            case 9 -> SlightlyAmplified();
            case 10 -> BasaltValley();
            case 11 -> BrokenSkyPlateu();
            case 12 -> SubtleRidges();
            case 13 -> HillyPlains();
            case 14 -> BumpySpikes();
            case 15 -> ForestHills();
            case 16 -> GentleValleys();
            case 17 -> SpikeyHills();
            case 18 -> RollingPlains();

            default -> CursedRegular();
        };
    }

    public static int ExtremeHills() {
        return (int) (64 + 192 * noiseSampler.sample(x * 0.01, z * 0.01));
    }

    public static int BrokenSkyworld() {
        return (int) (64 + 96 * Math.sin(x * 0.05 + seed) * Math.cos(z * 0.05 + seed) + 96);
    }

    public static int RoundedBigExtremeHills() {
        double noise = noiseSampler.sample(x * 0.005, z * 0.005);
        return (int) (64 + 192 * Math.abs(noise));
    }

    public static int SpikeWorld() {
        double noise = noiseSampler.sample(x * 0.01, z * 0.01) * 0.5 +
                noiseSampler.sample(x * 0.05, z * 0.05) * 0.3 +
                noiseSampler.sample(x * 0.1, z * 0.1) * 0.2;
        return Math.min(256, Math.max(64, (int) (128 + 128 * noise)));
    }

    public static int BrokenSkywave() {
        int plateau = (x % 64 < 32 && z % 64 < 32) ? 180 : 100;
        double noise = noiseSampler.sample(x * 0.02, z * 0.02);
        return Math.min(256, Math.max(64, plateau + (int) (32 * noise)));
    }

    public static int AlmostAmplified() {
        double noise = noiseSampler.sample(x * 0.01, z * 0.01);
        return Math.min(256, Math.max(64, (int) (192 + 64 * noise)));
    }

    public static int SomehowAWorldInverter() {
        double dunes = Math.sin(x * 0.05) * Math.cos(z * 0.05);
        return (int) (64 + 32 * dunes);
    }

    public static int CursedRegular() {
        double noise = noiseSampler.sample(x * 0.01, z * 0.01);
        return Math.min(256, Math.max(64, (int) (80 + 16 * noise)));
    }

    public static int SlightlyAmplified() {
        double noise = noiseSampler.sample(x * 0.02, z * 0.02) * 0.5 +
                noiseSampler.sample(x * 0.05, z * 0.05) * 0.3;
        return Math.min(256, Math.max(64, (int) (100 + 48 * noise)));
    }

    public static int BasaltValley() {
        double base = 64 + 16 * noiseSampler.sample(x * 0.1, z * 0.1);
        double swampLakes = noiseSampler.sample(x * 0.02, z * 0.02) > 0 ? -8 : 8;
        return Math.min(256, Math.max(64, (int) (base + swampLakes)));
    }

    public static int BrokenSkyPlateu() {
        double distanceFromCenter = Math.sqrt(x * x + z * z) * 0.01;
        double crater = 1.0 / (distanceFromCenter + 0.1) - 8 * noiseSampler.sample(x * 0.01, z * 0.01);
        return Math.min(256, Math.max(64, (int) (200 + crater * 32)));
    }

    public static int SubtleRidges() {
        double ridge = Math.sin(x * 0.01) * 32 + noiseSampler.sample(x * 0.005, z * 0.005) * 16;  // Creates subtle ridges
        return Math.min(256, Math.max(64, (int) (80 + ridge)));
    }

    public static int HillyPlains() {
        double baseHeight = 72 + 16 * noiseSampler.sample(x * 0.01, z * 0.01);  // Low, consistent hills
        double detail = 2 * noiseSampler.sample(x * 0.1, z * 0.1);  // Adds very subtle randomness
        return Math.min(256, Math.max(64, (int) (baseHeight + detail)));
    }

    public static int BumpySpikes() {
        double plateauBase = 100 + 8 * noiseSampler.sample(x * 0.02, z * 0.02);  // Flat-ish plateau
        double minorNoise = 4 * noiseSampler.sample(x * 0.1, z * 0.1);  // Subtle noise for a natural look
        return Math.min(256, Math.max(64, (int) (plateauBase + minorNoise)));
    }

    public static int ForestHills() {
        double base = 64 + 24 * noiseSampler.sample(x * 0.01, z * 0.01);  // Rolling hills
        double detail = 4 * noiseSampler.sample(x * 0.1, z * 0.1);  // Adds minor bumpy variation
        return Math.min(256, Math.max(64, (int) (base + detail)));
    }

    public static int GentleValleys() {
        double baseHeight = 64 + 16 * noiseSampler.sample(x * 0.01, z * 0.01);  // Base terrain with smooth height changes
        double valleys = -20 * Math.cos(x * 0.01) * Math.sin(z * 0.01);  // Creates valley-like structures
        return Math.min(256, Math.max(64, (int) (baseHeight + valleys)));
    }

    public static int SpikeyHills() {
        double baseHeight = 64 + 32 * noiseSampler.sample(x * 0.01, z * 0.01);  // Gentle hills
        double smallDetail = 8 * noiseSampler.sample(x * 0.1, z * 0.1);  // Adds slight variation
        return Math.min(256, Math.max(64, (int) (baseHeight + smallDetail)));
    }

    public static int RollingPlains() {
        double wideNoise = noiseSampler.sample(x * 0.005, z * 0.005);  // Large-scale rolling hills
        double subtleNoise = noiseSampler.sample(x * 0.05, z * 0.05) * 0.2;  // Adds minor height differences
        return Math.min(256, Math.max(64, (int) (72 + 24 * wideNoise + subtleNoise)));
    }
}