package one.devos.nautical.shenanigans.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import one.devos.nautical.shenanigans.packetsplit.PacketMerger;
import one.devos.nautical.shenanigans.packetsplit.PacketSplitting;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PacketDecoder.class)
public abstract class PacketDecoderMixin {
	@Shadow
	protected abstract void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception;

	@Unique
	private final PacketMerger merger = new PacketMerger();

	@WrapOperation(
			method = "decode",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
			)
	)
	private boolean handleFragments(List<Object> out, Object packet, Operation<Boolean> original,
									ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out2) throws Exception {
		FriendlyByteBuf data = getData(packet);
		if (data != null) {
			FriendlyByteBuf combined = merger.receiveFragment(data);
			if (combined != null) {
				// when a packet has been recombined, feed it back into decoding
				this.decode(ctx, combined, out2);
				return false; // do not add the fragment to the output list
			}
		}
		return original.call(out, packet);
	}

	@Unique
	@Nullable
	private static FriendlyByteBuf getData(Object packet) {
		if (packet instanceof ClientboundCustomPayloadPacket c && c.getIdentifier().equals(PacketSplitting.FRAGMENT)) {
			return c.getData();
		} else if (packet instanceof ServerboundCustomPayloadPacket s && s.getIdentifier().equals(PacketSplitting.FRAGMENT)) {
			return s.getData();
		}
		return null;
	}
}
