package one.devos.nautical.shenanigans.mixin.packetsplit;

import io.netty.channel.ChannelPipeline;

import one.devos.nautical.shenanigans.packetsplit.PacketEncoderExt;
import one.devos.nautical.shenanigans.packetsplit.PacketSplitter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

@Mixin(Connection.class)
public class ConnectionMixin {
	@Inject(method = "configureSerialization", at = @At("TAIL"))
	private static void addSplitter(ChannelPipeline pipeline, PacketFlow flow, CallbackInfo ci) {
		// for outbound events (sending a packet) netty events flow upwards from the bottom of the addLast chain
		PacketSplitter splitter = new PacketSplitter(flow.getOpposite());
		pipeline.addAfter("encoder", "shenanigans_splitter", splitter);
		// give the encoder the splitter for retrieving pre-written data
		PacketEncoderExt encoder = (PacketEncoderExt) pipeline.get("encoder");
		encoder.shenanigans$setSplitter(splitter);
	}
}
