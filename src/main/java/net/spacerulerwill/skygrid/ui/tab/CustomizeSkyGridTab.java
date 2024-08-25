package net.spacerulerwill.skygrid.ui.tab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.text.Text;
import net.spacerulerwill.skygrid.ui.widget.CustomizeSkyGridListWidget;
import net.spacerulerwill.skygrid.ui.screen.CustomizeSkyGridScreen;
import net.spacerulerwill.skygrid.ui.SkyGridListWidgetConstructor;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class CustomizeSkyGridTab<T extends CustomizeSkyGridListWidget<?>, K extends AlwaysSelectedEntryListWidget.Entry<K>> extends GridScreenTab {
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

    public void refreshListWidget() {
        this.listWidget.refreshEntries();
    }

    public void reset() {
        this.listWidget.refreshEntries();
        this.listWidget.setScrollAmount(0.0);
    }

    public void onDimensionChange() {
        this.listWidget.onDimensionChange();
    }

    public boolean shouldDeleteButtonBeActive() {
        return this.listWidget.getSelectedOrNull() != null;
    }

    public abstract boolean shouldAddButtonBeActive();
    public abstract void deleteButtonCallback();
    public abstract CustomizeSkyGridScreen.CustomizeSkyGridTextFieldWidget.AutoCompleteListWidget<K> getAutoCompleteListWidget(String text);
    public void addButtonCallback() {
        this.listWidget.setScrollAmount(this.listWidget.getMaxScroll());
    }
}
