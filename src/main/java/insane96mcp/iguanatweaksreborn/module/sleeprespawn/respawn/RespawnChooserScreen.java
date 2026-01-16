package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RespawnChooserScreen extends Screen {

    List<RespawnPoint> respawnPoints;
    private final List<Button> respawnButtons = Lists.newArrayList();

    public RespawnChooserScreen(List<RespawnPoint> respawnPoints) {
        super(Component.translatable("respawnPointsScreen.title"));
        this.respawnPoints = respawnPoints;
    }

    @Override
    protected void init() {
        int btns = 0;
        for (RespawnPoint respawnPoint : this.respawnPoints) {
            ++btns;
            this.respawnButtons.add(this.addRenderableWidget(Button.builder(respawnPoint.name(), (p_280794_) -> {
                RespawnPointSelectedMessage.send(Minecraft.getInstance().player, respawnPoint.pos());
            }).bounds(this.width / 2 - 100, 20 + btns * 22, 200, 20).build()));
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 10, 16777215);
        pGuiGraphics.pose().popPose();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
