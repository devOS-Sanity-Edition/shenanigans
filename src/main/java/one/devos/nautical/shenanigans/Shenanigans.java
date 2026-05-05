package one.devos.nautical.shenanigans;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;

import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Shenanigans.ID)
public class Shenanigans {
	public static final String ID = "shenanigans";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public Shenanigans(IEventBus modBus) {
		LOGGER.info("h");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
