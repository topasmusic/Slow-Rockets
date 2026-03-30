package com.example.slowrockets;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public final class SlowRocketsHandshake {
	private static final Identifier HANDSHAKE_CHANNEL = Identifier.of("slowrockets", "handshake");
	private static final String RESPONSE_TOKEN = "slowrockets";
	private static final Text REQUIRED_TEXT = Text.literal("Slow Rockets is required to join this server.");

	private SlowRocketsHandshake() {
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, (client, handler, buf, callbacksConsumer) -> {
			PacketByteBuf response = new PacketByteBuf(Unpooled.buffer());
			response.writeString(RESPONSE_TOKEN);
			return CompletableFuture.completedFuture(response);
		});
	}

	public static void initServer() {
		ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (!understood) {
				handler.disconnect(REQUIRED_TEXT);
				return;
			}
			String token = buf.readString(64);
			if (!RESPONSE_TOKEN.equals(token)) {
				handler.disconnect(REQUIRED_TEXT);
			}
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			PacketByteBuf request = new PacketByteBuf(Unpooled.buffer());
			request.writeString("hello");
			sender.sendPacket(HANDSHAKE_CHANNEL, request);
		});
	}
}
