package one.devos.nautical.shenanigans.mixin.packetsplit.unlimit;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {
	@ModifyExpressionValue(
			method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
			at = @At(
					value = "CONSTANT",
					args = "intValue=32767"
			)
	)
	private int increaseMaxBytes(int limit) {
		return Integer.MAX_VALUE;
	}
}
