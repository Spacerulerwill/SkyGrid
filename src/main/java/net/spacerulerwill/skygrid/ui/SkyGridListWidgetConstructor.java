package net.spacerulerwill.skygrid.ui;

import net.minecraft.client.MinecraftClient;

@FunctionalInterface
public interface SkyGridListWidgetConstructor<T> {
    T create(MinecraftClient client, CustomizeSkyGridScreen parent);
}
