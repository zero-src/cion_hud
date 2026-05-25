package cion.hud.client.config;

import cion.core.config.ConfigManager;

/**
 * Static facade over cion_core's ConfigManager for cion_hud.
 * Null-fallback for partial JSON happens via core's postLoad hook.
 */
public final class HudConfig {
	private static final ConfigManager<ClientConfig> MGR =
			new ConfigManager<>("cion_hud", ClientConfig.class, ClientConfig::new)
					.postLoad(HudConfig::fillNulls);

	private HudConfig() {}

	public static ClientConfig get() {
		return MGR.get();
	}

	public static void load() {
		MGR.load();
	}

	public static void save() {
		MGR.save();
	}

	private static void fillNulls(ClientConfig cfg) {
		if (cfg.hearts == null) cfg.hearts = new ClientConfig.HeartsConfig();
		if (cfg.armor == null) cfg.armor = new ClientConfig.ArmorConfig();
		if (cfg.crosshair == null) cfg.crosshair = new ClientConfig.CrosshairConfig();
		if (cfg.textIndicator == null) cfg.textIndicator = new ClientConfig.TextIndicatorConfig();
	}
}
