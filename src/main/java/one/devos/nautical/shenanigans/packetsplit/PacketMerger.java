package one.devos.nautical.shenanigans.packetsplit;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.protocol.Packet;

import one.devos.nautical.shenanigans.Shenanigans;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PacketMerger {
	private final Map<UUID, FragmentSet> fragmentSets = new HashMap<>();

	public void receiveHeader(FriendlyByteBuf buf) {

	}

	/**
	 * Handle a fragment packet and possibly return a recombined one.
	 */
	@Nullable
	public FriendlyByteBuf receiveFragment(FriendlyByteBuf buf) {
		UUID id = buf.readUUID();
		int index = buf.readVarInt();

		FragmentSet set = fragmentSets.get(id);
		if (set == null) {
			Shenanigans.LOGGER.error("Received fragment {} for set {}, which doesn't exist", index, id);
		} else if (!set.add(index, buf)) {
			Shenanigans.LOGGER.error("Received fragment {} for set {} twice", index, id);
		} else if (set.isComplete()) {
			// re-assemble fragments into a whole packet and send it to the handler
			FriendlyByteBuf whole = set.combine();
			// set is complete, we're done with it
			set.release();
			fragmentSets.remove(id);
			return whole;
		} else {
			// successfully received the fragment and saved it for later
			return null;
		}
	}

	public record FragmentSet(List<FriendlyByteBuf> data) {
		public static FragmentSet create(int size) {
			List<FriendlyByteBuf> list = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				list.add(null);
			}
			return new FragmentSet(list);
		}

		public boolean add(int index, FriendlyByteBuf data) {
			if (this.data.get(index) == null) {
				FriendlyByteBuf copy = PacketByteBufs.copy(data);
				this.data.set(index, copy);
				return true;
			}
			return false;
		}

		public boolean isComplete() {
			for (FriendlyByteBuf buf : this.data) {
				if (buf == null) {
					return false;
				}
			}
			return true;
		}

		public FriendlyByteBuf combine() {
			FriendlyByteBuf combined = PacketByteBufs.create();
			for (FriendlyByteBuf buf : this.data) {
				combined.writeBytes(buf);
			}
			return combined;
		}

		public void release() {
			for (FriendlyByteBuf buf : this.data) {
				buf.release();
			}
		}
	}
}
