package one.devos.nautical.shenanigans.packetsplit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.shenanigans.Shenanigans;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * added before {@link net.minecraft.network.PacketEncoder}
 */
public class PacketSplitter extends MessageToMessageEncoder<Packet<?>> {
	private final PacketFlow flow;

	public PacketSplitter(PacketFlow flow) {
		this.flow = flow;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet<?> packet, List<Object> out) {
		try {
			// simulate serialization
			FriendlyByteBuf buf = PacketByteBufs.create();
			packet.write(buf);
			int size = buf.writerIndex();

			if (true) {
				// safe size. just send it normally
				out.add(packet);
				return;
			}

			// packet is too large. split it into fragments.
			Shenanigans.LOGGER.warn("Packet [{}] is too large; splitting", PacketSplitting.toString(packet));

			List<Packet<?>> fragments = new ArrayList<>();
			UUID id = UUID.randomUUID();

			int i = 0;
			while (size > 0) {
				int toRead = Math.min(size, PacketSplitting.FRAGMENT_SIZE);
				size -= toRead;
				ByteBuf bytes = buf.readBytes(toRead);
				Packet<?> fragment = this.createFragment(id, i, bytes);
				fragments.add(fragment);
				i++;
			}
			buf.release();
			Packet<?> header = this.createHeader(id, i);
			// all is well. send the fragments
			out.add(header);
			out.addAll(fragments);
		} catch (Throwable t) {
			// if anything goes wrong, just log it and pass it on.
			Shenanigans.LOGGER.error("Error splitting packet [" + PacketSplitting.toString(packet) + ']', t);
			out.add(packet);
		}
	}

	protected Packet<?> createHeader(UUID id, int fragments) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUUID(id);
		buf.writeVarInt(fragments);
		return createPacket(this.flow, PacketSplitting.HEADER, buf);
	}

	protected Packet<?> createFragment(UUID id, int index, ByteBuf data) {
		FriendlyByteBuf wrapped = PacketByteBufs.create();
		wrapped.writeUUID(id);
		wrapped.writeVarInt(index);
		wrapped.writeBytes(data);
		return createPacket(this.flow, PacketSplitting.FRAGMENT, wrapped);
	}

	public static Packet<?> createPacket(PacketFlow flow, ResourceLocation id, FriendlyByteBuf data) {
		return switch (flow) {
			case CLIENTBOUND -> ServerPlayNetworking.createS2CPacket(id, data);
			case SERVERBOUND -> ServerboundPacketFactory.create(id, data);
		};
	}
}
