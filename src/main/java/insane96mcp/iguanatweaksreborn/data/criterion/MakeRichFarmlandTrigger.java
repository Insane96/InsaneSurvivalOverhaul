package insane96mcp.iguanatweaksreborn.data.criterion;

import com.google.gson.JsonObject;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MakeRichFarmlandTrigger extends SimpleCriterionTrigger<MakeRichFarmlandTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation(InsaneSurvivalTweaks.MOD_ID, "make_rich_farmland");

	@Override
	protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate pPredicate, DeserializationContext context) {
		return new TriggerInstance(pPredicate);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, (TriggerInstance::matches));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(ContextAwarePredicate pPredicate) {
			super(ID, pPredicate);
		}

		public JsonObject serializeToJson(SerializationContext context) {
			return super.serializeToJson(context);
		}

		public boolean matches() {
			return true;
		}
	}
}
