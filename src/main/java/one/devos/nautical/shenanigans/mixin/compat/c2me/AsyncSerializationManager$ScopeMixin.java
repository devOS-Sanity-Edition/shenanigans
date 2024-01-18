package one.devos.nautical.shenanigans.mixin.compat.c2me;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.ishland.c2me.threading.chunkio.common.AsyncSerializationManager$Scope")
public class AsyncSerializationManager$ScopeMixin {
	@Redirect(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
			)
	)
	private void silence(Logger logger, String message, Object arg1, Object arg2) {
		// shhhh.
	}
}
