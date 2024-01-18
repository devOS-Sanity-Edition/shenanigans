package one.devos.nautical.shenanigans.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityDataHolder;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Shadow
	abstract void broadcastAndSend(Packet<?> packet);

	@Inject(method = "sendPairingData", at = @At("RETURN"))
	private void pairInjectedEntityData(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer, CallbackInfo ci) {
		var injectedData = ((InjectedEntityDataHolder) entity.getEntityData()).shenanigans$injectedData();
		if (injectedData != null)
			injectedData.createSyncPacket(entity, false).ifPresent(consumer::accept);
	}

	@Inject(method = "sendDirtyEntityData", at = @At("RETURN"))
	private void sendDirtyInjectedEntityData(CallbackInfo ci) {
		var injectedData = ((InjectedEntityDataHolder) entity.getEntityData()).shenanigans$injectedData();
		if (injectedData != null)
			injectedData.createSyncPacket(entity, true).ifPresent(this::broadcastAndSend);
	}
}
