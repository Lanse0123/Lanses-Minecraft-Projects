package lanse.imageworld.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lanse.imageworld.ImageWorld;
import lanse.imageworld.WorldEditor;
import lanse.imageworld.automata.Automata3D;
import lanse.imageworld.imagecalculator.ColorMapGenerator;
import lanse.imageworld.imagecalculator.CoordinateCalculator;
import lanse.imageworld.imagecalculator.ImageCalculator;
import lanse.imageworld.imagecalculator.ImageConverter;
import lanse.imageworld.imagecalculator.worldpresets.FullColorMesaPreset;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Database {
    public static boolean changed = false;

    public static Database getOrCreate(MinecraftServer server) {
        Path configFolder = server.getRunDirectory().toPath().resolve("config/imageworld");
        Path configFile = configFolder.resolve("config.json");

        // Check if the JSON config file exists
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException ignored) {}
        }
        return new Database();
    }

    //TODO - add the other new settings here.
    public static void getSettings(ServerCommandSource source) {
        int chunkLoadingSpeed = ImageWorld.maxColumnsPerTick;
        boolean permaSave = ImageWorld.permaSave;
        int frameIndex = ImageCalculator.currentFrameIndex;
        String videoName = ImageCalculator.videoName;
        int frameHeight = ImageConverter.targetHeight;
        int frameWidth = ImageConverter.targetWidth;

        String settingsMessage = String.format(
                """     
                        Image World Settings:
                       
                        - Chunk Loading Speed: %d
                        - Permanent Save Enabled: %b
                        - Current Frame Index: %d
                        - Video Name: %s
                        - Frame Height: %d
                        - Frame Width: %d""",
                chunkLoadingSpeed, permaSave, frameIndex, videoName, frameHeight,
                frameWidth
        );
        source.sendFeedback(() -> Text.literal(settingsMessage), false);
    }

    //Save Data to JSON
    public void saveToJson(Path configFolder) {
        Path configFile = configFolder.resolve("config.json");

        try {
            //TODO - I AM SURE THIS COULD BE BETTER

            //To overwrite data, delete the current file.
            if (Files.exists(configFile)) {
                Files.delete(configFile);
            }

            Files.createFile(configFile);

            try (Writer writer = Files.newBufferedWriter(configFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject jsonConfig = toJson();
                gson.toJson(jsonConfig, writer);
            }
        } catch (IOException ignored) {}
    }

    //Save Data to JSON
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("maxColumnsPerTick", ImageWorld.maxColumnsPerTick);
        json.addProperty("pauseTimerSeconds", ImageWorld.pauseTimerLength);

        json.addProperty("coordinateMode", CoordinateCalculator.coordinateMode.name());

        json.addProperty("currentFramePixelWidth", ImageCalculator.currentFramePixelWidth);
        json.addProperty("currentFramePixelHeight", ImageCalculator.currentFramePixelHeight);
        json.addProperty("currentFrameIndex", ImageCalculator.currentFrameIndex);
        json.addProperty("videoName", ImageCalculator.videoName);

        json.addProperty("inputFolder", ImageConverter.inputFolder);
        json.addProperty("inputFile", ImageConverter.inputFile);
        json.addProperty("targetHeight", ImageConverter.targetHeight);
        json.addProperty("targetWidth", ImageConverter.targetWidth);

        json.addProperty("AutomatacolorPalette", Automata3D.colorPallet);

        json.addProperty("colorPalette", WorldEditor.colorPalette.name());
        json.addProperty("clampHeight", WorldEditor.clampHeight);

        json.addProperty("colorMapScale", ColorMapGenerator.biomeScale);

        json.addProperty("naturalMesaTop", FullColorMesaPreset.naturalMesaTop);

        json.addProperty("skyblockMode", Automata3D.skyblockMode);

        changed = false;
        return json;
    }

    //Load data from JSON
    public static void fromJson(JsonObject json) {

        ImageWorld.maxColumnsPerTick = getInt(json, "maxColumnsPerTick", 50);
        ImageWorld.pauseTimerLength = getInt(json, "pauseTimerSeconds", 200);

        String coordinatemodeString = getString(json, "coordinateMode", "LINE");
        try { CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.valueOf(coordinatemodeString);
        } catch (IllegalArgumentException e) { CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.LINE; }

        ImageCalculator.currentFramePixelWidth = getInt(json, "currentFramePixelWidth", 240);
        ImageCalculator.currentFramePixelHeight = getInt(json, "currentFramePixelHeight", 184);
        ImageCalculator.currentFrameIndex = getInt(json, "currentFrameIndex", 1);
        ImageCalculator.videoName = getString(json, "videoName", "Untitled");

        ImageConverter.inputFolder = getString(json, "inputFolder", "UNKNOWN");
        ImageConverter.inputFile = getString(json, "inputFile", "UNKNOWN");
        ImageConverter.targetHeight = getInt(json, "targetHeight", 240);
        ImageConverter.targetWidth = getInt(json, "targetWidth", 184);

        Automata3D.colorPallet = getString(json, "AutomatacolorPalette", "");
        Automata3D.setColorPalette(Automata3D.colorPallet);

        String colorPaletteString = getString(json, "colorPalette", "BLACK_AND_WHITE");
        try { WorldEditor.colorPalette = WorldEditor.ColorPalette.valueOf(colorPaletteString);
        } catch (IllegalArgumentException e) { WorldEditor.colorPalette = WorldEditor.ColorPalette.BLACK_AND_WHITE; }
        WorldEditor.clampHeight = getBoolean(json, "clampHeight", true);

        ColorMapGenerator.biomeScale = getDouble(json, "colorMapScale", 500);

        FullColorMesaPreset.naturalMesaTop = getBoolean(json, "naturalMesaTop", false);

        Automata3D.skyblockMode = getBoolean(json, "skyblockMode", false);

        //More future variables here if I ever update this
    }

    public static void returnToDefaultValues() {
        ImageWorld.maxColumnsPerTick = 50;
        ImageWorld.pauseTimerLength = 200;

        CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.LINE;

        ImageCalculator.currentFramePixelWidth = 240;
        ImageCalculator.currentFramePixelHeight = 184;
        ImageCalculator.currentFrameIndex = 1;
        ImageCalculator.videoName = "Untitled";

        ImageConverter.inputFolder = "UNKNOWN";
        ImageConverter.inputFile = "UNKNOWN";
        ImageConverter.targetHeight = 240;
        ImageConverter.targetWidth = 184;

        Automata3D.colorPallet = "full_rainbow";

        WorldEditor.colorPalette = WorldEditor.ColorPalette.BLACK_AND_WHITE;
        WorldEditor.clampHeight = true;

        ColorMapGenerator.biomeScale = 500;

        FullColorMesaPreset.naturalMesaTop = false;

        Automata3D.skyblockMode = false;
    }

    private static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : defaultValue;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : defaultValue;
    }

    private static double getDouble(JsonObject json, String key, double defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsDouble() : defaultValue;
    }

    private static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : defaultValue;
    }
}