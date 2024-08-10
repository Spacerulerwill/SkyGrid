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
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class CustomizeSkyGridScreen extends Screen {
    // UI stuff
    CreateWorldScreen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    // Other stuff
    private final Map<RegistryKey<DimensionOptions>, SkyGridChunkGeneratorConfig> dimensionChunkGeneratorConfigs;

    public CustomizeSkyGridScreen(
            CreateWorldScreen parent
    ) {
        super(Text.translatable("createWorld.customize.skygrid.title"));
        this.parent = parent;
        dimensionChunkGeneratorConfigs = new HashMap<>();
        dimensionChunkGeneratorConfigs.put(DimensionOptions.OVERWORLD, SkyGridChunkGenerator.getDefaultOverworldConfig());
        dimensionChunkGeneratorConfigs.put(DimensionOptions.NETHER, SkyGridChunkGenerator.getDefaultNetherConfig());
        dimensionChunkGeneratorConfigs.put(DimensionOptions.END, SkyGridChunkGenerator.getDefaultEndConfig());
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
                        new BlockTab(),
                        new MobSpawnerTab(),
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
        dimensionChunkGeneratorConfigs.forEach((dimensionOptionsRegistryKey, config) -> {
            parent.getWorldCreator().applyModifier(createModifier(dimensionOptionsRegistryKey, config));
        });
    }

    private GeneratorOptionsHolder.RegistryAwareModifier createModifier(RegistryKey<DimensionOptions> dimensionOptionsRegistryKey, SkyGridChunkGeneratorConfig config) {
        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            // We must create an ENTIRELY NEW dimension options map to replace it because it is immutable... :(
            // Get our registries from the dynamic registry manager
            Registry<Biome> biomeRegistry = dynamicRegistryManager.get(RegistryKeys.BIOME);
            RegistryEntry<Biome> biomeEntry = biomeRegistry.entryOf(BiomeKeys.THE_VOID);

            // new chunk generator and new map (copy of previous)
            ChunkGenerator chunkGenerator = new SkyGridChunkGenerator(new FixedBiomeSource(biomeEntry), config);
            Map<RegistryKey<DimensionOptions>, DimensionOptions> updatedDimensions = new HashMap<>(dimensionsRegistryHolder.dimensions());

            // Get the dimension type from the dimension option registry and create new dimension options and update in the map
            DimensionOptions dimensionOptions = parent.getWorldCreator().getGeneratorOptionsHolder().selectedDimensions().dimensions().get(dimensionOptionsRegistryKey);
            RegistryEntry<DimensionType> dimensionTypeRegistryEntry = dimensionOptions.dimensionTypeEntry();
            DimensionOptions newDimensionOptions = new DimensionOptions(dimensionTypeRegistryEntry, chunkGenerator);
            updatedDimensions.put(dimensionOptionsRegistryKey, newDimensionOptions);

            // Return as an immutable map
            return new DimensionOptionsRegistryHolder(ImmutableMap.copyOf(updatedDimensions));
        };
    }

    @Environment(EnvType.CLIENT)
    private class BlockTab extends GridScreenTab {
        public BlockTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.block"));
        }
    }

    @Environment(EnvType.CLIENT)
    private class MobSpawnerTab extends GridScreenTab {
        public MobSpawnerTab() {
            super(Text.translatable("createWorld.customize.skygrid.tab.mob_spawner"));
        }
    }
}
