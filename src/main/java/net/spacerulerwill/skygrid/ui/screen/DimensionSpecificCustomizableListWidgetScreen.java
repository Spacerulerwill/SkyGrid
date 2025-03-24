package net.spacerulerwill.skygrid.ui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionOptions;
import net.spacerulerwill.skygrid.ui.util.RenderUtils;
import net.spacerulerwill.skygrid.worldgen.SkyGridConfig;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;


/// A screen that will allow you to adjust dimension specific SkyGrid features via a ListWidget
@Environment(EnvType.CLIENT)
public abstract class DimensionSpecificCustomizableListWidgetScreen<T extends AlwaysSelectedEntryListWidget.Entry<T>, V> extends Screen {
    private final static Text CLEAR_TEXT = Text.translatable("createWorld.customize.skygrid.clear");
    private final static List<RegistryKey<DimensionOptions>> DIMENSIONS = List.of(DimensionOptions.OVERWORLD, DimensionOptions.NETHER, DimensionOptions.END);
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final Text title;
    private final Text textFieldPlaceholder;
    private final CustomizeSkyGridScreen parent;
    private final int entryHeight;
    protected ListWidget listWidget;
    protected TextField textField;
    private ButtonWidget addButton;
    private ButtonWidget deleteButton;
    private CyclingButtonWidget<RegistryKey<DimensionOptions>> dimensionsSelector;
    private ButtonWidget doneButton;
    private ButtonWidget cancelButton;
    protected RegistryKey<DimensionOptions> currentDimension;
    protected SkyGridConfig currentConfig;

    public DimensionSpecificCustomizableListWidgetScreen(CustomizeSkyGridScreen parent, SkyGridConfig currentConfig, Text title, Text textFieldPlaceholder, int entryHeight) {
        super(title);
        this.title = title;
        this.textFieldPlaceholder = textFieldPlaceholder;
        this.entryHeight = entryHeight;
        this.parent = parent;
        this.currentDimension = DimensionOptions.OVERWORLD;
        this.currentConfig = new SkyGridConfig(currentConfig);
    }

    private void initHeader() {
        this.layout.addHeader(this.title, this.textRenderer);
    }

    private void initBody() {
        this.listWidget = this.layout.addBody(new ListWidget(this.client, this.width, this.height - 117, 43, this.entryHeight));
    }

    private void initFooter() {
        DirectionalLayoutWidget rows = DirectionalLayoutWidget.vertical().spacing(4);
        // Row 1 - Done, Clear, Cancel
        DirectionalLayoutWidget row1 = DirectionalLayoutWidget.horizontal().spacing(8);
        this.doneButton = row1.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.parent.updateSkyGridConfig(this.currentConfig);
            this.close();
        }).width(75).build());
        this.cancelButton = row1.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.close();
        }).width(75).build());
        row1.add(ButtonWidget.builder(CLEAR_TEXT, (button -> {
            this.onClear();
            this.listWidget.clearEntries();
            this.listWidget.setScrollY(0.0);
        })).width(75).build());
        // Row 2 - Dimension selector and Delete button
        DirectionalLayoutWidget row2 = DirectionalLayoutWidget.horizontal().spacing(8);
        this.dimensionsSelector = row2.add(new CyclingButtonWidget.Builder<RegistryKey<DimensionOptions>>(value -> Text.translatable(value.getValue().toTranslationKey()))
                .values(DIMENSIONS)
                .build(0, 0, 158, 20, Text.translatable("createWorld.customize.skygrid.dimension"), ((button, dimension) -> {
                    this.currentDimension = dimension;
                    this.regenerateListEntries();
                    this.updateAddButtonActive();
                    this.updateDeleteButtonActive();
                })));
        this.deleteButton = row2.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.delete"), (button) -> {
            T entry = this.listWidget.getSelectedOrNull();
            if (entry == null) {
                return;
            }
            this.listWidget.removeEntry(entry);
            this.onDelete(entry);
            this.updateAddButtonActive();
            this.updateDeleteButtonActive();
        }).width(75).build());
        // Row 3 - Text field and Add button
        DirectionalLayoutWidget row3 = DirectionalLayoutWidget.horizontal().spacing(8);
        this.textField = row3.add(new TextField(textRenderer, 158, 20, this.textFieldPlaceholder));
        this.addButton = row3.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.add"), (button) -> {
            Optional<V> v = this.getFromTextField(this.textField.getText());
            v.ifPresent(this::onAdd);
            this.updateAddButtonActive();
            this.updateDeleteButtonActive();
            this.textField.setText("");
        }).width(75).build());
        rows.add(row3);
        rows.add(row2);
        rows.add(row1);
        this.layout.addFooter(rows);
        this.layout.setFooterHeight(80);
    }

    @Override
    protected void init() {
        // Header for title
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.regenerateListEntries();
        this.refreshWidgetPositions();
        this.updateAddButtonActive();
        this.updateDeleteButtonActive();
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.listWidget != null) {
            this.listWidget.position(this.width, this.layout);
        }
        this.textField.refreshPositions();
    }

    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void regenerateListEntries() {
        this.listWidget.replaceEntries(this.getEntriesFromConfig());
        this.listWidget.setScrollY(0.0);
    }

    private void updateAddButtonActive() {
        Optional<V> thing = getFromTextField(this.textField.getText());
        this.addButton.active = thing.isPresent() && this.canAdd(thing.get());
    }

    private void updateDeleteButtonActive() {
        T entry = this.listWidget.getSelectedOrNull();
        this.deleteButton.active = entry != null;
    }

    private void hideWidgetsForAutocompleteBox() {
        this.remove(this.doneButton);
        this.remove(this.cancelButton);
        this.remove(this.dimensionsSelector);
    }

    private void showWidgetsForAutocompleteBox() {
        this.addDrawableChild(this.doneButton);
        this.addDrawableChild(this.cancelButton);
        this.addDrawableChild(this.dimensionsSelector);
    }

    protected abstract void onClear();
    protected abstract Optional<V> getFromTextField(String text);
    protected abstract List<AutocompleteListWidget.Entry> getAutocompleteSuggestions(String text);
    protected abstract void onAdd(V thing);
    protected abstract boolean canAdd(V thing);
    protected abstract void onDelete(T entry);
    protected abstract List<T> getEntriesFromConfig();

    @Environment(EnvType.CLIENT)
    protected class ListWidget extends AlwaysSelectedEntryListWidget<T> {
        public ListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l) {
            super(minecraftClient, i, j, k, l);
        }

        @Override
        public void setSelected(@Nullable T entry) {
            super.setSelected(entry);
            DimensionSpecificCustomizableListWidgetScreen.this.updateDeleteButtonActive();
        }

        @Override
        public void setSelected(int index) {
            super.setSelected(index);
            DimensionSpecificCustomizableListWidgetScreen.this.updateDeleteButtonActive();
        }
    }

    @Environment(EnvType.CLIENT)
    protected class TextField extends TextFieldWidget {
        @Nullable
        public AutocompleteListWidget autocompleteListWidget;

        public TextField(TextRenderer textRenderer, int x, int y, Text text) {
            super(textRenderer, x, y, text);
        }

        private void refreshPositions() {
            if (this.autocompleteListWidget != null) {
                this.autocompleteListWidget.setX(this.getX());
                this.autocompleteListWidget.setY(this.getY() + this.getHeight() + 4);
            }
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            boolean result = super.charTyped(chr, modifiers);
            this.onTextChanged();
            return result;
        }

        @Override
        public void setText(String text) {
            super.setText(text);
            this.onTextChanged();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean result = super.keyPressed(keyCode, scanCode, modifiers);
            if (result && (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE)) {
                this.onTextChanged();
            }
            return result;
        }

        private void onTextChanged() {
            DimensionSpecificCustomizableListWidgetScreen.this.updateAddButtonActive();
            List<AutocompleteListWidget.Entry> autocompleteResults = DimensionSpecificCustomizableListWidgetScreen.this.getAutocompleteSuggestions(this.getText());
            if (autocompleteResults.isEmpty()) {
                if (this.autocompleteListWidget != null) {
                    DimensionSpecificCustomizableListWidgetScreen.this.remove(this.autocompleteListWidget);
                    DimensionSpecificCustomizableListWidgetScreen.this.showWidgetsForAutocompleteBox();
                    this.autocompleteListWidget = null;
                }
            } else {
                if (this.autocompleteListWidget == null) {
                    this.autocompleteListWidget = new AutocompleteListWidget(DimensionSpecificCustomizableListWidgetScreen.this.client, DimensionSpecificCustomizableListWidgetScreen.this);
                    this.refreshPositions();
                    for (AutocompleteListWidget.Entry entry : autocompleteResults) {
                        this.autocompleteListWidget.addEntry(entry);
                    }
                    DimensionSpecificCustomizableListWidgetScreen.this.addDrawableChild(this.autocompleteListWidget);
                    DimensionSpecificCustomizableListWidgetScreen.this.hideWidgetsForAutocompleteBox();
                } else {
                    this.autocompleteListWidget.replaceEntries(autocompleteResults);
                }

            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class AutocompleteListWidget extends AlwaysSelectedEntryListWidget<AutocompleteListWidget.Entry> {
        DimensionSpecificCustomizableListWidgetScreen<?, ?> parent;

        public AutocompleteListWidget(MinecraftClient minecraftClient, DimensionSpecificCustomizableListWidgetScreen<?, ?> parent) {
            super(minecraftClient, 158, 44, 0, 24);
            this.parent = parent;
        }

        @Override
        public int getRowWidth() {
            return this.getWidth() - 16;
        }

        @Override
        protected int getScrollbarX() {
            return this.getX() + this.getWidth() - 6;
        }

        @Override
        public void setSelected(@Nullable DimensionSpecificCustomizableListWidgetScreen.AutocompleteListWidget.Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                this.parent.textField.setText(entry.valueText);
            }
        }

        @Override
        public void setSelected(int index) {
            super.setSelected(index);
            AutocompleteListWidget.Entry entry = this.getEntry(index);
            this.parent.textField.setText(entry.valueText);
        }

        @Environment(EnvType.CLIENT)
        public static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            @Nullable
            private final Item iconItem;
            private final String displayText;
            public final String valueText;
            private final TextRenderer textRenderer;

            public Entry(@Nullable Item iconItem, String displayText, String valueText, TextRenderer textRenderer) {
                this.iconItem = iconItem;
                this.displayText = displayText;
                this.valueText = valueText;
                this.textRenderer = textRenderer;
            }

            @Override
            public Text getNarration() {
                return Text.empty();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if (this.iconItem == null) {
                    context.drawText(this.textRenderer, this.displayText, x + 5, y + 3, 16777215, false);
                } else {
                    RenderUtils.renderItemIcon(this.iconItem, context, x, y);
                    context.drawText(this.textRenderer, this.displayText, x + 18 + 5, y + 3, 16777215, false);
                }
            }
        }
    }
}
