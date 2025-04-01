package net.spacerulerwill.skygrid_reloaded.ui.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid_reloaded.SkyGridReloaded;
import net.spacerulerwill.skygrid_reloaded.ui.widget.TextField;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class SkyGridPresetsScreen extends Screen {
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 33, 64);
    private final CustomizeSkyGridScreen parent;
    private final MinecraftClient client;
    private TextField textField;
    private ButtonWidget selectPresetButton;
    private ButtonWidget savePresetButton;
    private SkyGridPresetListWidget listWidget;

    protected SkyGridPresetsScreen(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(Text.translatable("createWorld.customize.skygrid.presets"));
        this.client = client;
        this.parent = parent;
    }

    protected void init() {
        this.layout.addHeader(this.title, this.textRenderer);
        this.listWidget = this.layout.addBody(new SkyGridPresetListWidget());
        DirectionalLayoutWidget rows = DirectionalLayoutWidget.vertical().spacing(4);
        DirectionalLayoutWidget row1 = DirectionalLayoutWidget.horizontal().spacing(8);
        this.selectPresetButton = row1.add(ButtonWidget.builder(Text.translatable("createWorld.skygrid.customize.presets.select"), (buttonWidget) -> {
            SkyGridPresetListWidget.SkyGridPresetEntry entry = this.listWidget.getSelectedOrNull();
            this.parent.setConfigFromPreset(entry.preset);
            this.client.setScreen(this.parent);
        }).build());
        row1.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
        }).build());
        rows.add(row1);
        DirectionalLayoutWidget row2 = DirectionalLayoutWidget.horizontal().spacing(8);
        this.textField = row2.add(new PresetsTextField());
        this.savePresetButton = row2.add(ButtonWidget.builder(Text.translatable("createWorld.skygrid.customize.presets.save"), (buttonWidget) -> {
            this.savePreset(this.textField.getText());
        }).build());
        rows.add(row2);
        rows.forEachChild(this::addDrawableChild);
        this.layout.addFooter(rows);
        this.layout.forEachChild(this::addDrawableChild);
        this.updateSelectPresetButtonActive();
        this.refreshWidgetPositions();
        this.updateSaveButtonActive();
    }

    private void updateSaveButtonActive() {
        this.savePresetButton.active = !this.textField.getText().isEmpty();
    }

    protected void refreshWidgetPositions() {
        if (this.listWidget != null) {
            this.listWidget.position(this.width, this.layout);
        }
        this.layout.refreshPositions();
    }

    public void updateSelectPresetButtonActive() {
        this.selectPresetButton.active = this.listWidget.getSelectedOrNull() != null;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void savePreset(String name) {
        try {
            // Get hash of name
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes(StandardCharsets.UTF_8));

            String hashedName = Hex.encodeHexString(hash);
            // Get the preset
            SkyGridConfig currentConfig = this.parent.getCurrentSkyGridConfig();
            // Icon will be the most common block
            int maxWeight = 0;
            // Should be better
            List<Item> allItems = new ArrayList<>();
            currentConfig.overworldConfig().blocks().keySet().stream()
                    .map(Block::asItem)
                    .forEach(allItems::add);
            allItems.addAll(currentConfig.overworldConfig().chestItems().keySet());
            currentConfig.netherConfig().blocks().keySet().stream()
                    .map(Block::asItem)
                    .forEach(allItems::add);
            allItems.addAll(currentConfig.overworldConfig().chestItems().keySet());
            currentConfig.endConfig().blocks().keySet().stream()
                    .map(Block::asItem)
                    .forEach(allItems::add);
            allItems.addAll(currentConfig.overworldConfig().chestItems().keySet());
            Random random = new Random();
            Item icon = allItems.get(random.nextInt(allItems.size()));
            SkyGridPreset preset = new SkyGridPreset(icon, name, currentConfig);
            // Encode it as json
            JsonElement element = new JsonObject();
            DataResult<JsonElement> json = SkyGridPreset.CODEC.encode(preset, JsonOps.INSTANCE, element);
            String jsonString = json.getOrThrow().toString();
            // Write json to file
            String fileName = FabricLoader.getInstance().getConfigDir().toString() + "/" + SkyGridReloaded.MOD_ID + "/" + hashedName + ".json";
            try (PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
                writer.write(jsonString);
            }
            // Reload necessary stuff
            SkyGridReloaded.reloadCustomPresets();
            this.listWidget.refreshEntries();
        } catch (Exception e) {
            SkyGridReloaded.LOGGER.error("Failed to save preset {}: {}", name, e);
        }
    }

    @Environment(EnvType.CLIENT)
    private class SkyGridPresetListWidget extends AlwaysSelectedEntryListWidget<SkyGridPresetListWidget.SkyGridPresetEntry> {
        public SkyGridPresetListWidget() {
            super(SkyGridPresetsScreen.this.client, SkyGridPresetsScreen.this.width, SkyGridPresetsScreen.this.height - 77, 33, 24);
            this.refreshEntries();
        }

        public void refreshEntries() {
            this.clearEntries();
            for (SkyGridPreset preset : SkyGridReloaded.PRESETS) {
                this.addEntry(new SkyGridPresetEntry(preset));
            }
            for (SkyGridPreset preset : SkyGridReloaded.CUSTOM_PRESETS) {
                this.addEntry(new SkyGridCustomPresetEntry(preset));
            }
        }

        @Override
        public void setSelected(@Nullable SkyGridPresetsScreen.SkyGridPresetListWidget.SkyGridPresetEntry entry) {
            super.setSelected(entry);
            SkyGridPresetsScreen.this.updateSelectPresetButtonActive();
        }

        @Environment(EnvType.CLIENT)
        public class SkyGridPresetEntry extends Entry<SkyGridPresetEntry> {
            private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
            private final SkyGridPreset preset;

            public SkyGridPresetEntry(SkyGridPreset preset) {
                this.preset = preset;
            }

            @Override
            public Text getNarration() {
                return Text.empty();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawGuiTexture(RenderLayer::getGuiTextured, SLOT_TEXTURE, x + 1, y + 1, 0, 18, 18);
                context.drawItemWithoutEntity(preset.item().getDefaultStack(), x + 2, y + 2);
                context.drawText(SkyGridPresetsScreen.this.textRenderer, Text.translatable(preset.name()), x + 18 + 5, y + 3, 16777215, false);
            }
        }

        @Environment(EnvType.CLIENT)
        public class SkyGridCustomPresetEntry extends SkyGridPresetEntry {
            private final ButtonWidget deleteButton;

            public SkyGridCustomPresetEntry(SkyGridPreset preset) {
                super(preset);
                this.deleteButton = ButtonWidget.builder(Text.translatable("createWorld.skygrid.customize.presets.delete"), button -> {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(preset.name().getBytes(StandardCharsets.UTF_8));
                        String hashedName = Base64.getEncoder().encodeToString(hash);
                        String fileName = FabricLoader.getInstance().getConfigDir().toString() + "/" + SkyGridReloaded.MOD_ID + "/" + hashedName + ".json";
                        File file = new File(fileName);
                        Files.deleteIfExists(file.toPath());
                        SkyGridReloaded.reloadCustomPresets();
                        SkyGridPresetListWidget.this.removeEntry(this);
                    } catch (Exception e) {
                        SkyGridReloaded.LOGGER.error("Failed to delete preset {}: {}", preset, e);
                    }
                }).width(50).build();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
                this.deleteButton.setX(x + entryWidth - this.deleteButton.getWidth() - 5);
                this.deleteButton.setY(y);
                this.deleteButton.render(context, mouseX, mouseY, tickDelta);
            }

            // Allowing slider to be draggable
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.deleteButton.isMouseOver(mouseX, mouseY)) {
                    return this.deleteButton.mouseClicked(mouseX, mouseY, button);
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }
    }


    @Environment(EnvType.CLIENT)
    protected class PresetsTextField extends TextField {
        public PresetsTextField() {
            super(SkyGridPresetsScreen.this.textRenderer, 150, 20, Text.empty());
        }

        @Override
        protected void onTextChanged() {
            SkyGridPresetsScreen.this.updateSaveButtonActive();
        }
    }

}