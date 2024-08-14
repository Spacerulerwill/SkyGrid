package net.spacerulerwill.skygrid.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CustomizeSkyGridPresetsScreen extends Screen {
    private CustomizeSkyGridScreen parent;
    private MinecraftClient client;
    protected CustomizeSkyGridPresetsScreen(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(Text.translatable("createWorld.customize.skygrid.presets"));
        this.client = client;
        this.parent = parent;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
