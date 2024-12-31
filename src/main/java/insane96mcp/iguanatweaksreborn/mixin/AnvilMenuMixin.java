package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.anvils.AnvilRepair;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.Anvils;
import insane96mcp.iguanatweaksreborn.module.items.UnbreakableItems;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(value = AnvilMenu.class, priority = 1001)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

	@Shadow
	@Final
	private static int COST_RENAME;

	@Shadow
	@Final
	private DataSlot cost;

	@Shadow
	public int repairItemCountCost;

	@Shadow
	private String itemName;

	public AnvilMenuMixin(@Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
		super(p_39773_, p_39774_, p_39775_, p_39776_);
	}

	/**
	 * Remove the requirement for cost to be at least 1
	 */
	@Inject(
			method = "mayPickup",
			at = @At("HEAD"),
			cancellable = true
	)
	private void mayPickUp(Player player, boolean unused, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue((player.getAbilities().instabuild || player.experienceLevel >= this.cost.get()));
	}

	@Inject(
			at = @At("HEAD"),
			method = "createResult",
			cancellable = true
	)
	public void createResult(CallbackInfo ci) {
		if (!Feature.isEnabled(Anvils.class))
			return;

		ci.cancel();

		ItemStack left = this.inputSlots.getItem(0);
		this.cost.set(1);
		int mergeCost = 0;
		int baseCost = 0;
		boolean isRenaming = false;

		if (left.isEmpty()) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
			return;
		}

		ItemStack resultStack = left.copy();
		ItemStack right = this.inputSlots.getItem(1);
		Map<Enchantment, Integer> leftEnchantments = EnchantmentHelper.getEnchantments(resultStack);
		//Don't add the repair cost of the items if remove repair cost increase
		if (!Anvils.noXpRepairCost)
			baseCost += left.getBaseRepairCost() + (right.isEmpty() ? 0 : right.getBaseRepairCost());
		this.repairItemCountCost = 0;
		boolean isEnchantedBook = false;

		if (!right.isEmpty()) {
			if (!net.minecraftforge.common.ForgeHooks.onAnvilChange((AnvilMenu) (Object) this, left, right, resultSlots, itemName, baseCost, this.player))
				return;
			isEnchantedBook = right.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(right).isEmpty();
			Optional<AnvilRepair.RepairData> oRepairData = Anvils.getCustomAnvilRepair(left, right);
			//If it's a damageable item and a material check if trying to repair it
			if (resultStack.isDamageableItem() && (resultStack.getItem().isValidRepairItem(left, right) || oRepairData.isPresent())) {
				int repairItemCountCost;
				//If a custom anvil repair is present, use that
				if (oRepairData.isPresent()) {
					AnvilRepair.RepairData repairData = oRepairData.get();
					int maxPartialRepairDmg = Mth.ceil(resultStack.getMaxDamage() * (1f - repairData.maxRepair()));
					float amountRequired = repairData.amountRequired();
					int xpCost = 0;
					if (Anvils.moreMaterialIfEnchanted > 0f && left.isEnchanted()) {
						float increase = 0f;
						for (Integer lvl : EnchantmentHelper.getEnchantments(left).values()) {
							increase += Anvils.moreMaterialIfEnchanted.floatValue() * lvl;
							if (Anvils.differentXpRepairCost)
								xpCost += lvl;
						}
						amountRequired *= 1 + increase;
					}
					if (Anvils.moreMaterialIfEnchantedFlat > 0f && left.isEnchanted()) {
						float increase = 0f;
						for (Integer lvl : EnchantmentHelper.getEnchantments(left).values()) {
							increase += Anvils.moreMaterialIfEnchantedFlat.floatValue() * lvl;
							if (Anvils.differentXpRepairCost)
								xpCost += lvl;
						}
						amountRequired += (increase * oRepairData.get().costMultiplier());
					}
					float repairSteps = Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / amountRequired);
					if (repairSteps <= 0 || resultStack.getDamageValue() <= maxPartialRepairDmg) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}

					float damageValue = resultStack.getDamageValue();
					for (repairItemCountCost = 0; repairSteps > 0 && repairItemCountCost < right.getCount() && damageValue > maxPartialRepairDmg; ++repairItemCountCost) {
						damageValue -= repairSteps;
						if (!Anvils.noXpRepairCost)
							//Vanilla behaviour
							++mergeCost;
						repairSteps = Math.min(damageValue, resultStack.getMaxDamage() / amountRequired);
					}
					resultStack.setDamageValue((int) Math.max(maxPartialRepairDmg, damageValue));
					float repairedDurabilityRatio = ((resultStack.getMaxDamage() - resultStack.getDamageValue()) - (left.getMaxDamage() - left.getDamageValue())) / (float) resultStack.getMaxDamage();
					xpCost = Mth.ceil(xpCost * repairedDurabilityRatio);
					mergeCost += xpCost;
				}
				//Otherwise, vanilla behaviour
				else {
					float amountRequired = 4;
					int xpCost = 0;
					if (Anvils.moreMaterialIfEnchanted > 0f && left.isEnchanted()) {
						float increase = 0f;
						for (Integer lvl : EnchantmentHelper.getEnchantments(left).values()) {
							increase += Anvils.moreMaterialIfEnchanted.floatValue() * lvl;
							if (Anvils.differentXpRepairCost)
								xpCost += lvl;
						}
						amountRequired *= 1 + increase;
					}
					if (Anvils.moreMaterialIfEnchantedFlat > 0f && left.isEnchanted()) {
						float increase = 0f;
						for (Integer lvl : EnchantmentHelper.getEnchantments(left).values()) {
							increase += Anvils.moreMaterialIfEnchantedFlat.floatValue() * lvl;
							if (Anvils.differentXpRepairCost)
								xpCost += lvl;
						}
						amountRequired += increase;
					}
					int repairSteps = Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / (int) amountRequired);
					if (repairSteps <= 0) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}

					for (repairItemCountCost = 0; repairSteps > 0 && repairItemCountCost < right.getCount(); ++repairItemCountCost) {
						int dmgAfterRepair = resultStack.getDamageValue() - repairSteps;
						resultStack.setDamageValue(dmgAfterRepair);
						if (!Anvils.noXpRepairCost)
							//Vanilla behaviour
							++mergeCost;
						repairSteps = (int) Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / amountRequired);
					}
					float repairedDurabilityRatio = ((resultStack.getMaxDamage() - resultStack.getDamageValue()) - (left.getMaxDamage() - left.getDamageValue())) / (float) resultStack.getMaxDamage();
					xpCost = Mth.ceil(xpCost * repairedDurabilityRatio);
					mergeCost += xpCost;
				}

				this.repairItemCountCost = repairItemCountCost;
			}
			//Else it's merging items
			else {
				if ((!isEnchantedBook && (!resultStack.is(right.getItem())
						|| !resultStack.isDamageableItem()))
						//If unbreakable items is enabled then I cannot use broken items to repair
						|| Feature.isEnabled(UnbreakableItems.class) && Feature.isEnabled(UnbreakableItems.class) && UnbreakableItems.isBroken(right)) {
					this.resultSlots.setItem(0, ItemStack.EMPTY);
					this.cost.set(0);
					return;
				}

				if (resultStack.isDamageableItem() && !isEnchantedBook && Anvils.allowMergingItems) {
					int leftDurabilityLeft = left.getMaxDamage() - left.getDamageValue();
					int rightDurabilityLeft = right.getMaxDamage() - right.getDamageValue();
					int rightDurabilityLeftPlusBonus = rightDurabilityLeft + resultStack.getMaxDamage() * Anvils.mergingRepairBonus / 100;
					/*if (Anvils.reducedRepairWithOtherItemIfEnchanted > 0f && left.isEnchanted()) {
						rightDurabilityLeftPlusBonus = (int) (rightDurabilityLeft * Anvils.reducedRepairWithOtherItemIfEnchanted);
					}*/
					if (left.isEnchanted()) {
						//TODO reduce repair amount if left item is enchanted
					}

					int resultDurabilityLeft = leftDurabilityLeft + rightDurabilityLeftPlusBonus;
					int damageValue = resultStack.getMaxDamage() - resultDurabilityLeft;
					if (damageValue < 0)
						damageValue = 0;

					if (damageValue < resultStack.getDamageValue()) {
						resultStack.setDamageValue(damageValue);
						if (!Anvils.mergingCostBasedOffResult)
							mergeCost += 2;
					}
				}

				Map<Enchantment, Integer> rightEnchantments = EnchantmentHelper.getEnchantments(right);
				boolean canEnchant = false;
				boolean cannotEnchant = false;

				for (Enchantment rightEnchantment : rightEnchantments.keySet()) {
                    if (rightEnchantment == null)
						continue;

					int leftLvl = leftEnchantments.getOrDefault(rightEnchantment, 0);
					int rightLvl = rightEnchantments.get(rightEnchantment);
					rightLvl = leftLvl == rightLvl && rightLvl < rightEnchantment.getMaxLevel() ? rightLvl + 1 : Math.max(rightLvl, leftLvl);
					boolean canEnchant2 = rightEnchantment.canEnchant(left);
					if (this.player.getAbilities().instabuild || left.is(Items.ENCHANTED_BOOK))
						canEnchant2 = true;

					for (Enchantment enchantment : leftEnchantments.keySet()) {
						if (enchantment != rightEnchantment && !rightEnchantment.isCompatibleWith(enchantment)) {
							canEnchant2 = false;
							if (!Anvils.mergingCostBasedOffResult)
								++mergeCost;
						}
					}

					if (!canEnchant2) {
						cannotEnchant = true;
					}
					else {
						canEnchant = true;
						if (rightLvl > rightEnchantment.getMaxLevel()
								&& leftLvl == rightLvl /*Added to allow over max level enchantment books to be applied to items*/)
							rightLvl = rightEnchantment.getMaxLevel();

						leftEnchantments.put(rightEnchantment, rightLvl);
						int enchantmentRarityCost = Anvils.getRarityCost(rightEnchantment);

						if (isEnchantedBook)
							enchantmentRarityCost = Math.max(1, enchantmentRarityCost / 2);

						if (!Anvils.mergingCostBasedOffResult)
							mergeCost += enchantmentRarityCost * rightLvl;
					}
                }

				//If "Merging cost is based off result" is enabled, I loop all the enchantments to calculate the cost based off the result
				if (Anvils.mergingCostBasedOffResult) {
					for (Map.Entry<Enchantment, Integer> enchantment : leftEnchantments.entrySet()) {
						int enchantmentRarityCost = Anvils.getRarityCost(enchantment.getKey());

						if (isEnchantedBook)
							enchantmentRarityCost = Math.max(1, enchantmentRarityCost / 2);
						mergeCost += enchantmentRarityCost * enchantment.getValue();
					}
				}
				//If the right item is not enchanted then I'm repairing the left one with it, so there should be no cost
				if (!right.isEnchanted())
					mergeCost = 0;

				if (cannotEnchant && !canEnchant) {
					this.resultSlots.setItem(0, ItemStack.EMPTY);
					this.cost.set(0);
					return;
				}
			}
		}

		if (StringUtils.isBlank(this.itemName)) {
			if (left.hasCustomHoverName()) {
				isRenaming = true;
				resultStack.resetHoverName();
			}
		}
		else if (!this.itemName.equals(left.getHoverName().getString())) {
			isRenaming = true;
			resultStack.setHoverName(Component.literal(this.itemName));
		}
		if (isEnchantedBook && !resultStack.isBookEnchantable(right))
			resultStack = ItemStack.EMPTY;

		this.cost.set((int) Math.round((baseCost + mergeCost) * Anvils.repairCostMultiplier));
		if (isRenaming && !Anvils.renamingNoCost)
			this.cost.set(this.cost.get() + COST_RENAME);

		if (isRenaming && Anvils.renamingNoCost && mergeCost <= 0)
			this.cost.set(0);
		if (!isRenaming && right.isEmpty())
			resultStack = ItemStack.EMPTY;

		//Set Too Expensive cap
		if (this.cost.get() >= Anvils.anvilRepairCap && !this.player.getAbilities().instabuild)
			resultStack = ItemStack.EMPTY;

		if (!resultStack.isEmpty()) {
			if (!Anvils.noXpRepairCost) {
				int toolRepairCost = resultStack.getBaseRepairCost();
				if (!right.isEmpty() && toolRepairCost < right.getBaseRepairCost())
					toolRepairCost = right.getBaseRepairCost();
				if (mergeCost >= 1)
					toolRepairCost = AnvilMenu.calculateIncreasedRepairCost(toolRepairCost);
				resultStack.setRepairCost(toolRepairCost);
			}
			EnchantmentHelper.setEnchantments(leftEnchantments, resultStack);
		}

		this.resultSlots.setItem(0, resultStack);
		this.broadcastChanges();
	}

	@Shadow
	protected abstract boolean mayPickup(@NotNull Player p_39798_, boolean p_39799_);

	@Shadow
	protected abstract void onTake(@NotNull Player p_150601_, @NotNull ItemStack p_150602_);

	@Shadow
	protected abstract boolean isValidBlock(@NotNull BlockState p_39788_);

	@Shadow
	public abstract void createResult();

	@Shadow
	protected abstract ItemCombinerMenuSlotDefinition createInputSlotDefinitions();
}
