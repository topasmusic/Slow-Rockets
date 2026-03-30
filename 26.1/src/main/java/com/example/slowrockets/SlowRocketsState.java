package com.example.slowrockets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SlowRocketsState {
	private static final long BOOST_ACTIVE_WINDOW_TICKS = 20L;
	private static final Map<UUID, BoostData> LAST_BOOST = new HashMap<>();

	private SlowRocketsState() {
	}

	public static void recordBoost(UUID playerId, long tick, double baseSpeed) {
		BoostData existing = LAST_BOOST.get(playerId);
		if (existing == null || (tick - existing.lastTick) > BOOST_ACTIVE_WINDOW_TICKS) {
			LAST_BOOST.put(playerId, new BoostData(tick, baseSpeed));
			return;
		}

		existing.lastTick = tick;
	}

	public static double getBaseSpeed(UUID playerId, long tick) {
		BoostData data = LAST_BOOST.get(playerId);
		if (data == null || (tick - data.lastTick) > BOOST_ACTIVE_WINDOW_TICKS) {
			return -1.0D;
		}
		return data.baseSpeed;
	}

	private static final class BoostData {
		private long lastTick;
		private final double baseSpeed;

		private BoostData(long lastTick, double baseSpeed) {
			this.lastTick = lastTick;
			this.baseSpeed = baseSpeed;
		}
	}
}
