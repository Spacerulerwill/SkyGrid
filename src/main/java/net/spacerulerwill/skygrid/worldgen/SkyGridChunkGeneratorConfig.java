package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public record SkyGridChunkGeneratorConfig(LinkedHashMap<Block, Integer> blocks,
                                          LinkedHashSet<EntityType<?>> spawnerEntities,
                                          LinkedHashMap<Item, Integer> chestItems) {
    static Codec<LinkedHashMap<Block, Integer>> BLOCK_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.BLOCK.getCodec(), Codecs.POSITIVE_INT)
            .xmap(LinkedHashMap::new, map -> map);
    static Codec<LinkedHashMap<Item, Integer>> ITEM_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.ITEM.getCodec(), Codecs.POSITIVE_INT)
            .xmap(LinkedHashMap::new, map -> map);
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    // Custom codec for blocks
                    BLOCK_WEIGHT_MAP_CODEC.fieldOf("blocks").forGetter(SkyGridChunkGeneratorConfig::blocks),
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities")
                            .xmap(
                                    LinkedHashSet::new,
                                    ArrayList::new
                            )
                            .forGetter(SkyGridChunkGeneratorConfig::spawnerEntities),
                    ITEM_WEIGHT_MAP_CODEC.fieldOf("chest_items").forGetter(SkyGridChunkGeneratorConfig::chestItems)

            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );

    public SkyGridChunkGeneratorConfig(SkyGridChunkGeneratorConfig other) {
        this(
                new LinkedHashMap<>(other.blocks),
                new LinkedHashSet<>(other.spawnerEntities),
                new LinkedHashMap<>(other.chestItems)
        );
    }
}

