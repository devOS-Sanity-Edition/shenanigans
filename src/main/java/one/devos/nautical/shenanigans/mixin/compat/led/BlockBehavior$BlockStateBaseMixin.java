package one.devos.nautical.shenanigans.mixin.compat.led;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockBehaviour.BlockStateBase.class, priority = 600) // inject before LED
public class BlockBehavior$BlockStateBaseMixin {
	@Shadow
	@Final
	private float destroySpeed;

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void onGetHardness(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		// Adorn calls getDestroySpeed with null params to make its hardness match vanilla blocks.
		// LED does not like this.
		if (level == null) {
			cir.setReturnValue(this.destroySpeed);
		}
	}
}
