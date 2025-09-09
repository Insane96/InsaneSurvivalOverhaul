package insane96mcp.iguanatweaksreborn.module.misc.beaconconduit;

import com.google.common.collect.Lists;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.network.message.SetITRBeaconEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ISOBeaconScreen extends AbstractContainerScreen<ISOBeaconMenu> {
    static final ResourceLocation BEACON_LOCATION = InsaneSO.location("textures/gui/container/beacon.png");
    public final List<BeaconButton> beaconButtons = Lists.newArrayList();
    @Nullable
    MobEffect effect;
    int maxAmplifier;
    int amplifier;

    public ISOBeaconScreen(final ISOBeaconMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 230;
        this.imageHeight = 219;
        pMenu.addSlotListener(new ContainerListener() {
            /**
             * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
             * contents of that slot.
             */
            public void slotChanged(AbstractContainerMenu abstractContainerMenu, int slot, ItemStack stack) {
            }

            public void dataChanged(AbstractContainerMenu abstractContainerMenu, int id, int value) {
                ISOBeaconScreen.this.effect = pMenu.getEffect();
                ISOBeaconScreen.this.updateMaxAmplifier();
                ISOBeaconScreen.this.amplifier = pMenu.getAmplifier();
            }
        });
    }

    private <T extends AbstractWidget & BeaconButton> void addBeaconButton(T pBeaconButton) {
        this.addRenderableWidget(pBeaconButton);
        this.beaconButtons.add(pBeaconButton);
    }

    @Override
    protected void init() {
        super.init();
        this.beaconButtons.clear();
        int topLeftCornerX = (this.width - this.imageWidth) / 2;
        int topLeftCornerY = (this.height - this.imageHeight) / 2;

        int amplifierCount = 8;
        int amplifierSpacing = 25;
        int amplifierButtonWidth = 13;
        int amplifierTotalWidth = (amplifierCount - 1) * amplifierSpacing + amplifierButtonWidth;
        int amplifierStartX = topLeftCornerX + (this.imageWidth - amplifierTotalWidth) / 2;

        for (int i = 0; i < amplifierCount; i++) {
            int x = amplifierStartX + i * amplifierSpacing;
            int y = topLeftCornerY + 78;
            BeaconAmplifierButton amplifierButton = new BeaconAmplifierButton(x, y, i);
            amplifierButton.active = true;
            amplifierButton.visible = false;
            this.addBeaconButton(amplifierButton);
        }

        int effectsCount = BeaconConduit.effects.size();
        if (effectsCount > 0) {
            int topRowCount = (effectsCount + 1) / 2;
            int bottomRowCount = effectsCount / 2;
            int spacing = 25;
            int buttonWidth = 22;

            int topTotalWidth = (topRowCount - 1) * spacing + buttonWidth;
            int topStartX = topLeftCornerX + (this.imageWidth - topTotalWidth) / 2;
            int topY = topLeftCornerY + 15;

            for (int i = 0; i < topRowCount; i++) {
                MobEffect effect = BeaconConduit.effects.get(i).getEffect();
                int heightRequired = BeaconConduit.effects.get(i).getHeightRequired();

                int x = topStartX + i * spacing;
                BeaconPowerButton button = new BeaconPowerButton(x, topY, effect, heightRequired);
                button.active = true;
                if (Objects.equals(this.effect, effect)) {
                    button.setSelected(true);
                }
                this.addBeaconButton(button);
            }

            if (bottomRowCount > 0) {
                int bottomTotalWidth = (bottomRowCount - 1) * spacing + buttonWidth;
                int bottomStartX = topLeftCornerX + (this.imageWidth - bottomTotalWidth) / 2;
                int bottomY = topLeftCornerY + 15 + spacing;

                for (int i = 0; i < bottomRowCount; i++) {
                    int effectIndex = topRowCount + i;
                    MobEffect effect = BeaconConduit.effects.get(effectIndex).getEffect();
                    int heightRequired = BeaconConduit.effects.get(effectIndex).getHeightRequired();

                    int x = bottomStartX + i * spacing;
                    BeaconPowerButton button = new BeaconPowerButton(x, bottomY, effect, heightRequired);
                    button.active = true;
                    if (Objects.equals(this.effect, effect)) {
                        button.setSelected(true);
                    }
                    this.addBeaconButton(button);
                }
            }
        }
    }

    private void updateMaxAmplifier() {
        this.maxAmplifier = this.menu.getMaxAmplifier(this.effect);
    }

    public void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    void updateAmplifierButtons() {
        List<BeaconAmplifierButton> visibleButtons = this.beaconButtons.stream()
                .filter(b -> b instanceof BeaconAmplifierButton)
                .map(b -> (BeaconAmplifierButton) b)
                .filter(bab -> bab.visible)
                .toList();

        if (visibleButtons.isEmpty())
            return;

        int spacing = 25;
        int buttonWidth = 13;
        int totalWidth = (visibleButtons.size() - 1) * spacing + buttonWidth;
        int startX = (this.width - this.imageWidth) / 2 + (this.imageWidth - totalWidth) / 2;
        int y = (this.height - this.imageHeight) / 2 + 78;

        for (int i = 0; i < visibleButtons.size(); i++) {
            BeaconAmplifierButton btn = visibleButtons.get(i);
            btn.setX(startX + i * spacing);
            btn.setY(y);
        }
    }


    void updateButtons() {
        this.beaconButtons.forEach(BeaconButton::updateStatus);
        updateAmplifierButtons();
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        /*pGuiGraphics.drawCenteredString(this.font, EFFECT_LABEL, 62, 10, 14737632);
        pGuiGraphics.drawCenteredString(this.font, AMPLIFIER_LABEL, 169, 10, 14737632);*/
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(BEACON_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int topLeftCornerX = (this.width - this.imageWidth) / 2;
        int topLeftCornerY = (this.height - this.imageHeight) / 2;
        if (BeaconConduit.beacon$requiresPayment)
            pGuiGraphics.drawCenteredString(minecraft.font, Component.literal(StringUtil.formatTickDuration(this.menu.getTimeLeft() / BeaconConduit.getEffectTimeScale(this.effect, this.amplifier))), topLeftCornerX + 130, topLeftCornerY + 114, 16777215);
        else if (this.effect == null)
            pGuiGraphics.drawCenteredString(minecraft.font, Component.translatable("iguanatweaksreborn.beacon.select_an_effect"), topLeftCornerX + 130, topLeftCornerY + 114, 16777215);
        else
            pGuiGraphics.drawCenteredString(minecraft.font, Component.translatable("iguanatweaksreborn.beacon.effect_info", createEffectDescription(this.effect, this.amplifier), this.menu.getRange()), topLeftCornerX + 130, topLeftCornerY + 114, 16777215);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    protected static MutableComponent createEffectDescription(MobEffect effect, int amplifier) {
        MutableComponent component = Component.translatable(effect.getDescriptionId()).append(" ").append(getEffectAmplifier(amplifier));
        if (BeaconConduit.beacon$requiresPayment)
            component.append(Component.translatable("iguanatweaksreborn.beacon.time_cost", BeaconConduit.getEffectTimeScale(effect, amplifier)));
        return component;
    }

    private static Component getEffectAmplifier(int amplifier) {
        return amplifier == 0
                ? Component.literal("I")
                : Component.translatable("potion.potency." + amplifier);
    }

    public interface BeaconButton {
        void updateStatus();
    }

    public class BeaconPowerButton extends BeaconScreenButton {
        public MobEffect effect;
        public int heightRequired;
        private TextureAtlasSprite sprite;

        public BeaconPowerButton(int pX, int pY, MobEffect pEffect, int heightRequired) {
            super(pX, pY);
            this.setEffect(pEffect);
            this.heightRequired = heightRequired;
        }

        protected void setEffect(MobEffect pEffect) {
            this.effect = pEffect;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(pEffect);
            this.setTooltip(Tooltip.create(this.createEffectDescription(pEffect), null));
        }

        protected MutableComponent createEffectDescription(MobEffect pEffect) {
            return Component.translatable(pEffect.getDescriptionId());
        }

        public void onPress() {
            if (!this.isSelected()) {
                ISOBeaconScreen.this.effect = this.effect;
                ISOBeaconScreen.this.amplifier = 0;
                ISOBeaconScreen.this.updateMaxAmplifier();
                SetITRBeaconEffects.updateServer(minecraft.player, ISOBeaconScreen.this.effect, amplifier);
                ISOBeaconScreen.this.updateButtons();
            }
        }

        protected void renderIcon(GuiGraphics pGuiGraphics) {
            pGuiGraphics.blit(this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);
        }

        public void updateStatus() {
            this.active = this.heightRequired <= ISOBeaconScreen.this.menu.getLayers();
            if (this.active)
                this.setTooltip(Tooltip.create(this.createEffectDescription(this.effect), null));
            else
                this.setTooltip(Tooltip.create(Component.translatable("iguanatweaksreborn.beacon.requires_higher_pyramid"), null));
            this.setSelected(this.effect == ISOBeaconScreen.this.effect);
        }

        protected MutableComponent createNarrationMessage() {
            return this.createEffectDescription(this.effect);
        }
    }

    public class BeaconAmplifierButton extends BeaconScreenButton {
        public int amplifier;

        public BeaconAmplifierButton(int pX, int pY, int amplifier) {
            super(pX, pY, 13, 13, 88);
            this.setAmplifier(amplifier);
        }

        protected void setAmplifier(int amplifier) {
            this.amplifier = amplifier;
        }

        public void onPress() {
            if (!this.isSelected()) {
                ISOBeaconScreen.this.amplifier = this.amplifier;
                SetITRBeaconEffects.updateServer(minecraft.player, ISOBeaconScreen.this.effect, this.amplifier);
                ISOBeaconScreen.this.updateButtons();
            }
        }

        public void updateStatus() {
            this.visible = this.amplifier <= ISOBeaconScreen.this.maxAmplifier && ISOBeaconScreen.this.effect != null;
            this.active = this.amplifier + 1 <= ISOBeaconScreen.this.menu.getLayers();
            this.setSelected(this.amplifier == ISOBeaconScreen.this.amplifier);
            if (ISOBeaconScreen.this.effect != null) {
                if (this.active)
                    this.setTooltip(Tooltip.create(createEffectDescription(ISOBeaconScreen.this.effect, this.amplifier), null));
                else
                    this.setTooltip(Tooltip.create(Component.translatable("iguanatweaksreborn.beacon.requires_higher_pyramid"), null));
            }
        }

        @Override
        protected void renderIcon(GuiGraphics pGuiGraphics) {
            pGuiGraphics.drawCenteredString(minecraft.font, getEffectAmplifier(this.amplifier), this.getX() + 7, this.getY() + 3, 16777215);
        }
    }

    abstract static class BeaconScreenButton extends AbstractButton implements BeaconButton {
        private boolean selected;
        int u, v;

        protected BeaconScreenButton(int pX, int pY) {
            this(pX, pY, 22, 22, 0);
        }

        protected BeaconScreenButton(int pX, int pY, int width, int height, int u) {
            super(pX, pY, width, height, CommonComponents.EMPTY);
            this.u = u;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            int i = 219;
            int j = 0;
            if (!this.active) {
                j += this.width * 2;
            } else if (this.selected) {
                j += this.width;
            } else if (this.isHoveredOrFocused()) {
                j += this.width * 3;
            }

            pGuiGraphics.blit(BEACON_LOCATION, this.getX(), this.getY(), j + u, 219, this.width, this.height);
            this.renderIcon(pGuiGraphics);
        }

        protected abstract void renderIcon(GuiGraphics pGuiGraphics);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean pSelected) {
            this.selected = pSelected;
        }

        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }
    }
}