package net.spacerulerwill.skygrid;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid.worldgen.SkyGridPreset;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PresetsReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.of(SkyGrid.MOD_ID, "presets");
    }

    @Override
    public void reload(ResourceManager manager) {
        // Load all presets
        SkyGrid.LOGGER.debug("Loading presets");
        for (Map.Entry<Identifier, Resource> entry : manager.findResources("presets", path -> path.toString().endsWith(".json")).entrySet()) {
            Identifier identifier = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.getInputStream()) {
                SkyGrid.LOGGER.debug("Loading preset {}", identifier);
                JsonElement json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                SkyGridPreset preset = SkyGridPreset.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                SkyGrid.PRESETS.add(preset);
                if (identifier.equals(Identifier.of(SkyGrid.MOD_ID, "presets/classic.json"))) {
                    SkyGrid.DEFAULT_PRESET = preset;
                }
                SkyGrid.LOGGER.info("Loaded preset {}", identifier);
            } catch (Exception e) {
                SkyGrid.LOGGER.error("Error occurred while loading preset json {}", identifier, e);
            }
        }
        SkyGrid.LOGGER.info("Loaded presets");
    }
}
