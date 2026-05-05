package one.devos.nautical.shenanigans.mixin;

import com.hrznstudio.titanium.TitaniumClient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TitaniumClient.class)
public class TitaniumClientMixin {
	/// @author TropheusJ
	/// @reason replaced with modifying the rendered shape
	@Overwrite
	@OnlyIn(Dist.CLIENT)
	public static void blockOverlayEvent(RenderHighlightEvent.Block event) {
		// nope!
	}
}
