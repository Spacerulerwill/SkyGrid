package net.spacerulerwill.skygrid;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
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
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SkyGridChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SkyGridChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyGridChunkGenerator::getBiomeSource)
            ).apply(instance, SkyGridChunkGenerator::new)
    );

    private final Random random;

    // List of block candidates for skygrid blocks
    private static final Block[] BLOCKS = new Block[]{
            // Stone variations
            Blocks.STONE,
            Blocks.ANDESITE,
            Blocks.DIORITE,
            Blocks.GRANITE,
            Blocks.DEEPSLATE,
            Blocks.SANDSTONE,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.OBSIDIAN,
            // Dirt type blocks
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.ROOTED_DIRT,
            Blocks.COARSE_DIRT,
            Blocks.SAND,
            Blocks.GRAVEL,
            Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW,
            Blocks.CLAY,
            // Food blocks / plants
            Blocks.PUMPKIN,
            Blocks.MELON,
            Blocks.CACTUS,
            Blocks.SUGAR_CANE,
            // Liquids
            Blocks.ICE,
            Blocks.BLUE_ICE,
            Blocks.PACKED_ICE,
            Blocks.FROSTED_ICE,
            Blocks.WATER,
            Blocks.LAVA,
            // Ores
            Blocks.COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.GOLD_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.LAPIS_ORE,
            Blocks.EMERALD_ORE,
            Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            // Log and leaves
            Blocks.ACACIA_LOG,
            Blocks.ACACIA_LEAVES,
            Blocks.BIRCH_LOG,
            Blocks.BIRCH_LEAVES,
            Blocks.CHERRY_LOG,
            Blocks.CHERRY_LEAVES,
            Blocks.OAK_LOG,
            Blocks.OAK_LEAVES,
            Blocks.JUNGLE_LOG,
            Blocks.JUNGLE_LEAVES,
            Blocks.DARK_OAK_LOG,
            Blocks.DARK_OAK_LEAVES,
            Blocks.MANGROVE_LOG,
            Blocks.MANGROVE_LEAVES,
            Blocks.SPRUCE_LOG,
            Blocks.SPRUCE_LEAVES,
            // Other
            Blocks.GLASS,
            Blocks.PISTON,
            Blocks.STICKY_PISTON,
            Blocks.COBWEB,
            Blocks.WHITE_WOOL,
            Blocks.TNT,
            Blocks.BOOKSHELF,
    };

    public SkyGridChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        this.random = new Random();
    }

    // Seed the random number generate with a hash function based on the column x and z
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
                for (int y = -64; y <= 320; y += 4) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    chunk.setBlockState(blockPos, BLOCKS[random.nextInt(BLOCKS.length)].getDefaultState(), false);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    // Get one column of the terrain
    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[(320 - (-64)) / 4 + 1];
        seedRandomForColumn(x, z);
        for (int y = -64; y <= 320; y += 4) {
            states[(y + 64) / 4] = BLOCKS[random.nextInt(BLOCKS.length)].getDefaultState();
        }
        return new VerticalBlockSample(-64, states);
    }

}
