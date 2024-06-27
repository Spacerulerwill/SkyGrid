package net.spacerulerwill.skygrid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

import java.util.List;

public record SkyGridChunkGeneratorConfig(List<BlockWeight> blocks, List<EntityType<?>> spawnerEntities) {
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockWeight.CODEC.listOf().fieldOf("blocks").forGetter(SkyGridChunkGeneratorConfig::blocks),
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities").forGetter(SkyGridChunkGeneratorConfig::spawnerEntities)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );
}

