package net.spacerulerwill.skygrid.worldgen;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class SkyGridPreset {
    public Item item;
    public String name;
    public SkyGridChunkGeneratorConfig overworldConfig;
    public SkyGridChunkGeneratorConfig netherConfig;
    public SkyGridChunkGeneratorConfig endConfig;

    public SkyGridPreset(Block block, String name, SkyGridChunkGeneratorConfig overworldConfig, SkyGridChunkGeneratorConfig netherConfig, SkyGridChunkGeneratorConfig endConfig) {
        this.item = block.asItem();
        this.name = name;
        this.overworldConfig = overworldConfig;
        this.netherConfig = netherConfig;
        this.endConfig = endConfig;
    }

    public SkyGridPreset(Item item, String name, SkyGridChunkGeneratorConfig overworldConfig, SkyGridChunkGeneratorConfig netherConfig, SkyGridChunkGeneratorConfig endConfig) {
        this.item = item;
        this.name = name;
        this.overworldConfig = overworldConfig;
        this.netherConfig = netherConfig;
        this.endConfig = endConfig;
    }
}