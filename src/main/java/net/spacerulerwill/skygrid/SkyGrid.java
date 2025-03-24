package net.spacerulerwill.skygrid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid.worldgen.SkyGridPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class SkyGrid implements ModInitializer {
    public static final String MOD_ID = "skygrid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ArrayList<SkyGridPreset> PRESETS = new ArrayList<>();
    public static SkyGridPreset DEFAULT_PRESET;

    @Override
    public void onInitialize() {
        Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(MOD_ID, "skygrid"), SkyGridChunkGenerator.MAP_CODEC);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new PresetsReloadListener());
        LOGGER.info("SkyGrid mod is initialised!");
    }
}