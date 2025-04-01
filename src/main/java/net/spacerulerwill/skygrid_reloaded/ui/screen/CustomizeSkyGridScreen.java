package net.spacerulerwill.skygrid_reloaded.ui.screen;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
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
import net.spacerulerwill.skygrid_reloaded.SkyGridReloaded;
import net.spacerulerwill.skygrid_reloaded.ui.widget.ClickableWidgetList;
import net.spacerulerwill.skygrid_reloaded.util.WorldPresetExtension;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGeneratorConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizeSkyGridScreen extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("createWorld.customize.skygrid.title");
    private static final Text BLOCKS_TEXT = Text.translatable("createWorld.customize.skygrid.blocks");
    private static final Text SPAWNERS_TEXT = Text.translatable("createWorld.customize.skygrid.spawners");
    private static final Text LOOT_TEXT = Text.translatable("createWorld.customize.skygrid.loot");
    private static final Text PRESETS_TEXT = Text.translatable("createWorld.customize.skygrid.presets");


    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final CreateWorldScreen parent;
    private ClickableWidgetList body;
    private SkyGridConfig currentConfig = new SkyGridConfig(SkyGridReloaded.DEFAULT_PRESET.config());


    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(TITLE_TEXT);
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Header for title
        this.layout.addHeader(TITLE_TEXT, this.textRenderer);
        // Body
        List<ClickableWidget> firstRow = List.of(
                ButtonWidget.builder(BLOCKS_TEXT, (button) -> {
                    if (this.client != null) {
                        this.client.setScreen(new CustomizeBlocksScreen(this, this.currentConfig));
                    }
                }).build(),
                ButtonWidget.builder(SPAWNERS_TEXT, (button) -> {
                    if (this.client != null) {
                        this.client.setScreen(new CustomizeSpawnerScreen(this, this.currentConfig));
                    }
                }).build()
        );
        List<ClickableWidget> secondRow = List.of(
                ButtonWidget.builder(LOOT_TEXT, (button) -> {
                    if (this.client != null) {
                        this.client.setScreen(new CustomizeLootScreen(this, this.currentConfig));
                    }
                }).build(),
                ButtonWidget.builder(PRESETS_TEXT, (button) -> {
                    if (this.client != null) {
                        this.client.setScreen(new SkyGridPresetsScreen(this.client, this));
                    }
                }).build()
        );
        List<List<ClickableWidget>> rows = List.of(firstRow, secondRow);
        this.body = this.layout.addBody(new ClickableWidgetList(this.client, rows, this.width, this.layout.getContentHeight(), this.layout.getHeaderHeight()));
        // Footer
        DirectionalLayoutWidget footerRow = DirectionalLayoutWidget.horizontal().spacing(8);
        footerRow.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.done();
            this.close();
        }).build());
        footerRow.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.done();
            this.close();
        }).build());
        this.layout.addFooter(footerRow);
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }

    public void updateSkyGridConfig(SkyGridConfig config) {
        this.currentConfig = config;
    }

    public SkyGridConfig getCurrentSkyGridConfig() {
        return this.currentConfig;
    }

    private void done() {
        this.parent.getWorldCreator().applyModifier(applyChunkCGeneratorConfigs());
    }

    public void setConfigFromPreset(SkyGridPreset preset) {
        this.currentConfig = new SkyGridConfig(preset.config());
    }

    private GeneratorOptionsHolder.RegistryAwareModifier applyChunkCGeneratorConfigs() {
        Map<RegistryKey<DimensionOptions>, SkyGridChunkGeneratorConfig> dimensionOptionsToChunkGeneratorConfigMap = Map.of(
                DimensionOptions.OVERWORLD, currentConfig.overworldConfig(),
                DimensionOptions.NETHER, currentConfig.netherConfig(),
                DimensionOptions.END, currentConfig.endConfig()
        );

        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            Registry<Biome> biomeRegistry = dynamicRegistryManager.getOrThrow(RegistryKeys.BIOME);
            Biome biome = biomeRegistry.get(BiomeKeys.THE_VOID);
            RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);
            Map<RegistryKey<DimensionOptions>, DimensionOptions> updatedDimensions = new HashMap<>(dimensionsRegistryHolder.dimensions());
            dimensionOptionsToChunkGeneratorConfigMap.forEach((dimensionOptionsRegistryKey, config) -> {
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
                        DimensionOptions defaultOverworld = (dynamicRegistryManager.getOrThrow(RegistryKeys.WORLD_PRESET).get(WorldPresets.DEFAULT)).getOverworld().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultOverworld);
                    } else if (dimensionOptionsRegistryKey == DimensionOptions.NETHER) {
                        WorldPreset preset = (dynamicRegistryManager.getOrThrow(RegistryKeys.WORLD_PRESET).get(WorldPresets.DEFAULT));
                        DimensionOptions defaultNether = ((WorldPresetExtension) preset).skygrid$GetNether().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultNether);
                    } else if (dimensionOptionsRegistryKey == DimensionOptions.END) {
                        WorldPreset preset = (dynamicRegistryManager.getOrThrow(RegistryKeys.WORLD_PRESET).get(WorldPresets.DEFAULT));
                        DimensionOptions defaultEnd = ((WorldPresetExtension) preset).skygrid$GetEnd().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultEnd);
                    }

                }
            });
            return new DimensionOptionsRegistryHolder(ImmutableMap.copyOf(updatedDimensions));
        };
    }

    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
