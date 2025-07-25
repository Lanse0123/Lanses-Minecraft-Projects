package lanse.imageworld.imagecalculator;

import java.awt.*;

public class ColorMapGenerator {

    public static double biomeScale = 500;
    public static boolean isUsingColorMap = false;

    public static Color getRandomMapColor(int x, int z){
        //magic
        double fx = x / biomeScale;
        double fy = z / biomeScale;
        int xInt = (int) Math.floor(fx);
        int yInt = (int) Math.floor(fy);
        double xFrac = fx - xInt;
        double yFrac = fy - yInt;
        int seed1 = (xInt * 49632) ^ (yInt * 325176) ^ 1337;
        seed1 = (seed1 << 13) ^ seed1;
        int hash1 = (seed1 * (seed1 * seed1 * 15731 + 789221) + 1376312589) & 0x7fffffff;
        double v1 = (hash1 / 1073741824.0) % 1.0;
        int seed2 = ((xInt + 1) * 49632) ^ (yInt * 325176) ^ 1337;
        seed2 = (seed2 << 13) ^ seed2;
        int hash2 = (seed2 * (seed2 * seed2 * 15731 + 789221) + 1376312589) & 0x7fffffff;
        double v2 = (hash2 / 1073741824.0) % 1.0;
        int seed3 = (xInt * 49632) ^ ((yInt + 1) * 325176) ^ 1337;
        seed3 = (seed3 << 13) ^ seed3;
        int hash3 = (seed3 * (seed3 * seed3 * 15731 + 789221) + 1376312589) & 0x7fffffff;
        double v3 = (hash3 / 1073741824.0) % 1.0;
        int seed4 = ((xInt + 1) * 49632) ^ ((yInt + 1) * 325176) ^ 1337;
        seed4 = (seed4 << 13) ^ seed4;
        int hash4 = (seed4 * (seed4 * seed4 * 15731 + 789221) + 1376312589) & 0x7fffffff;
        double v4 = (hash4 / 1073741824.0) % 1.0;
        double u = xFrac * xFrac * xFrac * (xFrac * (xFrac * 6 - 15) + 10);
        double v = yFrac * yFrac * yFrac * (yFrac * (yFrac * 6 - 15) + 10);
        double i1 = v1 + u * (v2 - v1);
        double i2 = v3 + u * (v4 - v3);
        double noise = i1 + v * (i2 - i1);
        noise = Math.max(0.0, Math.min(1.0, noise));
        Color color;
        if (noise < 0.1) {
            float brightness = (float) (noise / 0.1);
            color = new Color(brightness, brightness, brightness);
        } else if (noise > 0.9) {
            float brightness = (float) ((1.0 - noise) / 0.1);
            brightness = 1.0f - brightness;
            color = new Color(brightness, brightness, brightness);
        } else {
            float hue = (float) ((noise - 0.1) / 0.8);
            float saturation = 1.0f;
            float brightness = 1.0f;
            color = Color.getHSBColor(hue, saturation, brightness);
        }
        return color;
    }
}
