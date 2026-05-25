package cion.hud.client.hud;

import cion.hud.CionHud;
import cion.hud.client.config.HudConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class HudLayers {
    private static int tickCount;
    private static int lastHealth;
    private static int displayHealth;
    private static long lastHealthTime;
    private static long healthBlinkTime;

    private HudLayers() {}

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(mc -> tickCount++);

        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, original -> (graphics, deltaTracker) -> {
            if (!HudConfig.get().hearts.enabled) {
                original.extractRenderState(graphics, deltaTracker);
                return;
            }
            Player player = getPlayer();
            if (player == null) {
                original.extractRenderState(graphics, deltaTracker);
                return;
            }
            renderHealth(graphics, player);
        });

        HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR, original -> (graphics, deltaTracker) -> {
            if (!HudConfig.get().armor.enabled) {
                original.extractRenderState(graphics, deltaTracker);
                return;
            }
            Player player = getPlayer();
            if (player == null) {
                original.extractRenderState(graphics, deltaTracker);
                return;
            }
            renderArmor(graphics, player);
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, CionHud.id("harvest_indicator"),
                (graphics, deltaTracker) -> {
                    if (!HudConfig.get().crosshair.enabled) return;
                    CrosshairRenderer.draw(graphics);
                });
    }

    private static final int HEIGHT_HEALTH = 39;
    private static final int HEIGHT_ARMOR = 49;

    private static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    private static void renderHealth(net.minecraft.client.gui.GuiGraphicsExtractor graphics, Player player) {
        int currentHealth = Mth.ceil(player.getHealth());
        long millis = Util.getMillis();

        boolean blink = healthBlinkTime > (long) tickCount
                && (healthBlinkTime - (long) tickCount) / 3L % 2L == 1L;
        if (currentHealth < lastHealth && player.invulnerableTime > 0) {
            lastHealthTime = millis;
            healthBlinkTime = tickCount + 20;
        } else if (currentHealth > lastHealth && player.invulnerableTime > 0) {
            lastHealthTime = millis;
            healthBlinkTime = tickCount + 10;
        }
        if (millis - lastHealthTime > 1000L) {
            displayHealth = currentHealth;
            lastHealthTime = millis;
        }
        lastHealth = currentHealth;

        float maxHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH),
                (float) Math.max(displayHealth, currentHealth));
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int regenHeart = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regenHeart = tickCount % Mth.ceil(Math.min(20.0F, maxHealth) + 5.0F);
        }

        int posX = graphics.guiWidth() / 2 - 91;
        int posY = graphics.guiHeight() - HEIGHT_HEALTH;

        HeartsRenderer.INSTANCE.render(graphics, posX, posY, player, tickCount, regenHeart,
                maxHealth, displayHealth, currentHealth, absorption, blink);

        if (HudConfig.get().hearts.showTextIndicator) {
            TextIndicatorRenderer.draw(graphics, posX - 2, posY, currentHealth, true);
            int maxAbsorption = (20 - Mth.ceil(Math.min(20, currentHealth) / 2.0F)) * 2;
            TextIndicatorRenderer.draw(graphics, posX - 2, posY - 10, absorption, true, maxAbsorption);
        }
    }

    private static void renderArmor(net.minecraft.client.gui.GuiGraphicsExtractor graphics, Player player) {
        int armorPoints = player.getArmorValue();
        if (armorPoints <= 0) return;
        int posX = graphics.guiWidth() / 2 - 91;
        int posY = graphics.guiHeight() - HEIGHT_ARMOR;
        ArmorRenderer.render(graphics, posX, posY, player);
        if (HudConfig.get().armor.showTextIndicator) {
            TextIndicatorRenderer.draw(graphics, posX - 2, posY, armorPoints, true);
        }
    }
}
