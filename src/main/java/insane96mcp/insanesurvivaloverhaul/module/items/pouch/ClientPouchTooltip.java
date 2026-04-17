package insane96mcp.insanesurvivaloverhaul.module.items.pouch;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClientPouchTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/background");
    private static final int MARGIN_Y = 4;
    private static final int BORDER_WIDTH = 1;
    private static final int SLOT_SIZE_X = 18;
    private static final int SLOT_SIZE_Y = 20;
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

    private int backgroundWidth() {
        return this.gridSizeX() * SLOT_SIZE_X + 2;
    }

    private int backgroundHeight() {
        return this.gridSizeY() * SLOT_SIZE_Y + 2;
    }

    private int gridSizeX() {
        return 3;
    }

    private int gridSizeY() {
        return 3;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int i = 3;
        int j = 3;
        guiGraphics.blitSprite(BACKGROUND_SPRITE, x, y, this.backgroundWidth(), this.backgroundHeight());
        int k = 0;

        for (int l = 0; l < j; l++) {
            for (int i1 = 0; i1 < i; i1++) {
                int j1 = x + i1 * 18 + BORDER_WIDTH;
                int k1 = y + l * 20 + BORDER_WIDTH;
                this.renderSlot(j1, k1, k++, guiGraphics, font);
            }
        }
    }

    private void renderSlot(int x, int y, int itemIndex, GuiGraphics guiGraphics, Font font) {
        if (itemIndex >= this.items.size()) {
            this.blit(guiGraphics, x, y);
        } else {
            ItemStack itemstack = this.items.get(itemIndex);
            this.blit(guiGraphics, x, y);
            guiGraphics.renderItem(itemstack, x + BORDER_WIDTH, y + BORDER_WIDTH, itemIndex);
            guiGraphics.renderItemDecorations(font, itemstack, x + BORDER_WIDTH, y + BORDER_WIDTH);
        }
    }

    private void blit(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(Texture.SLOT.sprite, x, y, 0, ClientPouchTooltip.Texture.SLOT.w, ClientPouchTooltip.Texture.SLOT.h);
    }

    @OnlyIn(Dist.CLIENT)
    enum Texture {
        BLOCKED_SLOT(ResourceLocation.withDefaultNamespace("container/bundle/blocked_slot"), 18, 20),
        SLOT(ResourceLocation.withDefaultNamespace("container/bundle/slot"), 18, 20);

        public final ResourceLocation sprite;
        public final int w;
        public final int h;

        Texture(ResourceLocation sprite, int w, int h) {
            this.sprite = sprite;
            this.w = w;
            this.h = h;
        }
    }
}