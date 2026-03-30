package com.example.slowrockets;

import net.fabricmc.api.DedicatedServerModInitializer;

public final class SlowRocketsServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		SlowRockets.LOGGER.info("Slow Rockets loaded (server). Client mod required.");
		SlowRocketsHandshake.initServer();
	}
}
