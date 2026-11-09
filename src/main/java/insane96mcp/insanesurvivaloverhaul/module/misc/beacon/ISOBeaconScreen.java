package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ISOBeaconScreen extends AbstractContainerScreen<ISOBeaconMenu> {
    private static final ResourceLocation TEXTURE = InsaneSO.id("textures/gui/container/beacon.png");
    private static final int BUTTON_SPACING = 25;
    private static final int POWER_BUTTON_SIZE = 22;
    private static final int AMPLIFIER_BUTTON_SIZE = 13;
    private static final int POWER_BUTTONS_Y = 15;
    private static final int AMPLIFIER_BUTTONS_Y = 78;
    private static final int INFO_TEXT_Y = 106;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final Component REQUIRES_HIGHER_PYRAMID = Component.translatable("insanesurvivaloverhaul.beacon.requires_higher_pyramid");

    private final List<BeaconScreenButton> beaconButtons = new ArrayList<>();
    @Nullable
    private Holder<MobEffect> effect;
    private int amplifier;

    public ISOBeaconScreen(final ISOBeaconMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 230;
        this.imageHeight = 122;
        menu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu containerMenu, int slot, ItemStack stack) {
            }

            @Override
            public void dataChanged(AbstractContainerMenu containerMenu, int id, int value) {
                ISOBeaconScreen.this.effect = menu.getEffect();
                ISOBeaconScreen.this.amplifier = menu.getAmplifier();
            }
        });
    }

    private void addBeaconButton(BeaconScreenButton button) {
        this.addRenderableWidget(button);
        this.beaconButtons.add(button);
    }

    @Override
    protected void init() {
        super.init();
        this.beaconButtons.clear();

        for (int amplifier = 0; amplifier < ISOBeaconBlockEntity.MAX_LAYERS; amplifier++) {
            BeaconAmplifierButton button = new BeaconAmplifierButton(this.leftPos, this.topPos + AMPLIFIER_BUTTONS_Y, amplifier);
            button.visible = false;
            this.addBeaconButton(button);
        }

        List<BeaconEffect> effects = BeaconFeature.getAvailableEffects();
        int topRowCount = (effects.size() + 1) / 2;
        this.addPowerButtonsRow(effects.subList(0, topRowCount), this.topPos + POWER_BUTTONS_Y);
        this.addPowerButtonsRow(effects.subList(topRowCount, effects.size()), this.topPos + POWER_BUTTONS_Y + BUTTON_SPACING);
        this.updateButtons();
    }

    private void addPowerButtonsRow(List<BeaconEffect> effects, int y) {
        int startX = this.getRowStartX(effects.size(), POWER_BUTTON_SIZE);
        for (int i = 0; i < effects.size(); i++) {
            this.addBeaconButton(new BeaconPowerButton(startX + i * BUTTON_SPACING, y, effects.get(i)));
        }
    }

    private int getRowStartX(int buttonsCount, int buttonSize) {
        int rowWidth = (buttonsCount - 1) * BUTTON_SPACING + buttonSize;
        return this.leftPos + (this.imageWidth - rowWidth) / 2;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    private void updateButtons() {
        this.beaconButtons.forEach(BeaconScreenButton::updateStatus);

        List<BeaconAmplifierButton> visibleButtons = this.beaconButtons.stream()
                .filter(button -> button instanceof BeaconAmplifierButton && button.visible)
                .map(button -> (BeaconAmplifierButton) button)
                .toList();
        int startX = this.getRowStartX(visibleButtons.size(), AMPLIFIER_BUTTON_SIZE);
        for (int i = 0; i < visibleButtons.size(); i++) {
            visibleButtons.get(i).setX(startX + i * BUTTON_SPACING);
        }
    }

    @Nullable
    private BeaconEffect getSelectedBeaconEffect() {
        return this.effect == null ? null : BeaconFeature.getBeaconEffect(this.effect);
    }

    /**
     * L'amplifier che il beacon sta applicando, che può essere inferiore a quello salvato se la piramide si è ridotta.
     */
    private int getEffectiveAmplifier(BeaconEffect beaconEffect) {
        return beaconEffect.clampAmplifier(this.amplifier, this.menu.getLayers());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.getInfoText(), this.leftPos + this.imageWidth / 2, this.topPos + INFO_TEXT_Y, TEXT_COLOR);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private Component getInfoText() {
        if (this.effect == null)
            return Component.translatable("insanesurvivaloverhaul.beacon.select_an_effect");
        BeaconEffect beaconEffect = this.getSelectedBeaconEffect();
        if (beaconEffect == null || !beaconEffect.isAvailableAt(this.menu.getLayers()))
            return REQUIRES_HIGHER_PYRAMID;
        return Component.translatable("insanesurvivaloverhaul.beacon.effect_info", createEffectDescription(this.effect, this.getEffectiveAmplifier(beaconEffect)), this.menu.getRange());
    }

    private static MutableComponent createEffectName(Holder<MobEffect> effect) {
        return Component.translatable(effect.value().getDescriptionId());
    }

    private static MutableComponent createEffectDescription(Holder<MobEffect> effect, int amplifier) {
        return createEffectName(effect).append(" ").append(getAmplifierName(amplifier));
    }

    private static Component getAmplifierName(int amplifier) {
        return amplifier == 0
                ? Component.literal("I")
                : Component.translatable("potion.potency." + amplifier);
    }

    private class BeaconPowerButton extends BeaconScreenButton {
        private final BeaconEffect beaconEffect;
        private final Holder<MobEffect> effect;
        private final TextureAtlasSprite sprite;

        private BeaconPowerButton(int x, int y, BeaconEffect beaconEffect) {
            super(x, y, POWER_BUTTON_SIZE, 0);
            this.beaconEffect = beaconEffect;
            this.effect = beaconEffect.getEffect();
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(this.effect);
        }

        @Override
        public void onPress() {
            if (this.isSelected())
                return;
            ISOBeaconScreen.this.effect = this.effect;
            ISOBeaconScreen.this.amplifier = 0;
            ServerboundSetBeaconEffectPacket.send(this.effect, 0);
            ISOBeaconScreen.this.updateButtons();
        }

        @Override
        protected void renderIcon(GuiGraphics guiGraphics) {
            guiGraphics.blit(this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);
        }

        @Override
        void updateStatus() {
            this.active = this.beaconEffect.isAvailableAt(ISOBeaconScreen.this.menu.getLayers());
            this.setTooltip(Tooltip.create(this.active ? createEffectName(this.effect) : REQUIRES_HIGHER_PYRAMID));
            this.setSelected(this.effect.equals(ISOBeaconScreen.this.effect));
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return createEffectName(this.effect);
        }
    }

    private class BeaconAmplifierButton extends BeaconScreenButton {
        private final int amplifier;

        private BeaconAmplifierButton(int x, int y, int amplifier) {
            super(x, y, AMPLIFIER_BUTTON_SIZE, 88);
            this.amplifier = amplifier;
        }

        @Override
        public void onPress() {
            if (this.isSelected() || ISOBeaconScreen.this.effect == null)
                return;
            ISOBeaconScreen.this.amplifier = this.amplifier;
            ServerboundSetBeaconEffectPacket.send(ISOBeaconScreen.this.effect, this.amplifier);
            ISOBeaconScreen.this.updateButtons();
        }

        @Override
        protected void renderIcon(GuiGraphics guiGraphics) {
            guiGraphics.drawCenteredString(ISOBeaconScreen.this.font, getAmplifierName(this.amplifier), this.getX() + 7, this.getY() + 3, TEXT_COLOR);
        }

        @Override
        void updateStatus() {
            BeaconEffect beaconEffect = ISOBeaconScreen.this.getSelectedBeaconEffect();
            this.visible = beaconEffect != null && this.amplifier <= beaconEffect.getMaxAmplifier();
            if (beaconEffect == null)
                return;
            this.active = this.amplifier < ISOBeaconScreen.this.menu.getLayers();
            this.setSelected(this.amplifier == ISOBeaconScreen.this.getEffectiveAmplifier(beaconEffect));
            this.setTooltip(Tooltip.create(this.active ? createEffectDescription(ISOBeaconScreen.this.effect, this.amplifier) : REQUIRES_HIGHER_PYRAMID));
        }
    }

    private abstract static class BeaconScreenButton extends AbstractButton {
        private final int textureU;
        private boolean selected;

        private BeaconScreenButton(int x, int y, int size, int textureU) {
            super(x, y, size, size, CommonComponents.EMPTY);
            this.textureU = textureU;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int stateOffset = 0;
            if (!this.active)
                stateOffset = this.width * 2;
            else if (this.selected)
                stateOffset = this.width;
            else if (this.isHoveredOrFocused())
                stateOffset = this.width * 3;

            guiGraphics.blit(TEXTURE, this.getX(), this.getY(), stateOffset + this.textureU, 219, this.width, this.height);
            this.renderIcon(guiGraphics);
        }

        protected abstract void renderIcon(GuiGraphics guiGraphics);

        abstract void updateStatus();

        boolean isSelected() {
            return this.selected;
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}
