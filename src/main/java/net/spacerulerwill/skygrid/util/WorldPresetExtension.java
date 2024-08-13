package net.spacerulerwill.skygrid.util;

import net.minecraft.world.dimension.DimensionOptions;

import java.util.Optional;

public interface WorldPresetExtension {
    Optional<DimensionOptions> skygrid$GetNether();
    Optional<DimensionOptions> skygrid$GetEnd();
}
