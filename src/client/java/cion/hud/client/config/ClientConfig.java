package cion.hud.client.config;

public class ClientConfig {
    public HeartsConfig hearts = new HeartsConfig();
    public ArmorConfig armor = new ArmorConfig();
    public CrosshairConfig crosshair = new CrosshairConfig();
    public TextIndicatorConfig textIndicator = new TextIndicatorConfig();

    public static class HeartsConfig {
        public boolean enabled = true;
        public boolean showTextIndicator = true;
        public boolean inverseColoring = false;
    }

    public static class ArmorConfig {
        public boolean enabled = true;
        public boolean showTextIndicator = true;
    }

    public static class CrosshairConfig {
        public boolean enabled = true;
    }

    public static class TextIndicatorConfig {
        public String color = "WHITE";
        public boolean showX = true;
    }
}
