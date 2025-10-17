package insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VillagerTradesReloadListener extends SimpleJsonResourceReloadListener {
    public static HashMap<VillagerProfession, List<VillagerTrade>> TRADES = new HashMap<>();
    public static final VillagerTradesReloadListener INSTANCE;
    private static final Gson GSON = new GsonBuilder().create();
    public VillagerTradesReloadListener() {
        super(GSON, "villager_trades");
    }

    static {
        INSTANCE = new VillagerTradesReloadListener();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        TRADES.clear();
        if (!Villagers.loadVillagerTradesFromDataPacks)
            return;
        for (var entry : map.entrySet()) {
            VillagerProfession profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(entry.getKey());
            if (profession == null) {
                ISOLogHelper.warn("Failed to find Villager Profession %s", entry.getKey());
                continue;
            }
            try {
                List<VillagerTrade> villagerTrade = GSON.fromJson(entry.getValue(), VillagerTrade.LIST_TYPE);
                if (!TRADES.containsKey(profession))
                    TRADES.put(profession, new ArrayList<>());
                TRADES.get(profession).addAll(villagerTrade);
            }
            catch (JsonSyntaxException e) {
                ISOLogHelper.error("Parsing error loading Villager Trades %s: %s", entry.getKey(), e.getMessage());
            }
            catch (Exception e) {
                ISOLogHelper.error("Failed loading Villager Trades %s: %s", entry.getKey(), e.getMessage());
            }
        }

        ISOLogHelper.info("Loaded %s Villager Trades", TRADES.size());
    }

    public VillagerTrade getTradesOfLevel(VillagerProfession profession, int level) {
        List<VillagerTrade> villagerTrades = TRADES.get(profession);
        if (villagerTrades == null)
            return null;
        return villagerTrades.stream()
                .filter(vt -> vt.level == level)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getName() {
        return "Villager Trades Reload Listener";
    }
}
