package one.devos.nautical.shenanigans.injecteddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.shenanigans.Shenanigans;

public class InjectedEntityData {
	public static final ResourceLocation SYNC_ORDER_PACKET = Shenanigans.id("injected_entity_data_order");
	public static final ResourceLocation SYNC_PACKET = Shenanigans.id("injected_entity_data");

	public static final Object2ReferenceOpenHashMap<Class<? extends Entity>, Definition> DEFINITIONS = new Object2ReferenceOpenHashMap<>();

	public final List<DataValue<?>> values;

	private InjectedEntityData(Definition definition) {
		this.values = definition.createValues();
	}

	public static <T> InjectedEntityDataAccessor<T> define(Class<? extends Entity> entityClass, EntityDataSerializer<T> serializer) {
		var definition = DEFINITIONS.computeIfAbsent(entityClass, blah -> {
			var subclassDefinitions = new ArrayList<Definition>();
			for (var entry : DEFINITIONS.entrySet()) {
				if (entry.getKey().isAssignableFrom(entityClass))
					subclassDefinitions.add(entry.getValue());
			}
			Collections.sort(subclassDefinitions);
			if (subclassDefinitions.size() > 0) {
				var lastDefinition = subclassDefinitions.get(subclassDefinitions.size() - 1);
				return new Definition(lastDefinition.offset + lastDefinition.size());
			} else {
				return new Definition(0);
			}
		});
		return new InjectedEntityDataAccessor<>(definition.define(serializer), serializer);
	}

	public static Packet<ClientGamePacketListener> createSyncOrderPacket() {
		var buf = PacketByteBufs.create();
		buf.writeVarInt(DEFINITIONS.size());
		for (var entry : DEFINITIONS.entrySet()) {
			buf.writeUtf(entry.getKey().getName());
			var indices = entry.getValue().indices;
			buf.writeVarInt(indices.size());
			for (int index : indices) buf.writeVarInt(index);
		}
		return ServerPlayNetworking.createS2CPacket(SYNC_ORDER_PACKET, buf);
	}

	public static Optional<InjectedEntityData> tryCreate(Class<? extends Entity> entityClass) {
		var validDefinitions = new ArrayList<Definition>();
		for (var entry : DEFINITIONS.entrySet()) {
			if (entry.getKey().isAssignableFrom(entityClass))
				validDefinitions.add(entry.getValue());
		}
		if (validDefinitions.size() > 0)
			return Optional.of(new InjectedEntityData(Definition.merge(validDefinitions)));
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T> DataValue<T> get(int id) {
		return (DataValue<T>) values.get(id);
	}

	@SuppressWarnings("unchecked")
	public <T> void setDefault(int id, T value) {
		((DataValue<T>) values.get(id)).setDefault(value);
	}

	public Optional<Packet<ClientGamePacketListener>> createSyncPacket(Entity entity, boolean onlyDirty) {
		int sequence = 0;
		var dirtyValues = new ArrayList<DataValue<?>>();
		for (int i = 0; i < values.size(); i++) {
			var value = values.get(i);
			if (value.updated(!onlyDirty)) {
				sequence |= 1 << i;
				dirtyValues.add(value);
			}
		}

		if (dirtyValues.size() > 0) {
			var buf = PacketByteBufs.create();
			buf.writeVarInt(entity.getId());
			buf.writeVarInt(sequence);
			for (DataValue<?> value : dirtyValues) value.write(buf);
			return Optional.of(ServerPlayNetworking.createS2CPacket(SYNC_PACKET, buf));
		}
		return Optional.empty();
	}

	public static class Definition implements Comparable<Definition> {
		private final int offset;
		private final IntArrayList indices;
		private final ArrayList<Value<?>> values;

		private Definition(int offset) {
			this.offset = offset;
			this.indices = new IntArrayList();
			this.values = new ArrayList<>();
		}

		private static Definition merge(List<Definition> definitions) {
			var mergedDefinition = new Definition(0);
			Collections.sort(definitions);
			for (var definition : definitions) {
				mergedDefinition.indices.addAll(definition.indices);
				mergedDefinition.values.addAll(definition.values);
			}
			return mergedDefinition;
		}

		private <T> int define(EntityDataSerializer<T> serializer) {
			values.add(new Value<T>(serializer));
			int i = offset + size() - 1;
			indices.add(i);
			return i;
		}

		private int size() {
			return values.size();
		}

		private List<DataValue<?>> createValues() {
			var values = new ArrayList<DataValue<?>>();
			for (int index : this.indices)
				values.add(new DataValue<>(this.values.get(index).serializer));
			return values;
		}

		public List<Integer> indices() {
			return List.copyOf(indices);
		}

		public void sort(IntArrayList sortIndices) {
			var oldValues = new ArrayList<>(values);
			int oldIndex = 0;
			for (int index : sortIndices) {
				indices.set(oldIndex, index);
				values.set(oldIndex, oldValues.get(index - offset));
				oldIndex++;
			}
		}

		@Override
		public int compareTo(Definition o) {
			return Integer.signum(offset - o.offset);
		}

		private static record Value<T>(EntityDataSerializer<T> serializer) { }
	}

	public static class DataValue<T> {
		private final EntityDataSerializer<T> serializer;
		private T defaultValue;
		private Optional<T> value;
		private boolean dirty;

		DataValue(EntityDataSerializer<T> serializer) {
			this.serializer = serializer;
			this.defaultValue = null;
			this.value = Optional.empty();
			this.dirty = false;
		}

		public void write(FriendlyByteBuf buf) {
			serializer.write(buf, get());
		}

		public void read(FriendlyByteBuf buf) {
			value = Optional.of(serializer.read(buf));
			dirty = false;
		}

		public T get() {
			return value.orElseThrow(() -> new RuntimeException("[Shenanigans] VERY VERY BAD BAD BAD DATA VALUE WAS EMPTY, THIS SHOULD NEVER HAPPEN"));
		}

		public void setDefault(T value) {
			this.defaultValue = value;
			this.value = Optional.of(value);
		}

		public void set(T value) {
			if (value != this.value.orElse(null))
				this.dirty = true;
			this.value = Optional.of(value);
		}

		public boolean updated(boolean notDefault) {
			boolean old = dirty;
			dirty = false;
			return old || (notDefault && value.orElse(null) != defaultValue);
		}
	}
}
