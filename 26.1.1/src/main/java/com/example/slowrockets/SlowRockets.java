package com.example.slowrockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ClientModInitializer;

public final class SlowRockets implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("slowrockets");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Slow Rockets loaded (client).");
		SlowRocketsHandshake.initClient();
	}
}
