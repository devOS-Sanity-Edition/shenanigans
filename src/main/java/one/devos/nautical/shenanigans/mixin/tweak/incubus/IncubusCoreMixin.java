package one.devos.nautical.shenanigans.mixin.tweak.incubus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Pseudo
@Mixin(targets = "net.id.incubus_core.IncubusCore", remap = false)
public class IncubusCoreMixin {
	// shut.
	@Redirect(
			method = "onInitialize",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Random;nextInt(I)I"
			)
	)
	private int replaceRandomValue(Random random, int original) {
		return Integer.MAX_VALUE;
	}
}
