package one.devos.nautical.shenanigans;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.fml.common.Mod;

@Mod(Shenanigans.ID)
public class Shenanigans {
	public static final String ID = "shenanigans";

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
