package net.spacerulerwill.skygrid.ui.widget;


import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

// A list widget of rows of clickable widgets(buttons etc.)
@Environment(EnvType.CLIENT)
public class ClickableWidgetList extends ElementListWidget<ClickableWidgetList.ListWidgetEntry> {
    private static final int ENTRY_WIDTH = 310;
    public ClickableWidgetList(MinecraftClient minecraftClient, List<List<ClickableWidget>> widgets, int width, int height, int headerHeight) {
        super(minecraftClient, width, height, headerHeight, 25);
        for (List<ClickableWidget> row : widgets) {
            this.addEntry(new ListWidgetEntry(row));
        }
    }

    @Override
    public int getRowWidth() {
        return ENTRY_WIDTH;
    }

    @Environment(EnvType.CLIENT)
    protected static class ListWidgetEntry extends ElementListWidget.Entry<ListWidgetEntry> {
        private static final int PADDING = 10;

        private final List<ClickableWidget> widgets;

        ListWidgetEntry(List<ClickableWidget> widgets) {
            this.widgets = widgets;
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.widgets.isEmpty()) {
                return;
            }
            int currentX = x;
            int widgetWidth = (ENTRY_WIDTH - PADDING * (this.widgets.size() - 1)) / widgets.size();
            for(ClickableWidget clickableWidget : this.widgets) {
                clickableWidget.setPosition(currentX, y);
                clickableWidget.setWidth(widgetWidth);
                clickableWidget.render(context, mouseX, mouseY, tickDelta);
                currentX += widgetWidth + PADDING;
            }
        }

        public List<? extends Selectable> selectableChildren() {
            return this.widgets;
        }

        @Override
        public List<? extends Element> children() { return this.widgets; }
    }
}
