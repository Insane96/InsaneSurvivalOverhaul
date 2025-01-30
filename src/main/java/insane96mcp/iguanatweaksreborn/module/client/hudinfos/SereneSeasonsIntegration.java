package insane96mcp.iguanatweaksreborn.module.client.hudinfos;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonTime;

import java.util.List;
import java.util.Locale;

public class SereneSeasonsIntegration {
    public static void addSeasonInfo(List<String> toDraw, Level level) {
        if (!ModConfig.seasons.isDimensionWhitelisted(level.dimension()))
            return;

        int seasonCycleTicks = SeasonHelper.getSeasonState(level).getSeasonCycleTicks();
        SeasonTime time = new SeasonTime(seasonCycleTicks);
        int subSeasonDuration = ModConfig.seasons.subSeasonDuration;

        toDraw.add(Component.translatable("desc.sereneseasons." + time.getSubSeason().toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY).append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY)).append(Component.translatable("desc.sereneseasons." + time.getTropicalSeason().toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)).getString());
        toDraw.add(Component.translatable("desc.sereneseasons.day_counter", (time.getDay() % subSeasonDuration) + 1, subSeasonDuration).withStyle(ChatFormatting.GRAY).append(Component.translatable("desc.sereneseasons.tropical_day_counter", (((time.getDay() + subSeasonDuration) % (subSeasonDuration * 2)) + 1), subSeasonDuration * 2).withStyle(ChatFormatting.DARK_GRAY)).getString());
    }
}
