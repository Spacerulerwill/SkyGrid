package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.dimension.DimensionOptions;
import net.spacerulerwill.skygrid_reloaded.ui.widget.WeightSliderListWidgetEntry;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomizeBlocksScreen extends DimensionSpecificCustomizableListWidgetScreen<CustomizeBlocksScreen.BlockWeightListEntry, Block> {
    private static final double INITIAL_BLOCK_WEIGHT = 160;
    private static final double MIN_BLOCK_WEIGHT = 0;
    private static final double MAX_BLOCK_WEIGHT = 500;

    public CustomizeBlocksScreen(CustomizeSkyGridScreen parent, SkyGridConfig currentConfig) {
        super(parent, currentConfig, Text.translatable("createWorld.customize.skygrid.blocks"), Text.translatable("createWorld.customize.skygrid.blocks.placeholder"), 25);
    }

    private static Item getBlockItem(Block block) {
        if (block.equals(Blocks.LAVA)) {
            return Items.LAVA_BUCKET;
        } else if (block.equals(Blocks.WATER)) {
            return Items.WATER_BUCKET;
        } else {
            return block.asItem();
        }
    }

    @Override
    public void onClear() {
        Map<Block, Double> blocks = this.getCurrentBlocks();
        blocks.clear();
    }

    @Override
    protected Optional<Block> getFromTextField(String text) {
        try {
            return Registries.BLOCK.getOptionalValue(Identifier.of(text));
        } catch (InvalidIdentifierException e) {
            return Optional.empty();
        }
    }

    @Override
    protected List<AutocompleteListWidget.Entry> getAutocompleteSuggestions(String text) {
        List<AutocompleteListWidget.Entry> results = new ArrayList<>();
        if (text.isBlank()) {
            return results;
        }
        Registries.BLOCK.forEach(block -> {
            String displayString = Text.translatable(block.getTranslationKey()).getString();
            String valueString = Registries.BLOCK.getId(block).toString();
            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(getBlockItem(block), displayString, valueString, this.textRenderer));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(Block block) {
        Map<Block, Double> blocks = this.getCurrentBlocks();
        if (blocks.containsKey(block)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        blocks.put(block, INITIAL_BLOCK_WEIGHT);
        this.listWidget.addEntry(new BlockWeightListEntry(block));
    }

    @Override
    protected boolean canAdd(Block block) {
        return !getCurrentBlocks().containsKey(block);
    }

    @Override
    protected void onDelete(BlockWeightListEntry entry) {
        Map<Block, Double> blocks = getCurrentBlocks();
        blocks.remove(entry.block);
    }

    private Map<Block, Double> getCurrentBlocks() {
        Map<Block, Double> blocks;
        if (this.currentDimension == DimensionOptions.OVERWORLD) {
            blocks = this.currentConfig.overworldConfig().blocks();
        } else if (this.currentDimension == DimensionOptions.NETHER) {
            blocks = this.currentConfig.netherConfig().blocks();
        } else if (this.currentDimension == DimensionOptions.END) {
            blocks = this.currentConfig.endConfig().blocks();
        } else {
            throw new IllegalStateException("Current dimension is not one of overworld, nether or end: " + this.currentDimension.getValue().toTranslationKey());
        }
        return blocks;
    }

    @Override
    protected List<BlockWeightListEntry> getEntriesFromConfig() {
        List<BlockWeightListEntry> entries = new ArrayList<>();
        Map<Block, Double> blocks = this.getCurrentBlocks();
        for (Map.Entry<Block, Double> entry : blocks.entrySet()) {
            Block block = entry.getKey();
            double weight = entry.getValue();
            entries.add(new BlockWeightListEntry(block, weight));
        }
        return entries;
    }

    @Environment(EnvType.CLIENT)
    public class BlockWeightListEntry extends WeightSliderListWidgetEntry<BlockWeightListEntry> {
        private final Block block;

        public BlockWeightListEntry(Block block) {
            super(block.getName(), MIN_BLOCK_WEIGHT, MAX_BLOCK_WEIGHT, INITIAL_BLOCK_WEIGHT);
            this.block = block;
        }

        public BlockWeightListEntry(Block block, double initialWeight) {
            super(block.getName(), MIN_BLOCK_WEIGHT, MAX_BLOCK_WEIGHT, initialWeight);
            this.block = block;
        }

        @Override
        public void applyWeight(double weight) {
            Map<Block, Double> blocks = CustomizeBlocksScreen.this.getCurrentBlocks();
            blocks.put(this.block, weight);
        }

        @Override
        public Item getIcon() {
            return getBlockItem(this.block);
        }
    }
}
