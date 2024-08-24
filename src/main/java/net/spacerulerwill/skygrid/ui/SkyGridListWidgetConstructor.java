package net.spacerulerwill.skygrid.ui;

import net.minecraft.client.MinecraftClient;
import net.spacerulerwill.skygrid.ui.screen.CustomizeSkyGridScreen;

@FunctionalInterface
public interface SkyGridListWidgetConstructor<T> {
    T create(MinecraftClient client, CustomizeSkyGridScreen parent);
}
