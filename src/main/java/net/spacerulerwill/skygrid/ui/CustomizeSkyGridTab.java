package net.spacerulerwill.skygrid.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public abstract class CustomizeSkyGridTab<T extends CustomizeSkyGridListWidget<?>> extends GridScreenTab {
    CustomizeSkyGridScreen parent;
    T listWidget;
    public CustomizeSkyGridTab(MinecraftClient client, CustomizeSkyGridScreen parent, Text title, SkyGridListWidgetConstructor<T> constructor) {
        super(title);
        this.parent = parent;
        GridWidget.Adder adder = this.grid.setRowSpacing(8).createAdder(1);
        Positioner positioner = adder.copyPositioner();
        this.listWidget = adder.add(constructor.create(client, parent), positioner);
    }

    public void resize() {
        this.listWidget.setWidth(this.parent.width);
        this.listWidget.setHeight(this.parent.height - 117);
    }

    public void onDimensionChange() {
        this.listWidget.onDimensionChange();
    }

    public boolean shouldDeleteButtonBeActive() {
        return this.listWidget.getSelectedOrNull() != null;
    }

    public abstract boolean shouldAddButtonBeActive();
    public abstract void deleteButtonCallback();
    public void addButtonCallback() {
        this.listWidget.setScrollAmount(this.listWidget.getMaxScroll());
    }
}
