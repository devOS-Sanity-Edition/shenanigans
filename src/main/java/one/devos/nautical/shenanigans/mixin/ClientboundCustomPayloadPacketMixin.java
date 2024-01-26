package one.devos.nautical.shenanigans.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {
	@ModifyExpressionValue(
			method = {
					"<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
					"<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)V",
			},
			at = @At(
					value = "CONSTANT",
					args = "intValue=1048576"
			),
			require = 2
	)
	private int increaseByteLimit(int original) {
		return Integer.MAX_VALUE;
	}
}
