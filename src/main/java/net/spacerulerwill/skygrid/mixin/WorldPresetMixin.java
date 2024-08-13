package net.spacerulerwill.skygrid.mixin;

import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.spacerulerwill.skygrid.util.WorldPresetExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Optional;

/*
This mixin provides two methods for getting the default nether and end generation options.
There was already one implemented for the overworld, but I needed these 2 also.
 */
@Mixin(WorldPreset.class)
public class WorldPresetMixin implements WorldPresetExtension {
    @Shadow
    private Map<DimensionOptions, DimensionOptions> dimensions;

    @Override
    public Optional<DimensionOptions> skygrid$GetNether() {
        return Optional.ofNullable(dimensions.get(DimensionOptions.NETHER));
    }

    @Override
    public Optional<DimensionOptions> skygrid$GetEnd() {
        return Optional.ofNullable(dimensions.get(DimensionOptions.END));
    }
}
