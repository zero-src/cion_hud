package cion.hud.client;

import cion.hud.CionHud;
import cion.hud.client.config.HudConfig;
import cion.hud.client.hud.HudLayers;
import cion.hud.client.hud.ModHeartType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class CionHudClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudConfig.load();
		ModHeartType.bootstrap();
		HudLayers.init();
		CionHud.LOGGER.info("cion_hud client initialized");

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cion_hud")
					.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reload")
							.executes(ctx -> {
								HudConfig.load();
								ctx.getSource().sendFeedback(Component.literal("[cion_hud] config reloaded"));
								return 1;
							})));
		});
	}
}
