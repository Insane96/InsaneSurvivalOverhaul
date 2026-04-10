package insane96mcp.insanesurvivaloverhaul.mixin.module.farming.livestock;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Sheep.class)
public abstract class SheepMixin_Livestock extends Animal {
	protected SheepMixin_Livestock(EntityType<? extends Animal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Definition(id = "random", field = "Lnet/minecraft/world/entity/animal/Sheep;random:Lnet/minecraft/util/RandomSource;")
	@Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
	@Expression("1 + this.random.nextInt(?)")
	@ModifyExpressionValue(method = "shear", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	private int insanesurvivaloverhaulonWoolAmount(int amount) {
		if (!Feature.isEnabled(Livestock.class))
			return amount;

		Livestock.Age age = Livestock.getAge(this);
		if (age == Livestock.Age.ADULT)
			return MathHelper.getAmountWithDecimalChance(this.random, amount * 2f);
		return amount;
	}
}
