package insane96mcp.iguanatweaksreborn.module.items.pouch;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ClientPouchTooltip implements ClientTooltipComponent {
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.parse("textures/gui/container/bundle.png");
    private final NonNullList<ItemStack> items;

    public ClientPouchTooltip(PouchTooltip pBundleTooltip) {
        this.items = pBundleTooltip.items();
    }

    public int getHeight() {
        return 3 * 18 + 4;
    }

    public int getWidth(Font pFont) {
        return 3 * 18 + 2;
    }

    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        int k = 0;

        for (int l = 0; l < 3; ++l) {
            for(int i1 = 0; i1 < 3; ++i1) {
                int j1 = pX + i1 * 18 + 1;
                int k1 = pY + l * 18 + 1;
                this.renderSlot(j1, k1, k++, pGuiGraphics, pFont);
            }
        }
    }

    private void renderSlot(int pX, int pY, int pItemIndex, GuiGraphics pGuiGraphics, Font pFont) {
        if (pItemIndex >= this.items.size()) {
            this.blit(pGuiGraphics, pX, pY);
        }
        else {
            ItemStack itemstack = this.items.get(pItemIndex);
            this.blit(pGuiGraphics, pX, pY);
            pGuiGraphics.renderItem(itemstack, pX + 1, pY + 1, pItemIndex);
            pGuiGraphics.renderItemDecorations(pFont, itemstack, pX + 1, pY + 1);
        }
    }

    private void blit(GuiGraphics pGuiGraphics, int pX, int pY) {
        pGuiGraphics.blit(TEXTURE_LOCATION, pX, pY, 0, 0, 0, 18, 18, 128, 128);
    }
}