package com.example.slowrockets.mixin;

import com.example.slowrockets.SlowRocketsState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	private static final double BOOST_SCALE = 0.2D;

	@Shadow
	public ServerPlayerEntity player;

	@Shadow
	private int movePacketsCount;

	@Shadow
	private int lastTickMovePacketsCount;

	@Unique
	private boolean slowrockets$hasScaled;

	@Unique
	private double slowrockets$scaledX;

	@Unique
	private double slowrockets$scaledY;

	@Unique
	private double slowrockets$scaledZ;

	@Redirect(
		method = "onPlayerMove",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;getX(D)D",
			ordinal = 1
		)
	)
	private double slowrockets$scaleMoveX(PlayerMoveC2SPacket packet, double fallback) {
		if (slowrockets$tryScale(packet, fallback)) {
			return slowrockets$scaledX;
		}
		return packet.getX(fallback);
	}

	@Redirect(
		method = "onPlayerMove",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;getY(D)D",
			ordinal = 1
		)
	)
	private double slowrockets$scaleMoveY(PlayerMoveC2SPacket packet, double fallback) {
		if (slowrockets$hasScaled) {
			return slowrockets$scaledY;
		}
		return packet.getY(fallback);
	}

	@Redirect(
		method = "onPlayerMove",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;getZ(D)D",
			ordinal = 1
		)
	)
	private double slowrockets$scaleMoveZ(PlayerMoveC2SPacket packet, double fallback) {
		if (slowrockets$hasScaled) {
			return slowrockets$scaledZ;
		}
		return packet.getZ(fallback);
	}

	@Unique
	private boolean slowrockets$tryScale(PlayerMoveC2SPacket packet, double fallbackX) {
		slowrockets$hasScaled = false;

		double baseSpeed = slowrockets$getBaseSpeed();
		if (baseSpeed < 0.0D) {
			return false;
		}

		double currentX = player.getX();
		double currentY = player.getY();
		double currentZ = player.getZ();

		double desiredX = packet.getX(fallbackX);
		double desiredY = packet.getY(currentY);
		double desiredZ = packet.getZ(currentZ);

		double dx = desiredX - currentX;
		double dy = desiredY - currentY;
		double dz = desiredZ - currentZ;

		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len <= 0.0D) {
			return false;
		}

		int packetsThisTick = slowrockets$getPacketsThisTick();
		double basePerPacket = baseSpeed / (double) packetsThisTick;
		if (len <= basePerPacket) {
			return false;
		}

		double targetLen = basePerPacket + (len - basePerPacket) * BOOST_SCALE;
		double scale = targetLen / len;

		slowrockets$scaledX = currentX + dx * scale;
		slowrockets$scaledY = currentY + dy * scale;
		slowrockets$scaledZ = currentZ + dz * scale;
		slowrockets$hasScaled = true;

		return true;
	}

	@Unique
	private int slowrockets$getPacketsThisTick() {
		int packets = this.movePacketsCount - this.lastTickMovePacketsCount;
		return packets > 0 ? packets : 1;
	}

	@Unique
	private double slowrockets$getBaseSpeed() {
		if (this.player == null || this.player.getEntityWorld().isClient()) {
			return -1.0D;
		}
		if (!this.player.isGliding()) {
			return -1.0D;
		}

		long now = this.player.getEntityWorld().getTime();
		return SlowRocketsState.getBaseSpeed(this.player.getUuid(), now);
	}
}
