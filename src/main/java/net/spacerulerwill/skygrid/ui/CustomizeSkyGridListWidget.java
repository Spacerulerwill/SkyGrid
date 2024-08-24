package net.spacerulerwill.skygrid.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import org.jetbrains.annotations.Nullable;

public abstract class CustomizeSkyGridListWidget<T extends AlwaysSelectedEntryListWidget.Entry<T>> extends AlwaysSelectedEntryListWidget<T> {
    protected final CustomizeSkyGridScreen parent;

    public CustomizeSkyGridListWidget(MinecraftClient minecraftClient, CustomizeSkyGridScreen parent, int entryHeight) {
        super(minecraftClient, parent.width, parent.height - 117, 43, entryHeight);
        this.parent = parent;
        this.refreshEntries();
    }

    public void onDimensionChange() {
        this.refreshEntries();
        this.setScrollAmount(0.0);
    }

    @Override
    public void setSelected(@Nullable T entry) {
        super.setSelected(entry);
        this.parent.updateDeleteButtonActive();
    }

    public abstract void refreshEntries();
}
