package cion.hud.client.hud;

import cion.hud.CionHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class ArmorRenderer {
    private static final ArmorType ARMOR = new ArmorType(
            CionHud.id("hud/armor/empty"),
            CionHud.id("hud/armor/half"),
            CionHud.id("hud/armor/full"),
            CionHud.id("hud/armor/layer_half"),
            CionHud.id("hud/armor/layer_full"),
            CionHud.id("hud/armor/mixed"));

    private ArmorRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, int posX, int posY, Player player) {
        int armorPoints = player.getArmorValue();
        renderArmorBar(graphics, posX, posY, ARMOR, armorPoints, true);
    }

    static void renderArmorBar(GuiGraphicsExtractor graphics, int posX, int posY, ArmorType type, int value, boolean leftSide) {
        if (value <= 0) return;
        int lastRowArmorPoints = value > 20 ? (value - 1) % 20 + 1 : 0;
        for (int i = 0; i < 10; i++) {
            int startX = posX + (leftSide ? i * 8 : -i * 8 - 9);
            if (i * 2 + 1 < lastRowArmorPoints) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.layerFull(), startX, posY, 9, 9);
            } else if (i * 2 + 1 == lastRowArmorPoints) {
                if (value > 20) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.mixed(), startX, posY, 9, 9);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.layerHalf(), startX, posY, 9, 9);
                }
            } else if (i * 2 + 1 < value) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.full(), startX, posY, 9, 9);
            } else if (i * 2 + 1 == value) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.half(), startX, posY, 9, 9);
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.empty(), startX, posY, 9, 9);
            }
        }
    }

    record ArmorType(Identifier empty, Identifier half, Identifier full, Identifier layerHalf, Identifier layerFull, Identifier mixed) {}
}
