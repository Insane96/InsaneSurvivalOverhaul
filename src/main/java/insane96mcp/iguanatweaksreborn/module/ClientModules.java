package insane96mcp.iguanatweaksreborn.module;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.setup.client.ITRClientConfig;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.config.ModConfig;

public class ClientModules {
    public static Module client;

    public static void init() {
        client = Module.Builder.create(Ids.CLIENT, "Client", ModConfig.Type.CLIENT, ITRClientConfig.builder).build();
    }

    public static class Ids {
        public static final String CLIENT = InsaneSurvivalTweaks.RESOURCE_PREFIX + "client";
    }
}
