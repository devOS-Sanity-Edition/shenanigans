package one.devos.nautical.shenanigans.mixin.compat.c2me;

import com.bawnorton.mixinsquared.TargetHandler;

import net.minecraft.world.level.chunk.storage.ChunkSerializer;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChunkSerializer.class, priority = 1600)
public class ChunkSerializerMixin {
	@TargetHandler(
			mixin = "com.ishland.c2me.threading.chunkio.mixin.MixinChunkSerializer",
			name = "onChunkGetPackedBlockEntityNbt"
	)
	@Redirect(
			method = "@MixinSquared:Handler",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
			),
			require = 0
	)
	private static void silenceC2me(Logger instance, String message, Object arg1, Object arg2) {
	}
}
