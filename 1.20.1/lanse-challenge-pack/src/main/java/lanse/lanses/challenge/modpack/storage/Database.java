package lanse.lanses.challenge.modpack.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Database {
    public static boolean changed = false;

    public static Database getOrCreate(MinecraftServer server) {

        Path configFolder = server.getRunDirectory().toPath().resolve("config/lcp_challenge_pack");
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

        json.addProperty("preset", MainControl.modPreset.name());

        return json;
    }

    //Load data from JSON
    public static void fromJson(JsonObject json) {

        MainControl.modPreset = MainControl.Preset.valueOf(getString(json, "preset", "none"));

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