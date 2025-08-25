package insane96mcp.iguanatweaksreborn.module.items.copper;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.item.ISOArmorMaterial;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.item.ILItemTier;
import insane96mcp.shieldsplus.world.item.SPShieldItem;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Basically backports copper equipment from latest MC versions.")
public class CopperEquipment extends Feature {
	public static final ILItemTier ITEM_TIER = new ILItemTier(1, 191, 5f, 1f, 12, () -> Ingredient.of(Items.COPPER_INGOT));

	public static final RegistryObject<Item> SWORD = ISORegistries.ITEMS.register("copper_sword", () -> new SwordItem(ITEM_TIER, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> SHOVEL = ISORegistries.ITEMS.register("copper_shovel", () -> new ShovelItem(ITEM_TIER, 1.5F, -3.0F, new Item.Properties()));
	public static final RegistryObject<Item> PICKAXE = ISORegistries.ITEMS.register("copper_pickaxe", () -> new PickaxeItem(ITEM_TIER, 1, -2.8F, new Item.Properties()));
	public static final RegistryObject<Item> AXE = ISORegistries.ITEMS.register("copper_axe", () -> new AxeItem(ITEM_TIER, 7.0F, -3.1F, new Item.Properties()));
	public static final RegistryObject<Item> HOE = ISORegistries.ITEMS.register("copper_hoe", () -> new HoeItem(ITEM_TIER, -1, -2.0F, new Item.Properties()));

	private static final ISOArmorMaterial ARMOR_MATERIAL = new ISOArmorMaterial(InsaneSO.location("copper"), 11, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266652_) -> {
		p_266652_.put(ArmorItem.Type.BOOTS, 1);
		p_266652_.put(ArmorItem.Type.LEGGINGS, 3);
		p_266652_.put(ArmorItem.Type.CHESTPLATE, 4);
		p_266652_.put(ArmorItem.Type.HELMET, 2);
	}), 7, SoundEvents.ARMOR_EQUIP_CHAIN, 0f, 0f, () -> Ingredient.of(Items.COPPER_INGOT));

	public static final RegistryObject<Item> BOOTS = ISORegistries.ITEMS.register("copper_boots", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties()));
	public static final RegistryObject<Item> LEGGINGS = ISORegistries.ITEMS.register("copper_leggings", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties()));
	public static final RegistryObject<Item> CHESTPLATE = ISORegistries.ITEMS.register("copper_chestplate", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
	public static final RegistryObject<Item> HELMET = ISORegistries.ITEMS.register("copper_helmet", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties()));

	public CopperEquipment(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("copper_equipment", "Insane's Survival Extra Copper Expansion", () -> this.isEnabled() && !Packs.disableAllDataPacks);
	}

	/*@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| !event.getEntity().getMainHandItem().is(COPPER_TOOLS_EQUIPMENT)
				|| !event.getEntity().getMainHandItem().isCorrectToolForDrops(event.getState()))
			return;

		Level level = event.getEntity().level();
		if (!level.dimension().equals(Level.OVERWORLD))
			return;

		int y = getNormalizedY(event.getEntity().getBlockY(), level);
		if (y < 0)
			return;
		event.setNewSpeed((event.getNewSpeed() + getBonusMiningSpeed(y)));
	}

	public static float getBonusMiningSpeed(int normalizedY) {
		return (float) (0.24f * Math.pow(normalizedY, 0.655f));
	}

	@SubscribeEvent
	public void onHurtItemStack(HurtItemStackEvent event) {
		if (!this.isEnabled()
				|| (!event.getStack().is(COPPER_UNBREAKING_BONUS))
				|| event.getPlayer() == null)
			return;

		Level level = event.getEntity().level();
		if (!level.dimension().equals(Level.OVERWORLD))
			return;

		int amount = event.getAmount();
		int newAmount = 0;
		int y = getNormalizedY(event.getEntity().getBlockY(), level);
		if (y < 0)
			return;
		double chance = 1 - 1 / (1 + getBonusUnbreakability(y));
		for (int i = 0; i < amount; i++) {
			if (event.getRandom().nextFloat() >= chance)
				++newAmount;
		}
		event.setAmount(newAmount);
	}

	public static float getBonusUnbreakability(int normalizedY) {
		return (float) (0.37f * Math.pow(normalizedY, 0.67f));
	}

	public static int getNormalizedY(int y, Level level) {
		int startingY = level.getSeaLevel() + deepBonusSeaLevelRelative;
		if (y > startingY)
			return -1;
		//Normalize y to go from 0 at sea level + deepBonusSeaLevelRelative
		return (y - startingY) * -1;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| event.getEntity() == null)
			return;

		if (event.getItemStack().is(COPPER_TOOLS_EQUIPMENT)) {
			float bonus = getBonusMiningSpeed(getNormalizedY(event.getEntity().getBlockY(), event.getEntity().level()));
			if (bonus > 0)
				event.getToolTip().add(Component.translatable("iguanatweaksexpanded.copper.depth_mining_speed", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(bonus)).withStyle(Style.EMPTY.withColor(0xD1890D)));
		}
		if (event.getItemStack().is(COPPER_UNBREAKING_BONUS)) {
			float bonus = Math.round(getBonusUnbreakability(getNormalizedY(event.getEntity().getBlockY(), event.getEntity().level())) * 10f) / 10f;
			if (bonus > 0)
				event.getToolTip().add(Component.translatable("iguanatweaksexpanded.copper.depth_unbreaking", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(1f + bonus)).withStyle(Style.EMPTY.withColor(0xD1890D)));
		}
	}*/

	public static class ShieldsPlusIntegration {
		public static final RegistryObject<SPShieldItem> SHIELD = CopperShield.registerShield("copper_shield");

		public static void init() {

		}
	}
}