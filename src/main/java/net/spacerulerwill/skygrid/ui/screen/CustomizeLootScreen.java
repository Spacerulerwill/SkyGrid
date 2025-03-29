package net.spacerulerwill.skygrid.ui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.dimension.DimensionOptions;
import net.spacerulerwill.skygrid.ui.widget.WeightSliderListWidgetEntry;
import net.spacerulerwill.skygrid.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomizeLootScreen extends DimensionSpecificCustomizableListWidgetScreen<CustomizeLootScreen.ItemWeightListEntry, Item> {
    private static final double INITIAL_ITEM_WEIGHT = 1;
    private static final double MIN_ITEM_WEIGHT = 0;
    private static final double MAX_ITEM_WEIGHT = 100;

    public CustomizeLootScreen(CustomizeSkyGridScreen parent, SkyGridConfig currentConfig) {
        super(parent, currentConfig, Text.translatable("createWorld.customize.skygrid.loot"), Text.translatable("createWorld.customize.loot.placeholder"), 25);
    }

    @Override
    public void onClear() {
        Map<Item, Double> blocks = this.getChestItems();
        blocks.clear();
    }

    @Override
    protected Optional<Item> getFromTextField(String text) {
        try {
            return Registries.ITEM.getOptionalValue(Identifier.of(text));
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
        Registries.ITEM.forEach(item -> {
            if (item == Items.AIR) return;

            String displayString = Text.translatable(item.getTranslationKey()).getString();
            String valueString = Registries.ITEM.getId(item).toString();
            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(item, displayString, valueString, this.textRenderer));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(Item item) {
        Map<Item, Double> chestItems = this.getChestItems();
        if (chestItems.containsKey(item)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        chestItems.put(item, INITIAL_ITEM_WEIGHT);
        this.listWidget.addEntry(new ItemWeightListEntry(item));
    }

    @Override
    protected boolean canAdd(Item item) {
        return !getChestItems().containsKey(item);
    }

    @Override
    protected void onDelete(ItemWeightListEntry entry) {
        Map<Item, Double> chestItems = getChestItems();
        chestItems.remove(entry.item);
    }

    private Map<Item, Double> getChestItems() {
        Map<Item, Double> items;
        if (this.currentDimension == DimensionOptions.OVERWORLD) {
            items = this.currentConfig.overworldConfig().chestItems();
        } else if (this.currentDimension == DimensionOptions.NETHER) {
            items = this.currentConfig.netherConfig().chestItems();
        } else if (this.currentDimension == DimensionOptions.END) {
            items = this.currentConfig.endConfig().chestItems();
        } else {
            throw new IllegalStateException("Current dimension is not one of overworld, nether or end: " + this.currentDimension.getValue().toTranslationKey());
        }
        return items;
    }

    @Override
    protected List<ItemWeightListEntry> getEntriesFromConfig() {
        List<ItemWeightListEntry> entries = new ArrayList<>();
        Map<Item, Double> chestItems = this.getChestItems();
        for (Map.Entry<Item, Double> entry : chestItems.entrySet()) {
            Item item = entry.getKey();
            double weight = entry.getValue();
            entries.add(new ItemWeightListEntry(item, weight));
        }
        return entries;
    }

    @Environment(EnvType.CLIENT)
    public class ItemWeightListEntry extends WeightSliderListWidgetEntry<ItemWeightListEntry> {
        private final Item item;

        public ItemWeightListEntry(Item item) {
            super(item.getName(), MIN_ITEM_WEIGHT, MAX_ITEM_WEIGHT, INITIAL_ITEM_WEIGHT);
            this.item = item;
        }

        public ItemWeightListEntry(Item item, double initialWeight) {
            super(item.getName(), MIN_ITEM_WEIGHT, MAX_ITEM_WEIGHT, initialWeight);
            this.item = item;
        }

        @Override
        public void applyWeight(double weight) {
            Map<Item, Double> items = CustomizeLootScreen.this.getChestItems();
            items.put(this.item, weight);
        }

        @Override
        public Item getIcon() {
            return this.item;
        }
    }
}
