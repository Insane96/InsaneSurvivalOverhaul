package insane96mcp.iguanatweaksreborn.mixin.client;

import insane96mcp.iguanatweaksreborn.module.experience.anvils.Anvils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//Shamelessly stolen from Charm

@Mixin(value = AnvilScreen.class, priority = 1001)
public class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {

	@Final
	@Shadow
	private Player player;

	public AnvilScreenMixin(AnvilMenu anvilMenu, Inventory playerInventory, Component title, ResourceLocation menuResource) {
		super(anvilMenu, playerInventory, title, menuResource);
	}

	@Shadow
	protected void renderErrorIcon(GuiGraphics guiGraphics, int p_266822_, int p_267045_) {

	}

	//Set too expensive client side
	@ModifyConstant(
			method = "renderLabels",
			constant = @Constant(intValue = 40)
	)
	private int tooExpensiveCap(int cap) {
		return Anvils.anvilRepairCap;
	}

	//Show no cost text
	@Inject(
			method = "renderLabels",
			at = @At(
					value = "TAIL"
			)
	)
	public void renderLabels(GuiGraphics pGuiGraphics, int p_97891_, int p_97892_, CallbackInfo ci) {
		if (this.menu.getCost() == 0 && this.menu.getSlot(2).hasItem()) {
			int j = 8453920;
			Component component = Component.translatable("container.repair.free");
			if (!this.menu.getSlot(2).mayPickup(this.player)) {
				j = 16736352;
			}

			int k = this.imageWidth - 8 - this.font.width(component) - 2;
			int l = 69;
			pGuiGraphics.fill(k - 2, 67, this.imageWidth - 8, 79, 1325400064);
			pGuiGraphics.drawString(this.font, component, k, l, j);
		}
	}
}
