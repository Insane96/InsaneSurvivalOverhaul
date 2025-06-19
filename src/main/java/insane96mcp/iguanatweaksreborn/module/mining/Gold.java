package insane96mcp.iguanatweaksreborn.module.mining;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = Modules.Ids.MINING, description = "Changes Gold tools to have an innate Fortune/Looting I")
public class Gold extends Feature {

	public static final String LUCKY_GOLD_TOOLTIP = InsaneSO.MOD_ID + ".innate_luck";

	@Config(min = 0, max = 255, description = "Set the innate looting level of gold tools.")
	public static Integer lootingLevel = 1;
	@Config(min = 0, max = 255, description = "Set the innate fortune level of gold tools.")
	public static Integer fortuneLevel = 1;

	public Gold(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
	}

	@SubscribeEvent
	public void onLootingCheck(LootingLevelEvent event) {
		if (!this.isEnabled()
				|| event.getDamageSource() == null
				|| !(event.getDamageSource().getEntity() instanceof LivingEntity livingEntity))
			return;

		ItemStack stack = livingEntity.getMainHandItem();
		if (stack.is(ISOItemTagsProvider.GOLDEN_HAND_EQUIPMENT)) {
			if (event.getLootingLevel() < lootingLevel)
				event.setLootingLevel(lootingLevel);
		}
	}

	public static int getFortuneLevel(ItemStack stack, int original) {
		if (!shouldApply(stack))
			return original;
		int luckLvl = EnchantmentHelper.getTagEnchantmentLevel(EnchantmentsFeature.LUCK.get(), stack);
		if (original >= luckLvl)
			return Math.max(original, fortuneLevel);

		return Math.max(luckLvl, fortuneLevel);
	}

	public static boolean shouldApply(ItemStack stack) {
		return isEnabled(Gold.class) && stack.is(ISOItemTagsProvider.GOLDEN_HAND_EQUIPMENT);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !event.getItemStack().is(ISOItemTagsProvider.GOLDEN_HAND_EQUIPMENT)
				|| event.getItemStack().getEnchantmentLevel(Enchantments.BLOCK_FORTUNE) > 0
				|| event.getItemStack().getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0
				|| event.getItemStack().getEnchantmentLevel(Enchantments.MOB_LOOTING) > 0)
			return;

		event.getToolTip().add(Component.empty());
		event.getToolTip().add(Component.translatable(LUCKY_GOLD_TOOLTIP).withStyle(ChatFormatting.GOLD));
	}
}
