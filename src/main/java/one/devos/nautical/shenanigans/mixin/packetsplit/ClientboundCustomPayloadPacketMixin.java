package one.devos.nautical.shenanigans.mixin.packetsplit;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {
	@ModifyExpressionValue(
			method = "<init>*",
			at = @At(
					value = "CONSTANT",
					args = "intValue=1048576"
			)
	)
	private int bigify(int limit) {
		return Integer.MAX_VALUE;
	}
}
