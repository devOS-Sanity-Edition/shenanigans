package one.devos.nautical.shenanigans.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.shenanigans.Shenanigans;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityData;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityDataAccessor;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityDataHolder;

@Mixin(value = SynchedEntityData.class, priority = 300)  // inject before Canary
public class SynchedEntityDataMixin implements InjectedEntityDataHolder {
	@Unique
	private static final Object2BooleanOpenHashMap<Class<?>> USE_INJECTED_ENTITY_DATA = new Object2BooleanOpenHashMap<>();

	@Unique
	private InjectedEntityData injectedData = null;

	@Override
	@Nullable
	public InjectedEntityData shenanigans$injectedData() {
		return injectedData;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void createInjectedData(Entity entity, CallbackInfo ci) {
		InjectedEntityData.tryCreate(entity.getClass()).ifPresent(injectedData -> this.injectedData = injectedData);
	}

	@Inject(method = "defineId", at = @At("HEAD"), cancellable = true)
	private static <T> void defineInjectedData(Class<? extends Entity> clazz, EntityDataSerializer<T> serializer, CallbackInfoReturnable<EntityDataAccessor<T>> cir) {
		try {
			boolean useInjectedEntityData = USE_INJECTED_ENTITY_DATA.computeIfAbsent(clazz, blah -> {
				boolean ret = false;
				var mixinIds = new ArrayList<String>();

				try {
					for (Field field : clazz.getDeclaredFields()) {
						if (!Modifier.isStatic(field.getModifiers()) || field.getType() != EntityDataAccessor.class)
							continue;
						var mixinAnno = field.getDeclaredAnnotation(MixinMerged.class);
						if (mixinAnno != null) {
							var mixinId = mixinAnno.mixin();
							if (!mixinIds.contains(mixinId))
								Shenanigans.LOGGER.info("Unsafe tracked data registration detected for " + clazz.getName() + " by " + mixinId + ". Falling back to injected entity data");
							mixinIds.add(mixinId);
							ret = true;
						}
					}
				} catch (Throwable e) {
					if (e instanceof ClassNotFoundException || e instanceof NoClassDefFoundError) {
						// this is needed because for some horrible reason getDeclaredFields loads client only fields in a entity from steam n rails,
						// I have no idea how that happens since the loader is supposed to strip client only fields
					} else {
						throw new RuntimeException(e);
					}
				}

				return ret;
			});
			if (useInjectedEntityData)
				cir.setReturnValue(InjectedEntityData.define(clazz, serializer));
		} catch (Exception e) {
			throw new RuntimeException("[Shenanigans] Failed to define injected entity data!", e);
		}
	}

	@Inject(method = "define", at = @At("HEAD"), cancellable = true)
	private <T> void setInjectedValueDefault(EntityDataAccessor<T> key, T value, CallbackInfo ci) {
		if (key instanceof InjectedEntityDataAccessor) {
			injectedData.setDefault(key.getId(), value);
			ci.cancel();
		}
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private <T> void getInjectedValue(EntityDataAccessor<T> key, CallbackInfoReturnable<T> cir) {
		if (key instanceof InjectedEntityDataAccessor)
			cir.setReturnValue((T) injectedData.get(key.getId()).get());
	}

	@Inject(method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V", at = @At("HEAD"), cancellable = true)
	private <T> void setInjectedValue(EntityDataAccessor<T> key, T value, boolean force, CallbackInfo ci) {
		if (key instanceof InjectedEntityDataAccessor) {
			injectedData.get(key.getId()).set(value);
			ci.cancel();
		}
	}
}
