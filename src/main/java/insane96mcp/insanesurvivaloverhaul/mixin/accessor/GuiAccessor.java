package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiAccessor {
    @Invoker("renderExperienceBar")
    void callRenderExperienceBar(GuiGraphics guiGraphics, int x);
}
