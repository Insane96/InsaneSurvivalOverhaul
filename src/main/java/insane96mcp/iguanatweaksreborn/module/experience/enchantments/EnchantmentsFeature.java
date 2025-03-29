package insane96mcp.iguanatweaksreborn.module.experience.enchantments;

import com.google.common.collect.Lists;
import com.teamabnormals.allurement.core.AllurementConfig;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.event.EnchantmentBonusEfficiencyEvent;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.event.StackMaxDamageEvent;
import insane96mcp.iguanatweaksreborn.integration.ToolBeltIntegration;
import insane96mcp.iguanatweaksreborn.mixin.EnchantmentAccessor;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.FireAspect;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.IAttributeEnchantment;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.Knockback;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.Luck;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.BaneOfSSSS;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.BonusDamageEnchantment;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.Sharpness;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.Smite;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.protection.*;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.integration.Allurement;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinition;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinitionsReloadListener;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.world.item.DurabilityModifier;
import insane96mcp.insanelib.base.*;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.MathHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Label(name = "Enchantments", description = "Changes to some enchantments related stuff.")
@LoadFeature(module = Modules.Ids.EXPERIENCE)
public class EnchantmentsFeature extends JsonFeature {
	static final EnchantmentCategory ACTUALLY_WEARABLE = EnchantmentCategory.create("actually_wearable", item -> (item instanceof Equipable equipable && equipable.getEquipmentSlot().isArmor()) || Block.byItem(item) instanceof Equipable);

	public static final RegistryObject<Enchantment> SHARPNESS = ISORegistries.ENCHANTMENTS.register("sharpness", Sharpness::new);
	public static final RegistryObject<Enchantment> SMITE = ISORegistries.ENCHANTMENTS.register("smite", Smite::new);
	public static final RegistryObject<Enchantment> BANE_OF_SSSSS = ISORegistries.ENCHANTMENTS.register("bane_of_sssss", BaneOfSSSS::new);
	public static final RegistryObject<Enchantment> FIRE_ASPECT = ISORegistries.ENCHANTMENTS.register("fire_aspect", FireAspect::new);
	public static final RegistryObject<Enchantment> KNOCKBACK = ISORegistries.ENCHANTMENTS.register("knockback", Knockback::new);
	public static final RegistryObject<Enchantment> LUCK = ISORegistries.ENCHANTMENTS.register("luck", Luck::new);
	public static final RegistryObject<Enchantment> PROTECTION = ISORegistries.ENCHANTMENTS.register("protection", OverallProtection::new);
	public static final RegistryObject<Enchantment> BLAST_PROTECTION = ISORegistries.ENCHANTMENTS.register("blast_protection", BlastProtection::new);
	public static final RegistryObject<Enchantment> FIRE_PROTECTION = ISORegistries.ENCHANTMENTS.register("fire_protection", FireProtection::new);
	public static final RegistryObject<Enchantment> PROJECTILE_PROTECTION = ISORegistries.ENCHANTMENTS.register("projectile_protection", ProjectileProtection::new);
	public static final RegistryObject<Enchantment> FEATHER_FALLING = ISORegistries.ENCHANTMENTS.register("feather_falling", FeatherFalling::new);
	@Config
	@Label(description = "Infinity can go up to level 4. Each level makes an arrow have only 1 in level+1 chance to consume. E.g. with Infinity 4 there's 1 in 5 chance to consume the arrow, and 4 in 5 to not consume it.")
	public static Boolean infinityOverhaul = true;
	@Config
	@Label(description = "Unbreaking increases tool durability by 75% per level")
	public static Boolean unbreakingOverhaul = true;
	@Config
	@Label(description = "Changes Multishot to actually load 3 arrows, instead of materializing 2 arrows from thin air")
	public static Boolean actualMultishot = true;
	@Config
	@Label(description = "Thorns is no longer compatible with other protections, but deals damage every time (higher levels deal more damage) and no longer damages items.")
	public static Boolean thornsOverhaul = true;

	@Config
	@Label(name = "Tool Mining Speed Scaled Efficiency Formula", description = "Change the bonus efficiency formula from `lvl*lvl+1` to `tool_mining_speed * (0.5*lvl)`")
	public static Boolean changeEfficiencyFormula = true;
	@Config
	@Label(name = "Nerf Mending", description = "Mending only makes the tool repair by one durability every 2 xp instead of 2 durability/1 xp.")
	public static Boolean nerfMending = true;
	@Config
	@Label(name = "Nerf Respiration", description = "Respiration decreases air consumption by 50% per level instead of 100%.")
	public static Boolean nerfRespiration = true;

	@Config(min = 0d, max = 2d)
	@Label(name = "Power Enchantment Damage", description = "Set arrow's damage increase with the Power enchantment (vanilla is 0.5). If set to a value != 0.5 the flat 0.5 bonus is also removed. Set to 0.5 to disable.")
	public static Double powerEnchantmentDamage = 0.2d;

	@Config
	@Label(name = "Power affects base arrow damage", description = "If true, the formula for bonus damage for arrows is changed from `'Power Enchantment Damage' + 'Power Enchantment Damage' * lvl` to `base_damage * 'Power Enchantment Damage' * lvl`.")
	public static Boolean powerAffectsBaseArrowDamage = true;

	@Config
	@Label(name = "Prevent farmland trampling with Feather Falling")
	public static Boolean preventFarmlandTramplingWithFeatherFalling = true;
	@Config
	@Label(name = "Replace protection enchantments", description = "If true, vanilla protection enchantments are replaced with mod's ones. To re-enable vanilla enchantments refer to `disabled_enchantments.json`.\n" +
			"Protection has only one level, protects 6% per level and is treasure. Other protections work the same except for projectile that reduces the sight range of mobs by 2% per level. Feather falling protects for 16% per level instead of 12% + 1 per level.")
	public static Boolean replaceProtectionEnchantments = true;
	@Config
	@Label(name = "Replace damaging enchantments", description = """
            If true, vanilla damaging enchantments (such as smite or sharpness) are replaced with mod's ones. To re-enable vanilla enchantments refer to `disabled_enchantments.json`.
            Changes to damaging enchantments:
            Enchantments deal bonus damage based off the item's attack damage. So Sharpness on a Sword adds less damage than Sharpness on an Axe.
            Sharpness deals +0.5 damage per level
            Smite deals +1 damage per level to undead and applies weakness
            Bane of Arthropods has been replaced with Bane of SSSSS that deals +1 damage per level to arthropods and creepers and applies slowness""")
	public static Boolean replaceDamagingEnchantments = true;
	@Config
	@Label(description = "If true, Looting, Fortune and Luck of the Sea enchantments are replaced with a single level one: Luck. Luck works like Fortune/Looting/LoTS II. To re-enable vanilla enchantments refer to `disabled_enchantments.json`. Requires a Minecraft restart")
	public static Boolean reworkBonusLootEnchantments = true;
	@Config
	@Label(name = "Replace other enchantments", description = "If true, vanilla fire aspect and knockback are replaced with mod's ones. To re-enable vanilla enchantments refer to `disabled_enchantments.json`.")
	public static Boolean replaceOtherEnchantments = true;

	public static final ArrayList<IdTagMatcher> DISABLED_ENCHANTMENTS_DEFAULT = new ArrayList<>(List.of(
			IdTagMatcher.newId("minecraft:sharpness"),
			IdTagMatcher.newId("minecraft:smite"),
			IdTagMatcher.newId("minecraft:bane_of_arthropods"),
			IdTagMatcher.newId("minecraft:fire_aspect"),
			IdTagMatcher.newId("minecraft:knockback"),
			IdTagMatcher.newId("minecraft:protection"),
			IdTagMatcher.newId("minecraft:blast_protection"),
			IdTagMatcher.newId("minecraft:projectile_protection"),
			IdTagMatcher.newId("minecraft:fire_protection"),
			IdTagMatcher.newId("minecraft:feather_falling"),
			IdTagMatcher.newId("minecraft:looting"),
			IdTagMatcher.newId("minecraft:fortune"),
			IdTagMatcher.newId("minecraft:luck_of_the_sea"),
			IdTagMatcher.newId("minecraft:sweeping"),
			IdTagMatcher.newId("farmersdelight:backstabbing")

	));
	public static final ArrayList<IdTagMatcher> disabledEnchantments = new ArrayList<>();

	public EnchantmentsFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		addSyncType(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "disabled_enchantments"), new SyncType(json -> loadAndReadJson(json, disabledEnchantments, DISABLED_ENCHANTMENTS_DEFAULT, IdTagMatcher.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("disabled_enchantments.json", disabledEnchantments, DISABLED_ENCHANTMENTS_DEFAULT, IdTagMatcher.LIST_TYPE, true, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "disabled_enchantments")));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		if (infinityOverhaul) {
			Enchantments.INFINITY_ARROWS.rarity = Enchantment.Rarity.RARE;
			if (ModList.get().isLoaded("allurement"))
				AllurementConfig.COMMON.infinityRequiresArrows.set(true);
		}
		else
			Enchantments.INFINITY_ARROWS.rarity = Enchantment.Rarity.VERY_RARE;

		if (this.isEnabled() && thornsOverhaul)
			Enchantments.THORNS.rarity = Enchantment.Rarity.RARE;
		else
			Enchantments.THORNS.rarity = Enchantment.Rarity.VERY_RARE;

		//Can this make something blow up?
		if (isBonusLootEnchantmentReworkEnabled())
			Enchantments.BLOCK_FORTUNE = LUCK.get();

		if (this.isEnabled())
			((EnchantmentAccessor) Enchantments.BINDING_CURSE).setCategory(ACTUALLY_WEARABLE);
	}

	@SubscribeEvent
	public void onAttributeModifiers(ItemAttributeModifierEvent event) {
		event.getItemStack().getAllEnchantments().forEach((enchantment, lvl) -> {
			if (event.getItemStack().getItem() instanceof ArmorItem armorItem
					&& armorItem.getEquipmentSlot() != event.getSlotType())
				return;
			if (enchantment instanceof IAttributeEnchantment attributeEnchantment)
				attributeEnchantment.applyAttributeModifier(event, lvl);
		});
	}

	public static boolean isEnchantmentDisabled(Enchantment enchantment) {
		if (!Feature.isEnabled(EnchantmentsFeature.class))
			return false;

		for (IdTagMatcher idTagMatcher : disabledEnchantments) {
			if (idTagMatcher.matchesEnchantment(enchantment))
				return true;
		}
		return false;
	}

	public static boolean isUnbreakingOverhaul() {
		return Feature.isEnabled(EnchantmentsFeature.class) && unbreakingOverhaul;
	}

	@SubscribeEvent
	public void onStackDurability(StackMaxDamageEvent event) {
		if (!this.isEnabled()
				|| !unbreakingOverhaul)
			return;

		int lvl = event.getStack().getEnchantmentLevel(Enchantments.UNBREAKING);
		if (lvl <= 0)
			return;
		event.setNewMaxDamage((int) ((event.getNewMaxDamage() + getBonusFlatUnbreakingDurability(event.getStack()) * lvl) * (1f + lvl * 0.4f)));
	}

	public static int getBonusFlatUnbreakingDurability(ItemStack stack) {
		float durModifier = stack.getItem() instanceof DurabilityModifier durabilityModifier
				? durabilityModifier.getDurabilityMultiplier(stack)
				: 1f;
		for (ItemDefinition definition : ItemDefinitionsReloadListener.getDefinitions()) {
			if (!definition.item().matchesItem(stack))
				continue;

			if (definition.durability() != null && definition.durability().durabilityMultiplier != null)
				durModifier *= definition.durability().durabilityMultiplier;
		}
		if (stack.getItem() instanceof ShieldItem)
			return (int) (15 * durModifier);
		return (int) ((stack.getItem() instanceof ArmorItem ? 10 : 25) * durModifier);
	}

	public static boolean isBetterEfficiencyFormula() {
		return Feature.isEnabled(EnchantmentsFeature.class) && changeEfficiencyFormula;
	}

	@SubscribeEvent
	public void applyNewEfficiency(EnchantmentBonusEfficiencyEvent event) {
		int lvl = event.getStack().getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
		event.setNewEfficiency(event.getNewEfficiency() + getEfficiencyBonus(event.getNewEfficiency(), lvl));
	}

	public static float getEfficiencyBonus(float baseEfficiency, int lvl) {
		if (lvl == 0)
			return 0f;
        return EnchantmentsFeature.isBetterEfficiencyFormula() ? baseEfficiency * lvl * 0.2f : 1 + (lvl * lvl);
	}

	public static boolean isThornsOverhaul() {
		return Feature.isEnabled(EnchantmentsFeature.class) && thornsOverhaul;
	}

	//Run before knockback feature
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onKnockback(LivingKnockBackEvent event) {
		if (!this.isEnabled()
				|| event.getEntity().getCombatTracker().entries.isEmpty()
				|| event.getEntity().getPersistentData().contains("iguanatweaksexpanded:cancel_knockback"))
			return;

		Entity directAttacker = event.getEntity().getCombatTracker().entries.get(event.getEntity().getCombatTracker().entries.size() - 1).source().getDirectEntity();
		if (!(directAttacker instanceof LivingEntity attacker))
			return;

		float knockback = attacker.getMainHandItem().getEnchantmentLevel(KNOCKBACK.get());
		if (knockback == 0)
			return;
		if (attacker instanceof Player player) {
			float f = player.getAttackStrengthScale(0.5f);
			knockback *= f * f;
		}
		knockback *= insane96mcp.iguanatweaksreborn.module.combat.Knockback.getKnockbackMultiplier(attacker.getMainHandItem());
		event.setStrength(event.getStrength() + knockback);
	}

	@SubscribeEvent
	public void onLivingHurt(LivingEvent.LivingVisibilityEvent event) {
		if (!this.isEnabled())
			return;

		int lvl = EnchantmentHelper.getEnchantmentLevel(PROJECTILE_PROTECTION.get(), event.getEntity());
		if (lvl < 1)
			return;

		event.modifyVisibility(1f - 0.15f * lvl);
	}

	public static float bonusDamageEnchantment(Enchantment enchantment, int lvl, LivingEntity attacker, Entity target) {
		if (!(enchantment instanceof BonusDamageEnchantment bonusDamageEnchantment))
			return 0f;
		float damageBonus = bonusDamageEnchantment.getDamageBonus(attacker, target, attacker.getMainHandItem(), lvl);
		/*if (attacker instanceof Player player) {
			float f = player.getAttackStrengthScale(0.5f);
			damageBonus *= f * f;
		}*/
		return damageBonus;
	}

	@SubscribeEvent
	public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
		if (!this.isEnabled()
				|| !preventFarmlandTramplingWithFeatherFalling
				|| !(event.getEntity() instanceof LivingEntity entity)
				|| (EnchantmentHelper.getEnchantmentLevel(Enchantments.FALL_PROTECTION, entity) <= 0 && EnchantmentHelper.getEnchantmentLevel(FEATHER_FALLING.get(), entity) <= 0))
			return;

		event.setCanceled(true);
	}

	//Override vanilla behaviour
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onExperiencePickUp(PlayerXpEvent.PickupXp event) {
		if (!this.isEnabled()
				|| !nerfMending)
			return;

		event.setCanceled(true);
		Player player = event.getEntity();
		player.takeXpDelay = 2;
		player.take(event.getOrb(), 1);
		if (ModList.get().isLoaded("allurement"))
			Allurement.onExperiencePickup(player, event.getOrb());
		int i = repairPlayerItems(event.getOrb(), player, event.getOrb().value);
		if (i > 0) {
			player.giveExperiencePoints(i);
		}

		--event.getOrb().count;
		if (event.getOrb().count == 0) {
			event.getOrb().discard();
		}
	}

	private static int repairPlayerItems(ExperienceOrb xpOrb, Player player, int xp) {
		ItemStack stack = getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
        if (stack == null)
            return xp;

		float repairAmount = Math.min(xpOrb.value * 0.5f, stack.getDamageValue());
		stack.setDamageValue(stack.getDamageValue() - MathHelper.getAmountWithDecimalChance(player.getRandom(), repairAmount));
		int j = xp - Math.round(repairAmount * 2f);
		return j > 0 ? repairPlayerItems(xpOrb, player, j) : 0;
    }

	public static ItemStack getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity, Predicate<ItemStack> pStackCondition) {
		Map<EquipmentSlot, ItemStack> map = enchantment.getSlotItems(livingEntity);
		if (map.isEmpty())
			return null;

		List<ItemStack> list = Lists.newArrayList();

		for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
			ItemStack itemStack = entry.getValue();
			if (!itemStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack) > 0 && pStackCondition.test(itemStack)) {
				list.add(itemStack);
			}
		}
		if (ModList.get().isLoaded("toolbelt")) {
			/*List<ItemStack> toolbeltStacks = new ArrayList<>();
			ToolBelt.putItems(toolbeltStacks, livingEntity);
			toolbeltStacks.removeIf(stack -> stack.isEmpty() || EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) <= 0 || !stack.isDamaged());
			list.addAll(toolbeltStacks);*/
		}

		return list.isEmpty() ? null : list.get(livingEntity.getRandom().nextInt(list.size()));
	}

	/**
	 * Make Allurement's reforming work with ToolBelt items
	 */
	@SubscribeEvent
	public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
		/*if (!ModList.get().isLoaded("toolbelt")
				|| !ModList.get().isLoaded("allurement")
				|| event.getEntity().level().getGameTime() % AllurementConfig.COMMON.reformingTickRate.get() != 0)
			return;
		LivingEntity entity = event.getEntity();
		Level level = entity.getCommandSenderWorld();
		List<ItemStack> stacksInToolbelt = new ArrayList<>();
		ToolBelt.putItems(stacksInToolbelt, entity);
		for (ItemStack stack : stacksInToolbelt) {
			int lvl = EnchantmentHelper.getTagEnchantmentLevel(AllurementEnchantments.REFORMING.get(), stack);
			if (!stack.isEmpty() && stack.isDamaged() && lvl > 0) {
				stack.setDamageValue(stack.getDamageValue() - 1);
			}
		}*/
	}

	public static void destroyVanishingCurseItemsInToolBelt(LivingEntity player) {
		if (!ModList.get().isLoaded("toolbelt"))
			return;
		ToolBeltIntegration.removeCursedItemsOnDeath(player);
	}

	@SubscribeEvent
	public void onFall(LivingFallEvent event) {
		int lvl = event.getEntity().getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(FEATHER_FALLING.get());
		if (lvl <= 0)
			return;
		event.setDistance(event.getDistance() - lvl);
	}

	public static boolean isInfinityOverhaulEnabled() {
		return Feature.isEnabled(EnchantmentsFeature.class) && infinityOverhaul;
	}

	public static boolean shouldReplaceBaneOfArthropods(Enchantment enchantment) {
		return enchantment ==  Enchantments.BANE_OF_ARTHROPODS
				&& Feature.isEnabled(EnchantmentsFeature.class)
				&& EnchantmentsFeature.isEnchantmentDisabled(Enchantments.BANE_OF_ARTHROPODS)
				&& !EnchantmentsFeature.isEnchantmentDisabled(EnchantmentsFeature.BANE_OF_SSSSS.get());
	}

	public static boolean shouldReplaceWithLuck(Enchantment enchantment) {
		return (enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.MOB_LOOTING || enchantment == Enchantments.FISHING_LUCK)
				&& isBonusLootEnchantmentReworkEnabled();
	}

	public static int getLuckLevel(int original, int luckLvl) {
		if (!isBonusLootEnchantmentReworkEnabled()
				|| luckLvl == 0)
			return original;
		return 2;
	}

	public static int getLuckLevel(ItemStack stack, int original) {
		int luckLvl = EnchantmentHelper.getTagEnchantmentLevel(EnchantmentsFeature.LUCK.get(), stack);
		if (luckLvl <= 0)
			return original;
		return 2;
	}

	public static boolean isBonusLootEnchantmentReworkEnabled() {
		return Feature.isEnabled(EnchantmentsFeature.class)
				&& reworkBonusLootEnchantments;
	}

	public static int getEnchantmentValue(ItemStack stack) {
		for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.getDefinitions()) {
			if (itemDefinition.enchantability() != null && itemDefinition.item().matchesItem(stack))
				return itemDefinition.enchantability();
		}
		return stack.getEnchantmentValue();
	}

	public static float applyMiningSpeedModifiers(float miningSpeed, @Nullable BlockState state, boolean applyEfficiency, LivingEntity entity) {
		if (applyEfficiency) {
			int i = EnchantmentHelper.getBlockEfficiency(entity);
			ItemStack itemstack = entity.getMainHandItem();
			if (i > 0 && !itemstack.isEmpty())
				miningSpeed += ISOEventFactory.getBonusEnchantmentEfficiency(entity, state, itemstack, miningSpeed);
		}

		if (MobEffectUtil.hasDigSpeed(entity)) {
			miningSpeed *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2F;
		}

		if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float miningFatigueMultiplier = switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
				case 0 -> 0.3F;
				case 1 -> 0.09F;
				case 2 -> 0.0027F;
				default -> 8.1E-4F;
			};
			miningSpeed *= miningFatigueMultiplier;
		}

		if (entity.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(entity)) {
			miningSpeed /= 5.0F;
		}

		if (!entity.onGround()) {
			miningSpeed /= 5.0F;
		}

		return miningSpeed;
	}
}