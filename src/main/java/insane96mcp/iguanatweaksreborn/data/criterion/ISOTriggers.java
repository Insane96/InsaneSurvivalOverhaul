package insane96mcp.iguanatweaksreborn.data.criterion;

import net.minecraft.advancements.CriteriaTriggers;

public class ISOTriggers {

    public static AnvilRepairTrigger ANVIL_REPAIR = CriteriaTriggers.register(new AnvilRepairTrigger());
    public static MakeRichFarmlandTrigger MAKE_RICH_FARMLAND = CriteriaTriggers.register(new MakeRichFarmlandTrigger());
    public static UnfairOneShotTrigger UNFAIR_ONESHOT = CriteriaTriggers.register(new UnfairOneShotTrigger());
    public static SeasonChangedTrigger SEASON_CHANGED = CriteriaTriggers.register(new SeasonChangedTrigger());
    public static ActivateRespawnObeliskTrigger ACTIVATE_RESPAWN_OBELISK = CriteriaTriggers.register(new ActivateRespawnObeliskTrigger());
    public static OverweightPouchCarryTrigger OVERWEIGHT_POUCH_CARRY = CriteriaTriggers.register(new OverweightPouchCarryTrigger());

    public static void init() {
        //Do nothing, it's just to trigger the registration of CriteriaTriggers
    }
}
