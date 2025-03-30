package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.spacerulerwill.skygrid.util.MinecraftRandomAdapter;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.DiscreteProbabilityCollectionSampler;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SkyGridChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SkyGridChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyGridChunkGenerator::getBiomeSource),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("settings").forGetter(SkyGridChunkGenerator::getConfig)
            ).apply(instance, SkyGridChunkGenerator::new)
    );


    private final SkyGridChunkGeneratorConfig config;
    private final List<EntityType<?>> entities;
    private DiscreteProbabilityCollectionSampler<Block> blockProbabilities;
    private DiscreteProbabilityCollectionSampler<Item> chestItemProbabilities;


    public SkyGridChunkGenerator(BiomeSource biomeSource, SkyGridChunkGeneratorConfig config) {
        super(biomeSource);
        this.config = config;
        this.blockProbabilities = createWeightedProbabilityTable(config.blocks());
        if (config.chestItems().isEmpty()) {
            this.chestItemProbabilities = null;
        } else {
            this.chestItemProbabilities = createWeightedProbabilityTable(config.chestItems());
        }
        this.entities = config.spawnerEntities().stream().toList();
    }

    private <T> DiscreteProbabilityCollectionSampler<T> createWeightedProbabilityTable(Map<T, Double> blockWeights) {
        // Calculate the total weight
        int totalWeight = 0;
        for (Double value : blockWeights.values()) {
            totalWeight += value;
        }

        // Normalize each block's weight
        Map<T, Double> normalizedWeights = new LinkedHashMap<>();
        for (Map.Entry<T, Double> entry : blockWeights.entrySet()) {
            T t = entry.getKey();
            Double weight = entry.getValue();
            Double normalizedWeight = weight / totalWeight;
            normalizedWeights.put(t, normalizedWeight);
        }

        // Return the DiscreteProbabilityCollectionSampler with normalized weights
        return new DiscreteProbabilityCollectionSampler<T>(new MinecraftRandomAdapter(), normalizedWeights);
    }

    private Random getRandomForChunk(NoiseConfig noiseConfig, int x, int z) {
        return noiseConfig.getOreRandomDeriver().split((1610612741L * (long) x + 805306457L * (long) z + 402653189L) ^ 201326611L);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return MAP_CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {
    }

    public SkyGridChunkGeneratorConfig getConfig() {
        return config;
    }

    /*
    Empty methods - irrelevant for now
     */
    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }

    /*
    Used for getting the max block height of any given column in the terrain. Used for structure generation.
    We have no structures so is irrelevant for now - a zero value is fine.
     */
    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    // Max world height, how many blocks high from minimumY the chunks generate
    @Override
    public int getWorldHeight() {
        return 384;
    }

    // No oceans in skygrid
    @Override
    public int getSeaLevel() {
        return 0;
    }

    // Bottom of the world is here
    @Override
    public int getMinimumY() {
        return -64;
    }

    private void fillChestBlockEntityWithItems(LootableContainerBlockEntity blockEntity, Random random) {
        if (this.chestItemProbabilities != null) {
            // How many items for chest
            int numItems = random.nextBetween(2, 5);
            // Generate 26 numbers and shuffle them
            ArrayList<Integer> slots = new ArrayList<>();
            for (int i = 0; i <= 26; i++) {
                slots.add(i);
            }
            Collections.shuffle(slots);
            // Add the items
            int nextSlotIdx = 0;
            for (int i = 0; i < numItems; i++) {
                Item item = this.chestItemProbabilities.sample();
                int slotIdx = slots.get(nextSlotIdx);
                nextSlotIdx += 1;
                blockEntity.setStack(slotIdx, item.getDefaultStack());
            }
        }
    }

    // Doing it all here is good enough for now
    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        Random random = this.getRandomForChunk(noiseConfig, chunk.getPos().x, chunk.getPos().z);
        UniformRandomProvider uniformRandomProvider = new MinecraftRandomAdapter(random);
        this.blockProbabilities = this.blockProbabilities.withUniformRandomProvider(uniformRandomProvider);
        if (this.chestItemProbabilities != null) {
            this.chestItemProbabilities = this.chestItemProbabilities.withUniformRandomProvider(uniformRandomProvider);
        }
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                int worldX = chunk.getPos().x * 16 + x;
                int worldZ = chunk.getPos().z * 16 + z;
                for (int y = getMinimumY(); y < getMinimumY() + getWorldHeight(); y += 4) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = blockProbabilities.sample();
                    BlockState state = block.getDefaultState().withIfExists(Properties.PERSISTENT, true);
                    chunk.setBlockState(blockPos, state, false);
                    if (block.equals(Blocks.SPAWNER) && !this.entities.isEmpty()) {
                        MobSpawnerBlockEntity mobSpawnerBlockEntity = new MobSpawnerBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        mobSpawnerBlockEntity.setEntityType(this.entities.get(random.nextInt(config.spawnerEntities().size())), random);
                        chunk.setBlockEntity(mobSpawnerBlockEntity);
                    } else if (block.equals(Blocks.CHEST)) {
                        LootableContainerBlockEntity chestBlockEntity = new ChestBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        chunk.setBlockEntity(chestBlockEntity);
                        fillChestBlockEntityWithItems(chestBlockEntity, random);
                    } else if (block.equals(Blocks.BARREL)) {
                        LootableContainerBlockEntity barrelBlockEntity = new BarrelBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        chunk.setBlockEntity(barrelBlockEntity);
                        fillChestBlockEntityWithItems(barrelBlockEntity, random);
                    } else if (block.equals(Blocks.ENDER_CHEST)) {
                        EnderChestBlockEntity enderChestBlockEntity = new EnderChestBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        chunk.setBlockEntity(enderChestBlockEntity);
                    } else if (block.equals(Blocks.TRAPPED_CHEST)) {
                        LootableContainerBlockEntity trappedChestBlockEntity = new TrappedChestBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        chunk.setBlockEntity(trappedChestBlockEntity);
                        fillChestBlockEntityWithItems(trappedChestBlockEntity, random);
                    } else if (block.equals(Blocks.ENCHANTING_TABLE)) {
                        EnchantingTableBlockEntity enchantingTableBlockEntity = new EnchantingTableBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        chunk.setBlockEntity(enchantingTableBlockEntity);
                    }
                }
            }
        }

        // End portal placement
        if (chunk.getPos().x == 0 && chunk.getPos().z == 0) {
            chunk.setBlockState(new BlockPos(0, -64, 0), Blocks.AIR.getDefaultState(), false);
            chunk.setBlockState(new BlockPos(2, -64, 0), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(0, -64, 2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH), false); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(2, -64, 1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(1, -64, 2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH), false);  // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == -1 && chunk.getPos().z == 0) {
            chunk.setBlockState(new BlockPos(-2, -64, 0), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST), false);   // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-2, -64, 1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-1, -64, 2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH), false);  // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == 0 && chunk.getPos().z == -1) {
            chunk.setBlockState(new BlockPos(0, -64, -2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH), false); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(1, -64, -2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(2, -64, -1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false); // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == -1 && chunk.getPos().z == -1) {
            chunk.setBlockState(new BlockPos(-2, -64, -1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-1, -64, -2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH), false);   // Face towards center (1, -64, 1)
        }


        return CompletableFuture.completedFuture(chunk);
    }

    // Get one column of the terrain
    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        Random random = this.getRandomForChunk(noiseConfig, x >> 4, z >> 4);
        blockProbabilities = blockProbabilities.withUniformRandomProvider(new MinecraftRandomAdapter(random));
        BlockState[] states = new BlockState[getWorldHeight() / 4];
        for (int y = getMinimumY(); y < getMinimumY() + getWorldHeight(); y += 4) {
            states[(y - getMinimumY()) / 4] = blockProbabilities.sample().getDefaultState();
        }
        return new VerticalBlockSample(getMinimumY(), states);
    }
}
