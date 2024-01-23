package one.devos.nautical.shenanigans.packetsplit;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.shenanigans.Shenanigans;

public class PacketSplitting {
	public static final int MAX_SIZE = 8388608; // from PacketEncoder
	public static final int FRAGMENT_SIZE = MAX_SIZE - (5 * 32); // leave space for 5 ints (UUID + index)
	public static final ResourceLocation FRAGMENT = Shenanigans.id("fragment");
	public static final ResourceLocation HEADER = Shenanigans.id("header");

	public static String toString(Packet<?> packet) {
		String output = packet.getClass().getName();
		if (packet instanceof ClientboundCustomPayloadPacket c) {
			output = output + '[' + c.getIdentifier() + ']';
		} else if (packet instanceof ServerboundCustomPayloadPacket c) {
			output = output + '[' + c.getIdentifier() + ']';
		}
		return output;
	}
}
