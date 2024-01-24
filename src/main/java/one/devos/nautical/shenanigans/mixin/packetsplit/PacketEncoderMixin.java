package one.devos.nautical.shenanigans.mixin.packetsplit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;

import net.minecraft.network.protocol.Packet;

import one.devos.nautical.shenanigans.packetsplit.PacketEncoderExt;

import one.devos.nautical.shenanigans.packetsplit.PacketSplitter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin implements PacketEncoderExt {
	@Unique
	private PacketSplitter splitter;

	@Redirect(
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V"
			)
	)
	private void usePreWrittenData(Packet<?> instance, FriendlyByteBuf buf) {
		FriendlyByteBuf preWritten = this.splitter.getWrittenData(instance);
		if (preWritten != null) {
			buf.writeBytes(preWritten);
			preWritten.release();
		} else {
			instance.write(buf);
		}
	}

	@Override
	public void shenanigans$setSplitter(PacketSplitter splitter) {
		this.splitter = splitter;
	}
}
