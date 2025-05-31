package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

@LoadFeature(module = ClientModules.Ids.CLIENT,
        name = "Death",
        description = "Changes to death")
public class Death extends Feature {
    @Config(description = "If true, when you die, you switch to third person camera.")
    public static Boolean thirdPerson = true;
    @Config(description = "Why is that still a thing?.")
    public static Boolean removeScore = true;
    @Config(name = "Replace score with time since last death")
    public static Boolean replaceScoreWithTimeSinceLastDeath = true;

    public Death(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static boolean dead = false;
    public static void onDeath() {
        if (!Feature.isEnabled(Death.class)
                || !thirdPerson)
            return;

        CameraType cameratype = Minecraft.getInstance().options.getCameraType();
        if (cameratype != CameraType.FIRST_PERSON)
            return;
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        dead = true;
    }
}
