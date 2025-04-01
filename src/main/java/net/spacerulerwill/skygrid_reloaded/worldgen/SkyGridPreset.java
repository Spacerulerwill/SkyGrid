package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

public record SkyGridPreset(Item item, String name, SkyGridConfig config) {
    public static final Codec<SkyGridPreset> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Registries.ITEM.getCodec().fieldOf("item").forGetter(SkyGridPreset::item),
                    Codecs.NON_EMPTY_STRING.fieldOf("name").forGetter(SkyGridPreset::name),
                    SkyGridConfig.CODEC.fieldOf("config").forGetter(SkyGridPreset::config)
            ).apply(instance, SkyGridPreset::new)
    );
}