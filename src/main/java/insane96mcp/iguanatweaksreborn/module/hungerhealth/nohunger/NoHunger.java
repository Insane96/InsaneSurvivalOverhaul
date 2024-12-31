package insane96mcp.iguanatweaksreborn.module.hungerhealth.nohunger;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.mixin.client.GuiAccessor;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.integration.FarmersDelightIntegration;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegen;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.nohunger.integration.AutumnityIntegration;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.utils.Utils;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.CakeEatEvent;
import insane96mcp.insanelib.event.PlayerExhaustionEvent;
import insane96mcp.insanelib.util.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Label(name = "No Hunger", description = "Remove hunger and get back to the Beta 1.7.3 days")
@LoadFeature(module = Modules.Ids.HUNGER_HEALTH)
public class NoHunger extends Feature {

    private static final String FOOD_REGEN_LEFT = IguanaTweaksReborn.RESOURCE_PREFIX + "food_regen_left";
    private static final String FOOD_REGEN_STRENGTH = IguanaTweaksReborn.RESOURCE_PREFIX + "food_regen_strength";
    private static final int FOOD_REGEN_TICK_RATE = 10;

    private static final String HEALTH_LANG = IguanaTweaksReborn.MOD_ID + ".tooltip.health";
    private static final String MISSING_HEALTH_LANG = IguanaTweaksReborn.MOD_ID + ".tooltip.missing_health";
    private static final String SEC_LANG = IguanaTweaksReborn.MOD_ID + ".tooltip.sec";

    @Config(min = 0d)
    @Label(name = "Food Heal.Over Time", description = "The formula to calculate the health regenerated overtime when eating food. Leave empty to disable. Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html.")
    public static String healOverTime = "(hunger^1.37) * 0.5";
    @Config
    @Label(name = "Food Heal.Over time Strength", description = "How much HP does food regen each second? Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html")
    public static String healOverTimeStrength = "MAX(0.15, 5 * saturation_modifier * (1 / hunger))";
    @Config
    @Label(name = "Food Heal.Over time Decay", description = "Over Time Heal will be consumed at the rate of exhaustion multiplied by this")
    public static Double healOverTimeDecay = 0.02d;
    @Config(min = 0d)
    @Label(name = "Food Heal.Instant Heal", description = "The formula to calculate the health restored instantly when eating. Leave empty to disable. To have the same effect as pre-Beta 1.8 food just use \"hunger\". Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html.")
    public static String instantHeal = "0.5 * ROUND((hunger^1.3) * 0.35, 1) / 0.5";
    @Config(min = 0d)
    @Label(name = "Food Heal.Saturation threshold", description = "Foods below this saturation will instantly heal, foods equal or above this threshold will have overtime heal.")
    public static Double instantHealSaturationThreshold = 4d;
    @Config
    @Label(name = "Raw food.Heal Multiplier", description = "If true, raw food will heal by this percentage (this is applied after 'Food Heal.Health Multiplier'). Raw food is defined in the iguanatweaksreborn:raw_food tag")
    public static Double rawFoodHealPercentage = 1d;

    @Config
    @Label(name = "Convert Hunger to Weakness", description = "If true, Hunger effect is replaced by Weakness")
    public static Boolean convertHungerToWeakness = true;

    @Config
    @Label(name = "Convert Saturation to Haste", description = "If true, Saturation effect is replaced by Haste")
    public static Boolean convertSaturationToHaste = true;

    @Config
    @Label(name = "Buff cakes", description = "Make cakes restore 40% missing health")
    public static Boolean buffCakes = true;

    @Config
    @Label(name = "Food tooltip", description = "(Client Only) If enabled, Foods will show \"Snack\" when the food instantly heals and \"Meal\" when the food heals over time. If advanced tooltips are enabled, the food will show how much it restores")
    public static Boolean foodTooltip = true;

    @Config
    @Label(name = "Render armor at Hunger", description = "(Client Only) Armor is rendered in the place of Hunger bar")
    public static Boolean renderArmorAtHunger = true;

    public NoHunger(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!this.isEnabled()
                || event.player.level().isClientSide
                || event.phase.equals(TickEvent.Phase.START))
            return;

        if (HealthRegen.isPlayerHurt(event.player))
            event.player.getFoodData().foodLevel = 15;
        else
            event.player.getFoodData().foodLevel = 20;

        if (event.player.hasEffect(MobEffects.HUNGER) && convertHungerToWeakness) {
            MobEffectInstance effect = event.player.getEffect(MobEffects.HUNGER);
            //noinspection ConstantConditions; Checking with hasEffect
            event.player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, effect.getDuration() + 1, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
            event.player.removeEffect(MobEffects.HUNGER);
        }
        if (event.player.hasEffect(MobEffects.SATURATION) && convertSaturationToHaste) {
            MobEffectInstance effect = event.player.getEffect(MobEffects.SATURATION);
            //noinspection ConstantConditions; Checking with hasEffect
            event.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, (effect.getDuration() + 1) * 20, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
            event.player.removeEffect(MobEffects.SATURATION);
        }

        if (event.player.tickCount % FOOD_REGEN_TICK_RATE == 0 && getFoodRegenLeft(event.player) > 0f)
            consumeAndHealFromFoodRegen(event.player);
    }

    @SubscribeEvent
    public void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        if (!this.isEnabled()
                || event.getItem().getItem().getFoodProperties() == null
                || !(event.getEntity() instanceof Player player)
                || event.getEntity().level().isClientSide)
            return;

        Item item = event.getItem().getItem();
        healOnEat(player, item, item.getFoodProperties(event.getItem(), player));
    }

    private static final FoodProperties CAKE_FOOD_PROPERTIES = new FoodProperties.Builder().nutrition(2).saturationMod(0.8f).build();

    @SubscribeEvent
    public void onCakeEat(CakeEatEvent event) {
        if (!this.isEnabled()
                || ((Level)event.getLevel()).isClientSide)
            return;

        healOnEat(event.getEntity(), null, CAKE_FOOD_PROPERTIES);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFoodExhaustion(PlayerExhaustionEvent event) {
        if (!isEnabled(NoHunger.class)
                || event.getEntity().level().isClientSide
                || healOverTimeDecay == 0)
            return;

        ServerPlayer player = (ServerPlayer) event.getEntity();

        float regenLeft = getFoodRegenLeft(player);
        if (regenLeft <= 0)
            return;
        float regenStrength = getFoodRegenStrength(player);
        regenLeft -= event.getAmount() * healOverTimeDecay.floatValue();
        setHealOverTime(player, regenLeft, regenStrength);
    }

    /**
     * item is null when eating cakes
     */
    @SuppressWarnings("ConstantConditions")
    public static void healOnEat(Player player, @Nullable Item item, FoodProperties foodProperties) {
        boolean isRawFood = item != null && FoodDrinks.isRawFood(item);
        if (Utils.getFoodSaturationRestored(foodProperties) >= instantHealSaturationThreshold)
            onEatHealOverTime(player, item, foodProperties, isRawFood);
        else
            onEatInstantHeal(player, item, foodProperties, isRawFood);
    }

    /**
     * item is null when is a cake
     */
    public static void onEatHealOverTime(Player player, @Nullable Item item, FoodProperties foodProperties, boolean isRawFood) {
        if (!doesHealOverTime())
            return;

        float heal = Utils.computeFoodFormula(foodProperties, healOverTime);
        if (heal <= 0f)
            return;
        if (buffCakes && item == null)
            heal = Math.max((player.getMaxHealth() - player.getHealth()) * 0.4f, 1f);
        if (isRawFood && rawFoodHealPercentage != 1d)
            heal *= rawFoodHealPercentage;
        heal = applyModifiers(player, heal);

        float strength = Utils.computeFoodFormula(foodProperties, healOverTimeStrength) / 20f;
        setHealOverTime(player, heal, strength);
    }

    public static boolean doesHealOverTime() {
        return !StringUtils.isBlank(healOverTime) && !StringUtils.isBlank(healOverTimeStrength);
    }

    private static void onEatInstantHeal(Player player, @Nullable Item item, FoodProperties foodProperties, boolean isRawFood) {
        if (!doesHealInstantly())
            return;

        float heal = buffCakes && item == null
                ? Math.max((player.getMaxHealth() - player.getHealth()) * 0.2f, 1f)
                : getInstantHealAmount(foodProperties, isRawFood);
        heal = applyModifiers(player, heal);
        player.heal(heal);
    }

    public static float getInstantHealAmount(FoodProperties foodProperties, boolean isRawFood) {
        float heal = Utils.computeFoodFormula(foodProperties, instantHeal);
        if (isRawFood && rawFoodHealPercentage != 1d)
            heal *= rawFoodHealPercentage;
        return heal;
    }

    public static boolean doesHealInstantly() {
        return !StringUtils.isBlank(instantHeal);
    }

    private static float getFoodRegenLeft(Player player) {
        return player.getPersistentData().getFloat(FOOD_REGEN_LEFT);
    }

    public static void setHealOverTime(Player player, float amount, float strength) {
        player.getPersistentData().putFloat(FOOD_REGEN_LEFT, amount);
        player.getPersistentData().putFloat(FOOD_REGEN_STRENGTH, strength);
        if (player instanceof ServerPlayer serverPlayer) {
            Object msg = new FoodRegenSync(amount, strength);
            NetworkHandler.CHANNEL.sendTo(msg, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    private static void consumeAndHealFromFoodRegen(Player player) {
        float regenLeft = getFoodRegenLeft(player);
        float regenStrength = getFoodRegenStrength(player);
        if (player.getHealth() >= player.getMaxHealth())
            return;
        float healAmount = regenStrength * FOOD_REGEN_TICK_RATE;
        if (regenLeft < healAmount)
            healAmount = regenLeft;
        if (player.getMaxHealth() - player.getHealth() < healAmount)
            healAmount = player.getMaxHealth() - player.getHealth();
        if (ModList.get().isLoaded("farmersdelight"))
            healAmount = FarmersDelightIntegration.tryApplyComfort(player, healAmount);
        player.heal(healAmount);
        regenLeft -= healAmount;
        if (regenLeft <= 0f)
            regenLeft = 0f;
        setHealOverTime(player, regenLeft, regenStrength);
    }

    private static float getFoodRegenStrength(Player player) {
        return player.getPersistentData().getFloat(FOOD_REGEN_STRENGTH);
    }

    private static float applyModifiers(Player player, float amount) {
        if (ModList.get().isLoaded("autumnity"))
            amount = AutumnityIntegration.tryApplyFoulTaste(player, amount);
        return amount;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        NoHungerSync.sync(this.isEnabled(), (ServerPlayer) event.getEntity());
    }

    //Render before Regenerating absorption
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void removeFoodBar(final RenderGuiOverlayEvent.Pre event) {
        if (!this.isEnabled())
            return;

        if (event.getOverlay().equals(VanillaGuiOverlay.FOOD_LEVEL.type()))
            event.setCanceled(true);
        //Remove armor bar to render it on the right
        if (event.getOverlay().equals(VanillaGuiOverlay.ARMOR_LEVEL.type()) && renderArmorAtHunger)
            event.setCanceled(true);
            /*Minecraft mc = Minecraft.getInstance();
            ForgeGui gui = (ForgeGui) mc.gui;
            if (!mc.options.hideGui && gui.shouldDrawSurvivalElements())
                renderArmor(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());*/
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "armor", (gui, guiGraphics, partialTicks, screenWidth, screenHeight) -> {
            if (Feature.isEnabled(NoHunger.class) && renderArmorAtHunger && gui.shouldDrawSurvivalElements() && gui.shouldDrawSurvivalElements())
                renderArmor(guiGraphics, screenWidth, screenHeight);
        });
    }

    protected static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    @OnlyIn(Dist.CLIENT)
    protected static void renderArmor(GuiGraphics guiGraphics, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        ForgeGui gui = (ForgeGui) mc.gui;
        mc.getProfiler().push(IguanaTweaksReborn.RESOURCE_PREFIX + "armor");

        RenderSystem.enableBlend();
        int left = width / 2 + 82;
        int top = height - gui.rightHeight;

        int level = mc.player.getArmorValue();
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
                guiGraphics.blit(GUI_ICONS_LOCATION, left, top, 34, 9, 9, 9, 256, 256);
            else if (i == level)
                ClientUtils.blitVericallyMirrored(GUI_ICONS_LOCATION, guiGraphics, left, top, 25, 9, 9, 9, 256, 256);
            else
                guiGraphics.blit(GUI_ICONS_LOCATION, left, top, 16, 9, 9, 9, 256, 256);
            left -= 8;
        }
        if (level > 0)
            gui.rightHeight += 10;

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }

    protected static final ResourceLocation OT_REGEN_LOCATION = new ResourceLocation(IguanaTweaksReborn.MOD_ID, "textures/gui/ot_regen.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerGui(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "ot_regen", (gui, guiGraphics, partialTicks, screenWidth, screenHeight) -> {
            if (!Feature.isEnabled(NoHunger.class)
                    || !gui.shouldDrawSurvivalElements())
                return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null)
                return;

            int right = screenWidth / 2 - 91;
            int health = Mth.ceil(player.getHealth());
            int healthLast = ((GuiAccessor)gui).getDisplayHealth();
            AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            float healthMax = Math.max((float) attrMaxHealth.getValue(), Math.max(healthLast, health));
            int absorb = Mth.ceil(player.getAbsorptionAmount());
            int healthRows = Mth.ceil((healthMax + absorb) / 2.0F / 10.0F);
            int rowHeight = Math.max(10 - (healthRows - 2), 3);
            int top = screenHeight - gui.leftHeight + 6;
            top += (healthRows * rowHeight);
            if (rowHeight != 10) top += 10 - rowHeight;
            float regenLeft = Math.min(20, getFoodRegenLeft(player));
            float regenStrength = getFoodRegenStrength(player) * 20 * 2;
            if (regenStrength == 0f)
                return;
            int width = Mth.ceil(regenLeft / 2f * 8);
            ClientUtils.setRenderColor(1.2f - (regenStrength / 1.2f), 0.78f, 0.17f, 1f);
            guiGraphics.blit(OT_REGEN_LOCATION, right, top, 0f, 0f, width, 3, 90, 3);
            ClientUtils.resetRenderColor();
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!this.isEnabled()
                || event.getItemStack().getItem().getFoodProperties() == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        if (!foodTooltip)
            return;

        FoodProperties food = event.getItemStack().getItem().getFoodProperties(event.getItemStack(), event.getEntity());

        ChatFormatting color = FoodDrinks.isRawFood(event.getItemStack().getItem()) ? ChatFormatting.DARK_RED : ChatFormatting.GRAY;
        MutableComponent component = null;
        if (Utils.getFoodSaturationRestored(food) < instantHealSaturationThreshold && doesHealInstantly()) {
            boolean isRawFood = FoodDrinks.isRawFood(event.getItemStack().getItem());
            float heal = getInstantHealAmount(food, isRawFood);
            if (mc.options.advancedItemTooltips) {
                //noinspection ConstantConditions
                component = Component.literal(InsaneLib.ONE_DECIMAL_FORMATTER.format(heal))
                        .append(" ")
                        .append(Component.translatable(HEALTH_LANG));
            }
            else if (heal >= 1)
                component = Component.translatable("iguanatweaksreborn.tooltip.nosh");
            else
                component = Component.translatable("iguanatweaksreborn.tooltip.snack");
        }
        if (Utils.getFoodSaturationRestored(food) >= instantHealSaturationThreshold && doesHealOverTime()) {
            //noinspection ConstantConditions
            float heal = Utils.computeFoodFormula(food, healOverTime);
            if (mc.options.advancedItemTooltips) {
                //Half heart per second by default
                float strength = Utils.computeFoodFormula(food, healOverTimeStrength);
                component = Component.literal(InsaneLib.ONE_DECIMAL_FORMATTER.format(heal))
                        .append(" ")
                        .append(Component.translatable(HEALTH_LANG))
                        .append(" / ")
                        .append(InsaneLib.ONE_DECIMAL_FORMATTER.format(heal / strength))
                        .append(" ")
                        .append(Component.translatable(SEC_LANG));
            }
            else {
                component = Component.translatable("iguanatweaksreborn.tooltip.meal");
                if (heal > 8)
                    component = Component.translatable("iguanatweaksreborn.tooltip.feast");
            }
        }
        if (component != null)
            event.getToolTip().add(component.withStyle(color).withStyle(ChatFormatting.ITALIC));
    }

}
