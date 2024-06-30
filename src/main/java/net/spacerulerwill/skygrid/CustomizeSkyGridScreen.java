package net.spacerulerwill.skygrid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridScreen extends Screen {
    CreateWorldScreen parent;

    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(Text.of("SkyGrid Customization"));
        this.parent = parent;
    }

    public void close() {
        this.client.setScreen(this.parent);
    }
}
