package net.spacerulerwill.skygrid.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid.worldgen.SkyGridPreset;
import net.spacerulerwill.skygrid.worldgen.SkyGridPresets;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CustomizeSkyGridPresetsScreen extends Screen {
    private CustomizeSkyGridScreen parent;
    private MinecraftClient client;
    private ButtonWidget selectPresetButton;
    private SkyGridPresetListWidget listWidget;

    protected CustomizeSkyGridPresetsScreen(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(Text.translatable("createWorld.customize.skygrid.presets"));
        this.client = client;
        this.parent = parent;
    }

    protected void init() {
        this.selectPresetButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.skygrid.customize.presets.select"), (buttonWidget) -> {
            SkyGridPresetListWidget.SkyGridPresetEntry entry = this.listWidget.getSelectedOrNull();
            this.parent.setConfigFromPreset(entry.preset);
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.listWidget = this.addDrawableChild(new SkyGridPresetListWidget());
        this.updateSelectPresetButtonActive();
    }

    public void updateSelectPresetButtonActive() {
        this.selectPresetButton.active = this.listWidget.getSelectedOrNull() != null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Environment(EnvType.CLIENT)
    private class SkyGridPresetListWidget extends AlwaysSelectedEntryListWidget<SkyGridPresetListWidget.SkyGridPresetEntry> {
        public SkyGridPresetListWidget() {
            super(CustomizeSkyGridPresetsScreen.this.client, CustomizeSkyGridPresetsScreen.this.width, CustomizeSkyGridPresetsScreen.this.height - 77, 33, 24);

            for (Supplier<SkyGridPreset> preset : SkyGridPresets.ALL_PRESETS) {
                this.addEntry(new SkyGridPresetEntry(preset.get()));
            }
        }

        @Override
        public void setSelected(@Nullable CustomizeSkyGridPresetsScreen.SkyGridPresetListWidget.SkyGridPresetEntry entry) {
            super.setSelected(entry);
            CustomizeSkyGridPresetsScreen.this.updateSelectPresetButtonActive();
        }

        @Environment(EnvType.CLIENT)
        public class SkyGridPresetEntry extends AlwaysSelectedEntryListWidget.Entry<SkyGridPresetEntry> {
            private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
            private SkyGridPreset preset;

            public SkyGridPresetEntry(SkyGridPreset preset) {
                this.preset = preset;
            }
            @Override
            public Text getNarration() {
                return Text.of("bruh");
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawGuiTexture(SLOT_TEXTURE, x + 1, y + 1, 0, 18, 18);
                context.drawItemWithoutEntity(preset.item.getDefaultStack(), x + 2, y + 2);
                context.drawText(CustomizeSkyGridPresetsScreen.this.textRenderer, Text.translatable(preset.name), x + 18 + 5, y + 3, 16777215, false);
            }
        }
    }
}
