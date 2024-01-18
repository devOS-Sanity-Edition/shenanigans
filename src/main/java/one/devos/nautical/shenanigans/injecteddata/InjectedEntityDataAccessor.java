package one.devos.nautical.shenanigans.injecteddata;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public class InjectedEntityDataAccessor<T> extends EntityDataAccessor<T> {
	public InjectedEntityDataAccessor(int id, EntityDataSerializer<T> serializer) {
		super(id, serializer);
	}
}
