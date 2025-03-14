package net.spacerulerwill.skygrid.ui.tab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.spacerulerwill.skygrid.ui.util.RenderUtils;
import net.spacerulerwill.skygrid.ui.widget.CustomizeSkyGridListWidget;
import net.spacerulerwill.skygrid.ui.screen.CustomizeSkyGridScreen;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGeneratorConfig;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BlockWeightTab extends CustomizeSkyGridTab<BlockWeightTab.BlockListWidget, BlockWeightTab.BlockAutoCompleteListWidgetEntry> {
    private Optional<Block> currentBlock;
    private MinecraftClient client;
    private CustomizeSkyGridScreen parent;

    public BlockWeightTab(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(client, parent, Text.translatable("createWorld.customize.skygrid.tab.block"), BlockListWidget::new);
        this.client = client;
        this.parent = parent;
        this.currentBlock = Optional.empty();
    }

    @Override
    public boolean shouldAddButtonBeActive() {
        BlockAutoCompleteListWidgetEntry entry = (BlockAutoCompleteListWidgetEntry) this.parent.getSelectedEntryOrNull();
        if (entry == null) {
            this.currentBlock = Optional.empty();
            return false;
        } else {
            this.currentBlock = Optional.of(entry.block);
            return !this.parent.getCurrentConfig().blocks().containsKey(entry.block);
        }
    }

    @Override
    public void addButtonCallback() {
        Block block = currentBlock.get();
        this.listWidget.addBlock(block);
        this.parent.getCurrentConfig().blocks().put(block, CustomizeSkyGridScreen.DEFAULT_BLOCK_WEIGHT);
        super.addButtonCallback();
    }

    @Override
    public void deleteButtonCallback() {
        Block block = this.listWidget.getSelectedOrNull().block;
        this.listWidget.removeBlock(block);
        this.parent.getCurrentConfig().blocks().remove(block);
    }

    @Override
    public CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<BlockAutoCompleteListWidgetEntry> getAutoCompleteListWidget(String text) {
        List<BlockAutoCompleteListWidgetEntry> results = new ArrayList<>();
        if (text.isBlank()) {
            return new CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<BlockAutoCompleteListWidgetEntry>(
                    this.parent,
                    this.client,
                    results
            );
        }
        Registries.BLOCK.forEach(block -> {
            if (block.asItem() != Items.AIR) {
                String blockString = Text.translatable(block.getTranslationKey()).getString();
                if (blockString.trim().toLowerCase().startsWith(text)) {
                    results.add(new BlockAutoCompleteListWidgetEntry(block));
                }
            }
        });
        return new CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<BlockAutoCompleteListWidgetEntry>(
                this.parent,
                this.client,
                results
        );
    }

    @Environment(EnvType.CLIENT)
    public static class BlockListWidget extends CustomizeSkyGridListWidget<BlockListWidgetEntry> {
        public BlockListWidget(MinecraftClient minecraftClient, CustomizeSkyGridScreen parent) {
            super(minecraftClient, parent, 24);
        }

        @Override
        public void refreshEntries() {
            this.clearEntries();
            Map<Block, Integer> blocks = this.parent.getCurrentConfig().blocks();
            for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
                Block block = entry.getKey();
                int weight = entry.getValue();
                this.addEntry(new BlockListWidgetEntry(this.parent, block, weight));
            }
        }

        public void addBlock(Block block) {
            this.addEntry(new BlockListWidgetEntry(this.parent, block, 100));
        }

        public void removeBlock(Block block) {
            for (int i = 0; i < this.getEntryCount(); i++) {
                if (getEntry(i).block.equals(block)) {
                    remove(i);
                    return;
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class BlockListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry<BlockListWidgetEntry> {
        private final BlockWeightSliderWidget weightSliderWidget;
        private final Block block;

        public BlockListWidgetEntry(CustomizeSkyGridScreen parent, Block block, int initialWeight) {
            this.block = block;
            this.weightSliderWidget = new BlockWeightSliderWidget(parent, 0, 0, 193, 20, 0, 500, block, (int) initialWeight); // Adjust width and height as needed
        }

        @Override
        public Text getNarration() {
            return Text.of("bruh");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            RenderUtils.renderBlockIcon(block, context, x, y);
            this.weightSliderWidget.setX(x + 22); // Adjust as needed
            this.weightSliderWidget.setY(y); // Adjust as needed
            this.weightSliderWidget.render(context, mouseX, mouseY, tickDelta);
        }

        // Allowing slider to be draggable
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (weightSliderWidget.isMouseOver(mouseX, mouseY)) {
                return weightSliderWidget.mouseClicked(mouseX, mouseY, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (weightSliderWidget.isMouseOver(mouseX, mouseY)) {
                return weightSliderWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (weightSliderWidget.isMouseOver(mouseX, mouseY)) {
                return weightSliderWidget.mouseReleased(mouseX, mouseY, button);
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class BlockWeightSliderWidget extends SliderWidget {
        private final CustomizeSkyGridScreen parent;
        private final int minValue;
        private final int maxValue;
        private final Block block;

        public BlockWeightSliderWidget(CustomizeSkyGridScreen parent, int x, int y, int width, int height, int minValue, int maxValue, Block block, double initialValue) {
            super(x, y, width, height, Text.literal("FOV: " + (int) initialValue), (initialValue - minValue) / (maxValue - minValue));
            this.parent = parent;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.block = block;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            int calculatedValue = (int) (this.value * (this.maxValue - this.minValue) + this.minValue);
            MutableText message = block.getName().copy()
                    .append(Text.literal(": "))
                    .append(Text.literal(String.valueOf(calculatedValue)));
            this.setMessage(message);
        }

        @Override
        protected void applyValue() {
            int weight = (int) (this.value * (this.maxValue - this.minValue) + this.minValue);
            SkyGridChunkGeneratorConfig currentConfig = this.parent.getCurrentConfig();
            currentConfig.blocks().put(block, weight);
        }
    }

    @Environment(EnvType.CLIENT)
    public class BlockAutoCompleteListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry<BlockAutoCompleteListWidgetEntry> {
        public Block block;

        public BlockAutoCompleteListWidgetEntry(Block block) {
            this.block = block;
        }

        @Override
        public Text getNarration() {
            return Text.of("pain");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            RenderUtils.renderBlockIcon(block, context, x, y);
            context.drawText(BlockWeightTab.this.parent.getTextRenderer(), block.getName(), x + 18 + 5, y + 3, 16777215, false);
        }
    }
}