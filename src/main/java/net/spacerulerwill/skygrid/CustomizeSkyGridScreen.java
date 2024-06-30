package net.spacerulerwill.skygrid;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
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
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridScreen extends Screen {
    CreateWorldScreen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    private SkyGridChunkGeneratorConfig overworldConfig = SkyGridChunkGenerator.getDefaultOverworldConfig();
    private SkyGridChunkGeneratorConfig netherConfig = SkyGridChunkGenerator.getDefaultNetherConfig();
    private SkyGridChunkGeneratorConfig endConfig = SkyGridChunkGenerator.getDefaultEndConfig();

    public CustomizeSkyGridScreen(
            CreateWorldScreen parent
    ) {
        super(Text.translatable("createWorld.customize.skygrid.title"));
        this.parent = parent;
    }

    protected void init() {
        DirectionalLayoutWidget directionalLayoutWidget = (DirectionalLayoutWidget) layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        // Add the Cancel and Done buttons
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            applyConfigurations();
            this.close();
        }).build());
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.close();
        }).build());

        layout.forEachChild((child) -> {
            child.setNavigationOrder(1);
            addDrawableChild(child);
        });
        initTabNavigation();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        RenderSystem.enableBlend();
        context.drawTexture(Screen.FOOTER_SEPARATOR_TEXTURE, 0, height - layout.getFooterHeight() - 2, 0.0F, 0.0F, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    public void initTabNavigation() {
        tabNavigation = TabNavigationWidget.builder(tabManager, width)
                .tabs(new Tab[]{
                        new CustomizeSkyGridScreen.OverworldTab(),
                        new CustomizeSkyGridScreen.NetherTab(),
                        new CustomizeSkyGridScreen.EndTab()
                }).build();
        addDrawableChild(tabNavigation);
        if (tabNavigation != null) {
            tabNavigation.selectTab(0, false);
            tabNavigation.setWidth(width);
            tabNavigation.init();
            int i = tabNavigation.getNavigationFocus().getBottom();
            ScreenRect screenRect = new ScreenRect(0, i, width, height - layout.getFooterHeight() - i);
            tabManager.setTabArea(screenRect);
            layout.setHeaderHeight(i);
            layout.refreshPositions();
        }
    }

    public void close() {
        client.setScreen(parent);
    }

    // Method to apply the configurations to respective chunk generators
    private void applyConfigurations() {
        parent.getWorldCreator().applyModifier(createModifier(BiomeKeys.PLAINS, DimensionOptions.OVERWORLD, overworldConfig));
        parent.getWorldCreator().applyModifier(createModifier(BiomeKeys.NETHER_WASTES, DimensionOptions.NETHER, netherConfig));
        parent.getWorldCreator().applyModifier(createModifier(BiomeKeys.THE_END, DimensionOptions.END, endConfig));
    }

    private GeneratorOptionsHolder.RegistryAwareModifier createModifier(RegistryKey<Biome> biomeKey, RegistryKey<DimensionOptions> dimensionOptions, SkyGridChunkGeneratorConfig config) {
        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            // We must create an ENTIRELY NEW dimension options map to replace it because it is immutable... :(
            Registry<Biome> biomeRegistry = dynamicRegistryManager.get(RegistryKeys.BIOME);
            RegistryEntry<Biome> biomeEntry = biomeRegistry.entryOf(biomeKey);
            Registry<DimensionType> dimensionTypeRegistry = dynamicRegistryManager.get(RegistryKeys.DIMENSION_TYPE);

            // new chunk generator and new map
            ChunkGenerator chunkGenerator = new SkyGridChunkGenerator(new FixedBiomeSource(biomeEntry), config);
            Map<RegistryKey<DimensionOptions>, DimensionOptions> updatedDimensions = new HashMap<>(dimensionsRegistryHolder.dimensions());
            // Add dimensions
            if (dimensionOptions.equals(DimensionOptions.OVERWORLD)) {
                RegistryEntry<DimensionType> overworldDimensionTypeEntry = dimensionTypeRegistry.entryOf(DimensionTypes.OVERWORLD);
                DimensionOptions newDimensionOptions = new DimensionOptions(overworldDimensionTypeEntry, chunkGenerator);
                updatedDimensions.put(dimensionOptions, newDimensionOptions);
            } else if (dimensionOptions.equals(DimensionOptions.NETHER)) {
                RegistryEntry<DimensionType> netherDimensionTypeEntry = dimensionTypeRegistry.entryOf(DimensionTypes.THE_NETHER);
                DimensionOptions newDimensionOptions = new DimensionOptions(netherDimensionTypeEntry, chunkGenerator);
                updatedDimensions.put(dimensionOptions, newDimensionOptions);
            } else if (dimensionOptions.equals(DimensionOptions.END)) {
                RegistryEntry<DimensionType> endDimensionTypeEntry = dimensionTypeRegistry.entryOf(DimensionTypes.THE_END);
                DimensionOptions newDimensionOptions = new DimensionOptions(endDimensionTypeEntry, chunkGenerator);
                updatedDimensions.put(dimensionOptions, newDimensionOptions);
            }

            // Return as an immmutable map
            return new DimensionOptionsRegistryHolder(ImmutableMap.copyOf(updatedDimensions));
        };
    }

    @Environment(EnvType.CLIENT)
    private class OverworldTab extends GridScreenTab {
        public OverworldTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.overworld"));
        }
    }

    @Environment(EnvType.CLIENT)
    private class NetherTab extends GridScreenTab {
        public NetherTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.nether"));
        }
    }

    @Environment(EnvType.CLIENT)
    private class EndTab extends GridScreenTab {
        public EndTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.end"));
        }
    }
}
