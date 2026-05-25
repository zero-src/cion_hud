package cion.hud.client.hud;

import cion.hud.CionHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class CrosshairRenderer {
    private static final Identifier TEXTURE = CionHud.id("textures/gui/harvest_indicator.png");
    private static final int W = 3;
    private static final int H = 3;
    private static final int TEX_W = 9;
    private static final int TEX_H = 3;
    private static final int U_CAN = 0;
    private static final int U_CANT = 3;
    private static final int U_NEEDS_SPECIAL = 6;

    private CrosshairRenderer() {}

    public static void draw(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) return;

        Level level = player.level();
        BlockState state = level.getBlockState(bhr.getBlockPos());
        if (state.isAir()) return;

        int u = switch (HarvestEvaluator.evaluate(state, level, bhr.getBlockPos(), player)) {
            case CAN_HARVEST -> U_CAN;
            case CANT_HARVEST -> U_CANT;
            case NEEDS_SPECIAL -> U_NEEDS_SPECIAL;
        };

        int x = (graphics.guiWidth() - W) / 2;
        int y = (graphics.guiHeight() - H) / 2;
        int color = ARGB.opaque(0xFFFFFF);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, (float) u, 0.0F, W, H, TEX_W, TEX_H, color);
    }
}
