package net.spacerulerwill.skygrid_reloaded.util;

import net.minecraft.util.math.random.Random;
import org.apache.commons.rng.UniformRandomProvider;

public class MinecraftRandomAdapter implements UniformRandomProvider {
    private final Random minecraftRandom;

    public MinecraftRandomAdapter() {
        this.minecraftRandom = Random.create();
    }

    public MinecraftRandomAdapter(Random minecraftRandom) {
        this.minecraftRandom = minecraftRandom;
    }

    @Override
    public long nextLong() {
        return minecraftRandom.nextLong();
    }
}
