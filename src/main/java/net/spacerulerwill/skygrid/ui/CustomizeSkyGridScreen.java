package net.spacerulerwill.skygrid.ui;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CustomizeFlatLevelScreen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.spacerulerwill.skygrid.util.WorldPresetExtension;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGeneratorConfig;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridScreen extends Screen {
    // UI Stuff
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    protected final CreateWorldScreen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    private BlockTab blockTab;
    private MobSpawnerTab mobSpawnerTab;
    private ButtonWidget deleteButton;
    private ButtonWidget addButton;
    private CustomizeSkyGridTextFieldWidget textFieldWidget;
    private Block currentBlockToAdd;

    // Other Stuff
    private final Map<RegistryKey<DimensionOptions>, SkyGridChunkGeneratorConfig> dimensionChunkGeneratorConfigs;
    List<RegistryKey<DimensionOptions>> dimensions = new ArrayList<>();
    RegistryKey<DimensionOptions> currentDimension;

    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(Text.translatable("createWorld.customize.skygrid.title"));
        this.parent = parent;
        // Add initial dimension configs (default values for vanilla dimensions)
        dimensionChunkGeneratorConfigs = new HashMap<>();
        dimensionChunkGeneratorConfigs.put(DimensionOptions.OVERWORLD, SkyGridChunkGeneratorConfig.getDefaultOverworldConfig());
        dimensionChunkGeneratorConfigs.put(DimensionOptions.NETHER, SkyGridChunkGeneratorConfig.getDefaultNetherConfig());
        dimensionChunkGeneratorConfigs. put(DimensionOptions.END, SkyGridChunkGeneratorConfig.getDefaultEndConfig());
        // Array of dimensions options registry keys
        currentDimension = DimensionOptions.OVERWORLD;
        dimensions.add(DimensionOptions.OVERWORLD);
        dimensions.add(DimensionOptions.NETHER);
        dimensions.add(DimensionOptions.END);
        parent.getWorldCreator().getGeneratorOptionsHolder().dimensionOptionsRegistry().getEntrySet().forEach(entry -> {
            dimensions.add(entry.getKey());
            dimensionChunkGeneratorConfigs.put(entry.getKey(), SkyGridChunkGeneratorConfig.getDefaultConfigForModded());
        });
        currentBlockToAdd = Blocks.AIR;
    }

    protected void init() {
        blockTab = new BlockTab();
        mobSpawnerTab = new MobSpawnerTab();
        tabNavigation = TabNavigationWidget.builder(tabManager, width)
                .tabs(new Tab[]{
                        blockTab,
                        mobSpawnerTab
                }).build();
        tabNavigation.selectTab(0, false);
        layout.setFooterHeight(80);
        layout.forEachChild((child) -> {
            child.setNavigationOrder(1);
            addDrawableChild(child);
        });
        addDrawableChild(tabNavigation);

        DirectionalLayoutWidget rows = layout.addFooter(DirectionalLayoutWidget.vertical().spacing(4));

        DirectionalLayoutWidget row1 = DirectionalLayoutWidget.horizontal().spacing(8);
        textFieldWidget = row1.add(new CustomizeSkyGridTextFieldWidget(textRenderer, 158, 20, Text.translatable("createWorld.customize.skygrid.enterBlock")));
        addButton = row1.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.add"), (button) -> {
            getCurrentConfig().blocks().put(currentBlockToAdd, 100);
            blockTab.addBlock(currentBlockToAdd, 100);
            textFieldWidget.setText("");
            updateAddButtonActive();
            blockTab.widget.setScrollAmount(blockTab.widget.getMaxScroll());
        }).width(75).build());


        DirectionalLayoutWidget row2 = DirectionalLayoutWidget.horizontal().spacing(8);
        row2.add(new CyclingButtonWidget.Builder<RegistryKey<DimensionOptions>>(value -> Text.translatable(value.getValue().toTranslationKey()))
                .values(dimensions)
                .build(0, 0, 158, 20, Text.translatable("createWorld.customize.skygrid.button.dimension"), ((button, value) -> {
                    currentDimension = value;
                    blockTab.widget.setScrollAmount(0.0);
                    blockTab.refreshWidget();
                    updateDeleteButtonActive();
                    updateAddButtonActive();
                })));
        deleteButton = row2.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.delete"), (button) -> {
            Block selectedBlock = blockTab.getSelected();
            blockTab.removeBlock(selectedBlock);
            SkyGridChunkGeneratorConfig currentConfig = getCurrentConfig();
            currentConfig.blocks().remove(selectedBlock);
            updateDeleteButtonActive();
        }).width(75).build());

        DirectionalLayoutWidget row3 = DirectionalLayoutWidget.horizontal().spacing(8);
        row3.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            applyConfigurations();
            close();
        }).width(75).build());
        row3.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            close();
        }).width(75).build());
        row3.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.presets"), (button) -> {
        }).width(75).build());

        // Add rows to the main layout
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        layout.setFooterHeight(80);
        layout.forEachChild((child) -> {
            child.setNavigationOrder(1);
            addDrawableChild(child);
        });

        // Initialize tab navigation
        initTabNavigation();
        updateDeleteButtonActive();
        updateAddButtonActive();
    }

    public void initTabNavigation() {
        blockTab.resize();
        mobSpawnerTab.resize();

        if (tabNavigation != null) {
            tabNavigation.setWidth(width);
            tabNavigation.init();
            int i = tabNavigation.getNavigationFocus().getBottom();
            ScreenRect screenRect = new ScreenRect(0, i + 3, width, height - layout.getFooterHeight() - i);
            tabManager.setTabArea(screenRect);
            layout.setHeaderHeight(i);
            layout.refreshPositions();
        }
    }

    private void updateDeleteButtonActive() {
        this.deleteButton.active = this.blockTab.hasSelected();
    }

    private void updateAddButtonActive() {
        String text = textFieldWidget.getText();
        try {
            currentBlockToAdd = Registries.BLOCK.get(Identifier.of(text));
        } catch (InvalidIdentifierException e) {
            this.addButton.active = false;
            return;
        }
        this.addButton.active = !currentBlockToAdd.equals(Blocks.AIR) && !getCurrentConfig().blocks().containsKey(currentBlockToAdd);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        context.drawTexture(Screen.FOOTER_SEPARATOR_TEXTURE, 0, height - layout.getFooterHeight() - 2, 0.0F, 0.0F, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    public void close() {
        this.client.setScreen(this.parent);
    }

    // Method to apply the configurations to respective chunk generators
    private void applyConfigurations() {
        parent.getWorldCreator().applyModifier(createModifier());
    }

    private GeneratorOptionsHolder.RegistryAwareModifier createModifier() {
        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            Registry<Biome> biomeRegistry = dynamicRegistryManager.get(RegistryKeys.BIOME);
            RegistryEntry<Biome> biomeEntry = biomeRegistry.entryOf(BiomeKeys.THE_VOID);

            Map<RegistryKey<DimensionOptions>, DimensionOptions> updatedDimensions = new HashMap<>(dimensionsRegistryHolder.dimensions());

            dimensionChunkGeneratorConfigs.forEach((dimensionOptionsRegistryKey, config) -> {
                boolean hasNonZeroBlock = config.blocks().values().stream().anyMatch(weight -> weight > 0);

                if (hasNonZeroBlock) {
                    ChunkGenerator chunkGenerator = new SkyGridChunkGenerator(new FixedBiomeSource(biomeEntry), config);
                    DimensionOptions dimensionOptions = parent.getWorldCreator().getGeneratorOptionsHolder().selectedDimensions().dimensions().get(dimensionOptionsRegistryKey);
                    RegistryEntry<DimensionType> dimensionTypeRegistryEntry = dimensionOptions.dimensionTypeEntry();
                    DimensionOptions newDimensionOptions = new DimensionOptions(dimensionTypeRegistryEntry, chunkGenerator);
                    updatedDimensions.put(dimensionOptionsRegistryKey, newDimensionOptions);
                } else {
                    /*
                    There is no non-zero weighted block, so we must use default generation for this dimension. If it's
                    a vanilla dimension we must get it from a registry due to the fact that the default is overwritten by
                    our world preset json file. However for modded dimensions, we can leave it as they will have not
                    been overwritten by our world preset json
                     */
                    if (dimensionOptionsRegistryKey == DimensionOptions.OVERWORLD) {
                        DimensionOptions defaultOverworld = (dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.DEFAULT).value()).getOverworld().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultOverworld);
                    } else if (dimensionOptionsRegistryKey == DimensionOptions.NETHER) {
                        WorldPreset preset = (dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.DEFAULT).value());
                        DimensionOptions defaultNether = ((WorldPresetExtension) preset).skygrid$GetNether().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultNether);
                    } else if (dimensionOptionsRegistryKey == DimensionOptions.END) {
                        WorldPreset preset = (dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.DEFAULT).value());
                        DimensionOptions defaultEnd = ((WorldPresetExtension) preset).skygrid$GetEnd().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultEnd);
                    }

                }
            });
            return new DimensionOptionsRegistryHolder(ImmutableMap.copyOf(updatedDimensions));
        };
    }


    private SkyGridChunkGeneratorConfig getCurrentConfig() {
        return dimensionChunkGeneratorConfigs.get(currentDimension);
    }

    @Environment(EnvType.CLIENT)
    private class BlockTab extends GridScreenTab {
        private final SkyGridWeightListWidget widget;

        public BlockTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.block"));
            GridWidget.Adder adder = this.grid.setRowSpacing(8).createAdder(1);
            Positioner positioner = adder.copyPositioner();
            widget = adder.add(new SkyGridWeightListWidget(), positioner);
        }

        public void resize() {
            widget.setWidth(CustomizeSkyGridScreen.this.width);
            widget.setHeight(CustomizeSkyGridScreen.this.height - 117);
        }

        public void refreshWidget() {
            widget.refresh();
        }

        private Block getSelected() {
            return widget.getSelectedOrNull().block;
        }

        private void removeBlock(Block block) {
            widget.removeBlock(block);
        }

        private void addBlock(Block block, int initialWeight) {
            widget.addBlock(block, initialWeight);
        }

        private boolean hasSelected() {
            return this.widget.getSelectedOrNull() != null;
        }
    }

    @Environment(EnvType.CLIENT)
    public class WeightSliderWidget extends SliderWidget {
        private final int minValue;
        private final int maxValue;
        private final Text name;
        private final Block block;

        public WeightSliderWidget(int x, int y, int width, int height, int minValue, int maxValue, Block block, double initialValue, Text name) {
            super(x, y, width, height, Text.literal("FOV: " + (int) initialValue), (initialValue - minValue) / (maxValue - minValue));
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.name = name;
            this.block = block;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            int calculatedValue = (int) (this.value * (this.maxValue - this.minValue) + this.minValue);
            MutableText message = name.copy()
                    .append(Text.literal(": "))
                    .append(Text.literal(String.valueOf(calculatedValue)));
            this.setMessage(message);
        }

        @Override
        protected void applyValue() {
            int weight = (int) (this.value * (this.maxValue - this.minValue) + this.minValue);
            SkyGridChunkGeneratorConfig currentConfig = CustomizeSkyGridScreen.this.getCurrentConfig();
            currentConfig.blocks().put(block, weight);
        }
    }

    @Environment(EnvType.CLIENT)
    class CustomizeSkyGridTextFieldWidget extends TextFieldWidget {
        public CustomizeSkyGridTextFieldWidget(TextRenderer textRenderer, int x, int y, Text text) {
            super(textRenderer, x, y, text);
        }


        @Override
        public boolean charTyped(char chr, int modifiers) {
            boolean result = super.charTyped(chr, modifiers);
            onTextChanged();
            return result;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            // Detect if backspace or delete is pressed
            boolean result = super.keyPressed(keyCode, scanCode, modifiers);
            if (result && (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE)) {
                onTextChanged();
            }
            return result;
        }

        private void onTextChanged() {
            CustomizeSkyGridScreen.this.updateAddButtonActive();
        }
    }

    @Environment(EnvType.CLIENT)
    private class SkyGridWeightListWidget extends AlwaysSelectedEntryListWidget<CustomizeSkyGridScreen.SkyGridWeightListWidget.SkyGridWeightEntry> {
        public SkyGridWeightListWidget() {
            super(CustomizeSkyGridScreen.this.client, CustomizeSkyGridScreen.this.width, CustomizeSkyGridScreen.this.height - 117, 43, 24);
            this.refresh();
        }

        public void refresh() {
            this.clearEntries();
            LinkedHashMap<Block, Integer> blocks = CustomizeSkyGridScreen.this.getCurrentConfig().blocks();
            for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
                this.addEntry(new SkyGridWeightEntry(entry.getKey(), entry.getValue(), entry.getKey().getName()));
            }
        }

        @Override
        protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
            if (this.getEntryCount() > 0) {
                super.renderList(context, mouseX, mouseY, delta);
            }
        }

        @Override
        public void setSelected(@Nullable CustomizeSkyGridScreen.SkyGridWeightListWidget.SkyGridWeightEntry entry) {
            super.setSelected(entry);
            CustomizeSkyGridScreen.this.updateDeleteButtonActive();
        }

        private void removeBlock(Block block) {
            SkyGridChunkGeneratorConfig currentConfig = CustomizeSkyGridScreen.this.getCurrentConfig();
            List<Block> keys = currentConfig.blocks().keySet().stream().toList();
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i) == block) {
                    remove(i);
                }
            }
        }

        private void addBlock(Block block, int initialWeight) {
            addEntry(new SkyGridWeightEntry(block, initialWeight, CustomizeSkyGridScreen.this.currentBlockToAdd.getName()));
        }

        @Environment(EnvType.CLIENT)
        private class SkyGridWeightEntry extends AlwaysSelectedEntryListWidget.Entry<SkyGridWeightEntry> {
            private final WeightSliderWidget weightSliderWidget;
            private final Block block;

            public SkyGridWeightEntry(Block block, double initialWeight, Text name) {
                this.block = block;
                this.weightSliderWidget = new WeightSliderWidget(0, 0, 193, 20, 0, 500, block, (int) initialWeight, name); // Adjust width and height as needed
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                BlockState blockState = block.getDefaultState();
                ItemStack itemStack = createItemStackFor(blockState);

                // Render the block icon and name
                this.renderIcon(context, x, y, itemStack);

                // Update slider position relative to the entry position
                this.weightSliderWidget.setX(x + 22); // Adjust as needed
                this.weightSliderWidget.setY(y); // Adjust as needed
                this.weightSliderWidget.render(context, mouseX, mouseY, tickDelta);
            }

            private ItemStack createItemStackFor(BlockState state) {
                Item item = state.getBlock().asItem();
                if (item == Items.AIR) {
                    if (state.isOf(Blocks.WATER)) {
                        item = Items.WATER_BUCKET;
                    } else if (state.isOf(Blocks.LAVA)) {
                        item = Items.LAVA_BUCKET;
                    }
                }
                return new ItemStack(item);
            }

            private void renderIcon(DrawContext context, int x, int y, ItemStack iconItem) {
                this.renderIconBackgroundTexture(context, x + 1, y + 1);
                if (!iconItem.isEmpty()) {
                    context.drawItemWithoutEntity(iconItem, x + 2, y + 2);
                }

            }

            private void renderIconBackgroundTexture(DrawContext context, int x, int y) {
                context.drawGuiTexture(CustomizeSkyGridScreen.SLOT_TEXTURE, x, y, 0, 18, 18);
            }

            public Text getNarration() {
                return Text.of("test");
            }

            // Override mouseClicked to forward the event to the slider
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (weightSliderWidget.isMouseOver(mouseX, mouseY)) {
                    return weightSliderWidget.mouseClicked(mouseX, mouseY, button);
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            // Override mouseDragged to forward the event to the slider
            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (weightSliderWidget.isMouseOver(mouseX, mouseY)) {
                    return weightSliderWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }

            // Override mouseReleased to forward the event to the slider
            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (weightSliderWidget.isMouseOver(mouseX, mouseY)) {
                    return weightSliderWidget.mouseReleased(mouseX, mouseY, button);
                }
                return super.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private class MobSpawnerTab extends GridScreenTab {
        public MobSpawnerTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.mob_spawner"));
        }

        public void resize() {

        }
    }
}