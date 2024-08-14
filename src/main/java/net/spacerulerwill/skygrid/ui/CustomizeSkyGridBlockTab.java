package net.spacerulerwill.skygrid.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGeneratorConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridBlockTab extends CustomizeSkyGridTab<CustomizeSkyGridBlockTab.BlockListWidget> {
    private Optional<Block> currentBlock;

    public CustomizeSkyGridBlockTab(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(client, parent, Text.translatable("createWorld.customize.skygrid.tab.block"), BlockListWidget::new);
        this.currentBlock = Optional.empty();
    }

    @Override
    public boolean shouldAddButtonBeActive() {
        String text = this.parent.getText();
        try {
            this.currentBlock = Registries.BLOCK.getOrEmpty(Identifier.of(text));
        } catch (InvalidIdentifierException e) {
            this.currentBlock = Optional.empty();
            return false;
        }
        return currentBlock.isPresent() && !this.parent.getCurrentConfig().blocks().containsKey(currentBlock.get());
    }

    @Override
    public void addButtonCallback() {
        Block block = currentBlock.get();
        this.listWidget.addBlock(block);
        this.parent.getCurrentConfig().blocks().put(block, CustomizeSkyGridScreen.DEFAULT_BLOCK_WEIGHT);
    }

    @Override
    public void deleteButtonCallback() {
        Block block = this.listWidget.getSelectedOrNull().block;
        this.listWidget.removeBlock(block);
        this.parent.getCurrentConfig().blocks().remove(block);
    }

    @Environment(EnvType.CLIENT)
    public static class BlockListWidget extends CustomizeSkyGridListWidget<BlockListWidgetEntry> {
        public BlockListWidget(MinecraftClient minecraftClient, CustomizeSkyGridScreen parent) {
            super(minecraftClient, parent);
        }

        @Override
        public void refreshEntries() {
            this.clearEntries();
            LinkedHashMap<Block, Integer> blocks = this.parent.getCurrentConfig().blocks();
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
        private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
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
            BlockState blockState = block.getDefaultState();
            ItemStack itemStack = createItemStackFor(blockState);
            this.renderIcon(context, x, y, itemStack);

            this.weightSliderWidget.setX(x + 22); // Adjust as needed
            this.weightSliderWidget.setY(y); // Adjust as needed
            this.weightSliderWidget.render(context, mouseX, mouseY, tickDelta);
        }

        private ItemStack createItemStackFor(BlockState state) {
            Item item = state.getBlock().asItem();
            if (item == Items.AIR) {
                if (state.isOf(Blocks.WATER)) {
                    item = Items.WATER_BUCKET;
                } else if (state.isOf(Blocks.LAVA)) {
                    item = Items.LAVA_BUCKET;
                }
            }
            return new ItemStack(item);
        }

        private void renderIcon(DrawContext context, int x, int y, ItemStack iconItem) {
            this.renderIconBackgroundTexture(context, x + 1, y + 1);
            if (!iconItem.isEmpty()) {
                context.drawItemWithoutEntity(iconItem, x + 2, y + 2);
            }
        }

        private void renderIconBackgroundTexture(DrawContext context, int x, int y) {
            context.drawGuiTexture(SLOT_TEXTURE, x, y, 0, 18, 18);
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
}