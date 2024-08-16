package net.spacerulerwill.skygrid.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

public class SkyGridPresets {
    public static SkyGridPreset getClassicPreset() {
        return new SkyGridPreset(
                Blocks.GRASS_BLOCK,
                "createWorld.skygrid.customize.presets.preset.classic",
                createClassicOverworldConfig(),
                createClassicNetherConfig(),
                createClassicEndConfig());
    }

    private static SkyGridChunkGeneratorConfig createClassicOverworldConfig() {
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

        LinkedHashSet<EntityType<?>> entities = new LinkedHashSet<>();
        entities.add(EntityType.CREEPER);
        entities.add(EntityType.SKELETON);
        entities.add(EntityType.SPIDER);
        entities.add(EntityType.CAVE_SPIDER);
        entities.add(EntityType.ZOMBIE);
        entities.add(EntityType.SLIME);
        entities.add(EntityType.PIG);
        entities.add(EntityType.SHEEP);
        entities.add(EntityType.COW);
        entities.add(EntityType.CHICKEN);
        entities.add(EntityType.SQUID);
        entities.add(EntityType.WOLF);
        entities.add(EntityType.ENDERMAN);
        entities.add(EntityType.SILVERFISH);
        entities.add(EntityType.VILLAGER);

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }

    private static SkyGridChunkGeneratorConfig createClassicNetherConfig() {
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

        LinkedHashSet<EntityType<?>> entities = new LinkedHashSet<>();
        entities.add(EntityType.ZOMBIFIED_PIGLIN);
        entities.add(EntityType.BLAZE);
        entities.add(EntityType.MAGMA_CUBE);

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }

    private static SkyGridChunkGeneratorConfig createClassicEndConfig() {
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.END_STONE, 1);

        return new SkyGridChunkGeneratorConfig(blocks, new LinkedHashSet<>());
    }

    public static SkyGridPreset getBoomPreset() {
        return new SkyGridPreset(
                Blocks.TNT,
                "createWorld.skygrid.customize.presets.preset.boom",
                createBoomOverworldConfig(),
                createBoomNetherConfig(),
                createBoomEndConfig()
        );
    }

    private static SkyGridChunkGeneratorConfig createBoomOverworldConfig() {
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.TNT, 100);
        blocks.put(Blocks.SPAWNER, 1);

        LinkedHashSet<EntityType<?>> entities = new LinkedHashSet<>();
        entities.add(EntityType.CREEPER);

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }

    private static SkyGridChunkGeneratorConfig createBoomNetherConfig() {
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.TNT, 100);
        blocks.put(Blocks.SPAWNER, 1);

        LinkedHashSet<EntityType<?>> entities = new LinkedHashSet<>();
        entities.add(EntityType.BLAZE);
        entities.add(EntityType.GHAST);

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }

    private static SkyGridChunkGeneratorConfig createBoomEndConfig() {
        LinkedHashMap<Block, Integer> blocks = new LinkedHashMap<>();
        blocks.put(Blocks.TNT, 100);
        blocks.put(Blocks.SPAWNER, 1);

        LinkedHashSet<EntityType<?>> entities = new LinkedHashSet<>();
        entities.add(EntityType.ENDERMITE);
        entities.add(EntityType.ENDERMAN);

        return new SkyGridChunkGeneratorConfig(blocks, entities);
    }

    public static List<Supplier<SkyGridPreset>> ALL_PRESETS = new ArrayList<>();

    static {
        ALL_PRESETS.add(SkyGridPresets::getClassicPreset);
        ALL_PRESETS.add(SkyGridPresets::getBoomPreset);
    }
}
