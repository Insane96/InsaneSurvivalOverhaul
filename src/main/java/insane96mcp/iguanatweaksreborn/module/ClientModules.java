package insane96mcp.iguanatweaksreborn.module;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.setup.client.ISOClientConfig;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.config.ModConfig;

public class ClientModules {
    public static Module client;

    public static void init() {
        client = Module.Builder.create(Ids.CLIENT, "Client", ModConfig.Type.CLIENT, ISOClientConfig.builder).build();
    }

    public static class Ids {
        public static final String CLIENT = InsaneSO.RESOURCE_PREFIX + "client";
    }
}
