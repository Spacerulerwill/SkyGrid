package net.spacerulerwill.skygrid;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid.worldgen.SkyGridPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class SkyGrid implements ModInitializer {
	public static final String MOD_ID = "skygrid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ArrayList<SkyGridPreset> presets = new ArrayList<>();
	public static SkyGridPreset defaultPreset;

	@Override
	public void onInitialize() {
		Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(MOD_ID, "skygrid"), SkyGridChunkGenerator.MAP_CODEC);
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of(MOD_ID, "presets");
			}

			@Override
			public void reload(ResourceManager manager) {
				// Load all presets
				LOGGER.debug("Loading presets");
				for (Map.Entry<Identifier, Resource> entry : manager.findResources("presets", path -> path.toString().endsWith(".json")).entrySet()) {
					Identifier identifier = entry.getKey();
					Resource resource = entry.getValue();
					try (InputStream stream = resource.getInputStream()) {
						LOGGER.debug("Loading preset {}", identifier);
						JsonElement json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
						SkyGridPreset preset = SkyGridPreset.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
						presets.add(preset);
						if (identifier.equals(Identifier.of(MOD_ID, "presets/classic.json"))) {
							defaultPreset = preset;
						}
						LOGGER.info("Loaded preset {}", identifier);
					} catch (Exception e) {
						LOGGER.error("Error occurred while loading preset json {}", identifier, e);
					}
				}
				LOGGER.info("Loaded presets");
			}
		});
		LOGGER.info("SkyGrid mod is initialised!");
	}
}