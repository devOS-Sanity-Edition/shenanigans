package one.devos.nautical.shenanigans;

import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityData;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;

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
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
