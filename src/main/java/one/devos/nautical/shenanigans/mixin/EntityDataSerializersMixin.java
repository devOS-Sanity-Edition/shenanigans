package one.devos.nautical.shenanigans.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.shenanigans.Shenanigans;

import org.quiltmc.qsl.entity.networking.api.tracked_data.QuiltTrackedDataHandlerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityDataSerializers.class, priority = 300) // inject before Canary and QSL
public class EntityDataSerializersMixin {
	@SuppressWarnings("FieldMayBeFinal")
	@Unique
	private static boolean vanillaRegistered;

	@Unique
	private static final Object2IntMap<String> serializersPerClass = new Object2IntArrayMap<>();

	static {
		// static blocks are injected to tail of default static block
		vanillaRegistered = true;
	}

	@Inject(method = "registerSerializer", at = @At("HEAD"), cancellable = true)
	private static void onRegister(EntityDataSerializer<?> serializer, CallbackInfo ci) {
		if (vanillaRegistered) {
			// redirect to quilt registry

			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			// stackTrace[0] = getStackTrace
			// stackTrace[1] = onDefine
			// stackTrace[2] = defineId
			// stackTrace[3] = caller
			StackTraceElement caller = stackTrace[3];

			String callerClassName = caller.getClassName();
			ResourceLocation id = Shenanigans.id(makeId(callerClassName));

			Shenanigans.LOGGER.info("[Shenanigans]: Registering custom EntityDataSerializer [{}] as [{}]", serializer, id);
			QuiltTrackedDataHandlerRegistry.register(id, serializer);
			ci.cancel();
		}
	}

	@Unique
	private static String makeId(String className) {
		int serializerCount = serializersPerClass.computeInt(className, ($, count) -> count == null ? 0 : count + 1);
		char[] chars = className.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isUpperCase(c)) {
				chars[i] = Character.toLowerCase(c);
			} else if (!ResourceLocation.isAllowedInResourceLocation(c)) {
				chars[i] = '_';
			}
		}
		return new String(chars) + "_" + serializerCount;
	}
}
