package one.devos.nautical.shenanigans;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = Shenanigans.ID, dist = Dist.CLIENT)
public class ShenanigansClient {
	public ShenanigansClient(IEventBus modBus) {
		Shenanigans.LOGGER.info("h 2: client edition");
	}
}
