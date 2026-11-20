package dev.smoothhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager implements ModInitializer {
    public static class Config {
        public float speed = 25.0f;
    }

    private static Config config = new Config();
    public static Config getConfig() { return config; }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/smoothhud.json");
    public static final Logger LOGGER = LoggerFactory.getLogger("smoothhud");

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Config loadedConfig = GSON.fromJson(reader, Config.class);
                if (loadedConfig != null) {
                    config = loadedConfig;
                    return;
                }
                LOGGER.warn("config file is empty or contains null");
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("config file could not be read or parsed correctly; using defaults");
            }
        }
    }

    public static boolean saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
            return true;
        } catch (IOException e) {
            LOGGER.error("could not write config to file (got IOException)");
            return false;
        }
    }

    // load config directly in mod initialization
    @Override
    public void onInitialize() {
        loadConfig();
    }
}
