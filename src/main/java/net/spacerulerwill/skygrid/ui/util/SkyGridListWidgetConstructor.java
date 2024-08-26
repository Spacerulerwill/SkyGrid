package net.spacerulerwill.skygrid.ui.util;

import net.minecraft.client.MinecraftClient;
import net.spacerulerwill.skygrid.ui.screen.CustomizeSkyGridScreen;

@FunctionalInterface
public interface SkyGridListWidgetConstructor<T> {
    T create(MinecraftClient client, CustomizeSkyGridScreen parent);
}
