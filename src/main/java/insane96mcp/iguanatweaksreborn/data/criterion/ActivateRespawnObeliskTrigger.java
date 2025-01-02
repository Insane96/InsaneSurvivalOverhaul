package insane96mcp.iguanatweaksreborn.data.criterion;

import com.google.gson.JsonObject;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ActivateRespawnObeliskTrigger extends SimpleCriterionTrigger<ActivateRespawnObeliskTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation(IguanaTweaksReborn.MOD_ID, "activate_respawn_obelisk");

	@Override
	protected TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
		return new TriggerInstance(pPredicate);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, TriggerInstance::matches);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(ContextAwarePredicate pPredicate) {
			super(ID, pPredicate);
		}

		public boolean matches() {
			return true;
		}
	}
}
