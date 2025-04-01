package net.spacerulerwill.skygrid_reloaded.ui.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class RenderUtils {
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

    private static void renderIconBackgroundTexture(DrawContext context, int x, int y) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, SLOT_TEXTURE, x, y, 0, 18, 18);
    }

    public static void renderItemIcon(Item item, DrawContext context, int x, int y) {
        ItemStack itemStack = item.getDefaultStack();
        renderIconBackgroundTexture(context, x + 1, y + 1);
        if (!itemStack.isEmpty()) {
            context.drawItemWithoutEntity(itemStack, x + 2, y + 2);
        }
    }
}
