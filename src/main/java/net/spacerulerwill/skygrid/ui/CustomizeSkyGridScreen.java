package net.spacerulerwill.skygrid.ui;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
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
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.spacerulerwill.skygrid.util.BlockWeight;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid.worldgen.SkyGridChunkGeneratorConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridScreen extends Screen {
    // UI Stuff
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    protected final CreateWorldScreen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    private SkyGridWeightListWidget layers;
    private BlockTab blockTab;
    private MobSpawnerTab mobSpawnerTab;

    // Other Stuff
    private final Map<RegistryKey<DimensionOptions>, SkyGridChunkGeneratorConfig> dimensionChunkGeneratorConfigs;
    List<RegistryKey<DimensionOptions>> dimensions = new ArrayList<>();
    RegistryKey<DimensionOptions> currentDimension;

    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(Text.translatable("createWorld.customize.skygrid.title"));
        this.parent = parent;
        // Add initial dimension configs (default values for vanilla dimensions)
        dimensionChunkGeneratorConfigs = new HashMap<>();
        dimensionChunkGeneratorConfigs.put(DimensionOptions.OVERWORLD, SkyGridChunkGenerator.getDefaultOverworldConfig());
        dimensionChunkGeneratorConfigs.put(DimensionOptions.NETHER, SkyGridChunkGenerator.getDefaultNetherConfig());
        dimensionChunkGeneratorConfigs.put(DimensionOptions.END, SkyGridChunkGenerator.getDefaultEndConfig());
        // Array of dimensions options registry keys
        currentDimension = DimensionOptions.OVERWORLD;
        dimensions.add(DimensionOptions.OVERWORLD);
        dimensions.add(DimensionOptions.NETHER);
        dimensions.add(DimensionOptions.END);
        parent.getWorldCreator().getGeneratorOptionsHolder().dimensionOptionsRegistry().getEntrySet().forEach(entry -> {
            dimensions.add(entry.getKey());
            dimensionChunkGeneratorConfigs.put(entry.getKey(), SkyGridChunkGenerator.getDefaultConfigForModded());
        });
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
        row1.add(new TextFieldWidget(textRenderer, 158, 20, Text.translatable("createWorld.customize.skygrid.enterBlock")));
        row1.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.add"), (button) -> {
        }).width(75).build());


        DirectionalLayoutWidget row2 = DirectionalLayoutWidget.horizontal().spacing(8);
        row2.add(new CyclingButtonWidget.Builder<RegistryKey<DimensionOptions>>(value -> Text.translatable(value.getValue().toTranslationKey()))
                .values(dimensions)
                .build(0, 0, 158, 20, Text.translatable("createWorld.customize.skygrid.button.dimension"), ((button, value) -> {
                    currentDimension = value;
                    blockTab.refreshWidget();
                })));
        row2.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.delete"), (button) -> {
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
    }

    public void initTabNavigation() {
        blockTab.resize();
        mobSpawnerTab.resize();

        if (tabNavigation != null) {
            tabNavigation.setWidth(width);
            tabNavigation.init();
            int i = tabNavigation.getNavigationFocus().getBottom();
            ScreenRect screenRect = new ScreenRect(0, i+3, width, height - layout.getFooterHeight() - i);
            tabManager.setTabArea(screenRect);
            layout.setHeaderHeight(i);
            layout.refreshPositions();
        }
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
            // We must create an ENTIRELY NEW dimension options map to replace it because it is immutable... :(
            // Get our registries from the dynamic registry manager
            Registry<Biome> biomeRegistry = dynamicRegistryManager.get(RegistryKeys.BIOME);
            RegistryEntry<Biome> biomeEntry = biomeRegistry.entryOf(BiomeKeys.THE_VOID);

            // New map
            Map<RegistryKey<DimensionOptions>, DimensionOptions> updatedDimensions = new HashMap<>(dimensionsRegistryHolder.dimensions());

            dimensionChunkGeneratorConfigs.forEach((dimensionOptionsRegistryKey, config) -> {
                // If the config contains no blocks, use vanilla generation
                if (!config.blocks().isEmpty()) {
                    // new chunk generator
                    ChunkGenerator chunkGenerator = new SkyGridChunkGenerator(new FixedBiomeSource(biomeEntry), config);
                    // Get the dimension type from the dimension option registry and create new dimension options and update in the map
                    DimensionOptions dimensionOptions = parent.getWorldCreator().getGeneratorOptionsHolder().selectedDimensions().dimensions().get(dimensionOptionsRegistryKey);
                    RegistryEntry<DimensionType> dimensionTypeRegistryEntry = dimensionOptions.dimensionTypeEntry();
                    DimensionOptions newDimensionOptions = new DimensionOptions(dimensionTypeRegistryEntry, chunkGenerator);
                    updatedDimensions.put(dimensionOptionsRegistryKey, newDimensionOptions);
                }
            });

            // Return as an immutable map
            return new DimensionOptionsRegistryHolder(ImmutableMap.copyOf(updatedDimensions));
        };
    }

    private SkyGridChunkGeneratorConfig getCurrentConfig() {
        return dimensionChunkGeneratorConfigs.get(currentDimension);
    }

    @Environment(EnvType.CLIENT)
    private class BlockTab extends GridScreenTab {
        public SkyGridWeightListWidget widget;
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
    }

    public class WeightSliderWidget extends SliderWidget {
        private final double minValue;
        private final double maxValue;

        public WeightSliderWidget(int x, int y, int width, int height, double minValue, double maxValue, double initialValue) {
            super(x, y, width, height, Text.literal("FOV: " + (int) initialValue), (initialValue - minValue) / (maxValue - minValue));
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal("Weight: " + (int) (this.value * (this.maxValue - this.minValue) + this.minValue)));
        }

        @Override
        protected void applyValue() {
            double weight = this.value * (this.maxValue - this.minValue) + this.minValue;
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
            for(int i = 0; i < CustomizeSkyGridScreen.this.getCurrentConfig().blocks().size(); ++i) {
                this.addEntry(new SkyGridWeightEntry());
            }
        }

        @Override
        protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
            if (this.getEntryCount() > 0) {
                super.renderList(context, mouseX, mouseY, delta);
            }
        }

        @Environment(EnvType.CLIENT)
        private class SkyGridWeightEntry extends AlwaysSelectedEntryListWidget.Entry<SkyGridWeightEntry> {
            private final WeightSliderWidget weightSliderWidget;

            public SkyGridWeightEntry() {
                this.weightSliderWidget = new WeightSliderWidget(0, 0, 162, 20, 0, 1000, 500); // Adjust width and height as needed
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                SkyGridChunkGeneratorConfig currentConfig = CustomizeSkyGridScreen.this.getCurrentConfig();
                BlockWeight blockWeight = currentConfig.blocks().get(currentConfig.blocks().size() - index - 1);
                BlockState blockState = blockWeight.block().getDefaultState();
                ItemStack itemStack = this.createItemStackFor(blockState);

                // Render the block icon and name
                this.renderIcon(context, x, y, itemStack);
                context.drawText(CustomizeSkyGridScreen.this.textRenderer, itemStack.getName(), x + 18 + 5, y + 3, 16777215, false);

                // Update slider position relative to the entry position
                this.weightSliderWidget.setX(x + 53); // Adjust as needed
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