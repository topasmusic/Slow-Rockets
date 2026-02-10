package com.example.slowrockets.mixin;

import com.example.slowrockets.SlowRocketsState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {
	@Inject(method = "use", at = @At("HEAD"))
	private void slowrockets$recordBoost(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (world.isClient() || user == null || !user.isGliding()) {
			return;
		}

		double baseSpeed = user.getVelocity().length();
		SlowRocketsState.recordBoost(user.getUuid(), world.getTime(), baseSpeed);
	}
}
