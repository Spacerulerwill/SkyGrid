package net.spacerulerwill.skygrid.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

public record BlockWeight(Block block, double weight) {
    public static final Codec<BlockWeight> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Registries.BLOCK.getCodec().fieldOf("block").forGetter(BlockWeight::block),
                    Codec.DOUBLE.fieldOf("weight").forGetter(BlockWeight::weight)
            ).apply(instance, BlockWeight::new)
    );
}
