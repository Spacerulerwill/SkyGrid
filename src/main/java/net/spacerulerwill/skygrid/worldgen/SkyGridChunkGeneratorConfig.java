package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

import java.util.*;

public record SkyGridChunkGeneratorConfig(Map<Block, Integer> blocks, LinkedHashSet<EntityType<?>> spawnerEntities) {

    static Codec<Map<Block, Integer>> BLOCK_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.BLOCK.getCodec(), Codecs.POSITIVE_INT);
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    // Custom codec for blocks
                    BLOCK_WEIGHT_MAP_CODEC.fieldOf("blocks").forGetter(SkyGridChunkGeneratorConfig::blocks),
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities")
                            .xmap(
                                    LinkedHashSet::new,  // Convert List to LinkedHashSet
                                    ArrayList::new  // Convert LinkedHashSet to List for serialization
                            )
                            .forGetter(SkyGridChunkGeneratorConfig::spawnerEntities)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );
}

