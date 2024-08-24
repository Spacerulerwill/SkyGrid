package net.spacerulerwill.skygrid.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.LinkedHashSet;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridMobSpawnerTab extends CustomizeSkyGridTab<CustomizeSkyGridMobSpawnerTab.EntityListWidget> {
    private Optional<EntityType<?>> currentEntity;

    public CustomizeSkyGridMobSpawnerTab(MinecraftClient client, CustomizeSkyGridScreen parent) {
        super(client, parent, Text.translatable("createWorld.customize.skygrid.tab.mob_spawner"), EntityListWidget::new);
    }

    @Override
    public boolean shouldAddButtonBeActive() {
        String text = this.parent.getText();
        try {
            this.currentEntity = Registries.ENTITY_TYPE.getOrEmpty(Identifier.of(text));
        } catch (InvalidIdentifierException e) {
            this.currentEntity = Optional.empty();
            return false;
        }
        return currentEntity.isPresent() && !this.parent.getCurrentConfig().spawnerEntities().contains(currentEntity.get());
    }

    @Override
    public void addButtonCallback() {
        EntityType<?> entity = this.currentEntity.get();
        this.listWidget.addEntity(entity);
        this.parent.getCurrentConfig().spawnerEntities().add(entity);
    }

    @Override
    public void deleteButtonCallback() {
        EntityType<?> entity = this.listWidget.getSelectedOrNull().entityType;
        this.listWidget.removeEntity(entity);
        this.parent.getCurrentConfig().spawnerEntities().remove(entity);
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
                this.addEntry(new CustomizeSkyGridMobSpawnerTab.EntityListWidgetEntry(this.parent, entity));
            }
        }

        public void addEntity(EntityType<?> entity) {
            this.addEntry(new CustomizeSkyGridMobSpawnerTab.EntityListWidgetEntry(this.parent, entity));
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
}