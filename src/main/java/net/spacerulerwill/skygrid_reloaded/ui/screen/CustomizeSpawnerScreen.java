package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.dimension.DimensionOptions;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class CustomizeSpawnerScreen extends DimensionSpecificCustomizableListWidgetScreen<CustomizeSpawnerScreen.EntityListWidgetEntry, EntityType<?>> {
    public CustomizeSpawnerScreen(CustomizeSkyGridScreen parent, SkyGridConfig currentConfig) {
        super(parent, currentConfig, Text.translatable("createWorld.customize.skygrid.spawners"), Text.translatable("createWorld.customize.skygrid.spawners.placeholder"), 15);
    }

    private Set<EntityType<?>> getSpawnerEntities() {
        Set<EntityType<?>> entities;
        if (this.currentDimension == DimensionOptions.OVERWORLD) {
            entities = this.currentConfig.overworldConfig().spawnerEntities();
        } else if (this.currentDimension == DimensionOptions.NETHER) {
            entities = this.currentConfig.netherConfig().spawnerEntities();
        } else if (this.currentDimension == DimensionOptions.END) {
            entities = this.currentConfig.endConfig().spawnerEntities();
        } else {
            throw new IllegalStateException("Current dimension is not one of overworld, nether or end: " + this.currentDimension.getValue().toTranslationKey());
        }
        return entities;
    }

    @Override
    protected void onClear() {
        Set<EntityType<?>> entities = this.getSpawnerEntities();
        entities.clear();
    }

    @Override
    protected Optional<EntityType<?>> getFromTextField(String text) {
        try {
            return Registries.ENTITY_TYPE.getOptionalValue(Identifier.of(text));
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
        Registries.ENTITY_TYPE.forEach(entityType -> {
            String displayString = Text.translatable(entityType.getTranslationKey()).getString();
            String valueString = Registries.ENTITY_TYPE.getId(entityType).toString();
            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(null, displayString, valueString, this.textRenderer));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(EntityType<?> entity) {
        Set<EntityType<?>> entities = this.getSpawnerEntities();
        if (entities.contains(entity)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        entities.add(entity);
        this.listWidget.addEntry(new EntityListWidgetEntry(entity));
    }

    @Override
    protected boolean canAdd(EntityType<?> entity) {
        return !this.getSpawnerEntities().contains(entity);
    }

    @Override
    protected void onDelete(EntityListWidgetEntry entry) {
        Set<EntityType<?>> entities = getSpawnerEntities();
        entities.remove(entry.entityType);
    }

    @Override
    protected List<EntityListWidgetEntry> getEntriesFromConfig() {
        List<EntityListWidgetEntry> entries = new ArrayList<>();
        Set<EntityType<?>> entities = this.getSpawnerEntities();
        for (EntityType<?> entity : entities) {
            entries.add(new EntityListWidgetEntry(entity));
        }
        return entries;
    }

    @Environment(EnvType.CLIENT)
    protected class EntityListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry<EntityListWidgetEntry> {
        private final EntityType<?> entityType;

        public EntityListWidgetEntry(EntityType<?> entityType) {
            this.entityType = entityType;
        }

        @Override
        public Text getNarration() {
            return Text.empty();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(CustomizeSpawnerScreen.this.textRenderer, this.entityType.getName(), x + 3, y + 2, 16777215, false);
        }
    }
}
