package cion.hud.client.mixin;

import cion.hud.CionHud;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Hud.HeartType.class)
enum GuiHeartTypeMixin {
    CION_HUD_LAYER(
            CionHud.id("hud/heart/layer_full"),
            CionHud.id("hud/heart/layer_full_blinking"),
            CionHud.id("hud/heart/layer_half"),
            CionHud.id("hud/heart/layer_half_blinking"),
            CionHud.id("hud/heart/layer_hardcore_full"),
            CionHud.id("hud/heart/layer_hardcore_full_blinking"),
            CionHud.id("hud/heart/layer_hardcore_half"),
            CionHud.id("hud/heart/layer_hardcore_half_blinking"));

    @Shadow
    GuiHeartTypeMixin(Identifier full, Identifier fullBlinking, Identifier half, Identifier halfBlinking,
                      Identifier hardcoreFull, Identifier hardcoreFullBlinking, Identifier hardcoreHalf,
                      Identifier hardcoreHalfBlinking) {
    }
}
