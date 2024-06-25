package net.spacerulerwill.skygrid;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.spacerulerwill.skygrid.ProbabilityTable.Probability;

public class SkyGridChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SkyGridChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyGridChunkGenerator::getBiomeSource)
            ).apply(instance, SkyGridChunkGenerator::new)
    );

    private final Random random;

    // Black spawn candidates
    private static final ProbabilityTable<Block> blockProbailities = new ProbabilityTable<Block>(new Probability[] {
            new Probability<>(Blocks.STONE, 120.0),
            new Probability<>(Blocks.GRASS_BLOCK, 80.0),
            new Probability<>(Blocks.DIRT, 20.0),
            new Probability<>(Blocks.WATER, 10.0),
            new Probability<>(Blocks.LAVA, 5.0),
            new Probability<>(Blocks.SAND, 20.0),
            new Probability<>(Blocks.GRAVEL, 10.0),
            new Probability<>(Blocks.GOLD_ORE, 10.0),
            new Probability<>(Blocks.IRON_ORE, 20.0),
            new Probability<>(Blocks.COAL_ORE, 40.0),
            new Probability<>(Blocks.OAK_LOG, 100.0),
            new Probability<>(Blocks.OAK_LEAVES, 40.0),
            new Probability<>(Blocks.GLASS, 1.0),
            new Probability<>(Blocks.LAPIS_ORE, 5.0),
            new Probability<>(Blocks.SANDSTONE, 10.0),
            new Probability<>(Blocks.STICKY_PISTON, 1.0),
            new Probability<>(Blocks.COBWEB, 10.0),
            new Probability<>(Blocks.DEAD_BUSH, 3.0),
            new Probability<>(Blocks.PISTON, 1.0),
            new Probability<>(Blocks.WHITE_WOOL, 25.0),
            new Probability<>(Blocks.DANDELION, 2.0),
            new Probability<>(Blocks.POPPY, 2.0),
            new Probability<>(Blocks.BROWN_MUSHROOM, 2.0),
            new Probability<>(Blocks.RED_MUSHROOM, 2.0),
            new Probability<>(Blocks.TNT, 2.0),
            new Probability<>(Blocks.BOOKSHELF, 3.0),
            new Probability<>(Blocks.MOSSY_COBBLESTONE, 5.0),
            new Probability<>(Blocks.OBSIDIAN, 1.0),
            new Probability<>(Blocks.SPAWNER, 1.0),
            new Probability<>(Blocks.CHEST, 1.0),
            new Probability<>(Blocks.DIAMOND_ORE, 1.0),
            new Probability<>(Blocks.REDSTONE_ORE, 8.0),
            new Probability<>(Blocks.ICE, 4.0),
            new Probability<>(Blocks.SNOW_BLOCK, 8.0),
            new Probability<>(Blocks.CACTUS, 1.0),
            new Probability<>(Blocks.CLAY, 20.0),
            new Probability<>(Blocks.SUGAR_CANE, 15.0),
            new Probability<>(Blocks.PUMPKIN, 5.0),
            new Probability<>(Blocks.MELON, 5.0),
            new Probability<>(Blocks.MYCELIUM, 15.0)
    });

    // Candidates for mob spawners that generate in the skygrid, all have equal chance
    private static final EntityType<?>[] mobSpawnerCandidates = new EntityType<?>[]{
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
    };

    public SkyGridChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        random = Random.create();
    }

    // Seed the random number generator with a hash function based on the column x and z
    private void seedRandomForColumn(int x, int z) {
        random.setSeed((1610612741L * (long)x + 805306457L * (long)z + 402653189L) ^ 201326611L);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return MAP_CODEC;
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
                for (int y = getMinimumY(); y <= getMinimumY() + getWorldHeight(); y += 4) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = blockProbailities.pickRandom(random);
                    chunk.setBlockState(blockPos, block.getDefaultState(), false);
                    if (block.equals(Blocks.SPAWNER)) {
                        MobSpawnerBlockEntity mobSpawnerBlockEntity = new MobSpawnerBlockEntity(new BlockPos(worldX, y, worldZ), block.getDefaultState());
                        mobSpawnerBlockEntity.setEntityType(mobSpawnerCandidates[random.nextInt(mobSpawnerCandidates.length)], Random.create());
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
            states[(y - getMinimumY()) / 4] = blockProbailities.pickRandom(random).getDefaultState();
        }
        return new VerticalBlockSample(-64, states);
    }
}
