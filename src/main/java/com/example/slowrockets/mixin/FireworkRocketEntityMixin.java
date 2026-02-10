package com.example.slowrockets.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import com.example.slowrockets.SlowRocketsState;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.text.Text;
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
	private LivingEntity shooter;

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
			target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
		)
	)
	private void slowrockets$notifyBoost(CallbackInfo ci) {
		if (this.shooter == null || !this.shooter.isGliding()) {
			return;
		}
		if (this.shooter instanceof PlayerEntity player && player.getEntityWorld().isClient()) {
			double baseSpeed = player.getVelocity().length();
			SlowRocketsState.recordBoost(player.getUuid(), player.getEntityWorld().getTime(), baseSpeed);
		}
		slowrockets$maybeNotify();
	}

	@Unique
	private void slowrockets$maybeNotify() {
		if (!(this.shooter instanceof PlayerEntity player)) {
			return;
		}
		if (!player.getEntityWorld().isClient()) {
			return;
		}

		long now = player.getEntityWorld().getTime();
		Long last = LAST_MESSAGE_TICK.get(player.getUuid());
		if (last != null && (now - last) < MESSAGE_COOLDOWN_TICKS) {
			return;
		}

		LAST_MESSAGE_TICK.put(player.getUuid(), now);
		String msg = MESSAGES[ThreadLocalRandom.current().nextInt(MESSAGES.length)];
		player.sendMessage(Text.literal(msg), true);
	}
}
