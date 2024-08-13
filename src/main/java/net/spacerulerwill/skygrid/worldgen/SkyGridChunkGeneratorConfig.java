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

public record SkyGridChunkGeneratorConfig(LinkedHashMap<Block, Integer> blocks, List<EntityType<?>> spawnerEntities) {
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
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities").forGetter(SkyGridChunkGeneratorConfig::spawnerEntities)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );

    public static SkyGridChunkGeneratorConfig getDefaultOverworldConfig() {
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.STONE, 120);
        blocks.put(Blocks.GRASS_BLOCK, 80);
        blocks.put(Blocks.DIRT, 20);
        blocks.put(Blocks.WATER, 10);
        blocks.put(Blocks.LAVA, 5);
        blocks.put(Blocks.SAND, 20);
        blocks.put(Blocks.GRAVEL, 10);
        blocks.put(Blocks.COAL_ORE, 40);
        blocks.put(Blocks.LAPIS_ORE, 5);
        blocks.put(Blocks.IRON_ORE, 20);
        blocks.put(Blocks.GOLD_ORE, 10);
        blocks.put(Blocks.REDSTONE_ORE, 9);
        blocks.put(Blocks.DIAMOND_ORE, 1);
        blocks.put(Blocks.OAK_LOG, 100);
        blocks.put(Blocks.OAK_LEAVES, 40);
        blocks.put(Blocks.GLASS, 1);
        blocks.put(Blocks.SANDSTONE, 10);
        blocks.put(Blocks.STICKY_PISTON, 1);
        blocks.put(Blocks.COBWEB, 10);
        blocks.put(Blocks.DEAD_BUSH, 3);
        blocks.put(Blocks.PISTON, 1);
        blocks.put(Blocks.WHITE_WOOL, 25);
        blocks.put(Blocks.DANDELION, 2);
        blocks.put(Blocks.POPPY, 2);
        blocks.put(Blocks.BROWN_MUSHROOM, 2);
        blocks.put(Blocks.RED_MUSHROOM, 2);
        blocks.put(Blocks.TNT, 2);
        blocks.put(Blocks.BOOKSHELF, 3);
        blocks.put(Blocks.MOSSY_COBBLESTONE, 5);
        blocks.put(Blocks.OBSIDIAN, 5);
        blocks.put(Blocks.SPAWNER, 1);
        blocks.put(Blocks.CHEST, 1);
        blocks.put(Blocks.ICE, 4);
        blocks.put(Blocks.SNOW_BLOCK, 8);
        blocks.put(Blocks.CACTUS, 1);
        blocks.put(Blocks.CLAY, 20);
        blocks.put(Blocks.SUGAR_CANE, 15);
        blocks.put(Blocks.PUMPKIN, 5);
        blocks.put(Blocks.MELON, 5);
        blocks.put(Blocks.MYCELIUM, 15);

        List<EntityType<?>> entities = Arrays.asList(
                EntityType.CREEPER,
                EntityType.SKELETON,
                EntityType.SPIDER,
                EntityType.CAVE_SPIDER,
                EntityType.ZOMBIE,
                EntityType.SLIME,
                EntityType.PIG,
                EntityType.SHEEP,
                EntityType.COW,
                EntityType.CHICKEN,
                EntityType.SQUID,
                EntityType.WOLF,
                EntityType.ENDERMAN,
                EntityType.SILVERFISH,
                EntityType.VILLAGER
        );

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }

    public static SkyGridChunkGeneratorConfig getDefaultNetherConfig() {
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.LAVA, 50);
        blocks.put(Blocks.GRAVEL, 30);
        blocks.put(Blocks.SPAWNER, 2);
        blocks.put(Blocks.NETHERRACK, 300);
        blocks.put(Blocks.SOUL_SAND, 100);
        blocks.put(Blocks.GLOWSTONE, 50);
        blocks.put(Blocks.NETHER_BRICKS, 30);
        blocks.put(Blocks.NETHER_BRICK_FENCE, 10);
        blocks.put(Blocks.NETHER_BRICK_STAIRS, 15);
        blocks.put(Blocks.NETHER_WART, 30);

        List<EntityType<?>> entities = Arrays.asList(
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE
        );

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }


    public static SkyGridChunkGeneratorConfig getDefaultEndConfig() {
        // Create a map to store block weights
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.END_STONE, 1);

        return new SkyGridChunkGeneratorConfig(blocks, List.of());
    }


    public static SkyGridChunkGeneratorConfig getDefaultConfigForModded() {
        return new SkyGridChunkGeneratorConfig(
                new LinkedHashMap<>(), List.of()
        );
    }
}

