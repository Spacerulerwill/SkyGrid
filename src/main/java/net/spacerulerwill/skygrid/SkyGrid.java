package net.spacerulerwill.skygrid;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid.worldgen.SkyGridPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SkyGrid implements ModInitializer {
    public static final String MOD_ID = "skygrid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ArrayList<SkyGridPreset> PRESETS = new ArrayList<>();
    public static ArrayList<SkyGridPreset> CUSTOM_PRESETS = new ArrayList<>();
    public static SkyGridPreset DEFAULT_PRESET;

    public static void reloadCustomPresets() {
        CUSTOM_PRESETS.clear();
        LOGGER.debug("Loading custom presets");
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path modConfigDir = configDir.resolve(MOD_ID);

        // Create our config directory
        try {
            Files.createDirectories(modConfigDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create the mod's config directory: {}", modConfigDir, e);
            return;
        }

        // List the files in the mod's directory
        File[] directoryListing = modConfigDir.toFile().listFiles();
        if (directoryListing == null) {
            return;
        }

        // Process each custom preset
        for (File child : directoryListing) {
            if (!child.isFile() || !child.getPath().endsWith(".json")) {
                continue;
            }
            LOGGER.debug("Loading custom preset: {}", child.toPath());
            try {
                String fileContent = Files.readString(child.toPath());
                JsonElement json = JsonParser.parseString(fileContent);
                SkyGridPreset preset = SkyGridPreset.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                SkyGrid.CUSTOM_PRESETS.add(preset);
            } catch (Exception e) {
                LOGGER.error("Error loading while loading preset {}: {}", child.toPath(), e);
            }
            LOGGER.info("Loaded custom preset: {}", child.toPath());
        }
        LOGGER.info("Loaded custom presets");
    }

    public static List<SkyGridPreset> getAllPresets() {
        List<SkyGridPreset> all = new ArrayList<>();
        all.addAll(PRESETS);
        all.addAll(CUSTOM_PRESETS);
        return all;
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(MOD_ID, "skygrid"), SkyGridChunkGenerator.MAP_CODEC);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new PresetsReloadListener());
        reloadCustomPresets();
        LOGGER.info("SkyGrid mod is initialised!");
    }
}
