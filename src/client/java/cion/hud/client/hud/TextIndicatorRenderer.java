package cion.hud.client.hud;

import cion.hud.CionHud;
import cion.hud.client.config.ClientConfig;
import cion.hud.client.config.HudConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class TextIndicatorRenderer {
    private static final Identifier TINY_NUMBERS = CionHud.id("textures/font/tiny_numbers.png");

    private TextIndicatorRenderer() {}

    public static void draw(GuiGraphicsExtractor graphics, int posX, int posY, int barValue, boolean leftAlign) {
        draw(graphics, posX, posY, barValue, leftAlign, 20);
    }

    public static void draw(GuiGraphicsExtractor graphics, int posX, int posY, int barValue, boolean leftAlign, int maxRowSize) {
        if (barValue <= 0 || maxRowSize <= 0) return;
        float rowFloat = barValue / (float) maxRowSize;
        if (rowFloat <= 1.0F) return;

        ClientConfig.TextIndicatorConfig cfg = HudConfig.get().textIndicator;
        int rowNumber = Mth.ceil(rowFloat);
        int textColor = ARGB.opaque(resolveColor(cfg.color));
        drawWithTinyNumbers(graphics, posX, posY, rowNumber, leftAlign, cfg.showX, textColor);
    }

    private static void drawWithTinyNumbers(GuiGraphicsExtractor graphics, int posX, int posY, int rowNumber, boolean leftAlign, boolean showX, int textColor) {
        int n = rowNumber;
        int digitCount = 0;
        int[] digits = new int[10];
        if (n == 0) {
            digits[digitCount++] = 0;
        } else {
            while (n > 0 && digitCount < digits.length) {
                digits[digitCount++] = n % 10;
                n /= 10;
            }
        }
        int x = leftAlign ? posX - (showX ? 7 : 3) : posX + 4 * digitCount;
        for (int i = 0; i < digitCount; i++) {
            drawBorderedTinyDigit(graphics, x - 4 * i, posY + 2, digits[i] * 5, 0, textColor);
        }
        if (showX) {
            drawBorderedTinyDigit(graphics, x + 4, posY + 2, 0, 7, textColor);
        }
    }

    private static void drawBorderedTinyDigit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int textColor) {
        int w = 3, h = 5;
        int bg = ARGB.opaque(0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x - 1, y - 1, (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x,     y - 1, (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x + 1, y - 1, (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x - 1, y,     (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x + 1, y,     (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x - 1, y + 1, (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x,     y + 1, (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x + 1, y + 1, (float) u, (float) v, w, h, 256, 256, bg);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TINY_NUMBERS, x,     y,     (float) u, (float) v, w, h, 256, 256, textColor);
    }

    private static int resolveColor(String name) {
        if (name == null) return 0xFFFFFF;
        return switch (name.toUpperCase()) {
            case "BLACK" -> 0x000000;
            case "DARK_BLUE" -> 0x0000AA;
            case "DARK_GREEN" -> 0x00AA00;
            case "DARK_AQUA" -> 0x00AAAA;
            case "DARK_RED" -> 0xAA0000;
            case "DARK_PURPLE" -> 0xAA00AA;
            case "GOLD" -> 0xFFAA00;
            case "GRAY" -> 0xAAAAAA;
            case "DARK_GRAY" -> 0x555555;
            case "BLUE" -> 0x5555FF;
            case "GREEN" -> 0x55FF55;
            case "AQUA" -> 0x55FFFF;
            case "RED" -> 0xFF5555;
            case "LIGHT_PURPLE" -> 0xFF55FF;
            case "YELLOW" -> 0xFFFF55;
            default -> 0xFFFFFF;
        };
    }
}
