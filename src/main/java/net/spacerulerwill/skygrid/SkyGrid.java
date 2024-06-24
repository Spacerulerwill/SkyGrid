package net.spacerulerwill.skygrid;

import net.fabricmc.api.ModInitializer;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkyGrid implements ModInitializer {
	public static final String MOD_ID = "skygrid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(MOD_ID, "skygrid"), SkyGridChunkGenerator.MAP_CODEC);
		LOGGER.info("SkyGrid mod is initialised!");
	}
}