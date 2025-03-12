package net.spacerulerwill.skygrid.ui.tab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.spacerulerwill.skygrid.ui.widget.CustomizeSkyGridListWidget;
import net.spacerulerwill.skygrid.ui.screen.CustomizeSkyGridScreen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridMobSpawnerTab extends CustomizeSkyGridTab<CustomizeSkyGridMobSpawnerTab.EntityListWidget, CustomizeSkyGridMobSpawnerTab.EntityAutoCompleteListWidgetEntry> {
    private Optional<EntityType<?>> currentEntity;
    private MinecraftClient client;

    public CustomizeSkyGridMobSpawnerTab(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(client, parent, Text.translatable("createWorld.customize.skygrid.tab.mob_spawner"), EntityListWidget::new);
        this.client = client;
    }

    @Override
    public boolean shouldAddButtonBeActive() {
        EntityAutoCompleteListWidgetEntry entry = (EntityAutoCompleteListWidgetEntry) this.parent.getSelectedEntryOrNull();
        if (entry == null) {
            this.currentEntity = Optional.empty();
            return false;
        } else {
            this.currentEntity = Optional.of(entry.entity);
            return !this.parent.getCurrentConfig().spawnerEntities().contains(entry.entity);
        }
    }

    @Override
    public void addButtonCallback() {
        EntityType<?> entity = this.currentEntity.get();
        this.listWidget.addEntity(entity);
        this.parent.getCurrentConfig().spawnerEntities().add(entity);
        super.addButtonCallback();
    }

    @Override
    public void deleteButtonCallback() {
        EntityType<?> entity = this.listWidget.getSelectedOrNull().entityType;
        this.listWidget.removeEntity(entity);
        this.parent.getCurrentConfig().spawnerEntities().remove(entity);
    }

    @Override
    public CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<EntityAutoCompleteListWidgetEntry> getAutoCompleteListWidget(String text) {
        List<EntityAutoCompleteListWidgetEntry> results = new ArrayList<>();
        if (text.isBlank()) {
            return new CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<EntityAutoCompleteListWidgetEntry>(
                    this.parent,
                    this.client,
                    results
            );
        }
        Registries.ENTITY_TYPE.forEach(entity -> {
            String blockString = Text.translatable(entity.getTranslationKey()).getString();
            if (blockString.trim().toLowerCase().startsWith(text)) {
                results.add(new EntityAutoCompleteListWidgetEntry(entity));
            }
        });
        return new CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<EntityAutoCompleteListWidgetEntry>(
                this.parent,
                this.client,
                results
        );
    }
    @Environment(EnvType.CLIENT)
    public static class EntityListWidget extends CustomizeSkyGridListWidget<EntityListWidgetEntry> {
        public EntityListWidget(MinecraftClient minecraftClient, CustomizeSkyGridScreen parent) {
            super(minecraftClient, parent, 15);
        }

        @Override
        public void refreshEntries() {
            this.clearEntries();
            LinkedHashSet<EntityType<?>> entities = this.parent.getCurrentConfig().spawnerEntities();
            for (EntityType<?> entity: entities) {
                this.addEntry(new EntityListWidgetEntry(this.parent, entity));
            }
        }

        public void addEntity(EntityType<?> entity) {
            this.addEntry(new EntityListWidgetEntry(this.parent, entity));
        }

        public void removeEntity(EntityType<?> entity) {
            for (int i = 0; i < this.getEntryCount(); i++) {
                if (getEntry(i).entityType.equals(entity)) {
                    remove(i);
                    return;
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class EntityListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry<EntityListWidgetEntry> {
        CustomizeSkyGridScreen parent;
        private final EntityType<?> entityType;

        public EntityListWidgetEntry(CustomizeSkyGridScreen parent, EntityType<?> entityType) {
            this.parent = parent;
            this.entityType = entityType;
        }

        @Override
        public Text getNarration() {
            return Text.of("bruh");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(this.parent.getTextRenderer(), this.entityType.getName(), x, y, 16777215, false);
        }
    }

    @Environment(EnvType.CLIENT)
    public class EntityAutoCompleteListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry<EntityAutoCompleteListWidgetEntry> {
        public EntityType<?> entity;

        public EntityAutoCompleteListWidgetEntry(EntityType<?> entity) {
            this.entity = entity;
        }

        @Override
        public Text getNarration() {
            return Text.of("pain");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(CustomizeSkyGridMobSpawnerTab.this.parent.getTextRenderer(), this.entity.getName(), x, y, 16777215, false);
        }
    }
}