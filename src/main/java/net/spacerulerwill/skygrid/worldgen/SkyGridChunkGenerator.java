package net.spacerulerwill.skygrid.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import net.spacerulerwill.skygrid.util.ProbabilityTable;
import net.spacerulerwill.skygrid.util.ProbabilityTable.Probability;

public class SkyGridChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SkyGridChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyGridChunkGenerator::getBiomeSource),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("settings").forGetter(SkyGridChunkGenerator::getConfig)
            ).apply(instance, SkyGridChunkGenerator::new)
    );

    private final SkyGridChunkGeneratorConfig config;
    private final Random random;
    private final ProbabilityTable<Block> blockProbabilities;

    public SkyGridChunkGenerator(BiomeSource biomeSource, SkyGridChunkGeneratorConfig config) {
        super(biomeSource);
        this.config = config;
        random = Random.create();
        blockProbabilities = createBlockProbabilities(config.blocks());
    }

    private ProbabilityTable<Block> createBlockProbabilities(LinkedHashMap<Block, Integer> blockWeights) {
        ArrayList<Probability<Block>> probabilities = new ArrayList<>();
        probabilities.ensureCapacity(blockWeights.size());
        for (Map.Entry<Block, Integer> entry : blockWeights.entrySet()) {
            probabilities.add(new ProbabilityTable.Probability<>(entry.getKey(), entry.getValue()));
        }
        return new ProbabilityTable<>(probabilities);
    }


    // Seed the random number generator with a hash function based on the column x and z
    private void seedRandomForColumn(int x, int z) {
        random.setSeed((1610612741L * (long)x + 805306457L * (long)z + 402653189L) ^ 201326611L);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() { return MAP_CODEC; }

    public SkyGridChunkGeneratorConfig getConfig() {
        return config;
    }

    /*
    Empty methods - irrelevant for now
     */
    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {}

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {}

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {}

    @Override
    public void populateEntities(ChunkRegion region) {}

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}

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

    // Doing it all here is good enough for now
    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                int worldX = chunk.getPos().x * 16 + x;
                int worldZ = chunk.getPos().z * 16 + z;
                seedRandomForColumn(worldX, worldZ);
                for (int y = getMinimumY(); y < getMinimumY() + getWorldHeight(); y += 4) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = blockProbabilities.pickRandom(random);
                    chunk.setBlockState(blockPos, block.getDefaultState(), false);
                    if (block.equals(Blocks.SPAWNER)) {
                        MobSpawnerBlockEntity mobSpawnerBlockEntity = new MobSpawnerBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        mobSpawnerBlockEntity.setEntityType(config.spawnerEntities().get(random.nextInt(config.spawnerEntities().size())), Random.create());
                        chunk.setBlockEntity(mobSpawnerBlockEntity);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    // Get one column of the terrain
    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[getWorldHeight() / 4 + 1];
        seedRandomForColumn(x, z);
        for (int y = getMinimumY(); y <= getMinimumY() + getWorldHeight(); y += 4) {
            states[(y - getMinimumY()) / 4] = blockProbabilities.pickRandom(random).getDefaultState();
        }
        return new VerticalBlockSample(getMinimumY(), states);
    }
}
