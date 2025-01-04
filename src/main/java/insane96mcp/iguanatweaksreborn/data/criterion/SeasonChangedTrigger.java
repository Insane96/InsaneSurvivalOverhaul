package insane96mcp.iguanatweaksreborn.data.criterion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonChangedEvent;

import java.util.ArrayList;
import java.util.List;

public class SeasonChangedTrigger extends SimpleCriterionTrigger<SeasonChangedTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "season_changed");

	@Override
	protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate pPredicate, DeserializationContext context) {
		List<Season.SubSeason> seasons = new ArrayList<>();
		if (jsonObject.has("seasons")) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "seasons");
			for(int i = 0; i < jsonArray.size(); ++i) {
				JsonElement jsonElement = jsonArray.get(i);
				if (jsonElement.isJsonPrimitive()) {
					seasons.add(Season.SubSeason.valueOf(jsonElement.getAsString()));
				}
			}
		}
		return new TriggerInstance(pPredicate, seasons);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public void trigger(ServerPlayer player, Season.SubSeason season) {
		this.trigger(player, triggerInstance -> triggerInstance.matches(season));
	}

	@SubscribeEvent
	public static void onSeasonChanged(SeasonChangedEvent.Standard event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel))
			return;

		serverLevel.players().forEach(serverPlayer -> ISOTriggers.SEASON_CHANGED.trigger(serverPlayer, event.getNewSeason()));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public List<Season.SubSeason> seasons;
		public TriggerInstance(ContextAwarePredicate pPredicate, List<Season.SubSeason> seasons) {
			super(ID, pPredicate);
			this.seasons = seasons;
		}

		public JsonObject serializeToJson(SerializationContext context) {
			JsonObject jsonObject = super.serializeToJson(context);
			JsonArray jsonArray = new JsonArray();
			for (Season.SubSeason season : this.seasons) {
				jsonArray.add(season.toString());
			}
			jsonObject.add("seasons", jsonArray);

			return jsonObject;
		}

		public boolean matches(Season.SubSeason season) {
			return this.seasons.contains(season);
		}
	}
}
