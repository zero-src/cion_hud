package cion.hud.client.hud;

import net.minecraft.client.gui.Hud;

import java.util.Locale;

public final class ModHeartType {
    public static final Hud.HeartType LAYER;

    static {
        LAYER = Hud.HeartType.valueOf("CION_HUD_LAYER".toUpperCase(Locale.ROOT));
    }

    private ModHeartType() {}

    public static void bootstrap() {
    }
}
