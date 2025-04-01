package net.spacerulerwill.skygrid_reloaded.ui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.spacerulerwill.skygrid_reloaded.ui.util.RenderUtils;

public abstract class WeightSliderListWidgetEntry<T extends AlwaysSelectedEntryListWidget.Entry<T>> extends AlwaysSelectedEntryListWidget.Entry<T> {
    private final WeightSlider weightSlider;

    public WeightSliderListWidgetEntry(Text text, double min, double max, double initial) {
        this.weightSlider = new WeightSlider(0, 0, 193, 20, text, min, max, initial) {
            @Override
            protected void applyWeight(double weight) {
                WeightSliderListWidgetEntry.this.applyWeight(weight);
            }
        };
    }

    public abstract void applyWeight(double weight);

    public abstract Item getIcon();

    @Override
    public Text getNarration() {
        return Text.empty();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.weightSlider.setX(x + 22);
        this.weightSlider.setY(y);
        this.weightSlider.render(context, mouseX, mouseY, tickDelta);
        RenderUtils.renderItemIcon(this.getIcon(), context, x, y);
    }

    // Allowing slider to be draggable
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.weightSlider.isMouseOver(mouseX, mouseY)) {
            return this.weightSlider.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.weightSlider.isMouseOver(mouseX, mouseY)) {
            return this.weightSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.weightSlider.isMouseOver(mouseX, mouseY)) {
            return this.weightSlider.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
