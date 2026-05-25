package cion.hud.client.hud;

import cion.hud.client.config.ClientConfig;
import cion.hud.client.config.HudConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public final class HeartsRenderer {
    public static final HeartsRenderer INSTANCE = new HeartsRenderer();

    private final RandomSource random = RandomSource.create();

    private HeartsRenderer() {}

    public void render(GuiGraphicsExtractor graphics, int posX, int posY, Player player,
                       int tickCount, int regenHeart, float maxHealth,
                       int displayHealth, int currentHealth, int absorption, boolean blink) {
        this.random.setSeed(tickCount * 312871L);
        renderHearts(graphics, player, posX, posY, regenHeart, maxHealth, currentHealth, displayHealth, absorption, blink);
    }

    private void renderHearts(GuiGraphicsExtractor graphics, Player player, int posX, int posY, int regenHeart, float maxHealth, int currentHealth, int displayHealth, int absorption, boolean blink) {
        boolean hardcore = player.level().getLevelData().isHardcore();
        int normalHearts = Math.min(10, Mth.ceil(maxHealth / 2.0));
        int maxAbsorptionHearts = 20 - normalHearts;
        int absorptionHearts = Math.min(20 - normalHearts, Mth.ceil(absorption / 2.0));

        ClientConfig.HeartsConfig cfg = HudConfig.get().hearts;

        for (int heart = 0; heart < normalHearts + absorptionHearts; heart++) {
            int hx = posX + (heart % 10) * 8;
            int hy = posY;
            if (currentHealth + absorption <= 4) {
                hy += this.random.nextInt(2);
            }
            if (heart < normalHearts && regenHeart == heart) {
                hy -= 2;
            }

            blit(graphics, Hud.HeartType.CONTAINER, hx, hy, blink, false, hardcore);

            if (heart >= normalHearts) {
                int curAbs = heart * 2 - normalHearts * 2;
                if (curAbs < absorption) {
                    int maxAbsHp = maxAbsorptionHearts * 2;
                    boolean halfHeart = curAbs + 1 == absorption % maxAbsHp;
                    boolean layer = absorption > maxAbsHp && curAbs + 1 <= (absorption - 1) % maxAbsHp + 1;
                    if (halfHeart && layer) {
                        blit(graphics, forPlayer(player, true, false, cfg), hx, hy, false, false, hardcore);
                    }
                    blit(graphics, forPlayer(player, true, layer, cfg), hx, hy, false, halfHeart, hardcore);
                }
            }

            if (blink && heart * 2 < Math.min(20, displayHealth)) {
                boolean halfHeart = heart * 2 + 1 == (displayHealth - 1) % 20 + 1;
                boolean layer = displayHealth > 20 && heart * 2 + 1 <= (displayHealth - 1) % 20 + 1;
                if (halfHeart && layer) {
                    blit(graphics, forPlayer(player, false, false, cfg), hx, hy, true, false, hardcore);
                }
                blit(graphics, forPlayer(player, false, layer, cfg), hx, hy, true, halfHeart, hardcore);
            }

            if (heart * 2 < Math.min(20, currentHealth)) {
                boolean halfHeart = heart * 2 + 1 == (currentHealth - 1) % 20 + 1;
                boolean layer = currentHealth > 20 && heart * 2 + 1 <= (currentHealth - 1) % 20 + 1;
                if (halfHeart && layer) {
                    blit(graphics, forPlayer(player, false, false, cfg), hx, hy, false, false, hardcore);
                }
                blit(graphics, forPlayer(player, false, layer, cfg), hx, hy, false, halfHeart, hardcore);
            }
        }
    }

    private static void blit(GuiGraphicsExtractor graphics, Hud.HeartType type, int x, int y, boolean blinking, boolean halfHeart, boolean hardcore) {
        Identifier sprite = type.getSprite(hardcore, halfHeart, blinking);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 9, 9);
    }

    private static Hud.HeartType forPlayer(Player player, boolean absorbing, boolean layer, ClientConfig.HeartsConfig cfg) {
        Hud.HeartType base = Hud.HeartType.forPlayer(player);
        if (base != Hud.HeartType.NORMAL) {
            return base;
        }
        boolean inverse = cfg.inverseColoring;
        if (layer) {
            return absorbing || !inverse ? ModHeartType.LAYER : base;
        }
        return absorbing ? Hud.HeartType.ABSORBING : (inverse ? ModHeartType.LAYER : base);
    }
}
