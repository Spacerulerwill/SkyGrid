package net.spacerulerwill.skygrid.ui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public abstract class WeightSlider extends SliderWidget {
    private final double minValue;
    private final double maxValue;
    private final Text text;

    public WeightSlider(int x, int y, int width, int height, Text text, double minValue, double maxValue, double initialValue) {
        super(x, y, width, height, Text.empty(), 0.0);
        this.value = (initialValue - minValue) / (maxValue - minValue);
        this.text = text;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.updateMessage();
    }

    private MutableText createMessage() {
        int weight = getWeight();
        return this.text.copy()
                .append(Text.literal(": "))
                .append(Text.literal(String.valueOf(weight)));
    }

    private int getWeight() {
        return (int) (this.value * (this.maxValue - this.minValue) + this.minValue);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.createMessage());
    }

    protected abstract void applyWeight(int weight);

    @Override
    protected void applyValue() {
        this.applyWeight(this.getWeight());
    }
}
