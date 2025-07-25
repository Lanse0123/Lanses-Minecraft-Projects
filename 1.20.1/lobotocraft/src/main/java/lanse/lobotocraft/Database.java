package lanse.lobotocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lanse.lobotocraft.terraincalculator.TerrainPresets;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Database {
    public static boolean changed = false;

    public static Database getOrCreate(MinecraftServer server) {

        Path configFolder = server.getRunDirectory().toPath().resolve("config/lobotocraft");
        Path configFile = configFolder.resolve("config.json");

        // Check if the JSON config file exists
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException ignored) {}
        }
        return new Database();
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

        json.addProperty("terrainPreset", TerrainPresets.preset);
        json.addProperty("mode", Lobotocraft.currentMode.name());

        changed = false;
        return json;
    }

    //Load data from JSON
    public static void fromJson(JsonObject json) {

        TerrainPresets.preset = getInt(json, "terrainPreset", -1);

        String modeString = getString(json, "mode", "DEMENTIA");
        try { Lobotocraft.currentMode = Lobotocraft.Mode.valueOf(modeString);
        } catch (IllegalArgumentException e) { Lobotocraft.currentMode = Lobotocraft.Mode.DEMENTIA; }

        //More future variables here if I ever update this
    }

    private static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : defaultValue;
    }

    private static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : defaultValue;
    }
}