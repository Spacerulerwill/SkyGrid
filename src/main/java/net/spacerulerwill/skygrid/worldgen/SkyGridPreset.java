package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;
import net.spacerulerwill.skygrid.util.BlockWeight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public record SkyGridPreset(Item item, String name, SkyGridChunkGeneratorConfig overworldConfig, SkyGridChunkGeneratorConfig netherConfig, SkyGridChunkGeneratorConfig endConfig) {
    public static final Codec<SkyGridPreset> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Registries.ITEM.getCodec().fieldOf("item").forGetter(SkyGridPreset::item),
                    Codecs.NON_EMPTY_STRING.fieldOf("name").forGetter(SkyGridPreset::name),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("overworld_config").forGetter(SkyGridPreset::overworldConfig),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("nether_config").forGetter(SkyGridPreset::netherConfig),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("end_config").forGetter(SkyGridPreset::endConfig)
            ).apply(instance, SkyGridPreset::new)
    );
}