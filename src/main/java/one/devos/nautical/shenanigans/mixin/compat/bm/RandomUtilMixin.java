package one.devos.nautical.shenanigans.mixin.compat.bm;

import net.minecraft.util.RandomSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "party.lemons.biomemakeover.util.RandomUtil")
public class RandomUtilMixin {
	@Redirect(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"
			)
	)
	private static RandomSource useThreadsafeRandom() {
		// C2ME makes triggering threading protection on the normal random way easier.
		//noinspection deprecation
		return RandomSource.createThreadSafe();
	}
}
