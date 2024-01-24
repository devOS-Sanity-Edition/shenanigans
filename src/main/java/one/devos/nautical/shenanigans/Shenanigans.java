package one.devos.nautical.shenanigans;

import static net.minecraft.commands.Commands.literal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityData;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import one.devos.nautical.shenanigans.packetsplit.PacketSplitting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shenanigans implements ModInitializer {
	public static final String ID = "shenanigans";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize(ModContainer mod) {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			handler.send(InjectedEntityData.createSyncOrderPacket());
		});

		if (!QuiltLoader.isDevelopmentEnvironment())
			return;

		CommandRegistrationCallback.EVENT.register((dispatcher, buildCtx, env) -> {
			if (env.includeDedicated) {
				dispatcher.register(literal("shenanigans").then(literal("sendbigpacket").executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					FriendlyByteBuf buf = PacketByteBufs.create();
					while (buf.writerIndex() < PacketSplitting.MAX_SIZE + 1000) {
						buf.writeLong(0);
					}
					ServerPlayNetworking.send(player, id("big_packet"), buf);
					return 1;
				})));
			}
		});
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
