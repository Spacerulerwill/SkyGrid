package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.spacerulerwill.skygrid.util.BlockWeight;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record SkyGridChunkGeneratorConfig(List<BlockWeight> blocks, List<EntityType<?>> spawnerEntities) {
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockWeight.CODEC.listOf().fieldOf("blocks").forGetter(SkyGridChunkGeneratorConfig::blocks),
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities").forGetter(SkyGridChunkGeneratorConfig::spawnerEntities)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );

    public static SkyGridChunkGeneratorConfig getDefaultOverworldConfig() {
        return new SkyGridChunkGeneratorConfig(
                Arrays.asList(
                        new BlockWeight(Blocks.STONE, 120.0),
                        new BlockWeight(Blocks.GRASS_BLOCK, 80.0),
                        new BlockWeight(Blocks.DIRT, 20.0),
                        new BlockWeight(Blocks.WATER, 10.0),
                        new BlockWeight(Blocks.LAVA, 5.0),
                        new BlockWeight(Blocks.SAND, 20.0),
                        new BlockWeight(Blocks.GRAVEL, 10.0),
                        new BlockWeight(Blocks.COAL_ORE, 40.0),
                        new BlockWeight(Blocks.LAPIS_ORE, 5.0),
                        new BlockWeight(Blocks.IRON_ORE, 20.0),
                        new BlockWeight(Blocks.GOLD_ORE, 10.0),
                        new BlockWeight(Blocks.REDSTONE_ORE, 9.0),
                        new BlockWeight(Blocks.DIAMOND_ORE, 1.0),
                        new BlockWeight(Blocks.OAK_LOG, 100.0),
                        new BlockWeight(Blocks.OAK_LEAVES, 40.0),
                        new BlockWeight(Blocks.GLASS, 1.0),
                        new BlockWeight(Blocks.REDSTONE_ORE, 8.0),
                        new BlockWeight(Blocks.SANDSTONE, 10.0),
                        new BlockWeight(Blocks.STICKY_PISTON, 1.0),
                        new BlockWeight(Blocks.COBWEB, 10.0),
                        new BlockWeight(Blocks.DEAD_BUSH, 3.0),
                        new BlockWeight(Blocks.PISTON, 1.0),
                        new BlockWeight(Blocks.WHITE_WOOL, 25.0),
                        new BlockWeight(Blocks.DANDELION, 2.0),
                        new BlockWeight(Blocks.POPPY, 2.0),
                        new BlockWeight(Blocks.BROWN_MUSHROOM, 2.0),
                        new BlockWeight(Blocks.RED_MUSHROOM, 2.0),
                        new BlockWeight(Blocks.TNT, 2.0),
                        new BlockWeight(Blocks.BOOKSHELF, 3.0),
                        new BlockWeight(Blocks.MOSSY_COBBLESTONE, 5.0),
                        new BlockWeight(Blocks.OBSIDIAN, 5.0),
                        new BlockWeight(Blocks.SPAWNER, 1.0),
                        new BlockWeight(Blocks.CHEST, 1.0),
                        new BlockWeight(Blocks.ICE, 4.0),
                        new BlockWeight(Blocks.SNOW_BLOCK, 8.0),
                        new BlockWeight(Blocks.CACTUS, 1.0),
                        new BlockWeight(Blocks.CLAY, 20.0),
                        new BlockWeight(Blocks.SUGAR_CANE, 15.0),
                        new BlockWeight(Blocks.PUMPKIN, 5.0),
                        new BlockWeight(Blocks.MELON, 5.0),
                        new BlockWeight(Blocks.MYCELIUM, 15.0)
                ),
                Arrays.asList(
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
                )
        );
    }

    public static SkyGridChunkGeneratorConfig getDefaultNetherConfig() {
        return new SkyGridChunkGeneratorConfig(
                Arrays.asList(
                        new BlockWeight(Blocks.LAVA, 50.0),
                        new BlockWeight(Blocks.GRAVEL, 30.0),
                        new BlockWeight(Blocks.SPAWNER, 2.0),
                        new BlockWeight(Blocks.NETHERRACK, 300.0),
                        new BlockWeight(Blocks.SOUL_SAND, 100.0),
                        new BlockWeight(Blocks.GLOWSTONE, 50.0),
                        new BlockWeight(Blocks.NETHER_BRICKS, 30.0),
                        new BlockWeight(Blocks.NETHER_BRICK_FENCE, 10.0),
                        new BlockWeight(Blocks.NETHER_BRICK_STAIRS, 15.0),
                        new BlockWeight(Blocks.NETHER_WART, 30.0)
                ),
                Arrays.asList(
                        EntityType.ZOMBIFIED_PIGLIN,
                        EntityType.BLAZE,
                        EntityType.MAGMA_CUBE
                )
        );
    }

    public static SkyGridChunkGeneratorConfig getDefaultEndConfig() {
        return new SkyGridChunkGeneratorConfig(
                Arrays.asList(
                        new BlockWeight(Blocks.END_STONE, 1.0)
                ),
                Collections.emptyList()
        );
    }

    public static SkyGridChunkGeneratorConfig getDefaultConfigForModded() {
        return new SkyGridChunkGeneratorConfig(
                List.of(), List.of()
        );
    }
}

