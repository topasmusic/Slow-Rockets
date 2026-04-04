package com.example.slowrockets.mixin;

import com.example.slowrockets.SlowRocketsState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {
	@Inject(method = "use", at = @At("HEAD"))
	private void slowrockets$recordBoost(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (world.isClientSide() || user == null || !user.isFallFlying()) {
			return;
		}

		double baseSpeed = user.getDeltaMovement().length();
		SlowRocketsState.recordBoost(user.getUUID(), world.getGameTime(), baseSpeed);
	}
}
