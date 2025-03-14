package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

import java.util.*;

public record SkyGridChunkGeneratorConfig(Map<Block, Integer> blocks, LinkedHashSet<EntityType<?>> spawnerEntities, Map<Item, Integer> chestItems) {
    static Codec<Map<Block, Integer>> BLOCK_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.BLOCK.getCodec(), Codecs.POSITIVE_INT)
            .xmap(HashMap::new, map -> map);
    static Codec<Map<Item, Integer>> ITEM_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.ITEM.getCodec(), Codecs.POSITIVE_INT)
            .xmap(HashMap::new, map -> map);

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
}

