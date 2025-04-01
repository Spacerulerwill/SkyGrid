package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkyGridConfig(SkyGridChunkGeneratorConfig overworldConfig, SkyGridChunkGeneratorConfig netherConfig,
                            SkyGridChunkGeneratorConfig endConfig) {
    public static final Codec<SkyGridConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("overworld_config").forGetter(SkyGridConfig::overworldConfig),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("nether_config").forGetter(SkyGridConfig::netherConfig),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("end_config").forGetter(SkyGridConfig::endConfig)
            ).apply(instance, SkyGridConfig::new)
    );

    public SkyGridConfig(SkyGridConfig other) {
        this(
                new SkyGridChunkGeneratorConfig(other.overworldConfig),
                new SkyGridChunkGeneratorConfig(other.netherConfig),
                new SkyGridChunkGeneratorConfig(other.endConfig)
        );
    }
}