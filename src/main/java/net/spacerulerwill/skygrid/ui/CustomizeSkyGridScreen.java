package net.spacerulerwill.skygrid.ui;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridScreen extends Screen {
    // UI Stuff
    protected final CreateWorldScreen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TabManager tabManager = new TabManager(this::onTabLoad, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    private ButtonWidget deleteButton;
    private ButtonWidget addButton;
    private CustomizeSkyGridTextFieldWidget textFieldWidget;
    private CustomizeSkyGridTab<?> currentTab;

    // Other Stuff
    private final Map<RegistryKey<DimensionOptions>, SkyGridChunkGeneratorConfig> dimensionChunkGeneratorConfigs;
    List<RegistryKey<DimensionOptions>> dimensions = new ArrayList<>();
    RegistryKey<DimensionOptions> currentDimension;

    public static final int DEFAULT_BLOCK_WEIGHT = 100;

    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(Text.translatable("createWorld.customize.skygrid.title"));
        this.parent = parent;
        // Add initial dimension configs (default values for vanilla dimensions)
        this.dimensionChunkGeneratorConfigs = new HashMap<>();
        this.dimensionChunkGeneratorConfigs.put(DimensionOptions.OVERWORLD, SkyGridChunkGeneratorConfig.getDefaultOverworldConfig());
        this.dimensionChunkGeneratorConfigs.put(DimensionOptions.NETHER, SkyGridChunkGeneratorConfig.getDefaultNetherConfig());
        this.dimensionChunkGeneratorConfigs. put(DimensionOptions.END, SkyGridChunkGeneratorConfig.getDefaultEndConfig());
        // Array of dimensions options registry keys
        this.currentDimension = DimensionOptions.OVERWORLD;
        this.dimensions.add(DimensionOptions.OVERWORLD);
        this.dimensions.add(DimensionOptions.NETHER);
        this.dimensions.add(DimensionOptions.END);
    }

    private void onTabLoad(ClickableWidget widget) {
        this.addDrawableChild(widget);
        this.currentTab = (CustomizeSkyGridTab<?>) tabManager.getCurrentTab();
        if (this.deleteButton != null)
            this.updateDeleteButtonActive();
        if (this.addButton != null)
            this.updateAddButtonActive();
    }

    protected void init() {
        // Build tab navigation
        CustomizeSkyGridBlockTab blockTab = new CustomizeSkyGridBlockTab(this.client, this);
        CustomizeMobSpawnerTab mobSpawnerTab = new CustomizeMobSpawnerTab(this.client, this);
        this.currentTab = blockTab;
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

        // Build button rows
        DirectionalLayoutWidget rows = layout.addFooter(DirectionalLayoutWidget.vertical().spacing(4));

        DirectionalLayoutWidget row1 = DirectionalLayoutWidget.horizontal().spacing(8);
        textFieldWidget = row1.add(new CustomizeSkyGridTextFieldWidget(textRenderer, 158, 20, Text.translatable("createWorld.customize.skygrid.enterBlock")));
        addButton = row1.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.add"), (button) -> {
            this.currentTab.addButtonCallback();
            this.textFieldWidget.setText("");
            this.updateAddButtonActive();
        }).width(75).build());

        DirectionalLayoutWidget row2 = DirectionalLayoutWidget.horizontal().spacing(8);
        row2.add(new CyclingButtonWidget.Builder<RegistryKey<DimensionOptions>>(value -> Text.translatable(value.getValue().toTranslationKey()))
                .values(dimensions)
                .build(0, 0, 158, 20, Text.translatable("createWorld.customize.skygrid.button.dimension"), ((button, value) -> {
                    this.currentDimension = value;
                    this.currentTab.onDimensionChange();
                    this.updateDeleteButtonActive();
                    this.updateAddButtonActive();
                })));
        deleteButton = row2.add(ButtonWidget.builder(Text.translatable("createWorld.customize.skygrid.button.delete"), (button) -> {
            this.currentTab.deleteButtonCallback();
            this.updateDeleteButtonActive();
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

        // Adjust footer height and make everything drawable
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
        this.currentTab.resize();

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

    public void updateDeleteButtonActive() {
        this.deleteButton.active = this.currentTab.shouldDeleteButtonBeActive();
    }

    public void updateAddButtonActive() {
        this.addButton.active = this.currentTab.shouldAddButtonBeActive();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        context.drawTexture(Screen.FOOTER_SEPARATOR_TEXTURE, 0, height - layout.getFooterHeight() - 2, 0.0F, 0.0F, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    public String getText() {
        return this.textFieldWidget.getText();
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
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


    public SkyGridChunkGeneratorConfig getCurrentConfig() {
        return dimensionChunkGeneratorConfigs.get(currentDimension);
    }

    @Environment(EnvType.CLIENT)
    private class CustomizeSkyGridTextFieldWidget extends TextFieldWidget {
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
}