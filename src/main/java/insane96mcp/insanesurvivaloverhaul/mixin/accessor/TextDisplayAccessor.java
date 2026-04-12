package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    /**
     * Exposes the private {@link Display.TextDisplay#setText(Component)} method.
     */
    @Invoker("setText")
    void insanesurvivaloverhaul$setText(Component text);
}
