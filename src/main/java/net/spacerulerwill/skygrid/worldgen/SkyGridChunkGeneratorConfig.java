package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.spacerulerwill.skygrid.util.BlockWeight;

import java.util.*;
import java.util.stream.Collectors;

public record SkyGridChunkGeneratorConfig(LinkedHashMap<Block, Integer> blocks, LinkedHashSet<EntityType<?>> spawnerEntities) {
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    // Custom codec for blocks
                    Codec.list(BlockWeight.CODEC).fieldOf("blocks")
                            .xmap(
                                    list -> list.stream()
                                            .collect(Collectors.toMap(
                                                    BlockWeight::block,  // Block is the key
                                                    BlockWeight::weight, // Double is the value
                                                    (existing, replacement) -> existing,  // Resolve conflicts (if any)
                                                    LinkedHashMap::new  // Preserve insertion order
                                            )),
                                    map -> map.entrySet().stream()
                                            .map(entry -> new BlockWeight(entry.getKey(), entry.getValue()))
                                            .collect(Collectors.toList())
                            )
                            .forGetter(SkyGridChunkGeneratorConfig::blocks),

                    // Codec for spawner entities
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities")
                            .xmap(
                                    LinkedHashSet::new,  // Convert List to LinkedHashSet
                                    ArrayList::new  // Convert LinkedHashSet to List for serialization
                            )
                            .forGetter(SkyGridChunkGeneratorConfig::spawnerEntities)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );
}

