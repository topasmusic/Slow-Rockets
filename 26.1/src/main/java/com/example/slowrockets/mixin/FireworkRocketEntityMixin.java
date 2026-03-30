package com.example.slowrockets.mixin;

import com.example.slowrockets.SlowRocketsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
	private static final double BOOST_SCALE = 0.2D;
	@Unique
	private static final long MESSAGE_COOLDOWN_TICKS = 400L;
	@Unique
	private static final Map<UUID, Long> LAST_MESSAGE_TICK = new HashMap<>();
	@Unique
	private static final String[] MESSAGES = {
		"Slow Rockets engaged. Enjoy the scenic route.",
		"Rockets were faster in the good old days.",
		"Boost? More like a polite suggestion.",
		"Turbo mode canceled by budget cuts.",
		"You are now flying in economy class.",
		"Rocket fuel? More like scented candle.",
		"Speed limit enforced by Slow Rockets.",
		"Congratulations, you unlocked snail mode.",
		"At least the view is nice.",
		"Who needs speed when you have vibes?",
		"Aerodynamics called, they want their momentum back.",
		"Warning: hype exceeds thrust.",
		"Your rocket applied for a nap.",
		"Now boarding: Flight 0.2X.",
		"Speedrun category: scenic%",
		"Boost denied by safety inspector.",
		"Fast is temporary. Slow is forever.",
		"This is fine. Glide slower.",
		"Rockets on strike. Please wait.",
		"Boost set to 'leisurely'."
	};

	@Shadow
	private LivingEntity attachedToEntity;

	@ModifyConstant(
		method = "tick",
		constant = @Constant(doubleValue = 1.5D)
	)
	private double slowrockets$scaleBoostTarget(double value) {
		return value * BOOST_SCALE;
	}

	@ModifyConstant(
		method = "tick",
		constant = @Constant(doubleValue = 0.1D)
	)
	private double slowrockets$scaleBoostNudge(double value) {
		return value * BOOST_SCALE;
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setVelocity(Lnet/minecraft/world/phys/Vec3;)V"
		)
	)
	private void slowrockets$notifyBoost(CallbackInfo ci) {
		if (this.attachedToEntity == null || !this.attachedToEntity.isFallFlying()) {
			return;
		}
		if (this.attachedToEntity instanceof Player player && player.level().isClientSide()) {
			double baseSpeed = player.getDeltaMovement().length();
			SlowRocketsState.recordBoost(player.getUUID(), player.level().getGameTime(), baseSpeed);
		}
		slowrockets$maybeNotify();
	}

	@Unique
	private void slowrockets$maybeNotify() {
		if (!(this.attachedToEntity instanceof Player player)) {
			return;
		}
		if (!player.level().isClientSide()) {
			return;
		}

		long now = player.level().getGameTime();
		Long last = LAST_MESSAGE_TICK.get(player.getUUID());
		if (last != null && (now - last) < MESSAGE_COOLDOWN_TICKS) {
			return;
		}

		LAST_MESSAGE_TICK.put(player.getUUID(), now);
		String msg = MESSAGES[ThreadLocalRandom.current().nextInt(MESSAGES.length)];
		player.sendOverlayMessage(Component.literal(msg));
	}
}
