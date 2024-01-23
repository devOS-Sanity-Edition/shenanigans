package one.devos.nautical.shenanigans.packetsplit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

/**
 * Need a wrapper around ClientPlayNetworking for safe access since it's marked as @ClientOnly.
 */
public class ServerboundPacketFactory {
	public static Packet<?> create(ResourceLocation id, FriendlyByteBuf data) {
		return ClientPlayNetworking.createC2SPacket(id, data);
	}
}
