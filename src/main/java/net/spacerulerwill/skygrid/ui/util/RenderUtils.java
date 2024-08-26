package net.spacerulerwill.skygrid.ui.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class RenderUtils {
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

    private static ItemStack createItemStackFor(BlockState state) {
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

    private static void renderIconBackgroundTexture(DrawContext context, int x, int y) {
        context.drawGuiTexture(SLOT_TEXTURE, x, y, 0, 18, 18);
    }

    public static void renderBlockIcon(Block block, DrawContext context, int x, int y) {
        BlockState state = block.getDefaultState();
        ItemStack itemStack = createItemStackFor(state);
        renderIconBackgroundTexture(context, x + 1, y + 1);
        if (!itemStack.isEmpty()) {
            context.drawItemWithoutEntity(itemStack, x + 2, y + 2);
        }
    }
}
