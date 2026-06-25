package com.example.slowrockets;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.concurrent.CompletableFuture;

public final class SlowRocketsHandshake {
	private static final Identifier HANDSHAKE_CHANNEL = Identifier.fromNamespaceAndPath("slowrockets", "handshake");
	private static final String RESPONSE_TOKEN = "slowrockets";
	private static final Component REQUIRED_TEXT = Component.literal("Slow Rockets is required to join this server.");

	private SlowRocketsHandshake() {
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, (client, handler, buf, callbacksConsumer) -> {
			FriendlyByteBuf response = new FriendlyByteBuf(Unpooled.buffer());
			response.writeUtf(RESPONSE_TOKEN);
			return CompletableFuture.completedFuture(response);
		});
	}

	public static void initServer() {
		ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (!understood) {
				handler.disconnect(REQUIRED_TEXT);
				return;
			}
			String token = buf.readUtf(64);
			if (!RESPONSE_TOKEN.equals(token)) {
				handler.disconnect(REQUIRED_TEXT);
			}
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			FriendlyByteBuf request = new FriendlyByteBuf(Unpooled.buffer());
			request.writeUtf("hello");
			sender.sendPacket(HANDSHAKE_CHANNEL, request);
		});
	}
}
